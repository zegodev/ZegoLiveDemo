//
//  ZegoVideoDelegate.h
//  zegoavkit
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol ZegoVideoDelegate <NSObject>

typedef unsigned int	uint32;

typedef enum {
    ShowErrCode_Temp_Broken = 1,
    ShowErrCode_End = 2
} ShowErrCode;

/// \brief 发布直播成功
/// \param zegoToken 聊天室token，进入一个聊天室，需要同时有zegoToken和zegoId
/// \param zegoId 聊天室唯一标识
/// \param title 直播名称
- (void) onPublishSucc:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title;

/// \brief 发布直播失败
/// \param err 1临时中断，2结束发布
/// \param zegoToken 聊天室token，进入一个聊天室，需要同时有zegoToken和zegoId
/// \param zegoId 聊天室唯一标识
/// \param title 直播名称
- (void) onPublishStop:(ShowErrCode)err zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title;

/// \brief 观看直播成功
/// \param zegoToken 聊天室token，进入一个聊天室，需要同时有zegoToken和zegoId
/// \param zegoId 聊天室唯一标识
/// \param streamID 直播流的唯一标识
/// \param title 直播名称
- (void) onPlaySucc:(long long)streamID zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title;

/// \brief 观看直播失败
/// \param err 1临时中断，2结束观看
/// \param zegoToken 聊天室token，进入一个聊天室，需要同时有zegoToken和zegoId
/// \param zegoId 聊天室唯一标识
/// \param streamID 直播流的唯一标识
/// \param title 直播名称
- (void) onPlayStop:(uint32)err streamID:(long long)streamID zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title;

/// \brief 观看直播人数更新
/// \param userCount 观看直播的人数
- (void) onPlayerCountUpdate:(uint32)userCount;

/// \brief 设置发布直播的额外数据结果，在startPublish后才回调
/// \param errCode 0上传成功，非0表示错误
/// \param zegoToken 聊天室token，进入一个聊天室，需要同时有zegoToken和zegoId
/// \param zegoId 聊天室唯一标识
/// \param dataKey setPublishExtraData时候传入的dataKey
- (void) onSetPublishExtraDataResult:(uint32)errCode zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId dataKey:(NSString*)strDataKey;

/// \brief 视频的宽度和高度变化通知,startPlay后，如果视频宽度或者高度发生变化(首次的值也会)，则收到该通知
/// \param width 宽
/// \param height 高
/// \param streamID 流的唯一标识
- (void) onVideoSizeChanged:(long long)streamID width:(uint32)width height:(uint32)height;

/// \brief 截取观看直播 view 图像结果
- (void)onTakeRemoteViewSnapshot:(CGImageRef)img;

/// \brief 截取本地预览视频 view 图像结果
- (void)onTakeLocalViewSnapshot:(CGImageRef)img;


@end
