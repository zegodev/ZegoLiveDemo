//
//  ZegoAnchorOptionViewController.h
//  LiveDemo3
//
//  Created by Strong on 16/6/23.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol ZegoAnchorOptionDelegate <NSObject>

- (void)onUseFrontCamera:(BOOL)use;
- (void)onEnableMicrophone:(BOOL)enabled;
- (void)onEnableTorch:(BOOL)enable;
- (void)onSelectedBeautify:(NSInteger)row;
- (void)onSelectedFilter:(NSInteger)row;
- (void)onEnableCamera:(BOOL)enabled;

@end

@interface ZegoAnchorOptionSwitchCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UISwitch *switchButton;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;

@end

@interface ZegoAnchorOptionPickerCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIPickerView *pickerView;

@end

@interface ZegoAnchorOptionViewController : UIViewController

@property (nonatomic, assign) BOOL useFrontCamera;
@property (nonatomic, assign) BOOL enableMicrophone;
@property (nonatomic, assign) BOOL enableTorch;
@property (nonatomic, assign) NSUInteger beautifyRow;
@property (nonatomic, assign) NSUInteger filterRow;
@property (nonatomic, assign) BOOL enableCamera;

@property (nonatomic, weak) id<ZegoAnchorOptionDelegate> delegate;

@end
