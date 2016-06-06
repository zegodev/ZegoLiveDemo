//
//  ZegoAVApiDefines.h
//  zegoavkit
//
//  Copyright © 2016 Zego. All rights reserved.
//

#ifndef ZegoAVApiDefines_h
#define ZegoAVApiDefines_h


typedef unsigned int	uint32;

typedef enum{
    FLAG_RESOLUTION = 0x1,
    FLAG_FPS = 0x2,
    FLAG_BITRATE = 0x4
}SetConfigReturnType;

typedef enum{
    ZegoVideoViewModeScaleAspectFit     = 0,    //等比缩放，可能有黑边
    ZegoVideoViewModeScaleAspectFill    = 1,    //等比缩放填充整View，可能有部分被裁减
    ZegoVideoViewModeScaleToFill        = 2,    //填充整个View
}ZegoVideoViewMode;

typedef enum{
    CustomDataType_data = 1,    //NSData存的byte数组
    CustomDataType_file = 2
}CustomDataType;

typedef enum
{
    CAPTURE_ROTATE_0    = 0,
    CAPTURE_ROTATE_90   = 90,
    CAPTURE_ROTATE_180  = 180,
    CAPTURE_ROTATE_270  = 270
}CAPTURE_ROTATE;

typedef enum
{
    RemoteViewIndex_First = 0,
    RemoteViewIndex_Second = 1,
    RemoteViewIndex_Third = 2
}RemoteViewIndex;


typedef enum : NSUInteger {
    ZEGO_FILTER_NONE        = 0,    ///< 不使用滤镜
    ZEGO_FILTER_LOMO        = 1,    ///< 简洁
    ZEGO_FILTER_BLACKWHITE  = 2,    ///< 黑白
    ZEGO_FILTER_OLDSTYLE    = 3,    ///< 老化
    ZEGO_FILTER_GOTHIC      = 4,    ///< 哥特
    ZEGO_FILTER_SHARPCOLOR  = 5,    ///< 锐色
    ZEGO_FILTER_FADE        = 6,    ///< 淡雅
    ZEGO_FILTER_WINE        = 7,    ///< 酒红
    ZEGO_FILTER_LIME        = 8,    ///< 青柠
    ZEGO_FILTER_ROMANTIC    = 9,    ///< 浪漫
    ZEGO_FILTER_HALO        = 10,   ///< 光晕
    ZEGO_FILTER_BLUE        = 11,   ///< 蓝调
    ZEGO_FILTER_ILLUSION    = 12,   ///< 梦幻
    ZEGO_FILTER_DARK        = 13    ///< 夜色
} ZegoFilter;



/// \brief 美白特性，
typedef enum : NSUInteger {
    ZEGO_BEAUTIFY_NONE          = 0,        ///< 无美颜
    ZEGO_BEAUTIFY_POLISH        = 1,        ///< 磨皮
    ZEGO_BEAUTIFY_WHITEN        = 1 << 1,   ///< 全屏美白
    ZEGO_BEAUTIFY_SKINWHITEN    = 1 << 2    ///< 皮肤美白，一般与磨皮结合使用 ZEGO_BEAUTIFY_POLISH | ZEGO_BEAUTIFY_SKINWHITEN
} ZegoBeautifyFeature;

#endif /* ZegoAVApiDefines_h */
