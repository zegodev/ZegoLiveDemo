//
//  ZegoInstrument.h
//  LiveDemo3
//
//  Created by Strong on 2016/10/13.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface ZegoInstrument : NSObject

+ (instancetype)shareInstance;

- (float)getCPUUsage;
- (float)getMemoryUsage;
- (float)getBatteryLevel;

@end
