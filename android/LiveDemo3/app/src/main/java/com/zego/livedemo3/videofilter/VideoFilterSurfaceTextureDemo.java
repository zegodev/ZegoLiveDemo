package com.zego.livedemo3.videofilter;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.zego.zegoavkit2.videofilter.ZegoVideoFilter;
import com.zego.zegoimagefilter.ZegoImageFilter;

import java.nio.ByteBuffer;

/**
 * Created by robotding on 16/12/3.
 */

public class VideoFilterSurfaceTextureDemo extends ZegoVideoFilter {
    private ZegoVideoFilter.Client mClient = null;
    private ZegoImageFilter mFilter = null;
    private int mWidth = 0;
    private int mHeight = 0;
    private Surface mOutputSurface = null;

    @Override
    protected void allocateAndStart(Client client) {
        mClient = client;
        mFilter = new ZegoImageFilter();

        mFilter.init();
        mFilter.setCustomizedFilter(2);

        mWidth = 0;
        mHeight = 0;
    }

    @Override
    protected void stopAndDeAllocate() {
        mFilter.setOutputSurface(null);
        mFilter.uninit();

        if (mOutputSurface != null) {
            mOutputSurface.release();
            mOutputSurface = null;
        }

        mClient.destroy();
        mClient = null;
    }

    @Override
    protected int supportBufferType() {
        return BUFFER_TYPE_SURFACE_TEXTURE;
    }

    @Override
    protected int dequeueInputBuffer(int width, int height, int stride) {
        if (stride != width * 4) {
            return -1;
        }

        if (mWidth != width || mHeight != height) {
            if (mOutputSurface != null) {
                mOutputSurface.release();
                mOutputSurface = null;
            }

            if (mClient.dequeueInputBuffer(width, height, stride) < 0) {
                return -1;
            }

            SurfaceTexture surfaceTexture = mClient.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(width, height);
            mOutputSurface = new Surface(surfaceTexture);
            mFilter.setOutputSurface(mOutputSurface);
            mWidth = width;
            mHeight = height;
        }

        return 0;
    }

    @Override
    protected ByteBuffer getInputBuffer(int index) {
        return null;
    }

    @Override
    protected void queueInputBuffer(int bufferIndex, int width, int height, int stride, long timestamp_100n) {
    }

    @Override
    protected SurfaceTexture getSurfaceTexture() {
        return mFilter.getInputSurfaceTexture();
    }
}
