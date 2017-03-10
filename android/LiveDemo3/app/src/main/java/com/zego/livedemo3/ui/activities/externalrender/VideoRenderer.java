package com.zego.livedemo3.ui.activities.externalrender;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zego.zegoavkit2.callback.ZegoExternalRenderCallback;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by robotding on 16/9/23.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class VideoRenderer implements Choreographer.FrameCallback, ZegoExternalRenderCallback,
        TextureView.SurfaceTextureListener, SurfaceHolder.Callback {
    private static final String TAG = "VideoRenderer";

    public static final Object lock = new Object();

    // Android-specific extension.
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    public static final int[] CONFIG_RGBA = {
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
    };

    private EGLContext eglContext;
    private EGLConfig eglConfig;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private GlShader shader;
    private int m_nFrameUniform = 0;
    private int m_nFactorUniform = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private boolean mIsRunning = false;

    // Vertex coordinates in Normalized Device Coordinates, i.e.
    // (-1, -1) is bottom-left and (1, 1) is top-right.
    private static final FloatBuffer DEVICE_RECTANGLE =
            GlUtil.createFloatBuffer(new float[] {
                    -1.0f, -1.0f,  // Bottom left.
                    1.0f, -1.0f,  // Bottom right.
                    -1.0f,  1.0f,  // Top left.
                    1.0f,  1.0f,  // Top right.
            });

    // Texture coordinates - (0, 0) is bottom-left and (1, 1) is top-right.
    private static final FloatBuffer TEXTURE_RECTANGLE =
            GlUtil.createFloatBuffer(new float[] {
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
            });

    private static final String VERTEX_SHADER =
            "attribute vec4 position;\n "
            + "attribute mediump vec4 texcoord;\n"
            + "varying mediump vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "    gl_Position = position;\n"
            + "    textureCoordinate = texcoord.xy;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate; \n"
            +"uniform sampler2D frame; \n"
            +"uniform lowp float factor;\n"
            +"lowp vec3 whiteFilter;\n"
            +"\n"
            +"void main() {\n"
            +"    whiteFilter = vec3(factor);\n"
            +"    gl_FragColor = texture2D(frame, textureCoordinate) * vec4(whiteFilter, 1.0); \n"
            +"}\n";

    private HandlerThread mThread = null;
    private Handler mHandler = null;
    private int mTextureId = 0;

    private TextureView mTextureView = null;
    private Surface mTempSurface = null;

    private SurfaceView mSurfaceView = null;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                uninitEGLSurface();
            }
        });
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                uninitEGLSurface();
            }
        });
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                uninitEGLSurface();
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                uninitEGLSurface();
            }
        });
    }

    static class PixelBuffer {
        public int width;
        public int height;
        public ByteBuffer buffer;
    }

    private ArrayList<PixelBuffer> mProduceQueue = null;
    private int mWriteIndex = 0;
    private int mWriteRemain = 0;
    private final Object mInputlock = new Object();
    
    private ConcurrentLinkedQueue<PixelBuffer> mConsumeQueue = null;

    private int mMaxBufferSize = 0;

    public final int init() {
        mThread = new HandlerThread("VideoRenderer" + hashCode());
        mThread.start();
        mHandler = new Handler(mThread.getLooper());

        final CountDownLatch barrier = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                eglDisplay = getEglDisplay();
                eglConfig = getEglConfig(eglDisplay, CONFIG_RGBA);
                eglContext = createEglContext(null, eglDisplay, eglConfig);

                Choreographer.getInstance().postFrameCallback(VideoRenderer.this);
                mIsRunning = true;

                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mProduceQueue = new ArrayList<PixelBuffer>();
        mConsumeQueue = new ConcurrentLinkedQueue<PixelBuffer>();
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

    public int setRendererView(TextureView view) {
        final TextureView temp = view;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mTextureView != null) {
                    if (eglSurface != EGL14.EGL_NO_SURFACE) {
                        uninitEGLSurface();
                    }
                    mTextureView.setSurfaceTextureListener(null);
                }

                mTextureView = temp;
                if (mTextureView != null) {
                    mTextureView.setSurfaceTextureListener(VideoRenderer.this);
                }
            }
        });

        return 0;
    }

    public int setRendererView(SurfaceView view) {
        final SurfaceView temp = view;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSurfaceView != null) {
                    if (eglSurface != EGL14.EGL_NO_SURFACE) {
                        uninitEGLSurface();
                    }
                    mSurfaceView.getHolder().removeCallback(VideoRenderer.this);
                }

                mSurfaceView = temp;
                if (mSurfaceView != null) {
                    mSurfaceView.getHolder().addCallback(VideoRenderer.this);
                }
            }
        });

        return 0;
    }

    private void attachTextureView() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            return ;
        }

        if (!mTextureView.isAvailable()) {
            return ;
        }

        mTempSurface = new Surface(mTextureView.getSurfaceTexture());
        mWidth = mTextureView.getWidth();
        mHeight = mTextureView.getHeight();
        try {
            initEGLSurface(mTempSurface);
        } catch (Exception e) {
            mWidth = 0;
            mHeight = 0;
        }
    }

    private void attachSurfaceView() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            return ;
        }

        SurfaceHolder holder = mSurfaceView.getHolder();
        if (holder.isCreating() || null == holder.getSurface()) {
            return ;
        }

        Rect size = holder.getSurfaceFrame();
        mWidth = size.width();
        mHeight = size.height();
        try {
            initEGLSurface(holder.getSurface());
        } catch (Exception e) {
            mWidth = 0;
            mHeight = 0;
        }
    }

    private void detachSurfaceView() {
        uninitEGLSurface();
        mTempSurface.release();
    }

    private void initEGLSurface(Surface surface) {
        try {
            // Both these statements have been observed to fail on rare occasions, see BUG=webrtc:5682.
            createSurface(surface);
            makeCurrent();
        } catch (RuntimeException e) {
            // Clean up before rethrowing the exception.
            uninitEGLSurface();
            throw e;
        }

        shader = new GlShader(VERTEX_SHADER, FRAGMENT_SHADER);
        shader.useProgram();
        m_nFrameUniform = shader.getUniformLocation("frame");
        m_nFactorUniform = shader.getUniformLocation("factor");

        //GlUtil.checkNoGLES2Error("Initialize fragment shader uniform values.");
        // Initialize vertex shader attributes.
        shader.setVertexAttribArray("position", 2, DEVICE_RECTANGLE);
        shader.setVertexAttribArray("texcoord", 2, TEXTURE_RECTANGLE);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        mTextureId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);

        detachCurrent();
    }

    private void createSurface(Object surface) {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
            throw new IllegalStateException("Input must be either a Surface or SurfaceTexture");
        }

        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Already has an EGLSurface");
        }
        int[] surfaceAttribs = {EGL14.EGL_NONE};
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0);
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException(
                    "Failed to create window surface: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        Log.i(TAG, "createSurface");
    }

    private void makeCurrent() {
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't make current");
        }
        synchronized (lock) {
            if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                throw new RuntimeException(
                        "eglMakeCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }
    }

    // Detach the current EGL context, so that it can be made current on another thread.
    private void detachCurrent() {
        synchronized (lock) {
            if (!EGL14.eglMakeCurrent(
                    eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
                throw new RuntimeException(
                        "eglDetachCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }
    }

    private void uninitEGLSurface() {
        if (mTextureId != 0) {
            int[] textures = new int[] {mTextureId};
            GLES20.glDeleteTextures(1, textures, 0);
            mTextureId = 0;
        }

        releaseSurface();
        detachCurrent();

        if (mTempSurface != null) {
            mTempSurface.release();
            mTempSurface = null;
        }
    }

    private void release() {
        uninitEGLSurface();

        EGL14.eglDestroyContext(eglDisplay, eglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(eglDisplay);
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglDisplay = EGL14.EGL_NO_DISPLAY;
        eglConfig = null;
    }

    private void releaseSurface() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public void swapBuffers() {
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't swap buffers");
        }
        synchronized (lock) {
            EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        }
    }

    public void swapBuffers(long timeStampNs) {
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't swap buffers");
        }
        synchronized (lock) {
            // See https://android.googlesource.com/platform/frameworks/native/+/tools_r22.2/opengl/specs/EGL_ANDROID_presentation_time.txt
            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, timeStampNs);
            EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        }
    }

    // Return an EGLDisplay, or die trying.
    private static EGLDisplay getEglDisplay() {
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException(
                    "Unable to get EGL14 display: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException(
                    "Unable to initialize EGL14: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        return eglDisplay;
    }

    // Return an EGLConfig, or die trying.
    private static EGLConfig getEglConfig(EGLDisplay eglDisplay, int[] configAttributes) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(
                eglDisplay, configAttributes, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException(
                    "eglChooseConfig failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        if (numConfigs[0] <= 0) {
            throw new RuntimeException("Unable to find any matching EGL config");
        }
        final EGLConfig eglConfig = configs[0];
        if (eglConfig == null) {
            throw new RuntimeException("eglChooseConfig returned null");
        }
        return eglConfig;
    }

    // Return an EGLConfig, or die trying.
    private static EGLContext createEglContext(
            EGLContext sharedContext, EGLDisplay eglDisplay, EGLConfig eglConfig) {
        if (sharedContext != null && sharedContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Invalid sharedContext");
        }
        int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        EGLContext rootContext =
                sharedContext == null ? EGL14.EGL_NO_CONTEXT : sharedContext;
        final EGLContext eglContext;
        synchronized (VideoRenderer.lock) {
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, rootContext, contextAttributes, 0);
        }
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException(
                    "Failed to create EGL context: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        return eglContext;
    }

    private void createTemp() {

    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!mIsRunning) {
            return ;
        }
        Choreographer.getInstance().postFrameCallback(this);

        if (mTextureView != null) {
            attachTextureView();
        } else if (mSurfaceView != null) {
            attachSurfaceView();
        }

        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            draw();
        }
    }

    private void draw() {
        PixelBuffer pixelBuffer = getConsumerPixelBuffer();
        if (pixelBuffer == null) {
            return;
        }

        makeCurrent();

        long now = SystemClock.elapsedRealtime();
        float factor = 1.0f;

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        pixelBuffer.buffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, pixelBuffer.width, pixelBuffer.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer.buffer);

        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUniform1i(m_nFrameUniform, 1);
        GLES20.glUniform1f(m_nFactorUniform, factor);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        swapBuffers();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        detachCurrent();

        returnProducerPixelBuffer(pixelBuffer);
    }

    private PixelBuffer getConsumerPixelBuffer() {
        if (mConsumeQueue.isEmpty()) {
            return null;
        }
        return mConsumeQueue.poll();
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

    @Override
    public synchronized int dequeueInputBuffer(int width, int height, int stride) {
        if (stride * height > mMaxBufferSize) {
            if (mMaxBufferSize != 0) {
                mProduceQueue.clear();
            }

            mMaxBufferSize = stride * height;
            createPixelBufferPool(4);
        }

        if (mWriteRemain == 0) {
            return -1;
        }

        mWriteRemain--;
        return (mWriteIndex + 1) % mProduceQueue.size();
    }

    @Override
    public synchronized ByteBuffer getInputBuffer(int index) {
        if (mProduceQueue.isEmpty()) {
            return null;
        }
        return mProduceQueue.get(index).buffer;
    }

    @Override
    public synchronized void queueInputBuffer(int bufferIndex, int nChannelIdx, int width, int height, int stride) {
        if (bufferIndex == -1) {
            return ;
        }

        PixelBuffer pixelBuffer = mProduceQueue.get(bufferIndex);
        pixelBuffer.width = width;
        pixelBuffer.height = height;
        mConsumeQueue.add(pixelBuffer);
        mWriteIndex++;
    }

    private synchronized void returnProducerPixelBuffer(PixelBuffer pixelBuffer) {
        if (pixelBuffer.buffer.capacity() == mMaxBufferSize) {
            mWriteRemain++;
        }
    }
}
