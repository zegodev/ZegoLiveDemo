//
//  ZegoSettings.h
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ZegoAVKitManager.h"

@interface ZegoSettings : NSObject
{
    ZegoAVConfig *_currentConfig;
}

+ (instancetype)sharedInstance;

@property (nonatomic, strong) NSString *userID;
@property (nonatomic, strong) NSString *userName;
//@property (nonatomic, strong) NSString *channelID;

@property (readonly) NSArray *presetVideoQualityList;
@property (nonatomic, strong) ZegoAVConfig *currentConfig;
@property (readonly) NSInteger presetIndex;

- (BOOL)selectPresetQuality:(NSInteger)presetIndex;

@property (readonly) ZegoAVConfigVideoResolution currentResolution;

@property (nonatomic, copy) NSString *publishingStreamID;
@property (nonatomic, copy) NSString *publishingLiveChannel;

- (ZegoUser *)getZegoUser;

//用token跟id拼接出一个channelID
- (NSString *)getChannelID:(unsigned int)bizToken bizID:(unsigned int)bizID;

- (UIImage *)getBackgroundImage:(CGSize)viewSize withText:(NSString *)text;

@end
