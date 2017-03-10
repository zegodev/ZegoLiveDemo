package com.zego.livedemo3.videofilter;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.zego.zegoavkit2.videofilter.ZegoVideoFilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by robotding on 16/12/3.
 */

public class VideoFilterMemDemo extends ZegoVideoFilter {
    private static final String TAG = "VideoFilterMemDemo";

    private ZegoVideoFilter.Client mClient = null;
    private HandlerThread mThread = null;
    private volatile Handler mHandler = null;

    static class PixelBuffer {
        public int width;
        public int height;
        public int stride;
        public long timestamp_100n;
        public ByteBuffer buffer;
    }
    private ArrayList<PixelBuffer> mProduceQueue = new ArrayList<PixelBuffer>();
    private int mWriteIndex = 0;
    private int mWriteRemain = 0;
    private ConcurrentLinkedQueue<PixelBuffer> mConsumeQueue = new ConcurrentLinkedQueue<PixelBuffer>();
    private int mMaxBufferSize = 0;

    private boolean mIsRunning = false;

    @Override
    protected void allocateAndStart(Client client) {
        mClient = client;
        mThread = new HandlerThread("video-filter");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mIsRunning = true;

        mProduceQueue.clear();
        mConsumeQueue.clear();
        mWriteIndex = 0;
        mWriteRemain = 0;
        mMaxBufferSize = 0;
    }

    @Override
    protected void stopAndDeAllocate() {
        mIsRunning = false;

        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler = null;

        if (Build.VERSION.SDK_INT >= 18) {
            mThread.quitSafely();
        } else {
            mThread.quit();
        }
        mThread = null;

        mClient.destroy();
        mClient = null;
    }

    @Override
    protected int supportBufferType() {
        return BUFFER_TYPE_MEM;
    }

    @Override
    protected synchronized int dequeueInputBuffer(int width, int height, int stride) {
        if (stride * height * 4 > mMaxBufferSize) {
            if (mMaxBufferSize != 0) {
                mProduceQueue.clear();
            }

            mMaxBufferSize = stride * height * 4;
            createPixelBufferPool(4);
        }

        if (mWriteRemain == 0) {
            return -1;
        }

        mWriteRemain--;
        return (mWriteIndex + 1) % mProduceQueue.size();
    }

    @Override
    protected synchronized ByteBuffer getInputBuffer(int index) {
        if (mProduceQueue.isEmpty()) {
            return null;
        }
        ByteBuffer buffer = mProduceQueue.get(index).buffer;
        buffer.position(0);
        return buffer;
    }

    @Override
    protected synchronized void queueInputBuffer(int bufferIndex, final int width, int height, int stride, long timestamp_100n) {
        if (bufferIndex == -1) {
            return ;
        }

        PixelBuffer pixelBuffer = mProduceQueue.get(bufferIndex);
        pixelBuffer.width = width;
        pixelBuffer.height = height;
        pixelBuffer.stride = stride;
        pixelBuffer.timestamp_100n = timestamp_100n;
        pixelBuffer.buffer.limit(height * stride);
        mConsumeQueue.add(pixelBuffer);

        mWriteIndex = (mWriteIndex + 1) % mProduceQueue.size();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mIsRunning) {
                    Log.e(TAG, "already stopped");
                    return ;
                }

                PixelBuffer pixelBuffer = getConsumerPixelBuffer();

                int index = mClient.dequeueInputBuffer(pixelBuffer.width, pixelBuffer.height, pixelBuffer.stride);
                if (index >= 0) {
                    ByteBuffer dst = mClient.getInputBuffer(index);
                    dst.position(0);
                    pixelBuffer.buffer.position(0);
                    dst.put(pixelBuffer.buffer);

                    mClient.queueInputBuffer(index, pixelBuffer.width, pixelBuffer.height, pixelBuffer.stride, pixelBuffer.timestamp_100n);
                }

                returnProducerPixelBuffer(pixelBuffer);
            }
        });
    }

    @Override
    protected SurfaceTexture getSurfaceTexture() {
        return null;
    }

    private void createPixelBufferPool(int count) {
        for (int i = 0; i < count; i++) {
            PixelBuffer pixelBuffer = new PixelBuffer();
            pixelBuffer.buffer = ByteBuffer.allocateDirect(mMaxBufferSize);
            mProduceQueue.add(pixelBuffer);
        }

        mWriteRemain = count;
        mWriteIndex = -1;
    }

    private PixelBuffer getConsumerPixelBuffer() {
        if (mConsumeQueue.isEmpty()) {
            return null;
        }
        return mConsumeQueue.poll();
    }

    private synchronized void returnProducerPixelBuffer(PixelBuffer pixelBuffer) {
        if (pixelBuffer.buffer.capacity() == mMaxBufferSize) {
            mWriteRemain++;
        }
    }
}
