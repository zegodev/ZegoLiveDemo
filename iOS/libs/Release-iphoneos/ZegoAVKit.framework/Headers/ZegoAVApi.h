#import <Foundation/Foundation.h>

#if TARGET_OS_IPHONE
#import <UIKit/UIKit.h>
#elif TARGET_OS_MAC
#import <AppKit/AppKit.h>
#endif

#import "ZegoUser.h"
#import "ZegoShowListDelegate.h"
#import "ZegoChatDelegate.h"
#import "ZegoVideoDelegate.h"
#import "ZegoAVConfig.h"
#if TARGET_OS_IPHONE
#import <ZegoAVKit/ZegoAVDefines.h>
#elif TARGET_OS_MAC
#import <ZegoAVKitosx/ZegoAVDefines.h>
#endif

@interface ZegoAVApi : NSObject

+ (void)setLogLevel:(int)logLevel;

/// \brief 初始化SDK
/// \param appID Zego派发的数字ID，各个开发者的唯一标识
/// \param appSignature Zego派发的签名,用来校验对应appID的合法性
/// \return true:调用成功；false:调用失败
- (bool) initSDK:(uint32)appID appSignature:(NSData*)appSignature;

/// \brief 设置用来观看直播的View
/// \param index View的序号，目前支持一个聊天室两个主播
/// \param view 展示视频的View
/// \return true:调用成功；false:调用失败
#if TARGET_OS_IPHONE
- (bool) setRemoteView:(RemoteViewIndex)index view:(UIView*)view;
#elif TARGET_OS_MAC
- (bool) setRemoteView:(RemoteViewIndex)index view:(NSView*)view;
#endif

/// \brief 设置观看直播的View的模式
/// \param index View的序号
/// \param mode 模式，详见ZegoVideoViewMode
/// \return true:调用成功；false:调用失败
- (bool) setRemoteViewMode:(RemoteViewIndex)index mode:(ZegoVideoViewMode)mode;

/// \brief 设置本地预览视频的View
/// \param view 展示视频的View
/// \return true:调用成功；false:调用失败
#if TARGET_OS_IPHONE
- (bool) setLocalView:(UIView*)view;
#elif TARGET_OS_MAC
- (bool) setLocalView:(NSView*)view;
#endif


/// \brief 设置本地预览视频View的模式
/// \param mode 模式，详见ZegoVideoViewMode
/// \return true:调用成功；false:调用失败
- (bool) setLocalViewMode:(ZegoVideoViewMode)mode;

/// \brief 设置视频配置
/// \param config 配置参数
/// \return 0表示成功，非0 分别用一位来表示对应的值设置失败，可以与上SetConfigReturnType的各个值来获取设置失败的原因
- (int) setAVConfig:(ZegoAVConfig*)config;

/// \brief 启动本地预览
/// \return true:成功；false:失败
- (bool) startPreview;

/// \brief 结束本地预览
/// \return true:成功；false:失败
- (bool) stopPreview;

/*
/// \brief 设置采集分辨率
/// \param width 宽度
/// \param height 高度
/// \return true:调用成功；false:调用失败
- (bool) setVideoResolution:(int)width height:(int)height;

/// \brief 设置每秒帧数
/// \param fps 每秒帧数
/// \return true:调用成功；false:调用失败
- (bool) setVideoFPS:(int)fps;

/// \brief 设置码率
/// \param bitrate 码率
/// \return true:调用成功；false:调用失败
- (bool) setVideoBitrate:(int)bitrate;
 */

/// \brief 设置是否使用前置摄像头
/// \param bFront 使用前置摄像头
/// \return true:调用成功；false:调用失败
- (bool) setFrontCam:(bool)bFront;

#if TARGET_OS_IPHONE
#elif TARGET_OS_MAC
- (bool) setVideoCaptureDeviceId:(NSString *)deviceId;
#endif



/// \brief 设置采集时摄像头方向,在startPublish前设置有效，startPublish后调用则返回false
/// \param rotate 方向
/// \return true:调用成功；false:调用失败
- (bool) setCaptureRotation:(CAPTURE_ROTATE)rotate;

/// \brief 开启关闭麦克风
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool) enableMic:(bool)bEnable;

/// \brief 截取观看直播 view 图像
/// \note 通过回调 onTakeRemoteViewSnapshot: 返回结果
- (bool)takeRemoteViewSnapshot;

- (bool)takeRemoteViewSnapshot:(RemoteViewIndex)idx;

/// \brief 截取本地预览视频 view 图像
/// \note 通过回调 onTakeLocalViewSnapshot: 返回结果
- (bool)takeLocalViewSnapshot;

/// \brief 反初始化SDK
/// \return true:调用成功；false:调用失败
- (bool) uninitSDK;

/// \brief 获取直播列表
/// \note 在用户的主界面，可以调用这个接口获取目前所有的直播列表
/// \param indexBegin 直播列表的起始位置
/// \param count 获取直播个数
/// \return true:调用成功，等待ZegoShowListDelegate中的onGetShowListResult的通知；false:调用失败
- (bool) getShowList:(uint32)indexBegin count:(uint32)count;

/// \brief 刷新直播列表
/// \note 重新从网络更新直播列表,刷新成功后，再次调用getShowList来获取直播列表
/// \return true:刷新成功；false:刷新失败
- (bool) refreshShowList;

/// \brief 获取回看的视频列表
/// \note 在用户的重播列表界面，可以调用这个接口获取目前所有的回看视频列表
/// \param indexBegin 回播列表的起始位置
/// \param count 获取回播个数
/// \return true:调用成功，等待ZegoShowListDelegate中的onGetReplayListResult的通知；false:调用失败
- (bool) getReplayList:(uint32)indexBegin count:(uint32)count;

/// \brief 刷新回播列表
/// \note 重新从网络更新回播列表,刷新成功后，再次调用getReplayList来获取直播列表
/// \return true:刷新成功；false:刷新失败
- (bool) refreshReplayList;

/// \brief 设置直播列表通知的委托
/// \note 设置了该委托，在拉取直播列表时，通过该代理返回拉取的结果
/// \param delegate 委托
/// \param delegateCallbackQueue 通知的queue
/// \return 无
- (void) setShowListDelegate:(id<ZegoShowListDelegate>)delegate callbackQueue:(dispatch_queue_t)delegateCallbackQueue;

/// \brief 设置聊天室通知的委托
/// \note 设置了该委托，在聊天室相关的通知都通过该委托返回
/// \param delegate 委托
/// \param delegateCallbackQueue 通知的queue
/// \return 无
- (void) setChatDelegate:(id<ZegoChatDelegate>)delegate callbackQueue:(dispatch_queue_t)delegateCallbackQueue;

/// \brief 设置直播通知的委托
/// \note 设置了该委托，发布和观看直播相关的通知都通过该委托返回
/// \param delegate 委托
/// \param delegateCallbackQueue 通知的queue
/// \return 无
- (void) setVideoDelegate:(id<ZegoVideoDelegate>)delegate callbackQueue:(dispatch_queue_t)delegateCallbackQueue;

/// \brief 获取观看直播的人数
/// \return 观看人数
- (uint32)  getPlayerCount;

///不带聊天室的直播接口
/// \brief 发布直播
/// \param user 发布者信息
/// \param title 直播的名称
/// \return true:调用成功，等待ZegoVideoDelegate中的onPublishSucc或onPublishStop通知；false:调用失败
//- (bool) startPublish:(ZegoUser*)user title:(NSString*)title;

/// \brief 停止直播
/// \return true:停止成功；false:停止失败
//- (bool) stopPublish;

/// \brief 观看直播
/// \param user 观看者信息
/// \param zegoToken 观看直播的令牌
/// \param zegoId 直播的Id，直播的唯一标识
/// \return true:调用成功，等待ZegoVideoDelegate中的onPlaySucc或onPlayStop通知；false:调用失败
//- (bool) startPlay:(ZegoUser*)user zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId;

/// \brief 停止观看
/// \return true:停止成功；false:停止失败
//- (bool) stopPlay;

///带聊天室功能的直播接口，都带有ChatRoom结尾的
/// \brief 进入聊天室
/// \note 使用直播同时带聊天室，必须在startPublishInChatRoom或者startPlayInChatRoom之前进入一个聊天室
/// \param user 用户信息，userID不能为空
/// \param zegoToken 房间的令牌，和zegoId组合来用
/// {zegoToken=0 && zegoId==0} 由Zego自动分配一个房间。在onGetInChatRoomResult成功，返回zegoToken和zegoId，用来让其他用户进入同一个房间的凭证
/// {zegoToken=0 && zegoId!=0} 由开发者自己维护的房间列表
/// {zegoToken=!0 && zegoId!=0} 进入一个由Zego分配的，并且已经存在的房间
/// \param zegoId 房间的Id，房间唯一标识
/// \return true:调用成功，等待ZegoChatDelegate的onGetInChatRoomResult的通知；false:调用失败
- (bool) getInChatRoom:(ZegoUser*)user zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId;

/// \brief 设置发布直播的额外数据
/// \note 调用该接口设置的数据，设置的数据会在startPublishInChatRoom上传，拉取直播列表时会带回来该数据，用户可以用来设置封面，地理位置等信息
/// \param type 设置的数据类型有文件和内存数据两种类型
/// \param dataKey 设置数据都是以{key,value}的形式来设置，datakey开发者可以自己定义
/// \param data 当dataKey==CustomDataType_data，用data来设置数据，NSData中是一个Byte数组
/// \param file 当dataKey==CustomDataType_file，用file来设置数据，本地文件路径，文件会上传
/// \return true:调用成功，等待ZegoVideoDelegate中onSetPublishExtraDataResult通知；false:调用失败
- (bool) setPublishExtraData:(CustomDataType)type dataKey:(NSString*)key data:(NSData*)data file:(NSString*)file;

/// \brief 开始直播
/// \note 在进入聊天室成功后，调用该接口来直播
/// \param title 直播的名称
/// \return true:调用成功，等待ZegoVideoDelegate中的onPublishSucc或onPublishStop通知；false:调用失败
- (bool) startPublishInChatRoom:(NSString*)title;

/// \brief 停止直播
/// \note 在聊天室内，停止直播调用该接口
/// \return true:停止成功；false:停止失败
- (bool) stopPublishInChatRoom;

/// \brief 获取主播流信息
/// \return 直播流信息，key 包含 kZegoPublishStreamIDKey/kZegoPublishStreamURLKey/kZegoPublishStreamAliasKey
- (NSDictionary *)currentPublishInfo;

/// \brief 观看直播
/// \note 在进入聊天室成功后，调用该接口来观看直播
/// \param index setRemoteView传进来的index
/// \param streamID onPlayListUpdate带回来的STREAM_ID字段
/// \return true:调用成功，等待ZegoVideoDelegate中的onPlaySucc或onPlayStop通知；false:调用失败
- (bool) startPlayInChatRoom:(RemoteViewIndex)index streamID:(long long)streamID;

/// \brief 停止观看
/// \note 在聊天室内，调用该接口来停止观看直播
/// \return true:停止成功；false:停止失败
- (bool) stopPlayInChatRoom:(long long)streamID;

/// \brief 在聊天室里广播文本消息
/// \note 在进入聊天室成功后，调用该接口广播文本消息
/// \param msg 文本消息
/// \return 0:表示失败，非0表示发送的文本消息的一个唯一ID，方便开发者通过这个ID来搜索消息记录
- (long long) sendBroadcastTextMsgInChatRoom:(NSString*)msg;

/// \brief 在聊天室里广播一个自定义消息
/// \note 在进入聊天室成功后，调用该接口广播自定义的消息，该接口有调用频率限制
/// \param msg 自定义消息，字段见CustomMsg，NSDictionary(SYNC_COUNT_0, 。。。 SYNC_COUNT_7, CUSTOM_DATA)
/// 前8个字段是unsigned int类型，可用于在房间内广播一些自定义计数（如点赞，送花，建议累计3秒发送一次），房间成员在onReceiveBroadcastCustomMsg收到该广播
/// \return 0:表示失败；负数表示发送太频繁需稍后在发送；大于0表示发送的文本消息的一个唯一ID，方便开发者通过这个ID来搜索消息记录
- (long long) sendBroadcastCustomMsgInChatRoom:(NSDictionary*)msg;

/// \brief 退出聊天室
/// \return true：退出成功；false:退出失败
- (bool) leaveChatRoom;

/// \brief 开关手电筒
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool) enableTorch:(bool) bEnable;

/// \brief 主播方开启美颜功能
/// \param bEnable true 打开，false 关闭
/// \return true: 成功；false: 失败
- (bool)enableBeautifying:(bool)bEnable;

- (bool)setFilter:(ZegoFilter)filter;

/// \brief （声音输出）静音开关
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool) enableSpeaker:(bool) bEnable;

/// \brief 手机内置扬声器开关
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)setBuiltInSpeakerOn:(bool)bOn;

/// 设置连接到测试服务器，开发者无需关心
- (bool) setTestServer:(NSString*)ip port:(int)port url:(NSString*)url;

@end


extern const NSString *kZegoPublishStreamIDKey;     ///< 主播流ID，uint64
extern const NSString *kZegoPublishStreamURLKey;    ///< 主播流观看 url，NSString
extern const NSString *kZegoPublishStreamAliasKey;  ///< 主播流别名，NSString
