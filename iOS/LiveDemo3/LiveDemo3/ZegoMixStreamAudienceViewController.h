//
//  ZegoMixStreamAudienceViewController.h
//  LiveDemo3
//
//  Created by Strong on 16/9/9.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoLiveViewController.h"

@interface ZegoMixStreamAudienceViewController : ZegoLiveViewController

@property (nonatomic, assign) unsigned int bizToken;
@property (nonatomic, assign) unsigned int bizID;
@property (nonatomic, strong) NSArray *currentStreamList;

@end
