package com.zego.livedemo3.videofilter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.zego.livedemo3.advanced.ve_gl.EglBase;
import com.zego.livedemo3.advanced.ve_gl.GlUtil;
import com.zego.zegoavkit2.videofilter.ZegoVideoFilter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by robotding on 17/2/9.
 */
public class VideoFilterHybridDemo extends ZegoVideoFilter {
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

    private EglBase captureEglBase;
    private int mTextureId = 0;

    @Override
    protected void allocateAndStart(Client client) {
        mClient = client;
        mThread = new HandlerThread("video-filter");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());

        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                captureEglBase = EglBase.create(null, EglBase.CONFIG_PIXEL_BUFFER);
                try {
                    captureEglBase.createDummyPbufferSurface();
                    captureEglBase.makeCurrent();
                } catch (RuntimeException e) {
                    // Clean up before rethrowing the exception.
                    captureEglBase.releaseSurface();
                    throw e;
                }

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                mTextureId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);

                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mProduceQueue.clear();
        mConsumeQueue.clear();
        mWriteIndex = 0;
        mWriteRemain = 0;
        mMaxBufferSize = 0;
    }

    @Override
    protected void stopAndDeAllocate() {
        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                release();
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler = null;

        mThread.quit();
        mThread = null;

        mClient.destroy();
        mClient = null;
    }

    @Override
    protected int supportBufferType() {
        // buffer类型, 必填
        return BUFFER_TYPE_HYBRID_MEM_GL_TEXTURE_2D;
    }

    @Override
    protected synchronized int dequeueInputBuffer(int width, int height, int stride) {

        // 创建buffer队列, 用于获取原始视频数据
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

        // 将buffer传递给zego引擎, 引擎将原始视频数据写到buffer
        return buffer;
    }

    @Override
    protected synchronized void queueInputBuffer(int bufferIndex, final int width, final int height, int stride, long timestamp_100n) {
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
        mWriteIndex++;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                PixelBuffer pixelBuffer = getConsumerPixelBuffer();

                if (pixelBuffer == null) {
                    return;
                }

                long start = System.currentTimeMillis();

                captureEglBase.makeCurrent();

                // 将原始数据加载到GPU缓冲区
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
                pixelBuffer.buffer.position(0);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, pixelBuffer.width, pixelBuffer.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer.buffer);

                //ToDo: 做美颜处理

                // 将处理后的数据回传到zego引擎
                mClient.onProcessCallback(mTextureId, pixelBuffer.width, pixelBuffer.height, pixelBuffer.timestamp_100n);

                long end = System.currentTimeMillis();

                Log.i("Hybrid", "time:" + (end - start));

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

    private void release() {
        if (captureEglBase.hasSurface()) {
            captureEglBase.makeCurrent();
            if (mTextureId != 0) {
                int[] textures = new int[] {mTextureId};
                GLES20.glDeleteTextures(1, textures, 0);
                mTextureId = 0;
            }

            captureEglBase.releaseSurface();
            captureEglBase.detachCurrent();
        }
    }

}
