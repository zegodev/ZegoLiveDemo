package com.zego.livedemo2.advanced;

import com.zego.zegoavkit2.ZegoVideoCaptureDevice;
import com.zego.zegoavkit2.ZegoVideoCaptureFactory;

/**
 * Created by robotding on 16/6/5.
 */
public class VideoCaptureFactoryDemo extends ZegoVideoCaptureFactory {
    VideoCaptureDeviceDemo mDevice = null;
    public ZegoVideoCaptureDevice create(String device_id) {
        if (null == mDevice) {
            mDevice = new VideoCaptureDeviceDemo();
        }
        return mDevice;
    }

    public void destroy(ZegoVideoCaptureDevice vc) {
        if (vc == mDevice) {
            mDevice = null;
        }
    }
}
