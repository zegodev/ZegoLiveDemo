//
//  ProfileViewController.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ProfileViewController : UIViewController<UIPickerViewDelegate, UIPickerViewDataSource>
@property (weak, nonatomic) IBOutlet UITextField *accountID;
@property (weak, nonatomic) IBOutlet UITextField *accountName;
@property (weak, nonatomic) IBOutlet UIImageView *accountPic;

@property (weak, nonatomic) IBOutlet UIPickerView *liveQuality;
@property (weak, nonatomic) IBOutlet UITextField *tfIP;
@property (weak, nonatomic) IBOutlet UITextField *tfPort;
@property (weak, nonatomic) IBOutlet UITextField *tfUrl;

@property (weak, nonatomic) IBOutlet UISlider *sliderResolution;
@property (weak, nonatomic) IBOutlet UISlider *sliderFPS;
@property (weak, nonatomic) IBOutlet UISlider *sliderBitrate;
@property (weak, nonatomic) IBOutlet UILabel *labelResolution;
@property (weak, nonatomic) IBOutlet UILabel *labelFPS;
@property (weak, nonatomic) IBOutlet UILabel *labelBitrate;
@property (weak, nonatomic) IBOutlet UIPickerView *switchAdvanced;
@property (weak, nonatomic) IBOutlet UILabel *lablePrompt;
- (IBAction)sliderResolutionChanged:(UISlider *)sender;
- (IBAction)sliderFPSChanged:(UISlider *)sender;
- (IBAction)sliderBitrateChanged:(UISlider *)sender;
- (IBAction)btnChangePicClicked:(UIButton *)sender;
- (IBAction)viewTouchDown:(UIControl *)sender;
- (IBAction)textEditingDidEnd:(UITextField *)sender;

@end
