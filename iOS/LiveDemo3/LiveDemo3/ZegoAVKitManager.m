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
BOOL g_useAlphaEnv = NO;

#if TARGET_OS_SIMULATOR
BOOL g_useHardwareEncode = NO;
BOOL g_useHardwareDecode = NO;
#else
BOOL g_useHardwareEncode = YES;
BOOL g_useHardwareDecode = YES;
#endif

BOOL g_enableVideoRateControl = NO;

BizLiveRoom *g_bizRoom = nil;

BOOL g_useExternalCaptrue = NO;
BOOL g_useExternalRender = NO;
BOOL g_useExternalFilter = NO;

BOOL g_enableReverb = NO;

void setCustomAppIDAndSign(uint32 appid, NSData* data)
{
    g_appID = appid;
    g_signKey = data;
}

static uint32 GetTestZegoAVKitAppID();
static NSData * GetTestZegoAVKitAppSign();
static uint32 GetTestBizAppID();
static NSData * GetTestBizAppSign();

NSData * zegoAppSignFromServer()
{
    //!! Demo 把signKey先写到代码中
    //!! 规范用法：这个signKey需要从server下发到App，避免在App中存储，防止盗用
    
    Byte signkey[] = {};
    
    return [NSData dataWithBytes:signkey length:32];
}


#import <ZegoAVKit2/ZegoVideoCapture.h>
#import "./advanced/video_capture_external_demo.h"
#import "./advanced/ZegoVideoCaptureFromImage.h"
#import "./advanced/ZegoVideoFilterDemo.h"

static __strong id<ZegoVideoCaptureFactory> g_factory = nullptr;
static __strong id<ZegoVideoFilterFactory> g_filterFactory = nullptr;

void ZegoSetVideoCaptureDevice()
{
#if TARGET_OS_SIMULATOR
    if (g_factory == nullptr) {
        
        g_useExternalCaptrue = YES;
        
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


BOOL isUsingExternalRender()
{
    return g_useExternalRender;
}

void setUsingExternalRender(BOOL bUse)
{
    if (g_useExternalRender == bUse)
        return;
    
    g_useExternalRender = bUse;
    
    releaseZegoAV_ShareInstance();
    
    [ZegoLiveApi setExtenralRender:bUse];
}

BOOL isUsingExternalCapture()
{
    return g_useExternalCaptrue;
}

void setUsingExternalCapture(BOOL bUse)
{
    if (g_useExternalCaptrue == bUse)
        return;
    
    g_useExternalCaptrue = bUse;
    
    releaseZegoAV_ShareInstance();
    
    if (bUse)
    {
        if (g_factory == nil)
            g_factory = [[ZegoVideoCaptureFactory alloc] init];
        
        [ZegoLiveApi setVideoCaptureFactory:g_factory];
    }
    else
    {
        [ZegoLiveApi setVideoCaptureFactory:nil];
    }
}

BOOL isUsingExternalFilter()
{
    return g_useExternalFilter;
}

void setUsingExternalFilter(BOOL bUse)
{
    if (g_useExternalFilter == bUse)
        return;
    
    g_useExternalFilter = bUse;
    
    releaseZegoAV_ShareInstance();
    
    if (bUse)
    {
        if (g_filterFactory == nullptr)
            g_filterFactory = [[ZegoVideoFilterFactoryDemo alloc] init];
        
        [ZegoLiveApi setVideoFilterFactory:g_filterFactory];
    }
    else
    {
        [ZegoLiveApi setVideoFilterFactory:nil];
    }
}

BOOL isRateControlOn()
{
    return g_enableVideoRateControl;
}

void setEnableRateControl(BOOL bEnable)
{
    if (g_enableVideoRateControl == bEnable)
        return;
    
    if (bEnable)
    {
        if (g_useHardwareEncode)
        {
            g_useHardwareEncode = NO;
            [ZegoLiveApi requireHardwareEncoder:false];
        }
        
        g_enableVideoRateControl = YES;
    }
    else
    {
        g_enableVideoRateControl = NO;
    }
    
    [g_zegoAV enableRateControl:g_enableVideoRateControl];
}

void setUsingHardwareEncode(bool bUse)
{
    if (g_useHardwareEncode == bUse)
        return;
    
    if (bUse)
    {
        if (g_enableVideoRateControl)
        {
            g_enableVideoRateControl = NO;
            [g_zegoAV enableRateControl:NO];
        }
    }
    
    g_useHardwareEncode = bUse;
    [ZegoLiveApi requireHardwareEncoder:bUse];
}

BOOL isUsingHardwareEncode()
{
    return g_useHardwareEncode;
}

void setUsingHardwareDecode(bool bUse)
{
    if (g_useHardwareDecode == bUse)
        return;
    
    g_useHardwareDecode = bUse;
    [ZegoLiveApi requireHardwareDecoder:g_useHardwareDecode];
}

BOOL isUsingHardwareDecode()
{
    return g_useHardwareDecode;
}

void prep_func(const short* inData, int inSamples, int sampleRate, short *outData)
{
    memcpy(outData, inData, inSamples * sizeof(short));
}

void setEnableReverb(BOOL bEnable)
{
    if (g_enableReverb == bEnable)
        return;
    
    g_enableReverb = bEnable;
    
    releaseZegoAV_ShareInstance();
    
    if (bEnable)
    {
        [ZegoLiveApi setAudioPrep:&prep_func];
    }
    else
    {
        [ZegoLiveApi setAudioPrep:nil];
    }
}

BOOL isEnableReverb()
{
    return g_enableReverb;
}



ZegoLiveApi * getZegoAV_ShareInstance()
{
    if (g_zegoAV == nil) {
        [ZegoLiveApi setLogLevel:4];
        [ZegoLiveApi setUseTestEnv:g_useTestEnv];
        [ZegoLiveApi setExtenralRender:isUsingExternalRender()];
        
#ifdef DEBUG
        [ZegoLiveApi setVerbose:YES];
#endif
        
        ZegoSetVideoCaptureDevice();
        
        if (g_appID != 0 && g_signKey != nil) {
            g_zegoAV = [[ZegoLiveApi alloc] initWithAppID:g_appID appSignature:g_signKey];
        } else {
            NSData * appSign =  zegoAppSignFromServer();
            g_zegoAV = [[ZegoLiveApi alloc] initWithAppID:1 appSignature:appSign];
        }
        
        setUsingHardwareDecode(g_useHardwareDecode);
        setUsingHardwareEncode(g_useHardwareEncode);
    }
    return g_zegoAV;
}


void releaseZegoAV_ShareInstance()
{
    g_zegoAV = nil;
}

BizLiveRoom *getBizRoomInstance()
{
    if (g_bizRoom == nil)
    {
        [BizLiveRoom setLogLevel:4];
        
        if (GetTestZegoAVKitAppID() != 0 && GetTestZegoAVKitAppSign() != nil)
        {
            g_bizRoom = [[BizLiveRoom alloc] initWithBizID:GetTestBizAppID() bizSignature:GetTestBizAppSign()];
        }
        else
        {
            NSData *appSign = zegoAppSignFromServer();
            g_bizRoom = [[BizLiveRoom alloc] initWithBizID:1 bizSignature:appSign];
        }
        
        if (g_bizRoom == nil)
        {
            NSString *alertMessage = NSLocalizedString(@"Zego Only", nil);
            NSLog(@"%@", alertMessage);
            
            UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:alertMessage delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
            [alertView show];
            
            assert(g_bizRoom != nil);
        }
    }
    
    return g_bizRoom;
}

void releaseBizRoomInstance()
{
    g_bizRoom = nil;
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
    
    NSData *sign = [NSData dataWithBytes:szSign length:32];
    return sign;
}

void ZegoDemoSetCustomAppIDAndSign(uint32 appid, NSString* strSign)
{
    NSData *d = ConvertStringToSign(strSign);
    
    if (d.length == 32 && appid != 0) {
        g_appID = appid;
        g_signKey = [[NSData alloc] initWithData:d];
    }
    
    g_zegoAV = nil;
    
    //清理bizRoom对象时，还需要重新设置一下原来的delegate
    g_bizRoom = nil;
    [[NSNotificationCenter defaultCenter] postNotificationName:@"RoomInstanceClear" object:nil userInfo:nil];
}

void setUseTestEnv(BOOL testEnv)
{
    if (g_useTestEnv != testEnv) {
        releaseZegoAV_ShareInstance();
    }
    
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

#pragma mark - helper

static uint32 GetTestZegoAVKitAppID()
{
    return g_appID;
}

static NSData * GetTestZegoAVKitAppSign()
{
    return g_signKey;
}

static uint32 GetTestBizAppID()
{
    return 0;
}

static NSData * GetTestBizAppSign()
{
    Byte signKey[] = {};
    return [NSData dataWithBytes:signKey length:32];
}


#pragma mark - alpha support

@interface NSObject()
// * suppress warning
+ (void)setUseAlphaEnv:(id)useAlphaEnv;
@end

void setUseAlphaEnv(BOOL alphaEnv)
{
    if ([ZegoLiveApi respondsToSelector:@selector(setUseAlphaEnv:)])
    {
        if (g_useAlphaEnv != alphaEnv)
            releaseZegoAV_ShareInstance();
        
        g_useAlphaEnv = alphaEnv;
        
        [ZegoLiveApi performSelector:@selector(setUseAlphaEnv:) withObject:@(alphaEnv)];
    }
}

BOOL isUsingAlphaEnv()
{
    return g_useAlphaEnv;
}
