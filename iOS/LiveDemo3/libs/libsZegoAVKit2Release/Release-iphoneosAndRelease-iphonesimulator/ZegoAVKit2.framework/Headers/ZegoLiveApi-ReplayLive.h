//
//  ZegoLiveApi-ReplayLive.h
//  zegoavkit
//
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoLiveApi_ReplayLive_h
#define ZegoLiveApi_ReplayLive_h

#import "ZegoLiveApi.h"
#import <ReplayKit/ReplayKit.h>

@interface ZegoLiveApi (ReplayLive)

/// \brief 初始化ReplayLive时调用
/// \note 必须在 InitSDK 前调用
+ (void)prepareReplayLiveCapture;

/// \brief 处理视频数据
/// \params sampleBuffer ReplayLiveKit返回的视频数据
- (void)handleVideoInputSampleBuffer:(CMSampleBufferRef)sampleBuffer;

/// \brief 处理音频数据
/// \params sampleBuffer ReplayLiveKit返回的音频数据
/// \params sampelBufferType类型 RPSampleBufferTypeAudioApp, RPSampleBufferTypeAudioMic
- (void)handleAudioInputSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType;

@end

#endif /* ZegoLiveApi_ReplayLive_h */
