//
//  ZegoAVKitManager.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#include "ZegoAVKitManager.h"

ZegoAVApi *g_zegoAV = NULL;

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
    if (g_zegoAV == NULL) {
        NSData * appSign =  zegoAppSignFromServer();
        g_zegoAV = [ZegoAVApi new];
        [g_zegoAV initSDK:1 appSignature:appSign];
    }
    return g_zegoAV;
}


