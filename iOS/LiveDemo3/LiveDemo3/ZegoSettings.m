//
//  ZegoSettings.m
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoSettings.h"
#import <sys/utsname.h>
#include <string>

NSString *kZegoDemoUserIDKey            = @"userid";
NSString *kZegoDemoUserNameKey          = @"username";
NSString *kZegoDemoChannelIDKey         = @"channelid";
NSString *kZegoDemoVideoPresetKey       = @"preset";
NSString *kZegoDemoVideoResolutionKey   = @"resolution";
NSString *kZegoDemoVideoFrameRateKey    = @"framerate";
NSString *kZegoDemoVideoBitRateKey      = @"bitrate";

NSString *kZegoDemoPublishingStreamID   = @"streamID";   ///< 当前直播流 ID
NSString *kZegoDemoPublishingLiveID     = @"liveID";        ///< 当前直播频道 ID

@implementation ZegoSettings
{
    NSString *_userID;
    NSString *_userName;
    NSString *_channelID;
}

+ (instancetype)sharedInstance {
    static ZegoSettings *s_instance = nil;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_instance = [self new];
    });
    
    return s_instance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _presetVideoQualityList = @[NSLocalizedString(@"超低质量", nil),
                                    NSLocalizedString(@"低质量", nil),
                                    NSLocalizedString(@"标准质量", nil),
                                    NSLocalizedString(@"高质量", nil),
                                    NSLocalizedString(@"超高质量", nil),
                                    NSLocalizedString(@"自定义", nil)];
        [self loadConfig];
    }
    
    return self;
}

- (ZegoUser *)getZegoUser
{
    ZegoUser *user = [ZegoUser new];
    user.userID = [ZegoSettings sharedInstance].userID;
    user.userName = [ZegoSettings sharedInstance].userName;
    
    return user;
}


- (NSString *)userID {
    if (_userID.length == 0) {
//        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSUserDefaults *ud = [[NSUserDefaults alloc] initWithSuiteName:@"group.liveDemo3"];
        NSString *userID = [ud stringForKey:kZegoDemoUserIDKey];
        if (userID.length > 0) {
            _userID = userID;
        } else {
            srand((unsigned)time(0));
            _userID = [NSString stringWithFormat:@"%u", (unsigned)rand()];
            [ud setObject:_userID forKey:kZegoDemoUserIDKey];
        }
    }
    
    return _userID;
}


- (void)setUserID:(NSString *)userID {
    if ([_userID isEqualToString:userID]) {
        return;
    }
    
    if (userID.length > 0) {
        _userID = userID;
//        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSUserDefaults *ud = [[NSUserDefaults alloc] initWithSuiteName:@"group.liveDemo3"];
        [ud setObject:_userID forKey:kZegoDemoUserIDKey];
    }
}

/*
- (NSString *)channelID {
    if (_channelID.length == 0) {
        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSString *channelID = [ud stringForKey:kZegoDemoChannelIDKey];
        if (channelID.length > 0) {
            _channelID = channelID;
        } else {
            _channelID = @"5190";
        }
    }
    
    return _channelID;
}


- (void)setChannelID:(NSString *)channelID {
    if ([_channelID isEqualToString:channelID]) {
        return;
    }
    
    if (channelID.length == 0) {
        channelID = @"5190";
    }
    
    _channelID = channelID;
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setObject:_channelID forKey:kZegoDemoChannelIDKey];
}
*/

- (NSString *)userName {
    if (_userName.length == 0) {
//        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSUserDefaults *ud = [[NSUserDefaults alloc] initWithSuiteName:@"group.liveDemo3"];
        NSString *userName = [ud stringForKey:kZegoDemoUserNameKey];
        if (userName.length > 0) {
            _userName = userName;
        } else {
            srand((unsigned)time(0));
            
#if TARGET_OS_IPHONE || TARGET_IPHONE_SIMULATOR
            NSString *systemVerion = nil;
            auto d = [UIDevice currentDevice];
            struct utsname systemInfo;
            uname(&systemInfo);
            NSString* code = [NSString stringWithCString:systemInfo.machine
                                                encoding:NSUTF8StringEncoding];
            code = [code stringByReplacingOccurrencesOfString:@"," withString:@"."];
            systemVerion = [NSString stringWithFormat:@"%@_%@_%@", d.model, code, d.systemVersion];
#endif
            
            _userName = [NSString stringWithFormat:@"%@-%u", systemVerion, (unsigned)rand()];
            
            [ud setObject:_userName forKey:kZegoDemoUserNameKey];
        }
    }
    
    return _userName;
}


- (void)setUserName:(NSString *)userName {
    if ([_userName isEqualToString:userName]) {
        return;
    }
    
    if (userName.length > 0) {
        _userName = userName;
//        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSUserDefaults *ud = [[NSUserDefaults alloc] initWithSuiteName:@"group.liveDemo3"];
        [ud setObject:_userName forKey:kZegoDemoUserNameKey];
    }
}


- (BOOL)selectPresetQuality:(NSInteger)presetIndex {
    if (presetIndex >= self.presetVideoQualityList.count) {
        return NO;
    }
    
    _presetIndex = presetIndex;
    if (_presetIndex < self.presetVideoQualityList.count - 1) {
        _currentConfig = [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)_presetIndex];
    }
    
    [self saveConfig];
    return YES;
}


- (void)setCurrentConfig:(ZegoAVConfig *)currentConfig {
    _presetIndex = self.presetVideoQualityList.count - 1;
    _currentConfig = currentConfig;
    
    [self saveConfig];
}


- (ZegoAVConfigVideoResolution)currentResolution {
    CGSize size = [self.currentConfig getVideoResolution];
    
    ZegoAVConfigVideoResolution r = ZegoAVConfigVideoResolution_640x360;
    switch ((int)size.width) {
        case 320:
            r = ZegoAVConfigVideoResolution_320x240;
            break;
        case 352:
            r = ZegoAVConfigVideoResolution_352x288;
            break;
        case 640:
        {
            if (size.height == 480)
                r = ZegoAVConfigVideoResolution_640x480;
            else if (size.height == 360)
                r = ZegoAVConfigVideoResolution_640x360;
        }
            break;
        case 960:
            r = (ZegoAVConfigVideoResolution)3;
            break;
        case 1280:
            r = ZegoAVConfigVideoResolution_1280x720;
            break;
        case 1920:
            r = ZegoAVConfigVideoResolution_1920x1080;
            break;
            
        default:
            r = (ZegoAVConfigVideoResolution)-1;
            break;
    }
    
    return r;
}


- (void)loadConfig {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    id preset = [ud objectForKey:kZegoDemoVideoPresetKey];
    if (preset) {
        _presetIndex = [preset integerValue];
        if (_presetIndex < _presetVideoQualityList.count - 1) {
            _currentConfig = [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)_presetIndex];
            return ;
        }
    } else {
        _presetIndex = ZegoAVConfigPreset_High;
        _currentConfig = [ZegoAVConfig defaultZegoAVConfig:ZegoAVConfigPreset_High];
        return ;
    }
    
    _currentConfig = [ZegoAVConfig defaultZegoAVConfig:ZegoAVConfigPreset_Generic];
    id resolution = [ud objectForKey:kZegoDemoVideoResolutionKey];
    if (resolution) {
        NSInteger resolutionIndex = [resolution integerValue];
        [_currentConfig setVideoResolution:(ZegoAVConfigVideoResolution)resolutionIndex];
    }
    
    id frameRate = [ud objectForKey:kZegoDemoVideoFrameRateKey];
    if (frameRate) {
        [_currentConfig setVideoFPS:(int)[frameRate integerValue]];
    }
    
    id bitRate = [ud objectForKey:kZegoDemoVideoBitRateKey];
    if (bitRate) {
        [_currentConfig setVideoBitrate:(int)[bitRate integerValue]];
    }
}


- (void)saveConfig {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setObject:@(_presetIndex) forKey:kZegoDemoVideoPresetKey];
    
    if (_presetIndex >= self.presetVideoQualityList.count - 1) {
        ZegoAVConfig *tmpConfig = [[ZegoAVConfig alloc] init];
        for (NSInteger i = 0; i <= 5; i++) {
            [tmpConfig setVideoResolution:(ZegoAVConfigVideoResolution)i];
            CGSize r = [tmpConfig getVideoResolution];
            if (CGSizeEqualToSize(r, [_currentConfig getVideoResolution])) {
                [ud setObject:@(i) forKey:kZegoDemoVideoResolutionKey];
                break;
            }
        }
        
        [ud setObject:@([_currentConfig getVideoFPS]) forKey:kZegoDemoVideoFrameRateKey];
        [ud setObject:@([_currentConfig getVideoBitrate]) forKey:kZegoDemoVideoBitRateKey];
    } else {
        [ud removeObjectForKey:kZegoDemoVideoResolutionKey];
        [ud removeObjectForKey:kZegoDemoVideoFrameRateKey];
        [ud removeObjectForKey:kZegoDemoVideoBitRateKey];
    }
}

- (void)setPublishingStreamID:(NSString *)publishingStreamID {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    
    if (publishingStreamID.length > 0) {
        [ud setObject:publishingStreamID forKey:kZegoDemoPublishingStreamID];
    } else {
        [ud removeObjectForKey:kZegoDemoPublishingStreamID];
    }
}

- (NSString *)publishingStreamID {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kZegoDemoPublishingStreamID];
}

- (void)setPublishingLiveChannel:(NSString *)publishingLiveChannel {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    if (publishingLiveChannel.length > 0) {
        [ud setObject:publishingLiveChannel forKey:kZegoDemoPublishingLiveID];
    } else {
        [ud removeObjectForKey:kZegoDemoPublishingLiveID];
    }
}

- (NSString *)publishingLiveChannel {
    return [[NSUserDefaults standardUserDefaults] objectForKey:kZegoDemoPublishingLiveID];
}

- (NSString *)getChannelID:(unsigned int)bizToken bizID:(unsigned int)bizID
{
    return [NSString stringWithFormat:@"0x%x-0x%x", bizID, bizToken];
}

- (UIImage *)getBackgroundImage:(CGSize)viewSize withText:(NSString *)text
{
    NSTimeInterval beginTime = [[NSDate date] timeIntervalSince1970];
    
    UIImage *backgroundImage = [UIImage imageNamed:@"ZegoBK"];
    UIGraphicsBeginImageContextWithOptions(viewSize, NO, [UIScreen mainScreen].scale);
    
    CGFloat height = viewSize.height;
    if (viewSize.height < viewSize.width)
        height = viewSize.width;
//    CGFloat width = viewSize.width;
    [backgroundImage drawInRect:CGRectMake((viewSize.width - height)/2, (viewSize.height - height)/2, height, height)];
    
    if (text.length != 0)
    {
        UIColor *textColor = [UIColor whiteColor];
        UIFont *textFont = [UIFont systemFontOfSize:30];
        NSDictionary *attributes = @{NSFontAttributeName: textFont, NSForegroundColorAttributeName: textColor};
        CGRect textRect = [text boundingRectWithSize:CGSizeZero options:NSStringDrawingUsesLineFragmentOrigin attributes:attributes context:nil];
        [text drawAtPoint:CGPointMake((viewSize.width - textRect.size.width)/2, (viewSize.height - textRect.size.height)/2) withAttributes:attributes];
    }
    
    UIImage *finalImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    NSLog(@"cost time is %f", [[NSDate date] timeIntervalSince1970] - beginTime);
    
    return finalImage;
}
@end
