//
//  ZegoLiveApi.h
//  zegoavkit
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>

#if TARGET_OS_IPHONE
#import <UIKit/UIKit.h>
#elif TARGET_OS_MAC
#import <AppKit/AppKit.h>
#endif

#import "ZegoUser.h"
#import "ZegoAVConfig.h"
#import "ZegoAVDefines.h"

#ifdef __cplusplus
#define ZEGO_EXTERN     extern "C"
#else
#define ZEGO_EXTERN     extern
#endif

ZEGO_EXTERN NSString *const kZegoStreamIDKey;           ///< 流ID，值为 NSString
ZEGO_EXTERN NSString *const kZegoMixStreamIDKey;        ///< 混流ID，值为 NSString
ZEGO_EXTERN NSString *const kZegoRtmpUrlListKey;        ///< rtmp 播放 url 列表，值为 NSArray<NSString *>
ZEGO_EXTERN NSString *const kZegoHlsUrlListKey;         ///< hls 播放 url 列表，值为 NSArray<NSString *>
ZEGO_EXTERN NSString *const kZegoFlvUrlListKey;         ///< flv 播放 url 列表，值为 NSArray<NSString *>


@protocol ZegoVideoCaptureFactory;

/// \brief 回调协议
/// \note 所有回调调用都会发生在主线程
@protocol ZegoLiveApiDelegate <NSObject>

/// \brief 获取流信息结果
/// \param err 0 成功，进一步等待流信息更新，否则出错
- (void)onLoginChannel:(NSString *)channel error:(uint32)err;

/// \brief 发布直播成功
/// \param streamID 发布流ID
/// \param channel 所在 channel
/// \param playUrl 主播流的播放 url
- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel streamInfo:(NSDictionary *)info;

/// \brief 发布直播失败
/// \param err 1 正常结束, 非 1 异常结束

///错误码	说明
///AVStateEnd = 1	直播正常停止
///TempBroken = 2	直播临时中断
///FatalError = 3	直播遇到严重的问题
///CreateStreamError = 4	创建直播流失败
///FetchStreamError = 5	获取流信息失败
///NoStreamError = 6	无流信息
///MediaServerNetWorkError = 7	媒体服务器连接失败
///DNSResolveError = 8	DNS 解释失败
- (void)onPublishStop:(uint32)err stream:(NSString *)streamID channel:(NSString *)channel;

/// \brief 观看直播成功
/// \param streamID 直播流的唯一标识
///
- (void)onPlaySucc:(NSString *)streamID channel:(NSString *)channel;

/// \brief 观看直播失败
/// \param err 1 正常结束, 非 1 异常结束
/// \param streamID 直播流的唯一标识

///错误码	说明
///AVStateEnd = 1	直播正常停止
///TempBroken = 2	直播临时中断
///FatalError = 3	直播遇到严重的问题
///CreateStreamError = 4	创建直播流失败
///FetchStreamError = 5     获取流信息失败
///NoStreamError = 6	无流信息
///MediaServerNetWorkError = 7	媒体服务器连接失败
///DNSResolveError = 8	DNS 解释失败
- (void)onPlayStop:(uint32)err streamID:(NSString *)streamID channel:(NSString *)channel;

/// \brief 发布质量更新
/// \param quality: 0 ~ 3 分别对应优良中差
/// \param streamID 发布流ID
- (void)onPublishQualityUpdate:(int)quality stream:(NSString *)streamID;

/// \brief 观看质量更新
/// \param quality: 0 ~ 3 分别对应优良中差
/// \param streamID 观看流ID
- (void)onPlayQualityUpdate:(int)quality stream:(NSString *)streamID;

/// \brief 视频的宽度和高度变化通知,startPlay后，如果视频宽度或者高度发生变化(首次的值也会)，则收到该通知
/// \param streamID 流的唯一标识
/// \param width 宽
/// \param height 高
- (void)onVideoSizeChanged:(NSString *)streamID width:(uint32)width height:(uint32)height;

/// \brief 采集视频的宽度和高度变化通知
/// \param width 宽
/// \param height 高
- (void)onCaptureVideoSizeChangedToWidth:(uint32)width height:(uint32)height;

/// \brief 截取观看直播 view 图像结果
/// \param img 图像数据
- (void)onTakeRemoteViewSnapshot:(CGImageRef)img view:(RemoteViewIndex)index;

/// \brief 截取本地预览视频 view 图像结果
/// \param img 图像数据
- (void)onTakeLocalViewSnapshot:(CGImageRef)img;

/// \brief 混音数据输入回调
/// \param pData 数据缓存起始地址
/// \param pDataLen [in] 缓冲区长度；[out]实际填充长度，必须为 0 或是缓冲区长度，代表有无混音数据
/// \param pSampleRate 混音数据采样率
/// \param pNumChannels 混音数据声道数
/// \note 混音数据 bit depth 必须为 16
- (void)onAuxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount;

/// \breif 音视频引擎停止
- (void)onAVEngineStop;

/// \brief 混流配置更新结果回调
/// \param errorCode 错误码，0 表示没有错误
/// \param mixStreamID 混流ID
/// \param info 混流播放信息
- (void)onMixStreamConfigUpdate:(int)errorCode mixStream:(NSString *)mixStreamID streamInfo:(NSDictionary *)info;

@end


@interface ZegoLiveApi : NSObject

@property (weak) id<ZegoLiveApiDelegate> delegate;

/// 设置日志记录等级
/// \param logLevel 4 - debug, 3 - generic
+ (void)setLogLevel:(int)logLevel;

/// \brief 设置业务类型
/// \param type 业务类型，默认为 0
/// \note 确保在创建接口对象前调用
+ (void)setBusinessType:(int)type;

/// \brief 初始化SDK
/// \param appID Zego派发的数字ID，各个开发者的唯一标识
/// \param appSignature Zego派发的签名,用来校验对应appID的合法性
- (instancetype)initWithAppID:(uint32)appID appSignature:(NSData*)appSignature;

/// \brief 设置用来观看直播的View
/// \param index View的序号，目前支持一个聊天室两个主播
/// \param view 展示视频的View
/// \return true:调用成功；false:调用失败

#if TARGET_OS_IPHONE
- (bool)setRemoteView:(RemoteViewIndex)index view:(UIView*)view;
#elif TARGET_OS_MAC
- (bool)setRemoteView:(RemoteViewIndex)index view:(NSView *)view;
#endif

/// \brief 设置观看直播的View的模式
/// \param index View的序号
/// \param mode 模式，详见ZegoVideoViewMode
/// \return true:调用成功；false:调用失败
- (bool)setRemoteViewMode:(RemoteViewIndex)index mode:(ZegoVideoViewMode)mode;

/// \brief 设置本地预览视频的View
/// \param view 展示视频的View
/// \return true:调用成功；false:调用失败
#if TARGET_OS_IPHONE
- (bool)setLocalView:(UIView*)view;
#elif TARGET_OS_MAC
- (bool)setLocalView:(NSView *)view;
#endif

/// \brief 设置本地预览视频View的模式
/// \param mode 模式，详见ZegoVideoViewMode
/// \return true:调用成功；false:调用失败
- (bool)setLocalViewMode:(ZegoVideoViewMode)mode;

/// \brief 设置手机姿势
/// \param rotate 逆时针旋转角度
/// \return true:调用成功；false:调用失败
- (bool)setDisplayRotation:(CAPTURE_ROTATE)rotate;

/// \brief 设置视频配置
/// \param config 配置参数
/// \return 0表示成功，非0 分别用一位来表示对应的值设置失败，可以与上SetConfigReturnType的各个值来获取设置失败的原因
- (int)setAVConfig:(ZegoAVConfig*)config;

/// \brief 启动本地预览
/// \return true:成功；false:失败
- (bool)startPreview;

/// \brief 结束本地预览
/// \return true:成功；false:失败
- (bool)stopPreview;

/// \brief 设置是否使用前置摄像头
/// \param bFront 使用前置摄像头
/// \return true:调用成功；false:调用失败
- (bool)setFrontCam:(bool)bFront;

/// \brief 设置采集时摄像头方向,在startPublish前设置有效，startPublish后调用则返回false
/// \param rotate 方向
/// \return true:调用成功；false:调用失败
- (bool)setCaptureRotation:(CAPTURE_ROTATE)rotate;

/// \brief 开启关闭音频采集噪声抑制
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableNoiseSuppress:(bool)bEnable;

/// \brief 开启采集监听
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableLoopback:(bool)bEnable;

/// \biref 设置播放音量
/// \param volume 音量大小 0 ~ 100
- (void)setPlayVolume:(int)volume;

/// \biref 设置采集监听音量
/// \param volume 音量大小 0 ~ 100
- (void)setLoopbackVolume:(int)volume;

/// \brief 开启关闭麦克风
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableMic:(bool)bEnable;

/// \brief 开启关闭视频采集
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableCamera:(bool)bEnable;

/// \brief 截取观看直播 view 图像
/// \param idx 视频通道
/// \note 通过回调 onTakeRemoteViewSnapshot: 返回结果
- (bool)takeRemoteViewSnapshot:(RemoteViewIndex)idx;

/// \brief 截取本地预览视频 view 图像
/// \note 通过回调 onTakeLocalViewSnapshot: 返回结果
- (bool)takeLocalViewSnapshot;

/// \brief 开关手电筒
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)enableTorch:(bool) bEnable;

/// \brief 主播方开启美颜功能
/// \param bEnable true 打开，false 关闭
/// \return true: 成功；false: 失败
- (bool)enableBeautifying:(ZegoBeautifyFeature)feature;

/// \brief 配置滤镜
/// \param filter 滤镜种类
- (bool)setFilter:(ZegoFilter)filter;

/// \brief （声音输出）静音开关
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)enableSpeaker:(bool) bEnable;

/// \brief 手机内置扬声器开关
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)setBuiltInSpeakerOn:(bool)bOn;

/// \brief 开关硬件编解码
/// \param bRequire 开关
/// \note ！！！打开硬编硬解开关需后台可控，避免碰到版本升级或者硬件升级时出现硬编硬解失败的问题
- (bool)requireHardwareAccelerated:(bool)bRequire;

/// \brief 登录频道
/// \param channel 频道 ID
/// \param user 用户
/// \return true 成功，等待异步回调，否则失败
- (bool)loginChannel:(NSString *)channel user:(ZegoUser *)user;

/// \brief 作为主播开始直播
/// \brief 直播标题
/// \param streamID 流 ID
/// \return true 成功，等待异步结果回调，否则失败
- (bool)startPublishingWithTitle:(NSString *)title streamID:(NSString *)streamID;

/// \brief 作为主播开始直播
/// \brief 直播标题
/// \param streamID 流 ID
/// \param mixStreamID 混流ID
/// \param flag 推流标记(按位取值)
/// \return true 成功，等待异步结果回调，否则失败
- (bool)startPublishingWithTitle:(NSString *)title streamID:(NSString *)streamID mixStreamID:(NSString *)mixStreamID flag:(int)flag;

/// \brief 更新混流配置
/// \param lstMixStreamInfo 混流配置列表，按列表顺序叠加涂层
/// \return true 成功，等待异步结果回调，否则失败
- (bool)updateMixStreamConfig:(NSArray<ZegoMixStreamInfo*> *)lstMixStreamInfo;

/// \brief 停止主播
/// \return true 成功，否则失败
- (bool)stopPublishing;

/// \brief 观看直播流
/// \param streamID 要观看的流 ID
/// \param index 视频播放的 view 编号
/// \return true 成功，等待异步回调，否则失败
- (bool)startPlayStream:(NSString *)streamID viewIndex:(RemoteViewIndex)index;

/// \brief 停止观看直播
/// \param streamID 要停止的流 ID
/// \return true 成功，等待异步回调，否则失败
- (bool)stopPlayStream:(NSString *)streamID;

/// \brief 退出当前频道
- (bool)logoutChannel;

/// \brief 主动出发日志上报
- (void)uploadLog;

/// \brief 获取 SDK 版本
- (NSString *)version;
- (NSString *)version2;

/// \brief 混音开关
/// \param bEable true 启用混音输入；false 关闭混音输入
- (bool)enableAux:(BOOL)enable;

/// \brief 是否启用测试环境
+ (void)setUseTestEnv:(bool)useTestEnv;

/// \brief 设置外部采集模块
/// \param factory 工厂对象
/// \note 必须在 InitSDK 前调用，并且不能置空
+ (void)setVideoCaptureFactory:(id<ZegoVideoCaptureFactory>)factory;

/// \brief 设置音频前处理函数
/// \param prep 前处理函数指针
+ (void)setAudioPrep:(void(*)(const short* inData, int inSamples, int sampleRate, short *outData))prep;

@end
