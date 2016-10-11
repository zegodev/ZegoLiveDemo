//
//  ZegoLiveApi-advanced.h
//  zegoavkit
//
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoLiveApi_advanced_h
#define ZegoLiveApi_advanced_h

#import "ZegoLiveApi.h"

@interface ZegoLiveApi (Advanced)

/// \brief 设置业务类型
/// \param type 业务类型，默认为 0
/// \note 确保在创建接口对象前调用
+ (void)setBusinessType:(int)type;

/// \brief 设置外部采集模块
/// \param factory 工厂对象
/// \note 必须在 InitSDK 前调用，并且不能置空
+ (void)setVideoCaptureFactory:(id<ZegoVideoCaptureFactory>)factory;

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
- (NSString *)version;

/// \brief 获取 SDK 版本2
- (NSString *)version2;

/// \brief 混音开关
/// \param bEable true 启用混音输入；false 关闭混音输入
- (bool)enableAux:(BOOL)enable;

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

@end

#endif /* ZegoLiveApi_advanced_h */
