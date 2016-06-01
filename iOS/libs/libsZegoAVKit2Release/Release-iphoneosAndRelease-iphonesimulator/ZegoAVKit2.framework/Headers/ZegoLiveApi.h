//
//  ZegoLiveApi.h
//  zegoavkit
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "ZegoUser.h"
#import "ZegoAVConfig.h"
#import "ZegoAVDefines.h"

#ifdef __cplusplus
#define ZEGO_EXTERN     extern "C"
#else
#define ZEGO_EXTERN     extern
#endif

ZEGO_EXTERN NSString *const kZegoStreamIDKey;           ///< 流ID，值为 NSString
ZEGO_EXTERN NSString *const kZegoPublishIDKey;
ZEGO_EXTERN NSString *const kZegoPublishNameKey;
ZEGO_EXTERN NSString *const kZegoStreamTitleKey;

ZEGO_EXTERN NSString *const kZegoOnlineNumsKey;         ///< 在线人数
ZEGO_EXTERN NSString *const kZegoOnlineCountKey;        ///< 历史观看人数

ZEGO_EXTERN NSString *const kZegoPublishStreamIDKey;    ///< 主播流ID，值为 NSString
ZEGO_EXTERN NSString *const kZegoPublishStreamURLKey;   ///< 当前直播流观看 url，值为 NSString


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
- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel playUrl:(NSString *)playUrl;

/// \brief 发布直播失败
/// \param err 1 正常结束, 非 1 异常结束
- (void)onPublishStop:(uint32)err stream:(NSString *)streamID channel:(NSString *)channel;

/// \brief 观看直播成功
/// \param streamID 直播流的唯一标识
- (void)onPlaySucc:(NSString *)streamID channel:(NSString *)channel;

/// \brief 观看直播失败
/// \param err 1 正常结束, 非 1 异常结束
/// \param streamID 直播流的唯一标识
- (void)onPlayStop:(uint32)err streamID:(NSString *)streamID channel:(NSString *)channel;

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

/// \brief 设置用来观看直播的View
/// \param index View的序号，目前支持一个聊天室两个主播
/// \param view 展示视频的View
/// \return true:调用成功；false:调用失败
- (bool)setRemoteView:(RemoteViewIndex)index view:(UIView*)view;

/// \brief 设置观看直播的View的模式
/// \param index View的序号
/// \param mode 模式，详见ZegoVideoViewMode
/// \return true:调用成功；false:调用失败
- (bool)setRemoteViewMode:(RemoteViewIndex)index mode:(ZegoVideoViewMode)mode;

/// \brief 设置本地预览视频的View
/// \param view 展示视频的View
/// \return true:调用成功；false:调用失败
- (bool)setLocalView:(UIView*)view;

/// \brief 设置本地预览视频View的模式
/// \param mode 模式，详见ZegoVideoViewMode
/// \return true:调用成功；false:调用失败
- (bool)setLocalViewMode:(ZegoVideoViewMode)mode;

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

/// \brief 开启关闭麦克风
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableMic:(bool)bEnable;

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
- (bool)requireHardwareAccelerated:(bool)bRequire;

/// \brief 登录频道
/// \param channel 频道 ID
/// \param user 用户
/// \return YES 成功，等待异步回调，否则失败
- (bool)loginChannel:(NSString *)channel user:(ZegoUser *)user;

/// \brief 作为主播开始直播
/// \param user 发布用户
/// \param streamID 流 ID
/// \param liveChannel 频道 ID
/// \return YES 成功，等待异步结果回调，否则失败
- (bool)startPublishingWithTitle:(NSString *)title streamID:(NSString *)streamID;

/// \brief 停止主播
/// \return YES 成功，否则失败
- (bool)stopPublishing;

/// \brief 观看直播流
/// \param streamID 要观看的流 ID
/// \param index 视频播放的 view 编号
/// \return YES 成功，等待异步回调，否则失败
- (bool)startPlayStream:(NSString *)streamID viewIndex:(RemoteViewIndex)index;

/// \brief 停止观看直播
/// \param streamID 要停止的流 ID
/// \return YES 成功，等待异步回调，否则失败
- (bool)stopPlayStream:(NSString *)streamID;

/// \brief 退出当前频道
- (bool)logoutChannel;

/// \brief 主动出发日志上报
- (void)uploadLog;

/// \brief 获取 SDK 版本
- (NSString *)version;

/// \brief 是否启用测试环境
+ (void)setUseTestEnv:(bool)useTestEnv;

/// \brief 设置外部采集模块
/// \param factory 工厂对象，继承自 ZEGO::AV::VideoCaptureFactory，
/// \note 必须在 InitSDK 前调用，并且不能置空
///    Example:
//+ (void)setVideoCaptureFactory:(void *)factory;

@end



