//
//  ZegoChatCommand.h
//  LiveDemo3
//
//  Created by Strong on 16/6/27.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ZegoAVKitManager.h"

extern NSString *const kZEGO_CHAT_REQUEST_PUBLISH;
extern NSString *const kZEGO_CHAT_RESPOND_PUBLISH;

extern NSString *const kZEGO_CHAT_CMD;
extern NSString *const kZEGO_CHAT_FROM_USERID;
extern NSString *const KZEGO_CHAT_FROM_USERNAME;
extern NSString *const kZEGO_CHAT_TO_USER;
extern NSString *const kZEGO_CHAT_TO_USERID;
extern NSString *const KZEGO_CHAT_TO_USERNAME;
extern NSString *const kZEGO_CHAT_CONTENT;
extern NSString *const kZEGO_CHAT_MAGIC;

extern NSString *const kZEGO_CHAT_AGREE_PUBLISH;
extern NSString *const kZEGO_CHAT_DISAGREE_PUBLISH;

@interface ZegoChatCommand : NSObject

+ (void)sendCommand:(NSString *)command toUsers:(NSArray<ZegoUser*> *)toUsers content:(NSString *)content magicNumber:(NSString *)magicNumber;

+ (NSDictionary *)getRequestPublishRsp:(NSData *)data;

@end
