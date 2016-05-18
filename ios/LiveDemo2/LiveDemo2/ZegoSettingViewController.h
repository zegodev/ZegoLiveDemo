//
//  ZegoSettingViewController.h
//  LiveDemo2
//
//  Created by Randy Qiu on 4/8/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZegoAVKitManager.h"

@interface ZegoSettingViewController : UITableViewController <UIPickerViewDelegate, UIPickerViewDataSource >

@end


@interface ZegoSettings : NSObject
{
    ZegoAVConfig *_currentConfig;
}

+ (instancetype)sharedInstance;

@property (nonatomic, strong) NSString *userID;
@property (nonatomic, strong) NSString *userName;

@property (readonly) NSArray *presetVideoQualityList;
@property (nonatomic, strong) ZegoAVConfig *currentConfig;
@property (readonly) NSInteger presetIndex;

- (BOOL)selectPresetQuality:(NSInteger)presetIndex;

@property (readonly) ZegoAVConfigVideoResolution currentResolution;

@property (nonatomic, copy) NSString *publishingStreamID;
@property (nonatomic, copy) NSString *publishingLiveChannel;

@end
