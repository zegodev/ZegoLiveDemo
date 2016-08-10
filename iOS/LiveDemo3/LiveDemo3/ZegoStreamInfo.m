//
//  ZegoStreamInfo.m
//  LiveDemo3
//
//  Created by Strong on 16/6/27.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoStreamInfo.h"
#import "ZegoAVKitManager.h"

@implementation ZegoStreamInfo

+ (instancetype)getStreamInfo:(NSDictionary *)streamDict
{
    ZegoStreamInfo *streamInfo = [ZegoStreamInfo new];
    streamInfo.index = [streamDict[kRoomIndexKey] unsignedIntValue];
    streamInfo.streamID = streamDict[kRoomStreamIDKey];
    streamInfo.title = streamDict[kRoomTitleKey];
    streamInfo.userName = streamDict[kRoomUserNameKey];
    
    return streamInfo;
}

@end
