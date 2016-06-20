package com.zego.livedemo2;


import android.content.Context;
import android.text.TextUtils;

import com.zego.livedemo2.utils.PreferenceUtils;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAvConfig;

/**
 * des: zego api管理器.
 */
public class ZegoApiManager {


    private static ZegoApiManager sInstance = null;

    private ZegoAVKit mZegoAVKit = null;

    private ZegoApiManager() {

        mZegoAVKit = new ZegoAVKit();

        String userID = PreferenceUtils.getInstance().getUserID();
        String userName = PreferenceUtils.getInstance().getUserName();

        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            // 初始化用户信息
            long ms = System.currentTimeMillis();
            userID = ms + "";
            userName = "Android-" + ms;

            PreferenceUtils.getInstance().setUserID(userID);
            PreferenceUtils.getInstance().setUserName(userName);
        }

        // 初始化频道
        if (TextUtils.isEmpty(PreferenceUtils.getInstance().getChannel())) {
            PreferenceUtils.getInstance().setChannel(ZegoApplication.sApplicationContext.getString(R.string.defult_channel));
        }
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

        // 即构分配的key与id
        byte[] signKey = {
                (byte) 0x91, (byte) 0x93, (byte) 0xcc, (byte) 0x66, (byte) 0x2a, (byte) 0x1c, (byte) 0xe, (byte) 0xc1,
                (byte) 0x35, (byte) 0xec, (byte) 0x71, (byte) 0xfb, (byte) 0x7, (byte) 0x19, (byte) 0x4b, (byte) 0x38,
                (byte) 0x15, (byte) 0xf1, (byte) 0x43, (byte) 0xf5, (byte) 0x7c, (byte) 0xd2, (byte) 0xb5, (byte) 0x9a,
                (byte) 0xe3, (byte) 0xdd, (byte) 0xdb, (byte) 0xe0, (byte) 0xf1, (byte) 0x74, (byte) 0x36, (byte) 0xd
        };
        int appID = 1;


        // 初始化sdk
        mZegoAVKit.init(appID, signKey, context);

        // 初始化设置级别为"High"
        mZegoAVKit.setAVConfig(new ZegoAvConfig(ZegoAvConfig.Level.High));
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
        mZegoAVKit.setAVConfig(config);
    }
}
