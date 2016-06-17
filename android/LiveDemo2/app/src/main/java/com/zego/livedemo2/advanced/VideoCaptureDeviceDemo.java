package com.zego.livedemo2.advanced;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zego.zegoavkit2.ZegoVideoCaptureDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by robotding on 16/6/5.
 */
public class VideoCaptureDeviceDemo extends ZegoVideoCaptureDevice implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCam = null;
    private Camera.CameraInfo mCamInfo = null;
    int mFront = 0;
    int mWidth = 640;
    int mHeight = 480;
    int mFrameRate = 15;
    int mRotation = 0;

    ZegoVideoCaptureDevice.Client mClient = null;

    private SurfaceView mSurView = null;
    private SurfaceHolder mHolder = null;

    boolean mIsPreview = false;
    boolean mIsCapture = false;

    private ByteBuffer mTempBuffer = null;

    protected void allocateAndStart(ZegoVideoCaptureDevice.Client client) {
        mClient = client;
    }

    protected void stopAndDeAllocate() {
        mClient.destroy();
        mClient = null;
    }

    protected int startCapture() {
        if (mIsCapture) {
            return 0;
        }

        if (!mIsPreview) {
            // * Create and Start Cam
            createCam();
            startCam();
        }

        mIsCapture = true;

        return 0;
    }

    protected int stopCapture() {
        if (!mIsCapture) {
            // * not started
            return 0;
        }

        mIsCapture = false;

        // * to_do: notify EOS

        if (!mIsPreview) {
            // * Stop and destroy cam
            stopCam();
            releaseCam();
        }

        return 0;
    }

    protected int setFrameRate(int framerate) {
        mFrameRate = framerate;
        if (mCam != null) {
            updateRate(framerate);
        }
        return 0;
    }

    protected int setResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
        restartCam();
        return 0;
    }

    protected int setFrontCam(int bFront) {
        mFront = bFront;
        restartCam();
        return 0;
    }

    protected int setView(SurfaceView view) {
        removeView();
        mSurView = view;
        if (mSurView != null) {
            mHolder = mSurView.getHolder();
            if (mHolder != null) {
                mHolder.addCallback(this);
            }
        }
        return 0;
    }

    protected int setViewMode(int nMode) {
        return 0;
    }

    protected int setViewRotation(int nRotation) { return 0; }

    protected int setCaptureRotation(int nRotation) {
        mRotation = nRotation;
        return 0;
    }

    protected int startPreview() {
        if (mIsPreview) {
            // * already started
            return 0;
        }

        if (!mIsCapture) {
            // * Create and Start Cam
            createCam();
            startCam();
        }

        // * set flags
        mIsPreview = true;
        return 0;
    }

    protected int stopPreview() {
        if (!mIsPreview) {
            // * not started
            return 0;
        }

        mIsPreview = false;

        // * to_do: notify EOS

        if (!mIsCapture) {
            // * Stop and destroy cam
            stopCam();
            releaseCam();
        }

        return 0;
    }

    protected int enableTorch(boolean bEnable) {
        if (bEnable) {
            return openTorch();
        } else {
            return closeTorch();
        }
    }

    protected int takeSnapshot() {
        int i = 0;
        i++;
        return 0;
    }

    protected int setPowerlineFreq(int nFreq) {
        return 0;
    }


    private int getOrientation() {
        if (mCamInfo != null) {
            return mCamInfo.orientation;
        }

        return 0;
    }

    private int updateRate(final int framerate) {
        mFrameRate = framerate;

        Camera.Parameters parms = mCam.getParameters();
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && entry[0] == mFrameRate * 1000) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                break;
            }
        }

        int[] realRate = new int[2];
        parms.getPreviewFpsRange(realRate);
        if (realRate[0] == realRate[1]) {
            mFrameRate = realRate[0] / 1000;
        } else {
            mFrameRate = realRate[1] / 2 / 1000;
        }

        try {
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i("ve", "vcap: update fps -- set camera parameters error with exception\n");
            ex.printStackTrace();
        }
        return 0;
    }

    private int createCam() {
        Log.i("ve", "board: " + Build.BOARD);
        Log.i("ve", "device: " + Build.DEVICE);
        Log.i("ve", "manufacturer: " + Build.MANUFACTURER);
        Log.i("ve", "brand: " + Build.BRAND);
        Log.i("ve", "model: " + Build.MODEL);
        Log.i("ve", "product: " + Build.PRODUCT);
        Log.i("ve", "sdk: " + Build.VERSION.SDK_INT);

        int nFacing = (mFront != 0) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;

        if (mCam != null) {
            // * already created
            return 0;
        }

        // * find camera
        mCamInfo = new Camera.CameraInfo();
        int nCnt = Camera.getNumberOfCameras();
        for (int i = 0; i < nCnt; i++) {
            Camera.getCameraInfo(i, mCamInfo);
            if (mCamInfo.facing == nFacing) {
                mCam = Camera.open(i);
                break;
            }
        }

        // * no camera found ??
        if (mCam == null) {
            Log.i("ve", "[WARNING] no camera found, try default\n");
            mCam = Camera.open();

            if (mCam == null) {
                Log.i("ve", "[ERROR] no camera found\n");
                return -1;
            }
        }

        // *
        // * Now set preview size
        // *
        boolean bSizeSet = false;
        Camera.Parameters parms = mCam.getParameters();
        Camera.Size psz = parms.getPreferredPreviewSizeForVideo();
        int nCandidateWidth = 0;
        int nCandidateHeight = 0;
        int nLargestWidth = 0;
        int nLargestHeight = 0;

        List<Camera.Size> lst = parms.getSupportedVideoSizes();
        if (lst == null) {
            lst = parms.getSupportedPreviewSizes();
        }
        // * for(Camera.Size sz : parms.getSupportedPreviewSizes())
        for (Camera.Size sz : lst) {
            // * find max
            if (sz.width * sz.height > nLargestWidth * nLargestHeight) {
                nLargestWidth = sz.width;
                nLargestHeight = sz.height;
            }
        }

        for (Camera.Size sz : lst) {
            if (sz.width * nLargestHeight != sz.height * nLargestWidth) {
                continue;
            }

            // * find best candidate
            if ((sz.width >= mWidth) && (sz.height >= mHeight)) {
                // * candidate
                if ((nCandidateWidth < mWidth) || (nCandidateHeight < mHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                } else if ((sz.width * sz.height) < (nCandidateWidth * nCandidateHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                }
            } else if (sz.width >= mWidth) {
                if ((nCandidateWidth >= mWidth) && (nCandidateHeight >= mHeight)) {
                    // * candidate is better, dont change
                } else if ((nCandidateWidth < mWidth) && (nCandidateHeight < mHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                } else if ((nCandidateWidth >= mWidth) && (sz.height > nCandidateHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                } else if ((sz.width * sz.height) > (nCandidateWidth * nCandidateHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                }
            } else if (sz.height >= mHeight) {
                if ((nCandidateWidth >= mWidth) && (nCandidateHeight >= mHeight)) {
                    // * candidate is better, dont change
                } else if ((nCandidateWidth < mWidth) && (nCandidateHeight < mHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                } else if ((nCandidateHeight >= mHeight) && (sz.width > nCandidateWidth)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                } else if ((sz.width * sz.height) > (nCandidateWidth * nCandidateHeight)) {
                    nCandidateWidth = sz.width;
                    nCandidateHeight = sz.height;
                }
            }
        }

        if (!bSizeSet) {
            if (nCandidateWidth * nCandidateHeight != 0) {
                parms.setPreviewSize(nCandidateWidth, nCandidateHeight);
                mWidth = nCandidateWidth;
                mHeight = nCandidateHeight;
                bSizeSet = true;
            } else {
                parms.setPreviewSize(nLargestWidth, nLargestHeight);
                mWidth = nLargestWidth;
                mHeight = nLargestHeight;
                bSizeSet = true;
            }
        }

        // * use preferred preview size for the following device
        if (Build.MANUFACTURER.equals("Xiaomi") && Build.MODEL.equals("MI 4LTE") && (Build.VERSION.SDK_INT <= 19)) {
            Log.i("ve", "$$$$$$$$ FIX Xiaomi MI 4LTE crash $$$$$$$$");
            bSizeSet = false;
        }

        // * hack -- use prefer preview size, too many phones don't handle aspect ratio correctly
//		if(psz != null)
//		{
//			if (psz.width >= mWidth && psz.height >= mHeight) {
//				bSizeSet = false;
//			}
//		}
//
        if (!bSizeSet && (psz != null)) {
            parms.setPreviewSize(psz.width, psz.height);
            mWidth = psz.width;
            mHeight = psz.height;
        }

        // *
        // * Now set fps
        // *
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && entry[0] == mFrameRate * 1000) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                break;
            }
        }

        int[] realRate = new int[2];
        parms.getPreviewFpsRange(realRate);
        if (realRate[0] == realRate[1]) {
            mFrameRate = realRate[0] / 1000;
        } else {
            mFrameRate = realRate[1] / 2 / 1000;
        }

        // *
        // * Recording hint
        // *
        parms.setRecordingHint(true);

        // *
        // * focus mode
        // *
        boolean bFocusModeSet = false;
        for (String mode : parms.getSupportedFocusModes()) {
            if (mode.compareTo(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) == 0) {
                try {
                    parms.setFocusMode(mode);
                    bFocusModeSet = true;
                    break;
                } catch (Exception ex) {
                    Log.i("ve", "[WARNING] vcap: set focus mode error (stack trace followed)!!!\n");
                    ex.printStackTrace();
                }
            }
        }
        if (!bFocusModeSet) {
            Log.i("ve", "[WARNING] vcap: focus mode left unset !!\n");
        }

        // *
        // * Now try to set parm
        // *
        try {
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i("ve", "vcap: set camera parameters error with exception\n");
            ex.printStackTrace();
        }

        Camera.Parameters actualParm = mCam.getParameters();
        mWidth = actualParm.getPreviewSize().width;
        mHeight = actualParm.getPreviewSize().height;
        Log.i("ve", "[WARNING] vcap: focus mode " + actualParm.getFocusMode());

        int size = mWidth * mHeight * 3 / 2;
        if (mTempBuffer == null || mTempBuffer.capacity() != size) {
            mTempBuffer = ByteBuffer.allocateDirect(size);
        }

        int result;
        if (mCamInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCamInfo.orientation + mRotation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (mCamInfo.orientation - mRotation + 360) % 360;
        }
        mCam.setDisplayOrientation(result);

        return 0;
    }

    private int startCam() {
        // * mCam.setDisplayOrientation(90);
        if (mHolder == null) {
            Log.i("ve", "vcap: mHolder == null\n");
            return -1;
        }

        try {
            mCam.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCam.setPreviewCallback(this);
        mCam.startPreview();
        return 0;
    }

    private int stopCam() {
        if (mCam != null) {
            mCam.stopPreview();
            mCam.setPreviewCallback(null);
            try {
                mCam.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private int restartCam() {
        boolean bStarted = mIsPreview || mIsCapture;
        if(bStarted) {
            stopCam();
            releaseCam();
            createCam();
            startCam();
        }

        return 0;
    }

    private int releaseCam() {
        // * release cam
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }

        // * release cam info
        mCamInfo = null;
        return 0;
    }

    private int setSurfaceTexture(SurfaceTexture st) {
        if (mCam == null) {
            return -1;
        }

        try {
            mCam.setPreviewTexture(st);
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }

        return 0;
    }

    private int openTorch() {
        if (mCam == null) {
            return -1;
        }
        Camera.Parameters parms = mCam.getParameters();

        boolean bFlashModeSet = false;
        for (String fmode : parms.getSupportedFlashModes()) {
            if (fmode.compareTo(Camera.Parameters.FLASH_MODE_TORCH) == 0) {
                try {
                    bFlashModeSet = true;
                    parms.setFlashMode(fmode);
                } catch (Exception ex) {
                    Log.i("ve", "[ERROR] vcap: set flash mode failed\n");
                    ex.printStackTrace();
                }
            }
        }

        if (!bFlashModeSet) {
            Log.i("ve", "vcap: flash mode left unset\n");
            return 0;
        }

        try {
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i("ve", "vcap: set flash mode -- set camera parameters error with exception\n");
            ex.printStackTrace();
        }

        return 0;
    }

    private int closeTorch() {
        if (mCam == null) {
            return -1;
        }
        Camera.Parameters parms = mCam.getParameters();

        boolean bFlashModeSet = false;
        for (String fmode : parms.getSupportedFlashModes()) {
            if (fmode.compareTo(Camera.Parameters.FLASH_MODE_OFF) == 0) {
                try {
                    bFlashModeSet = true;
                    parms.setFlashMode(fmode);
                } catch (Exception ex) {
                    Log.i("ve", "[ERROR] vcap: set flash mode failed\n");
                    ex.printStackTrace();
                }
            }
        }

        if (!bFlashModeSet) {
            Log.i("ve", "vcap: flash mode left unset\n");
            return 0;
        }

        try {
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i("ve", "vcap: set flash mode -- set camera parameters error with exception\n");
            ex.printStackTrace();
        }

        return 0;
    }

    private int removeView() {
        if (mSurView != null) {
            SurfaceHolder sh = mSurView.getHolder();
            if (sh != null) {
                sh.removeCallback(this);
            }
            mSurView = null;
            mHolder = null;
        }
        return 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        restartCam();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        restartCam();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        mIsCapture = false;
        mIsPreview = false;
        stopCam();
        releaseCam();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!mIsCapture) {
            return ;
        }

        if (mClient == null) {
            return;
        }

        if (mTempBuffer == null || mTempBuffer.capacity() != data.length) {
            mTempBuffer = ByteBuffer.allocateDirect(data.length);
        }

        if (!mTempBuffer.isDirect()) {
            return ;
        }

        mTempBuffer.clear();
        mTempBuffer.put(data);
        mTempBuffer.position(0);
        mTempBuffer.limit(data.length);

        VideoCaptureFormat format = new ZegoVideoCaptureDevice.VideoCaptureFormat();
        format.width = mWidth;
        format.height = mHeight;
        format.pixel_format = PIXEL_FORMAT_NV21;
        mClient.onIncomingCapturedData(mTempBuffer, format, System.currentTimeMillis(), 1000);
    }
}
