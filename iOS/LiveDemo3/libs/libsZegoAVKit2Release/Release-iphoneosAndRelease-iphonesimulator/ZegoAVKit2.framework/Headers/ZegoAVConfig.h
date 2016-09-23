//
//  ZegoAVConfig.h
//  zegoavkit
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreGraphics/CGGeometry.h>

typedef enum {
    ZegoAVConfigPreset_Verylow  = 0,
    ZegoAVConfigPreset_Low      = 1,
    ZegoAVConfigPreset_Generic  = 2,
    ZegoAVConfigPreset_High     = 3,    ///< 手机端直播建议使用High配置，效果最优
    ZegoAVConfigPreset_Veryhigh = 4
} ZegoAVConfigPreset;

typedef enum {
    ZegoAVConfigVideoFps_Verylow    = 5,
    ZegoAVConfigVideoFps_Low        = 10,
    ZegoAVConfigVideoFps_Generic    = 15,
    ZegoAVConfigVideoFps_High       = 20,
    ZegoAVConfigVideoFps_Veryhigh   = 25,
    ZegoAVConfigVideoFps_Superhigh  = 30
} ZegoAVConfigVideoFps;

typedef enum {
    ZegoAVConfigVideoBitrate_Verylow    = 250*1000,
    ZegoAVConfigVideoBitrate_Low        = 300*1000,
    ZegoAVConfigVideoBitrate_Generic    = 480*1000,
    ZegoAVConfigVideoBitrate_High       = 600*1000,
    ZegoAVConfigVideoBitrate_Veryhigh   = 800*1000,
    ZegoAVConfigVideoBitrate_Superhigh  = 1000*1000
} ZegoAVConfigVideoBitrate;


/// \brief 推流视频配置
@interface ZegoAVConfig : NSObject

@property (assign) CGSize videoEncodeResolution;    ///< 视频编码输出分辨率
@property (assign) CGSize videoCaptureResolution;   ///< 视频采集分辨率
@property (assign) int fps;                         ///< 视频帧率
@property (assign) int bitrate;                     ///< 视频码率

/// \brief 获取预设配置
/// \param preset 分为5个等级，根据机器性能和网络条件选择预设，手机端直播建议使用Generic配置，效果最优
/// \return 预设配置
+ (ZegoAVConfig *)defaultZegoAVConfig:(ZegoAVConfigPreset)preset;

@end
