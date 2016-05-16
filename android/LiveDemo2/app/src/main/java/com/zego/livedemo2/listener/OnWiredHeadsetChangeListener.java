package com.zego.livedemo2.listener;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: 耳机插拔监听器.
 */
public interface OnWiredHeadsetChangeListener {

    /**
     * 插入耳机.
     */
    void onWiredHeadsetOn();

    /**
     * 拔掉耳机.
     */
    void onWiredHeadsetOff();
}
