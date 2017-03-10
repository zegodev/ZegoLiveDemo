//
//  ZegoLiveApi-utils.h
//  zegoavkit
//
//  Created by Randy Qiu on 2016/12/9.
//  Copyright © 2016年 Zego. All rights reserved.
//

#import <ZegoAVKit2/ZegoLiveApi.h>
#import <AVFoundation/AVFoundation.h>

@interface ZegoLiveApi (Utils)

+ (bool)createPixelBufferPool:(CVPixelBufferPoolRef*)pool width:(int)width height:(int)height;
+ (void)destroyPixelBufferPool:(CVPixelBufferPoolRef*)pool;
+ (bool)copyPixelBufferFrom:(CVPixelBufferRef)src to:(CVPixelBufferRef)dst;

@end
