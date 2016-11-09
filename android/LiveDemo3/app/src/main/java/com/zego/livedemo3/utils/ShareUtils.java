package com.zego.livedemo3.utils;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zego.livedemo3.ZegoApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: 分享工具类.
 */

public class ShareUtils {

    private static ShareUtils sInstance;

    private Tencent mTencent;

    private ShareUtils() {
        mTencent = Tencent.createInstance("1105633775", ZegoApplication.sApplicationContext);
    }

    public static ShareUtils getInstance() {
        if (sInstance == null) {
            synchronized (ShareUtils.class) {
                if (sInstance == null) {
                    sInstance = new ShareUtils();
                }
            }
        }

        return sInstance;
    }

    /**
     * 分享到QQ.
     *
     * @param listShareUrls
     */
    public void shareToQQ(Activity activity, List<String> listShareUrls, long roomKey, long serverKey, String streamID) {
        if (listShareUrls != null && listShareUrls.size() >=2 && activity != null) {

            String url = "http://www.zego.im/share/index?video=" + listShareUrls.get(0) + "&rtmp=" + listShareUrls.get(1) + "&token=" + serverKey
                    + "&id=" + roomKey + "stream=" + streamID;

            Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

            params.putString(QQShare.SHARE_TO_QQ_TITLE, "LiveDemo");
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "快来围观我的直播");
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);

            File f = new File(activity.getExternalCacheDir() + "/logo.png");
            if (!f.exists()) {
                try {
                    InputStream is = activity.getAssets().open("logo.png");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();

                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(buffer);
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, f.getAbsolutePath());
            mTencent.shareToQQ(activity, params, new IUiListener() {
                @Override
                public void onComplete(Object o) {
                }

                @Override
                public void onError(UiError uiError) {
                }

                @Override
                public void onCancel() {
                }
            });
        }else {
            Toast.makeText(activity, "分享失败!", Toast.LENGTH_SHORT).show();
        }
    }
}
