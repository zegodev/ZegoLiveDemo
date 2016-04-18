package com.zego.zegolivedemo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast统一管理类.
 */
public class ToastUtils {

    private Context mContext;

    private ToastUtils(Context context) {
        mContext = context;
    }


    public boolean isShow = true;

    public static ToastUtils genToastUtils(Context context) {
        if (context == null) {
            throw new NullPointerException("activity不能为空");
        }

        return new ToastUtils(context);
    }

    /**
     * 短时间显示Toast
     */
    public void showShort(CharSequence message) {
        if (isShow) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 短时间显示Toast
     */
    public void showShort(int message) {
        if (isShow) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 长时间显示Toast
     */
    public void showLong(CharSequence message) {
        if (isShow) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 长时间显示Toast
     */
    public void showLong(int message) {
        if (isShow) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 自定义显示Toast时间
     */
    public void show(CharSequence message, int duration) {
        if (isShow) {
            Toast.makeText(mContext, message, duration).show();
        }
    }

    /**
     * 自定义显示Toast时间
     */
    public void show(int message, int duration) {
        if (isShow) {
            Toast.makeText(mContext, message, duration).show();
        }
    }
}
