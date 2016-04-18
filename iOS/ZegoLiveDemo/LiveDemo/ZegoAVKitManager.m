//
//  ZegoAVKitManager.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#include "ZegoAVKitManager.h"

ZegoAVApi *g_zegoAV = NULL;
NSData *g_signKey;
uint32 g_appID;
NSString *g_testIP = nil;
int g_testPort = 0;
NSString *g_testUrl = nil;

void setCustomAppIDAndSign(uint32 appid, NSData* data)
{
    g_appID = appid;
    g_signKey = data;
}

void setTestServer(NSString *ip, int port, NSString *url)
{
    g_testIP = ip;
    g_testPort = port;
    g_testUrl = url;
}

NSData * zegoAppSignFromServer()
{
    //!! Demo 把signKey先写到代码中
    //!! 规范用法：这个signKey需要从server下发到App，避免在App中存储，防止盗用
    
    Byte signkey[] = {0x91,0x93,0xcc,0x66,0x2a,0x1c,0xe,0xc1,
        0x35,0xec,0x71,0xfb,0x7,0x19,0x4b,0x38,
        0x15,0xf1,0x43,0xf5,0x7c,0xd2,0xb5,0x9a,
        0xe3,0xdd,0xdb,0xe0,0xf1,0x74,0x36,0xd};
    
    
    NSData * appSign = [[NSData alloc] initWithBytes:signkey length:32];
    
    return appSign;
}

ZegoAVApi * getZegoAV_ShareInstance()
{
    if (g_zegoAV == nil) {
        if (g_appID != 0 && g_signKey != nil) {
            g_zegoAV = [ZegoAVApi new];
            [g_zegoAV initSDK:g_appID appSignature:g_signKey];
        }
        else{
            NSData * appSign =  zegoAppSignFromServer();
            g_zegoAV = [ZegoAVApi new];
            [g_zegoAV initSDK:1 appSignature:appSign];
        }
        [g_zegoAV setTestServer:g_testIP port:g_testPort url:g_testUrl];
    }
    return g_zegoAV;
}


void releaseZegoAV_ShareInstance()
{
    if (g_zegoAV) {
        [g_zegoAV uninitSDK];
        g_zegoAV = nil;
    }
}
