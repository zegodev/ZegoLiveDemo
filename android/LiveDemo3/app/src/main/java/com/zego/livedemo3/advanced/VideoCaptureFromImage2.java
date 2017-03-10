package com.zego.livedemo3.advanced;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;


import com.zego.livedemo3.advanced.ve_gl.EglBase;
import com.zego.livedemo3.advanced.ve_gl.GlRectDrawer;
import com.zego.livedemo3.advanced.ve_gl.GlUtil;
import com.zego.zegoavkit2.ZegoVideoCaptureDevice;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by robotding on 17/2/15.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VideoCaptureFromImage2 extends ZegoVideoCaptureDevice
        implements Choreographer.FrameCallback, TextureView.SurfaceTextureListener, SurfaceHolder.Callback {
    private TextureView mTextureView = null;
    private SurfaceView mSurfaceView = null;
    private EglBase previewEglBase;

    private int mBitmapTextureId = 0;
    private int mPreviewTextureId = 0;
    private int mFrameBufferId = 0;

    private GlRectDrawer previewDrawer;
    private float[] flipMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f};
    private float[] transformationMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f};

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mImageWidth = 0;
    private int mImageHeight = 0;

    private boolean mIsRunning = false;
    private boolean mIsCapture = false;
    private boolean mIsPreview = false;
    private Bitmap mBitmap = null;

    private HandlerThread mThread = null;
    private Handler mHandler = null;

    private Context mContext = null;
    private ZegoVideoCaptureDevice.Client mClient = null;

    private int mX = 0;
    private int mY = 0;
    private int mDrawCounter = 0;

    VideoCaptureFromImage2(Context context) {
        mContext = context;
    }

    public final int init() {
        mThread = new HandlerThread("VideoCaptureFromImage2" + hashCode());
        mThread.start();
        mHandler = new Handler(mThread.getLooper());

        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                previewEglBase = EglBase.create(null, EglBase.CONFIG_RGBA);
                previewDrawer = new GlRectDrawer();

                Choreographer.getInstance().postFrameCallback(VideoCaptureFromImage2.this);
                mIsRunning = true;

                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public final int uninit() {
        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsRunning = false;
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

        if (Build.VERSION.SDK_INT >= 18) {
            mThread.quitSafely();
        } else {
            mThread.quit();
        }
        mThread = null;

        return 0;
    }

    public void setBitmap(final Bitmap bitmap) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBitmap = bitmap;
            }
        });
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!mIsRunning) {
            return;
        }
        Choreographer.getInstance().postFrameCallback(this);

        if (mBitmap == null) {
            return;
        }

        if (mIsPreview) {
            if (mTextureView != null) {
                attachTextureView();
            } else if (mSurfaceView != null) {
                attachSurfaceView();
            }
            if (previewEglBase.hasSurface()) {
                draw(mBitmap);
            }
        }

        if (mDrawCounter == 0) {
            mX = (mX + 1) % 4;
            if (mX == 0) {
                mY = (mY + 1) % 4;
            }
        }
        mDrawCounter = (mDrawCounter + 1) % 60;
    }

    private void draw(final Bitmap bitmap) {
        try {
            previewEglBase.makeCurrent();

            if (mBitmapTextureId == 0) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                mBitmapTextureId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            }

            if (mPreviewTextureId == 0) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                mPreviewTextureId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mImageWidth, mImageHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

                mFrameBufferId = GlUtil.generateFrameBuffer(mPreviewTextureId);
            } else {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
            }

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            previewDrawer.drawRgb(mBitmapTextureId, flipMatrix,
                                  mImageWidth, mImageHeight,
                                  mImageWidth / 4 * mX, mImageHeight / 4 * mY,
                                  mImageWidth / 4, mImageHeight / 4);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            previewDrawer.drawRgb(mPreviewTextureId, transformationMatrix,
                                  mViewWidth, mViewHeight,
                                  0, 0,
                                  mViewWidth, mViewHeight);

            previewEglBase.swapBuffers();

            if (mIsCapture) {
                long now = SystemClock.elapsedRealtime();
                mClient.onTextureCaptured(mPreviewTextureId, mImageWidth, mImageHeight, now);
            }

            previewEglBase.detachCurrent();
        } catch (RuntimeException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    protected void allocateAndStart(Client client) {
        mClient = client;

        init();
        setBitmap(createBitmapFromAsset());
    }

    @Override
    protected void stopAndDeAllocate() {
        uninit();

        mClient.destroy();
        mClient = null;
    }

    @Override
    protected int startCapture() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsCapture = true;
            }
        });
        return 0;
    }

    @Override
    protected int stopCapture() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsCapture = false;
            }
        });
        return 0;
    }

    @Override
    protected int supportBufferType() {
        return PIXEL_BUFFER_TYPE_GL_TEXTURE_2D;
    }

    @Override
    protected int setFrameRate(int framerate) {
        return 0;
    }

    @Override
    protected int setResolution(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
        return 0;
    }

    @Override
    protected int setFrontCam(int bFront) {
        return 0;
    }

    @Override
    protected int setView(View view) {
        if (view instanceof TextureView) {
            setRendererView((TextureView) view);
        } else if (view instanceof SurfaceView) {
            setRendererView((SurfaceView) view);
        }

        return 0;
    }

    @Override
    protected int setViewMode(int nMode) {
        return 0;
    }

    @Override
    protected int setViewRotation(int nRotation) {
        return 0;
    }

    @Override
    protected int setCaptureRotation(int nRotation) {
        return 0;
    }

    @Override
    protected int startPreview() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsPreview = true;
            }
        });
        return 0;
    }

    @Override
    protected int stopPreview() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsPreview = false;
            }
        });
        return 0;
    }

    @Override
    protected int enableTorch(boolean bEnable) {
        return 0;
    }

    @Override
    protected int takeSnapshot() {
        return 0;
    }

    @Override
    protected int setPowerlineFreq(int nFreq) {
        return 0;
    }

    private Bitmap createBitmapFromAsset() {
        Bitmap bitmap = null;
        try {
            AssetManager assetManager = mContext.getAssets();
            InputStream is = assetManager.open("ic_launcher.png");
            bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                System.out.println("测试一:width=" + bitmap.getWidth() + " ,height=" + bitmap.getHeight());
            } else {
                System.out.println("bitmap == null");
            }
        } catch (Exception e) {
            System.out.println("异常信息:" + e.toString());
        }
        return bitmap;
    }

    public int setRendererView(TextureView view) {
        final CountDownLatch barrier = new CountDownLatch(1);
        final TextureView temp = view;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mTextureView != null) {
                    if (mTextureView.getSurfaceTextureListener().equals(VideoCaptureFromImage2.this)) {
                        mTextureView.setSurfaceTextureListener(null);
                    }

                    releasePreviewSurface();
                }

                mTextureView = temp;
                if (mTextureView != null) {
                    mTextureView.setSurfaceTextureListener(VideoCaptureFromImage2.this);
                }
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int setRendererView(SurfaceView view) {
        final CountDownLatch barrier = new CountDownLatch(1);
        final SurfaceView temp = view;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSurfaceView != null) {
                    mSurfaceView.getHolder().removeCallback(VideoCaptureFromImage2.this);
                    releasePreviewSurface();
                }

                mSurfaceView = temp;
                if (mSurfaceView != null) {
                    mSurfaceView.getHolder().addCallback(VideoCaptureFromImage2.this);
                }
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void attachTextureView() {
        if (previewEglBase.hasSurface()) {
            return;
        }

        if (!mTextureView.isAvailable()) {
            return;
        }

        mViewWidth = mTextureView.getWidth();
        mViewHeight = mTextureView.getHeight();
        try {
            previewEglBase.createSurface(mTextureView.getSurfaceTexture());
        } catch (RuntimeException e) {
            previewEglBase.releaseSurface();
            mViewWidth = 0;
            mViewHeight = 0;
        }
    }

    private void attachSurfaceView() {
        if (previewEglBase.hasSurface()) {
            return;
        }

        SurfaceHolder holder = mSurfaceView.getHolder();
        if (holder.isCreating() || null == holder.getSurface()) {
            return;
        }

        Rect size = holder.getSurfaceFrame();
        mViewWidth = size.width();
        mViewHeight = size.height();
        try {
            previewEglBase.createSurface(holder.getSurface());
        } catch (RuntimeException e) {
            previewEglBase.releaseSurface();
            mViewWidth = 0;
            mViewHeight = 0;
        }
    }

    private void releasePreviewSurface() {
        if (previewEglBase.hasSurface()) {
            previewEglBase.makeCurrent();

            if (mBitmapTextureId != 0) {
                int[] textures = new int[]{mBitmapTextureId};
                GLES20.glDeleteTextures(1, textures, 0);
                mBitmapTextureId = 0;
            }

            if (mPreviewTextureId != 0) {
                int[] textures = new int[]{mPreviewTextureId};
                GLES20.glDeleteTextures(1, textures, 0);
                mPreviewTextureId = 0;
            }

            if (mFrameBufferId != 0) {
                int[] frameBuffers = new int[]{mFrameBufferId};
                GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
                mFrameBufferId = 0;
            }

            previewEglBase.releaseSurface();
            previewEglBase.detachCurrent();
        }
    }

    private void release() {
        releasePreviewSurface();
        if (previewDrawer != null) {
            previewDrawer.release();
            previewDrawer = null;
        }

        if (previewEglBase != null) {
            previewEglBase.release();
            previewEglBase = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        releasePreviewSurfaceSafe();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releasePreviewSurfaceSafe();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        releasePreviewSurfaceSafe();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releasePreviewSurfaceSafe();
    }

    private void releasePreviewSurfaceSafe() {
        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                releasePreviewSurface();
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
