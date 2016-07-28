package com.zego.livedemo3.utils;

import com.zego.zegoavkit2.ZegoAVKitCommon;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: 直播工具类.
 */
public class LiveUtils {

    public static ZegoAVKitCommon.ZegoRemoteViewIndex getZegoRemoteViewIndexByOrdinal(int playStreamOrdinal) {
        ZegoAVKitCommon.ZegoRemoteViewIndex index = null;

        switch (playStreamOrdinal) {
            case 0:
                index = ZegoAVKitCommon.ZegoRemoteViewIndex.First;
                break;
            case 1:
                index = ZegoAVKitCommon.ZegoRemoteViewIndex.Second;
                break;
            case 2:
                index = ZegoAVKitCommon.ZegoRemoteViewIndex.Third;
                break;
        }

        return index;
    }
}
