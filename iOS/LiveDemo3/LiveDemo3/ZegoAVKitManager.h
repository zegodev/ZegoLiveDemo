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
    
#import <ZegoAVKit2/ZegoLiveApi.h>
#import <ZegoAVKit2/ZegoLiveApi-advanced.h>
#import <ZegoAVKit2/ZegoLiveApi-deprecated.h>
    
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
    
    BOOL isUsingExternalRender();
    
#import <ZegoLiveRoom/BizLiveRoom.h>
    
    BizLiveRoom* getBizRoomInstance();
    void releaseBizRoomInstance();
    
#ifdef __cplusplus
}
#endif
