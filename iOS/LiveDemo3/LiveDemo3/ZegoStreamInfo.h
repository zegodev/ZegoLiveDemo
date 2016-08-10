//
//  ZegoStreamInfo.h
//  LiveDemo3
//
//  Created by Strong on 16/6/27.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ZegoStreamInfo : NSObject

@property (nonatomic, assign) NSUInteger index;
@property (nonatomic, copy) NSString *streamID;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *userName;
//@property (nonatomic, copy) NSString *streamUrl;

+ (instancetype)getStreamInfo:(NSDictionary *)streamDic;

@end
