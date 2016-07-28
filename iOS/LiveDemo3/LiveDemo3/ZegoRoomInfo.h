//
//  ZegoRoomInfo.h
//  LiveDemo3
//
//  Created by Strong on 16/6/23.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ZegoRoomInfo : NSObject

@property (nonatomic, assign) unsigned int bizToken;
@property (nonatomic, assign) unsigned int bizID;
@property (nonatomic, assign) NSUInteger createTime;
@property (nonatomic, assign) NSUInteger livesCount;
@property (nonatomic, copy) NSString *firstLiveTitle;
@property (nonatomic, strong) NSArray *streamList;

@end
