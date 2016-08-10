package com.zego.livedemo3;


import android.content.Context;
import android.text.TextUtils;

import com.zego.biz.BizLiveRoom;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.zegoavkit2.ZegoAVKit;


/**
 * des: zego api管理器.
 */
public class  BizApiManager {

    private static BizApiManager sInstance = null;

    private BizLiveRoom mBizLiveRoom = null;

    private BizApiManager() {
        mBizLiveRoom = new BizLiveRoom();

        String userID = PreferenceUtil.getInstance().getUserID();
        String userName = PreferenceUtil.getInstance().getUserName();

        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            // 初始化用户信息
            long ms = System.currentTimeMillis();
            userID = ms + "";
            userName = "Android-" + ms;

            PreferenceUtil.getInstance().setUserID(userID);
            PreferenceUtil.getInstance().setUserName(userName);
        }
    }

    public static BizApiManager getInstance() {
        if (sInstance == null) {
            synchronized (BizApiManager.class) {
                if (sInstance == null) {
                    sInstance = new BizApiManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化sdk.
     */
    public void init(Context context) {

        // 设置日志level
        mBizLiveRoom.setLogLevel(context, ZegoAVKit.LOG_LEVEL_DEBUG, null);

        // 即构分配的key与id
        byte[] signKey = {
                (byte) 0x91, (byte) 0x93, (byte) 0xcc, (byte) 0x66, (byte) 0x2a, (byte) 0x1c, (byte) 0xe, (byte) 0xc1,
                (byte) 0x35, (byte) 0xec, (byte) 0x71, (byte) 0xfb, (byte) 0x7, (byte) 0x19, (byte) 0x4b, (byte) 0x38,
                (byte) 0x15, (byte) 0xf1, (byte) 0x43, (byte) 0xf5, (byte) 0x7c, (byte) 0xd2, (byte) 0xb5, (byte) 0x9a,
                (byte) 0xe3, (byte) 0xdd, (byte) 0xdb, (byte) 0xe0, (byte) 0xf1, (byte) 0x74, (byte) 0x36, (byte) 0xd
        };
        long appID = 1;

        mBizLiveRoom.initSdk(appID, signKey, signKey.length, context);
    }


    /**
     * 释放sdk.
     */
    public void releaseSDK() {
        mBizLiveRoom.unInitSdk();
    }

    public BizLiveRoom getBizLiveRoom() {
        return mBizLiveRoom;
    }

}