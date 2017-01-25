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
/// \note 已废弃，请使用 setRemoteViewRotation、setLocalViewRotattion
- (bool)setDisplayRotation:(CAPTURE_ROTATE)rotate;

/// \brief 设置采集时摄像头方向,在startPublish前设置有效，startPublish后调用则返回false
/// \param rotate 方向
/// \return true:调用成功；false:调用失败
- (bool)setCaptureRotation:(CAPTURE_ROTATE)rotate;

/// \brief 设置预览渲染朝向
/// \param rotate 旋转角度
/// \return true 成功，false 失败
/// \note 使用setAppOrientation 替代
- (bool)setLocalViewRotation:(CAPTURE_ROTATE)rotate;

/// \brief 获取 SDK 版本1
/// \note 已废弃，请使用 +version
- (NSString *)version;

/// \brief 获取 SDK 版本2
/// \note 已废弃，请使用 +version2
- (NSString *)version2;

/// \brief 开关硬件编码
/// \param bRequire 开关
/// \note 已废弃，请使用 +requireHardwareEncoder
- (bool)requireHardwareEncoder:(bool)bRequire;

/// \brief 开关硬件解码
/// \param bRequire 开关
/// \note 已废弃，请使用 +requireHardwareDecoder
- (bool)requireHardwareDecoder:(bool)bRequire;

/// \brief 开关硬件编解码
/// \param bRequire 开关
/// \note 请用 +requireHardwareEncoder/+requireHardwareDecoder
- (bool)requireHardwareAccelerated:(bool)bRequire;

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
