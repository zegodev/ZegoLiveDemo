//
//  ZegoRenderViewController.h
//  LiveDemo3
//
//  Created by Strong on 2016/10/18.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZegoLiveViewController.h"

@interface ZegoRenderAudienceViewController : ZegoLiveViewController

@property (nonatomic, assign) unsigned int bizToken;
@property (nonatomic, assign) unsigned int bizID;
@property (nonatomic, strong) NSArray *currentStreamList;

@end
