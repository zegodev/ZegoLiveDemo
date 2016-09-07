//
//  ZegoAVKitManager.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#pragma once

#ifdef __cplusplus
extern "C"{
#endif
    
#include <ZegoAVKit2/ZegoLiveApi.h>
    
    
    ZegoLiveApi * getZegoAV_ShareInstance();
    void releaseZegoAV_ShareInstance();
    void ZegoDemoSetCustomAppIDAndSign(uint32 appid, NSString* strSign);
    
    void setUseTestEnv(BOOL testEnv);
    BOOL isUseingTestEnv();
    uint32 ZegoGetAppID();
    
    void setUseAlphaEnv(BOOL alphaEnv);
    BOOL isUsingAlphaEnv();
    
    void ZegoRequireHardwareAccelerated(bool hardwareAccelerated);
    BOOL ZegoIsRequireHardwareAccelerated();
    
    NSString *ZegoGetSDKVersion();
    
#include <ZegoLiveRoom/BizLiveRoom.h>
    
    BizLiveRoom* getBizRoomInstance();
    void releaseBizRoomInstance();
    
#ifdef __cplusplus
}
#endif
