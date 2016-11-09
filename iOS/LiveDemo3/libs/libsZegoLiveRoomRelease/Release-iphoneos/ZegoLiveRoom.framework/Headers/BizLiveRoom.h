//
//  BizLiveRoom.h
//  liveroom
//
//  Created by Randy Qiu on 6/5/16.
//  Copyright © 2016 Biz. All rights reserved.
//

#import <Foundation/Foundation.h>

#ifdef __cplusplus
#define BIZ_EXTERN     extern "C"
#else
#define BIZ_EXTERN     extern
#endif

//onStreamUpdate 的dictionary key
BIZ_EXTERN NSString *const kRoomIndexKey;
BIZ_EXTERN NSString *const kRoomTitleKey;
BIZ_EXTERN NSString *const kRoomStreamIDKey;
BIZ_EXTERN NSString *const kRoomUserNameKey;
BIZ_EXTERN NSString *const kRoomStreamUrlKey;

//onGetRoomInfoResult dictionary key
BIZ_EXTERN NSString *const kRoomInfoIDKey;
BIZ_EXTERN NSString *const kRoomInfoCreateTimeKey;
BIZ_EXTERN NSString *const kRoomInfoBizIDKey;
BIZ_EXTERN NSString *const kRoomInfoBizTokenKey;
BIZ_EXTERN NSString *const kRoomInfoLivesCount;
BIZ_EXTERN NSString *const kRoomInfoFirstLiveTitle;
BIZ_EXTERN NSString *const kRoomInfoLiveStreamIDs;

//onRoomUserUpdate dictionary key
BIZ_EXTERN NSString *const kUserInfoIndexKey;
BIZ_EXTERN NSString *const kUserInfoUpdateKey;
BIZ_EXTERN NSString *const kUserInfoUserIDKey;
BIZ_EXTERN NSString *const kUserInfoUserNameKey;

@protocol BizRoomInfoDelegate <NSObject>
- (void)onGetRoomInfoResult:(int)err totalCount:(int)totalCount beginIndex:(int)beginIndex roomInfoList:(NSArray *)roomInfoList;
@end

@protocol BizRoomStreamDelegate <NSObject>

- (void)onLoginRoom:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom;
- (void)onLeaveRoom:(int)err isPublicRoom:(bool)isPublicRoom;
- (void)onDisconnected:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom;

- (void)onStreamUpdate:(NSArray<NSDictionary *>*)streamList flag:(int)flag isPublicRoom:(bool)isPublicRoom;
- (void)onStreamCreate:(NSString *)streamID url:(NSString *)url isPublicRoom:(bool)isPublicRoom;

- (void)onSendMessage:(int)nErrorCode content:(NSData *)content messageType:(int)type isPublicRoom:(bool)isPublicRoom;
- (void)onReceiveMessage:(NSData *)content messageType:(int)type isPublicRoom:(bool)isPublicRoom;

- (void)onRoomUserUpdate:(NSArray<NSDictionary *> *)userInfoList flag:(int)flag isPublicRoom:(bool)isPublicRoom;

@end

@interface BizLiveRoom : NSObject

@property (nonatomic, weak) id<BizRoomInfoDelegate> roomDelegate;
@property (nonatomic, weak) id<BizRoomStreamDelegate> streamDelegate;

/// 设置日志记录等级
/// \param logLevel 4 - debug, 3 - generic
+ (void)setLogLevel:(int)logLevel;

/// \brief 初始化SDK
- (instancetype)initWithBizID:(unsigned int)bizID bizSignature:(NSData*)bizSignature;

- (bool)setNetType:(int)netType;

///带聊天室功能的直播接口，都带有ChatRoom结尾的
- (bool)loginLiveRoom:(NSString *)userID userName:(NSString *)userName bizToken:(unsigned int)bizToken bizID:(unsigned int)bizID isPublicRoom:(BOOL)isPublicRoom;

/// \brief 在聊天室里广播文本消息
- (bool)sendBroadcastTextMsgInChatRoom:(NSString*)msg isPublicRoom:(BOOL)isPublicRoom;

- (bool)sendBroadcastCustomMsgInChatRoom:(NSData *)data isPublicRoom:(BOOL)isPublicRoom;

/// \brief 退出聊天室
/// \return true：退出成功；false:退出失败
- (bool)leaveLiveRoom:(BOOL)isPublicRoom;

/// \brief 获取直播列表
- (bool) getShowList:(unsigned int)indexBegin count:(unsigned int)count;

/// \brief 刷新直播列表
- (bool) refreshShowList;

/**
 *  开始直播时创建流
 *
 *  @param streamTitle   直播的title
 *  @param preferredStreamID 期待的streamID
 *
 *  @return result
 */
- (bool)cteateStreamInRoom:(NSString *)streamTitle preferredStreamID:(NSString *)preferredStreamID isPublicRoom:(BOOL)isPublicRoom;

/**
 *  获取当前进入房间中正在直播的流列表，通过onStreamUpdate回调获取结果
 *
 *  @return result
 */
- (bool)getStreamList:(BOOL)isPublicRoom;

/**
 *  上报stream 行为
 *
 *  @param actionType    1:开始发布；2：结束发布
 *  @param strStreamID streamID
 *  @param strUserId     userID
 *
 *  @return result
 */
- (bool)reportStreamAction:(int)actionType streamID:(NSString *)streamID userID:(NSString *)userID isPublicRoom:(BOOL)isPublicRoom;

@end
