//
//  ZegoAVInChatRoomDelegate.h
//  zegoavkit
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CustomMsg.h"
#import "TextMsg.h"

@protocol ZegoChatDelegate <NSObject>

typedef unsigned int	uint32;

typedef enum {
    PlayListUpdateFlag_Error = 0,  ///< 获取直播流信息失败
    PlayListUpdateFlag_Add = 1,    ///< 新增流信息
    PlayListUpdateFlag_Remove = 2, ///< 流删除
    PlayListUpdateFlag_UpdateAll = 3,  ///< 全部更新，可能会有多条直播流 ***被废弃***
} PlayListUpdateFlag;

#define STREAM_ID @"stream_id"  //long long

/// \brief 进入聊天室的通知
/// \note 调用getInChatRoom后，会收到该消息通知
/// \param result 0表示进入聊天室成功，其他表示失败
/// \param zegoToken 聊天室token，进入一个已经存在的聊天室的凭证
/// \param zegoId 聊天室ID，唯一标识
- (void) onGetInChatRoomResult:(uint32)result zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId;

/// \brief 聊天室掉线
/// \note 网络异常的时候，会收到该通知
/// \param err 错误码，无需关心
- (void) onChatRoomDisconnected:(uint32)err;

/// \brief 被Server踢下线
/// \param reason 踢下线原因
/// \param msg 踢下线原因描述
- (void) onKickOut:(uint32) reason msg:(NSString*)msg;

@optional
/// \brief 聊天室成员更新
/// \note 在聊天室里，有其他成员进出的时候会收到该通知
/// \param arrNewUsers 新增成员列表
/// \param arrLeftUsers 离开成员列表
- (void) onChatRoomUsersUpdate:(NSArray*) arrNewUsers leftUsers:(NSArray*)arrLeftUsers;

/// \brief 聊天室内所有成员更新
/// \note 收到该通知，开发者只需清空原来的成员列表，全部重新赋值
/// \param arrUsers 聊天室成员列表
- (void) onChatRoomUserUpdateAll:(NSArray*) arrUsers;

/// \brief 广播消息的结果
/// \note 调用sendBroadcastTextMsgInChatRoom后，会收到该消息通知表示
/// \param result 0表示成功，其他为失败
/// \param msg 消息内容
/// \param msgSeq 发送时返回的seq
- (void) onSendBroadcastTextMsgResult:(uint32)result msg:(NSString*)msg msgSeq:(long long)msgSeq;

/// \brief 广播自定义消息的结果
/// \note 调用sendBroadcastCustomMsgInChatRoom后，会收到该消息通知表示
/// \param result 0表示成功，其他为失败
/// \param msg 消息内容
/// \param msgSeq 发送时返回的seq
- (void) onSendBroadcastCustomMsgResult:(uint32)result msg:(NSDictionary*)msg msgSeq:(long long)msgSeq;

/// \brief 收到广播消息
/// \param textMsg 收到的消息结构，包括发送者，发送时间，发送内容
- (void) onReceiveBroadcastTextMsg:(TextMsg*)textMsg;

/// \brief 收到广播自定义消息
/// \param customMsg 收到的消息结构，包括发送者，发送时间，发送内容，消息内容同sendBroadcastCustomMsgInChatRoom的参数
- (void) onReceiveBroadcastCustomMsg:(CustomMsg*)customMsg;

/// \brief 聊天室内直播更新
/// \note 直播更新
/// \param flag 发布流更新的状态，详见PlayListUpdateFlag
/// \param playList更新的流信息
/// list
///  | -- NSDictionary(SHOW_TITLE, PUBLISHER_ID, PUBLISHER_NAME, SHOW_PREVIEW_PIC_URL, STREAM_ID)
///  | -- NSDictionary(SHOW_TITLE, PUBLISHER_ID, PUBLISHER_NAME, SHOW_PREVIEW_PIC_URL, STREAM_ID)
///  | ...
- (void) onPlayListUpdate:(PlayListUpdateFlag)flag playList:(NSArray*)list;

/// \brief 用户自定义计数更新，所有观看同一个直播的用户sendBroadcastCustomMsgInChatRoom发送的数据的汇总
/// \param zegoToken 聊天室token，进入一个聊天室，需要同时有zegoToken和zegoId
/// \param zegoId 聊天室唯一标识
/// \param counts 在sendBroadcastCustomMsgInChatRoom中发送的前8个自定义计数的汇总（即房间内成员广播的总数，适用于点赞，送花给主播的总数）
- (void) onSyncTotalCountUpdate:(uint32)zegoToken zegoId:(uint32)zegoId counts:(NSDictionary*)counts;

@end
