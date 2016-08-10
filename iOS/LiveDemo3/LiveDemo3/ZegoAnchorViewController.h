//
//  ZegoAnchorViewController.h
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZegoLiveViewController.h"

@interface ZegoAnchorViewController : ZegoLiveViewController
//直播标题
@property (nonatomic, copy) NSString *liveTitle;
//预览的界面view
@property (nonatomic, strong) UIView *publishView;

@end
