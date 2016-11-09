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

@protocol ZegoVideoCaptureFactory;

/// \brief 回调协议
/// \note 所有回调调用都会发生在主线程
@protocol ZegoLiveApiDelegate <NSObject>

@optional

/// \brief 获取流信息结果
/// \param err 0 成功，进一步等待流信息更新，否则出错
- (void)onLoginChannel:(NSString *)channel error:(uint32)err;

/// \brief 发布直播成功
/// \param streamID 发布流ID
/// \param channel 所在 channel
/// \param info 主播流信息
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
/// \param quality 0 ~ 3 分别对应优良中差
/// \param streamID 发布流ID
/// \param fps 帧率(frame rate)
/// \param kbs 码率(bit rate) kb/s
- (void)onPublishQualityUpdate:(int)quality stream:(NSString *)streamID videoFPS:(double)fps videoBitrate:(double)kbs;

/// \brief 观看质量更新
/// \param quality 0 ~ 3 分别对应优良中差
/// \param streamID 观看流ID
/// \param fps 帧率(frame rate)
/// \param kbs 码率(bit rate) kb/s
- (void)onPlayQualityUpdate:(int)quality stream:(NSString *)streamID videoFPS:(double)fps videoBitrate:(double)kbs;

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
/// \param pChannelCount 混音数据声道数
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

/// \brief 初始化SDK
/// \param appID Zego派发的数字ID，各个开发者的唯一标识
/// \param appSignature Zego派发的签名,用来校验对应appID的合法性
- (instancetype)initWithAppID:(uint32)appID appSignature:(NSData*)appSignature;

/// \brief 登录频道
/// \param channel 频道 ID
/// \param user 用户
/// \return true 成功，等待异步回调，否则失败
- (bool)loginChannel:(NSString *)channel user:(ZegoUser *)user;

/// \brief 退出当前频道
- (bool)logoutChannel;

/// \brief 观看直播流
/// \param streamID 要观看的流 ID
/// \param index 视频播放的 view 编号
/// \note 在setRemoteView方法中绑定view与viewIndex (streamID --> viewIndex <-- view)
/// \return true 成功，等待异步回调，否则失败
- (bool)startPlayStream:(NSString *)streamID viewIndex:(RemoteViewIndex)index;

/// \brief 停止观看直播
/// \param streamID 要停止的流 ID
/// \return true 成功，等待异步回调，否则失败
- (bool)stopPlayStream:(NSString *)streamID;

/// \brief 设置用来观看直播的View
/// \param index View的序号
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

/// \brief 手机内置扬声器开关
/// \param bOn true打开，false关闭
/// \return true：成功；false:失败
- (bool)setBuiltInSpeakerOn:(bool)bOn;

/// \brief （声音输出）静音开关
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)enableSpeaker:(bool) bEnable;

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

/// \brief 启动本地预览
/// \return true:成功；false:失败
- (bool)startPreview;

/// \brief 结束本地预览
/// \return true:成功；false:失败
- (bool)stopPreview;

/// \brief 作为主播开始直播
/// \brief title 直播标题
/// \param streamID 流 ID
/// \return true 成功，等待异步结果回调，否则失败
- (bool)startPublishingWithTitle:(NSString *)title streamID:(NSString *)streamID;

/// \brief 停止主播
/// \return true 成功，否则失败
- (bool)stopPublishing;

/// \brief 设置视频配置
/// \param config 配置参数
/// \return 0表示成功，非0 分别用一位来表示对应的值设置失败，可以与上SetConfigReturnType的各个值来获取设置失败的原因
- (int)setAVConfig:(ZegoAVConfig*)config;

/// \brief 设置手机姿势，用于校正主播输出视频朝向
/// \param orientation 手机姿势
- (int)setAppOrientation:(UIInterfaceOrientation)orientation;

/// \brief 设置是否使用前置摄像头
/// \param bFront 使用前置摄像头
/// \return true:调用成功；false:调用失败
- (bool)setFrontCam:(bool)bFront;

/// \brief 开启关闭麦克风
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableMic:(bool)bEnable;

/// \brief 开启关闭视频采集
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableCamera:(bool)bEnable;

/// \brief 开关手电筒
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)enableTorch:(bool) bEnable;

/// \brief 主播方开启美颜功能
/// \param feature 美颜特性
/// \return true: 成功；false: 失败
- (bool)enableBeautifying:(ZegoBeautifyFeature)feature;

/// \brief 配置滤镜
/// \param filter 滤镜种类
- (bool)setFilter:(ZegoFilter)filter;

/// \brief 截取观看直播 view 图像
/// \param idx 视频通道
/// \note 通过回调 onTakeRemoteViewSnapshot: 返回结果
- (bool)takeRemoteViewSnapshot:(RemoteViewIndex)idx;

/// \brief 截取本地预览视频 view 图像
/// \note 通过回调 onTakeLocalViewSnapshot: 返回结果
- (bool)takeLocalViewSnapshot;

/// \brief 开关硬件编解码
/// \param bRequire 开关
/// \note ！！！打开硬编硬解开关需后台可控，避免碰到版本升级或者硬件升级时出现硬编硬解失败的问题
- (bool)requireHardwareAccelerated:(bool)bRequire;

/// \brief 主动出发日志上报
- (void)uploadLog;

@end
