package com.zego.livedemo2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zego.livedemo2.listener.OnWiredHeadsetChangeListener;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: 接收耳机插拔广播.
 */
public class WiredHeadsetChangeReceiver extends BroadcastReceiver {

    /**
     * 耳机插拔监听器.
     */
    private OnWiredHeadsetChangeListener mWiredHeadsetChangeListener;

    public WiredHeadsetChangeReceiver(OnWiredHeadsetChangeListener listener) {
        mWiredHeadsetChangeListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            if (intent.getIntExtra("state", 0) == 0) {
                if (mWiredHeadsetChangeListener != null) {
                    mWiredHeadsetChangeListener.onWiredHeadsetOff();
                }
            } else if (intent.getIntExtra("state", 0) == 1) {
                if (mWiredHeadsetChangeListener != null) {
                    mWiredHeadsetChangeListener.onWiredHeadsetOn();
                }
            }
        }
    }
}