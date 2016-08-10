//
//  ZegoChatCommand.m
//  LiveDemo3
//
//  Created by Strong on 16/6/27.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoChatCommand.h"
#import "ZegoSettings.h"

NSString *const kZEGO_CHAT_REQUEST_PUBLISH  = @"requestPublish";
NSString *const kZEGO_CHAT_RESPOND_PUBLISH  = @"requestPublishRespond";

NSString *const kZEGO_CHAT_CMD              = @"command";
NSString *const kZEGO_CHAT_FROM_USERID      = @"fromUserId";
NSString *const KZEGO_CHAT_FROM_USERNAME    = @"fromUserName";
NSString *const kZEGO_CHAT_TO_USER          = @"toUser";
NSString *const kZEGO_CHAT_TO_USERID        = @"toUserId";
NSString *const KZEGO_CHAT_TO_USERNAME      = @"toUserName";
NSString *const kZEGO_CHAT_CONTENT          = @"content";
NSString *const kZEGO_CHAT_MAGIC            = @"magic";

NSString *const kZEGO_CHAT_AGREE_PUBLISH    = @"YES";
NSString *const kZEGO_CHAT_DISAGREE_PUBLISH = @"NO";

@implementation ZegoChatCommand

+ (void)sendCommand:(NSString *)command toUsers:(NSArray<ZegoUser*> *)toUsers content:(NSString *)content magicNumber:(NSString *)magicNumber
{
    if (command.length == 0)
        return;
    
    NSMutableDictionary *requestInfo = [NSMutableDictionary dictionary];
    requestInfo[kZEGO_CHAT_CMD] = command;
    
    ZegoUser *fromUser = [[ZegoSettings sharedInstance] getZegoUser];
    requestInfo[kZEGO_CHAT_FROM_USERID] = fromUser.userID;
    requestInfo[KZEGO_CHAT_FROM_USERNAME] = fromUser.userName;
    
    if (toUsers.count != 0)
    {
        NSMutableArray *toUserList = [NSMutableArray array];
        for (ZegoUser *user in toUsers)
        {
            if (user.userName.length == 0 && user.userID.length == 0)
                continue;
            
            NSMutableDictionary *userDict = [NSMutableDictionary dictionary];
            if (user.userID.length != 0)
                userDict[kZEGO_CHAT_TO_USERID] = user.userID;
            if (user.userName.length != 0)
                userDict[KZEGO_CHAT_TO_USERNAME] = user.userName;
            
            [toUserList addObject:userDict];
        }
        
        if (toUserList.count != 0)
            requestInfo[kZEGO_CHAT_TO_USER] = toUserList;
    }
    
    if (content.length != 0)
        requestInfo[kZEGO_CHAT_CONTENT] = content;
    
    requestInfo[kZEGO_CHAT_MAGIC] = magicNumber;
    
    NSError *error;
    NSData *data = [NSJSONSerialization dataWithJSONObject:requestInfo options:0 error:&error];
    if (error)
    {
        NSLog(@"serialize json error %@", error);
        return;
    }
    
    [getBizRoomInstance() sendBroadcastCustomMsgInChatRoom:data];
}

+ (NSDictionary *)getRequestPublishRsp:(NSData *)data
{
    NSError *error;
    NSDictionary *info = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
    if (error)
        NSLog(@"unserialize json error %@", error);
    
    return info;
}



@end
