//
//  ZegoLiveApi-deprecated.h
//  zegoavkit
//
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoLiveApi_deprecated_h
#define ZegoLiveApi_deprecated_h

#import "ZegoLiveApi.h"
#import "ZegoAVConfig.h"

@interface ZegoLiveApi (Deprecated)

/// \brief 设置手机姿势
/// \param rotate 逆时针旋转角度
/// \return true:调用成功；false:调用失败
/// \note 已废弃，请使用 setRemoteViewRotation、setLocalViewRotattion 和 setCaptureRotation
- (bool)setDisplayRotation:(CAPTURE_ROTATE)rotate;

@end


typedef enum {
    ZegoAVConfigVideoResolution_320x240     = 0,
    ZegoAVConfigVideoResolution_352x288     = 1,
    ZegoAVConfigVideoResolution_640x480     = 2,
    //ZegoAVConfigVideoResolution_960x540 = 3,
    ZegoAVConfigVideoResolution_1280x720    = 4,
    ZegoAVConfigVideoResolution_1920x1080   = 5,
    ZegoAVConfigVideoResolution_640x360     = 6,
} ZegoAVConfigVideoResolution;

@interface ZegoAVConfig (Deprecated)

/// \brief 设置采集分辨率
/// \param resolution 分辨率
/// \return true:调用成功；false:调用失败
- (bool) setVideoResolution:(ZegoAVConfigVideoResolution)resolution;

/// \brief 设置发布直播的分辨率
/// \param size 分辨率
/// \return true:调用成功；false:调用失败
- (bool) setCustomVideoResolution:(CGSize)size;

/// \brief 设置发布直播的分辨率
/// \return 分辨率
- (CGSize) getVideoResolution;

/// \brief 设置每秒帧数
/// \param fps 每秒帧数
/// \return true:调用成功；false:调用失败
- (bool) setVideoFPS:(int)fps;

/// \brief 获取每秒帧数
/// \return 每秒帧数
- (int) getVideoFPS;

/// \brief 设置码率
/// \param bitrate 码率
/// \return true:调用成功；false:调用失败
- (bool) setVideoBitrate:(int)bitrate;

/// \brief 获取码率
/// \return 码率
- (int) getVideoBitrate;

@end


#endif /* ZegoLiveApi_deprecated_h */
