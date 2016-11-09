//
//  ZegoLiveApi-advanced.h
//  zegoavkit
//
//  Created by Randy Qiu on 16/9/7.
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoLiveApi_advanced_h
#define ZegoLiveApi_advanced_h

#import "ZegoLiveApi.h"

@protocol ZegoLiveApiRenderDelegate <NSObject>

/// \brief 从外部获取渲染需要的PixelBuffer地址
/// \param width 视频宽度
/// \param height 视频高度
/// \param stride 图像stride padding
- (CVPixelBufferRef)onCreateInputBufferWithWidth:(int)width height:(int)height stride:(int)stride;

/// \breif 视频数据拷贝完成
/// \param pixelBuffer 拷贝完的视频数据
/// \param index 视频播放对应的index
- (void)onPixelBufferCopyed:(CVPixelBufferRef)pixelBuffer index:(RemoteViewIndex)index;

@end


@protocol ZegoLiveApiAudioRecordDelegate <NSObject>

- (void)onAudioRecord:(NSData *)audioData sampleRate:(int)sampleRate numOfChannels:(int)numOfChannels bitDepth:(int)bitDepth;

@end


typedef enum : NSUInteger {
    Play_BeginRetry = 1,
    Play_RetrySuccess = 2,
    
    Publish_BeginRetry = 3,
    Publish_RetrySuccess = 4,
} ZegoLiveEvent;

@protocol ZegoLiveEventDelegate <NSObject>

- (void)zego_onLiveEvent:(ZegoLiveEvent)event info:(NSDictionary<NSString*, NSString*>*)info;

@end


@interface ZegoLiveApi (Advanced)

/// \brief 设置业务类型
/// \param type 业务类型，默认为 0
/// \note 确保在创建接口对象前调用
+ (void)setBusinessType:(int)type;

/// \brief 设置外部采集模块
/// \param factory 工厂对象
/// \note 必须在 InitSDK 前调用，并且不能置空
+ (void)setVideoCaptureFactory:(id<ZegoVideoCaptureFactory>)factory;

/// \brief 设置外部渲染
/// \param externalRender 是否外部渲染
/// \note 必须在InitSDK前调用
+ (void)setExtenralRender:(BOOL)externalRender;

/// \brief 设置音频前处理函数
/// \param prep 前处理函数指针
+ (void)setAudioPrep:(void(*)(const short* inData, int inSamples, int sampleRate, short *outData))prep;

/// \brief 作为主播开始直播
/// \brief 直播标题
/// \param streamID 流 ID
/// \param mixStreamID 混流ID
/// \param flag 推流标记(按位取值)
/// \return true 成功，等待异步结果回调，否则失败
- (bool)startPublishingWithTitle:(NSString *)title streamID:(NSString *)streamID mixStreamID:(NSString *)mixStreamID mixVideoSize:(CGSize)videoSize flag:(int)flag;

/// \brief 更新混流配置
/// \param lstMixStreamInfo 混流配置列表，按列表顺序叠加涂层
/// \note lstMixStreamInfo 设置为nil时停止混流
/// \return true 成功，等待异步结果回调，否则失败
- (bool)updateMixStreamConfig:(NSArray<ZegoMixStreamInfo*> *)lstMixStreamInfo;

/// \brief 设置播放渲染朝向
/// \param rotate 逆时针旋转角度
/// \param index 播放通道
/// \return true 成功，false 失败
- (bool)setRemoteViewRotation:(CAPTURE_ROTATE)rotate viewIndex:(RemoteViewIndex)index;

/// \brief 设置预览渲染朝向
/// \param rotate 逆时针旋转角度
/// \return true 成功，false 失败
- (bool)setLocalViewRotation:(CAPTURE_ROTATE)rotate;

/// \brief 设置采集时摄像头方向,在startPublish前设置有效，startPublish后调用则返回false
/// \param rotate 顺时针方向
/// \return true:调用成功；false:调用失败
- (bool)setCaptureRotation:(CAPTURE_ROTATE)rotate;

/// \brief 开启采集监听
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableLoopback:(bool)bEnable;

/// \brief 开启关闭音频采集噪声抑制
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableNoiseSuppress:(bool)bEnable;

/// \biref 设置播放音量
/// \param volume 音量大小 0 ~ 100
- (void)setPlayVolume:(int)volume;

/// \biref 设置采集监听音量
/// \param volume 音量大小 0 ~ 100
- (void)setLoopbackVolume:(int)volume;

/// \brief 获取 SDK 版本1
+ (NSString *)version;

/// \brief 获取 SDK 版本2
+ (NSString *)version2;

/// \brief 混音开关
/// \param bEable true 启用混音输入；false 关闭混音输入
- (bool)enableAux:(BOOL)enable;

/// \brief 音频录制回调开关
/// \param enable true 启用音频录制回调；false 关闭音频录制回调
/// \return true 成功，false 失败
- (bool)enableAudioRecord:(BOOL)enable;

/// \brief 设置美颜磨皮的采样步长
/// \param 采样半径 取值范围[1,16]
- (bool)setPolishStep:(float)step;

/// \brief 设置美颜采样颜色阈值
/// \brief factor 取值范围[0,16]
- (bool)setPolishFactor:(float)factor;

/// \brief 设置美颜美白的亮度修正参数
/// \param factor 取值范围[0,1]， 参数越大亮度越暗
- (bool)setWhitenFactor:(float)factor;

/// \brief 混音输入播放静音开关
/// \param bMute true: aux 输入播放静音；false: 不静音
- (bool)muteAux:(bool)bMute;

/// \brief 是否启用测试环境
+ (void)setUseTestEnv:(bool)useTestEnv;

/// \brief 调试信息开关
/// \desc 建议在调试阶段打开此开关，方便调试。默认关闭
/// \return true：成功；false:失败
+ (void)setVerbose:(bool)bOnVerbose;

/// \brief 外部渲染回调
/// \param renderDelegate 外部渲染回调协议
- (void)setRenderDelegate:(id<ZegoLiveApiRenderDelegate>)renderDelegate;

/// \brief 音频录制回调
/// \param renderDelegate 音频录制回调协议
- (void)setAudioRecordDelegate:(id<ZegoLiveApiAudioRecordDelegate>)audioRecordDelegate;

/// \brief 直播事件通知回调
/// \param liveEventDelegate 直播事件通知回调协议
- (void)setLiveEventDelegate:(id<ZegoLiveEventDelegate>)liveEventDelegate;

@end

#endif /* ZegoLiveApi_advanced_h */
