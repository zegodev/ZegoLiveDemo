//
//  ZegoAVKitManager.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#include "ZegoAVKitManager.h"

ZegoLiveApi *g_zegoAV = NULL;
NSData *g_signKey = nil;
uint32 g_appID = 0;

BOOL g_useTestEnv = NO;
BOOL g_requireHardwareAccelerated = NO;

void setCustomAppIDAndSign(uint32 appid, NSData* data)
{
    g_appID = appid;
    g_signKey = data;
}


NSData * zegoAppSignFromServer()
{
    //!! Demo 把signKey先写到代码中
    //!! 规范用法：这个signKey需要从server下发到App，避免在App中存储，防止盗用
    
    Byte signkey[] = {0x91,0x93,0xcc,0x66,0x2a,0x1c,0xe,0xc1,0x35,0xec,0x71,0xfb,0x7,0x19,0x4b,0x38,0x15,0xf1,0x43,0xf5,0x7c,0xd2,0xb5,0x9a,0xe3,0xdd,0xdb,0xe0,0xf1,0x74,0x36,0xd};
    
    return [NSData dataWithBytes:signkey length:32];
}


#import <ZegoAVKit2/ZegoVideoCapture.h>
#import "./advanced/video_capture_external_demo.h"
#import "./advanced/ZegoVideoCaptureFromImage.h"

static __strong id<ZegoVideoCaptureFactory> g_factory = nullptr;

void ZegoSetVideoCaptureDevice()
{
#if TARGET_OS_SIMULATOR
    if (g_factory == nullptr) {
        g_factory = [[ZegoVideoCaptureFactory alloc] init];
        [ZegoLiveApi setVideoCaptureFactory:g_factory];
    }
#else
/*
    // try VideoCaptureFactoryDemo for camera
    if (g_factory == nullptr)
    {
        g_factory = [[VideoCaptureFactoryDemo alloc] init];
        [ZegoLiveApi setVideoCaptureFactory:g_factory];
    }
 */
#endif
}


ZegoLiveApi * getZegoAV_ShareInstance()
{
    if (g_zegoAV == nil) {
        [ZegoLiveApi setLogLevel:4];
        [ZegoLiveApi setUseTestEnv:g_useTestEnv];
        
        ZegoSetVideoCaptureDevice();
        
        if (g_appID != 0 && g_signKey != nil) {
            g_zegoAV = [[ZegoLiveApi alloc] initWithAppID:g_appID appSignature:g_signKey];
        } else {
            NSData * appSign =  zegoAppSignFromServer();
            g_zegoAV = [[ZegoLiveApi alloc] initWithAppID:1 appSignature:appSign];
        }
        
        [g_zegoAV requireHardwareAccelerated:g_requireHardwareAccelerated];
    }
    return g_zegoAV;
}


void releaseZegoAV_ShareInstance()
{
    g_zegoAV = nil;
}

Byte toByte(NSString* c)
{
    NSString *str = @"0123456789abcdef";
    Byte b = [str rangeOfString:c].location;
    return b;
}

NSData* ConvertStringToSign(NSString* strSign)
{
    if(strSign == nil || strSign.length == 0)
        return nil;
    strSign = [strSign lowercaseString];
    strSign = [strSign stringByReplacingOccurrencesOfString:@" " withString:@""];
    strSign = [strSign stringByReplacingOccurrencesOfString:@"0x" withString:@""];
    NSArray* szStr = [strSign componentsSeparatedByString:@","];
    int nLen = (int)[szStr count];
    Byte szSign[32];
    for(int i = 0; i < nLen; i++)
    {
        NSString *strTmp = [szStr objectAtIndex:i];
        if(strTmp.length == 1)
            szSign[i] = toByte(strTmp);
        else
        {
            szSign[i] = toByte([strTmp substringWithRange:NSMakeRange(0, 1)]) << 4 | toByte([strTmp substringWithRange:NSMakeRange(1, 1)]);
        }
        NSLog(@"%x,", szSign[i]);
    }
    
//    NSData *sign = [[NSData alloc]initWithBytes:szSign length:32];
    NSData *sign = [NSData dataWithBytes:szSign length:32];
    return sign;
}

void ZegoDemoSetCustomAppIDAndSign(uint32 appid, NSString* strSign)
{
    NSData *d = ConvertStringToSign(strSign);
    
    if (d.length == 32 && appid != 0) {
        g_appID = appid;
        g_signKey = d;
    }
    
    g_zegoAV = nil;
}

void setUseTestEnv(BOOL testEnv)
{
    g_useTestEnv = testEnv;
    [ZegoLiveApi setUseTestEnv:testEnv];
}



BOOL isUseingTestEnv()
{
    return g_useTestEnv;
}

uint32 ZegoGetAppID()
{
    return g_appID;
}

void ZegoRequireHardwareAccelerated(bool hardwareAccelerated)
{
    g_requireHardwareAccelerated = hardwareAccelerated;
    [g_zegoAV requireHardwareAccelerated:hardwareAccelerated];
}

BOOL ZegoIsRequireHardwareAccelerated()
{
    return g_requireHardwareAccelerated;
}

NSString *ZegoGetSDKVersion()
{
    return [getZegoAV_ShareInstance() version];
}
