package com.zego.zegolivedemo;

import android.content.Context;

import com.zego.zegoavkit.ZegoAVApi;
import com.zego.zegoavkit.ZegoAvConfig;
import com.zego.zegoavkit.ZegoUser;

/**
 * Zego API Manager.
 */
public class ZegoApiManager {

    private static ZegoApiManager sInstance = null;

    private ZegoAVApi mZegoAVApi;

    private ZegoUser mZegoUser;

    private ZegoAvConfig mZegoAVConfig;

    private ZegoApiManager() {

        mZegoAVApi = new ZegoAVApi();

        // 初始化用户信息
        mZegoUser = new ZegoUser();
        long ms = System.currentTimeMillis();
        mZegoUser.name = "User" + ms;
        mZegoUser.id = ms + "";

        // 初始化配置信息
        mZegoAVConfig = new ZegoAvConfig();
        mZegoAVConfig.setResolution(640, 480);

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
     *
     * @param ctx  Application Context
     */
    public void initSdk(Context ctx) {
        mZegoAVApi.setLogLevel(ctx, ZegoAVApi.LOG_LEVEL_DEBUG, null);

        byte[] signKey = {
                (byte) 0x91, (byte) 0x93, (byte) 0xcc, (byte) 0x66, (byte) 0x2a, (byte) 0x1c, (byte) 0xe, (byte) 0xc1,
                (byte) 0x35, (byte) 0xec, (byte) 0x71, (byte) 0xfb, (byte) 0x7, (byte) 0x19, (byte) 0x4b, (byte) 0x38,
                (byte) 0x15, (byte) 0xf1, (byte) 0x43, (byte) 0xf5, (byte) 0x7c, (byte) 0xd2, (byte) 0xb5, (byte) 0x9a,
                (byte) 0xe3, (byte) 0xdd, (byte) 0xdb, (byte) 0xe0, (byte) 0xf1, (byte) 0x74, (byte) 0x36, (byte) 0xd
        };

        int appID = 1;
        mZegoAVApi.initSDK(appID, signKey, ctx);

        ZegoAvConfig config = new ZegoAvConfig();
        config.setResolution(640, 480);
        mZegoAVApi.setAVConfig(config);
    }

    /**
     * 释放sdk.
     */
    public void releaseSDK() {
        mZegoAVApi.unInitSDK();
        sInstance = null;
    }

    public ZegoAVApi getZegoAVApi() {
        return mZegoAVApi;
    }


    public ZegoAvConfig getZegoAVConfig() {
        return mZegoAVConfig;
    }

    public void setZegoAVConfig(ZegoAvConfig zegoAVConfig) {
        mZegoAVConfig = zegoAVConfig;
    }

    public ZegoUser getZegoUser() {
        return mZegoUser;
    }

    public void setZegoUser(ZegoUser zegoUser) {
        mZegoUser = zegoUser;
    }

    public static void setInstance(ZegoApiManager instance) {
        sInstance = instance;
    }
}
