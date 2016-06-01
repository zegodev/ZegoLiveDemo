//
//  ZegoAVConfig.h
//  zegoavkit
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreGraphics/CGGeometry.h>

typedef enum {
    ZegoAVConfigPreset_Verylow = 0,
    ZegoAVConfigPreset_Low = 1,
    ZegoAVConfigPreset_Generic = 2,  //手机端直播建议使用Generic配置，效果最优
    ZegoAVConfigPreset_High = 3,
    ZegoAVConfigPreset_Veryhigh = 4
}ZegoAVConfigPreset;

typedef enum {
    ZegoAVConfigVideoResolution_320x240 = 0,
    ZegoAVConfigVideoResolution_352x288 = 1,
    ZegoAVConfigVideoResolution_640x480 = 2,
    //ZegoAVConfigVideoResolution_960x540 = 3,
    ZegoAVConfigVideoResolution_1280x720 = 4,
    ZegoAVConfigVideoResolution_1920x1080 = 5
}ZegoAVConfigVideoResolution;

typedef enum {
    ZegoAVConfigVideoFps_Verylow = 5,
    ZegoAVConfigVideoFps_Low = 10,
    ZegoAVConfigVideoFps_Generic = 15,
    ZegoAVConfigVideoFps_High = 20,
    ZegoAVConfigVideoFps_Veryhigh = 25,
    ZegoAVConfigVideoFps_Superhigh = 30
}ZegoAVConfigVideoFps;

typedef enum {
    ZegoAVConfigVideoBitrate_Verylow = 250*1000,
    ZegoAVConfigVideoBitrate_Low = 300*1000,
    ZegoAVConfigVideoBitrate_Generic = 480*1000,
    ZegoAVConfigVideoBitrate_High = 600*1000,
    ZegoAVConfigVideoBitrate_Veryhigh = 800*1000,
    ZegoAVConfigVideoBitrate_Superhigh = 1000*1000
}ZegoAVConfigVideoBitrate;

@interface ZegoAVConfig : NSObject

/// \brief 获取预设配置
/// \param config 分为5个等级，根据机器性能和网络条件选择预设，手机端直播建议使用Generic配置，效果最优
/// \return true:调用成功；false:调用失败
+(ZegoAVConfig*) defaultZegoAVConfig:(ZegoAVConfigPreset)preset;

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
