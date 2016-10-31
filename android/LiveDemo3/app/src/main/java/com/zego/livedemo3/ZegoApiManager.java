package com.zego.livedemo3;


import android.content.Context;

import com.zego.livedemo3.advanced.VideoCaptureFactoryDemo;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAvConfig;

/**
 * des: zego api管理器.
 */
public class ZegoApiManager {


    private static ZegoApiManager sInstance = null;

    private ZegoAVKit mZegoAVKit = null;

    private ZegoAvConfig mZegoAvConfig;

    private boolean mUseExternalRender = false;

    private ZegoApiManager() {
        mZegoAVKit = new ZegoAVKit();
    }

    public static ZegoApiManager getInstance() {
        if (sInstance == null) {
            synchronized (ZegoApiManager.class) {
                if (sInstance == null) {
                    sInstance = new ZegoApiManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化sdk.
     */
    public void initSDK(Context context) {

        // 设置日志level
        mZegoAVKit.setLogLevel(context, ZegoAVKit.LOG_LEVEL_DEBUG, null);

        boolean bUseVideoCapture = false;
        if (bUseVideoCapture) {
            // 外部采集
            VideoCaptureFactoryDemo factoryDemo = new VideoCaptureFactoryDemo();
            mZegoAVKit.setVideoCaptureFactory(factoryDemo);
        }

        // 即构分配的key与id
        byte[] signKey = {
                (byte)0x91, (byte)0x93, (byte)0xcc, (byte)0x66, (byte)0x2a, (byte)0x1c, (byte)0x0e, (byte)0xc1,
                (byte)0x35, (byte)0xec, (byte)0x71, (byte)0xfb, (byte)0x07, (byte)0x19, (byte)0x4b, (byte)0x38,
                (byte)0x41, (byte)0xd4, (byte)0xad, (byte)0x83, (byte)0x78, (byte)0xf2, (byte)0x59, (byte)0x90,
                (byte)0xe0, (byte)0xa4, (byte)0x0c, (byte)0x7f, (byte)0xf4, (byte)0x28, (byte)0x41, (byte)0xf7
        };
        int appID = 1;

        // 开启外部渲染, 少数企业的需求
        if(mUseExternalRender){
            mZegoAVKit.setExternalRender(true);
        }

        // 初始化sdk
        mZegoAVKit.init(appID, signKey, context);

        mZegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.High);

        // 初始化设置级别为"High"
        mZegoAVKit.setAVConfig(mZegoAvConfig);
    }


    /**
     * 释放sdk.
     */
    public void releaseSDK() {
        mZegoAVKit.unInit();
        mZegoAVKit = null;
        sInstance = null;
    }

    public ZegoAVKit getZegoAVKit() {
        return mZegoAVKit;
    }

    public void setZegoConfig(ZegoAvConfig config) {
        mZegoAvConfig = config;
        mZegoAVKit.setAVConfig(config);
    }


    public ZegoAvConfig getZegoAvConfig(){
        return  mZegoAvConfig;
    }

    public boolean getUseExternalRender(){
        return mUseExternalRender;
    }
}
