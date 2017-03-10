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
    
    //硬件编码
    void setUsingHardwareEncode(bool bUse);
    BOOL isUsingHardwareEncode();
    
    //硬件解码
    void setUsingHardwareDecode(bool bUse);
    BOOL isUsingHardwareDecode();
    
    //外部采集
    void setUsingExternalCapture(BOOL bUse);
    BOOL isUsingExternalCapture();
    
    //外部渲染
    void setUsingExternalRender(BOOL bUse);
    BOOL isUsingExternalRender();
    
    //外部滤镜
    void setUsingExternalFilter(BOOL bUse);
    BOOL isUsingExternalFilter();
    
    //自适应码率
    void setEnableRateControl(BOOL bEnable);
    BOOL isRateControlOn();
    
    //混响
    void setEnableReverb(BOOL bEnable);
    BOOL isEnableReverb();
    
#import <ZegoLiveRoom/BizLiveRoom.h>
    
    BizLiveRoom* getBizRoomInstance();
    void releaseBizRoomInstance();
    
#ifdef __cplusplus
}
#endif
