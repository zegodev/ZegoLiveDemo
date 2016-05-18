//
//  ZegoLiveViewController.h
//  LiveDemo2
//
//  Created by Randy Qiu on 4/10/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZegoLiveViewController : UIViewController <UIPickerViewDelegate, UIPickerViewDataSource>

@property NSInteger liveType;   ///< 1 - show as anchor, 2 - show as audience, 3 - restore

#pragma mark - anchor info
@property (copy) NSString *liveTitle;
@property (copy) NSString *streamID;

#pragma mark - audience info
@property (copy) NSString *liveChannel;

@end
