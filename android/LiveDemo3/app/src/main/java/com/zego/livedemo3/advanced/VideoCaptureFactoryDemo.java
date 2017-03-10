package com.zego.livedemo3.advanced;

import android.content.Context;

import com.zego.zegoavkit2.ZegoVideoCaptureDevice;
import com.zego.zegoavkit2.ZegoVideoCaptureFactory;

/**
 * Created by robotding on 16/6/5.
 */
public class VideoCaptureFactoryDemo extends ZegoVideoCaptureFactory {
    private int mode = 0;
    private ZegoVideoCaptureDevice mDevice = null;
    private Context mContext = null;

    public ZegoVideoCaptureDevice create(String device_id) {
        if (mode == 0) {
            mDevice = new VideoCaptureFromCamera();
        } else if (mode == 1) {
            mDevice = new VideoCaptureFromImage(mContext);
        } else if (mode == 2) {
            mDevice = new VideoCaptureFromImage2(mContext);
        }

        return mDevice;
    }

    public void destroy(ZegoVideoCaptureDevice vc) {
        mDevice = null;
    }

    public void setContext(Context context) {
        mContext = context;
    }
}
