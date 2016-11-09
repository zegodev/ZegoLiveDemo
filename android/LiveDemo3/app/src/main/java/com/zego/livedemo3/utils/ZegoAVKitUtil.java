package com.zego.livedemo3.utils;

import com.zego.zegoavkit2.ZegoAVKitCommon;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class ZegoAVKitUtil {

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

    public static int getZegoBeauty(int index){

        int beauty = 0;

        switch (index) {
            case 0:
                beauty = ZegoAVKitCommon.ZegoBeauty.NONE;
                break;
            case 1:
                beauty = ZegoAVKitCommon.ZegoBeauty.POLISH;
                break;
            case 2:
                beauty = ZegoAVKitCommon.ZegoBeauty.WHITEN;
                break;
            case 3:
                beauty = ZegoAVKitCommon.ZegoBeauty.POLISH | ZegoAVKitCommon.ZegoBeauty.WHITEN;
                break;
            case 4:
                beauty = ZegoAVKitCommon.ZegoBeauty.POLISH | ZegoAVKitCommon.ZegoBeauty.SKIN_WHITEN;
                break;
        }

        return beauty;
    }


    public static ZegoAVKitCommon.ZegoFilter getZegoFilter(int index){
        ZegoAVKitCommon.ZegoFilter filter = ZegoAVKitCommon.ZegoFilter.None;
        switch (index) {
            case 0:
                filter = ZegoAVKitCommon.ZegoFilter.None;
                break;
            case 1:
                filter = ZegoAVKitCommon.ZegoFilter.Lomo;
                break;
            case 2:
                filter = ZegoAVKitCommon.ZegoFilter.BlackWhite;
                break;
            case 3:
                filter = ZegoAVKitCommon.ZegoFilter.OldStyle;
                break;
            case 4:
                filter = ZegoAVKitCommon.ZegoFilter.Gothic;
                break;
            case 5:
                filter = ZegoAVKitCommon.ZegoFilter.SharpColor;
                break;
            case 6:
                filter = ZegoAVKitCommon.ZegoFilter.Fade;
                break;
            case 7:
                filter = ZegoAVKitCommon.ZegoFilter.Wine;
                break;
            case 8:
                filter = ZegoAVKitCommon.ZegoFilter.Lime;
                break;
            case 9:
                filter = ZegoAVKitCommon.ZegoFilter.Romantic;
                break;
            case 10:
                filter = ZegoAVKitCommon.ZegoFilter.Halo;
                break;
            case 11:
                filter = ZegoAVKitCommon.ZegoFilter.Blue;
                break;
            case 12:
                filter = ZegoAVKitCommon.ZegoFilter.Illusion;
                break;
            case 13:
                filter = ZegoAVKitCommon.ZegoFilter.Dark;
                break;
        }

        return filter;
    }
}
