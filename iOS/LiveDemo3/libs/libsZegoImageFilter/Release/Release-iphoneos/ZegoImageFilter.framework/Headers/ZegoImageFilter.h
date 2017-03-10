#import <objc/NSObject.h>
#import <Foundation/Foundation.h>
#import <CoreVideo/CVPixelBuffer.h>

#pragma once

typedef enum : NSUInteger {
    ZEGO_FILTER_CUSTOM_NONE        = 0,    ///< 不使用滤镜
    ZEGO_FILTER_CUSTOM_LOMO        = 1,    ///< 简洁
    ZEGO_FILTER_CUSTOM_BLACKWHITE  = 2,    ///< 黑白
    ZEGO_FILTER_CUSTOM_OLDSTYLE    = 3,    ///< 老化
    ZEGO_FILTER_CUSTOM_GOTHIC      = 4,    ///< 哥特
    ZEGO_FILTER_CUSTOM_SHARPCOLOR  = 5,    ///< 锐色
    ZEGO_FILTER_CUSTOM_FADE        = 6,    ///< 淡雅
    ZEGO_FILTER_CUSTOM_WINE        = 7,    ///< 酒红
    ZEGO_FILTER_CUSTOM_LIME        = 8,    ///< 青柠
    ZEGO_FILTER_CUSTOM_ROMANTIC    = 9,    ///< 浪漫
    ZEGO_FILTER_CUSTOM_HALO        = 10,   ///< 光晕
    ZEGO_FILTER_CUSTOM_BLUE        = 11,   ///< 蓝调
    ZEGO_FILTER_CUSTOM_ILLUSION    = 12,   ///< 梦幻
    ZEGO_FILTER_CUSTOM_DARK        = 13    ///< 夜色
} ZegoFilterCustomType;

/// \brief 美白特性，
typedef enum : NSUInteger {
    ZEGO_FILTER_BEAUTIFY_NONE          = 0,        ///< 无美颜
    ZEGO_FILTER_BEAUTIFY_POLISH        = 1,        ///< 磨皮
    ZEGO_FILTER_BEAUTIFY_WHITEN        = 1 << 1,   ///< 全屏美白
    ZEGO_FILTER_BEAUTIFY_SKINWHITEN    = 1 << 2,   ///< 皮肤美白，一般与磨皮结合使用 ZEGO_FILTER_BEAUTIFY_POLISH | ZEGO_FILTER_BEAUTIFY_SKINWHITEN
    ZEGO_FILTER_BEAUTIFY_SHARPPEN      = 1 << 3
} ZegoFilterBeautifyFeature;

@interface ZegoImageFilter : NSObject

/// \brief 初始化滤镜
/// \return 0: 成功；-1: 失败
/// \note 确保create destroy render在同一个线程调用
-(int) create;
/// \brief 销毁滤镜
/// \return 0: 成功；-1: 失败
/// \note 确保create destroy render在同一个线程调用
-(int) destroy;
/// \brief 对图像进行处理
/// \return 0: 成功；-1: 失败
/// \note 确保create destroy render在同一个线程调用
-(CVPixelBufferRef) render:(CVPixelBufferRef)pixelBuffer;
/// \brief 主播方开启美颜功能
/// \return 0: 成功；-1: 失败
-(int) enableBeautifying:(ZegoFilterBeautifyFeature)flag;
/// \brief 配置滤镜
/// \return 0: 成功；-1: 失败
-(int) setCustomizedFilter:(ZegoFilterCustomType)filterIndex;
/// \brief 设置美颜磨皮的采样步长
/// \param 采样半径 取值范围[1,16]
/// \return 0: 成功；-1: 失败
-(int) setPolishStep:(float)step;
/// \brief 设置美颜采样颜色阈值
/// \param factor 取值范围[0,16]
/// \return 0: 成功；-1: 失败
-(int) setPolishFactor:(float)factor;
/// \brief 设置美颜美白的亮度修正参数
/// \param factor 取值范围[0,1]， 参数越大亮度越暗
-(int) setWhitenFactor:(float)factor;
/// \brief 设置锐化的修正参数
/// \param factor 取值范围[0,2]， 参数越大锐化越强
-(int) setSharppenFactor:(float)factor;
/// \brief 设置美颜粉白的修正参数
/// \param factor 取值范围[0,1]， 参数越大越粉白
-(int) setSkinWhitenFactor:(float)factor;

@end

// * end of file //
