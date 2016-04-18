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
    
#include "ZegoAVKit/ZegoAVApi.h"
#import "ZegoAVKit/ZegoChatDelegate.h"


ZegoAVApi * getZegoAV_ShareInstance();
void releaseZegoAV_ShareInstance();
void setCustomAppIDAndSign(uint32 appid, NSData* data);
    void setTestServer(NSString *ip, int port, NSString *url);
    
    
#ifdef __cplusplus
}
#endif
