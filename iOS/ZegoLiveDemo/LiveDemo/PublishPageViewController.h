//
//  PublishPageViewController.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PublishPageViewController : UIViewController<UIGestureRecognizerDelegate, UIActionSheetDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate>

@property (weak, nonatomic) IBOutlet UIImageView *coverImageView;
@property (weak, nonatomic) IBOutlet UITextField *publishDescription;
@property (weak, nonatomic) IBOutlet UIButton *btnBeginPublish;
@property (weak, nonatomic) IBOutlet UISwitch *previewSwitch;
- (IBAction)switchCameraClicked:(UIButton *)sender;
- (IBAction)enableTorchClicked:(UIButton *)sender;
- (IBAction)swichPreviewChanged:(UISwitch *)sender;
@end
