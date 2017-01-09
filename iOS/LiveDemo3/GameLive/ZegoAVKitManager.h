//
//  ZegoAVKitManager.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>
#import <ZegoAVKit2/ZegoLiveApi.h>
#import <ZegoAVKit2/ZegoLiveApi-advanced.h>
#import <ZegoAVKit2/ZegoLiveApi-deprecated.h>
#import <ZegoAVKit2/ZegoLiveApi-ReplayLive.h>

@interface ZegoAVKitManager : NSObject

+ (instancetype)sharedInstance;

- (void)handleVideoInputSampleBuffer:(CMSampleBufferRef)sampleBuffer;
- (void)handleAudioInputSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType;

- (void)startLiveWithTitle:(NSString *)liveTitle videoSize:(CGSize)videoSize;
- (void)stopLive;

@end
