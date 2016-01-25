//
//  CustomMsg.h
//  zegoavkit
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#if TARGET_OS_IPHONE
#import <ZegoAVKit/ZegoUser.h>
#elif TARGET_OS_MAC
#import <ZegoAVKitosx/ZegoUser.h>
#endif

#define SYNC_COUNT_0 @"SYNC_COUNT_0"
#define SYNC_COUNT_1 @"SYNC_COUNT_1"
#define SYNC_COUNT_2 @"SYNC_COUNT_2"
#define SYNC_COUNT_3 @"SYNC_COUNT_3"
#define SYNC_COUNT_4 @"SYNC_COUNT_4"
#define SYNC_COUNT_5 @"SYNC_COUNT_5"
#define SYNC_COUNT_6 @"SYNC_COUNT_6"
#define SYNC_COUNT_7 @"SYNC_COUNT_7"
#define CUSTOM_DATA  @"CUSTOM_DATA"

@interface CustomMsg : NSObject

@property (nonatomic,retain) ZegoUser* user;

@property (nonatomic) time_t sendTime;

@property (nonatomic,retain) NSDictionary* msg;


- (instancetype)init;

@end
