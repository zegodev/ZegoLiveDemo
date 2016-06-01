//
//  ZegoSettingViewController.m
//  LiveDemo2
//
//  Created by Randy Qiu on 4/8/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ZegoSettingViewController.h"

#include <string>

NSString *kZegoDemoUserIDKey            = @"userid";
NSString *kZegoDemoUserNameKey          = @"username";
NSString *kZegoDemoChannelIDKey         = @"channelid";
NSString *kZegoDemoVideoPresetKey       = @"preset";
NSString *kZegoDemoVideoResolutionKey   = @"resolution";
NSString *kZegoDemoVideoFrameRateKey    = @"framerate";
NSString *kZegoDemoVideoBitRateKey      = @"bitrate";

NSString *kZegoDemoPublishingStreamID   = @"streamID";   ///< 当前直播流 ID
NSString *kZegoDemoPublishingLiveID     = @"liveID";        ///< 当前直播频道 ID


@implementation ZegoSettings
{
    NSString *_userID;
    NSString *_userName;
    NSString *_channelID;
}

+ (instancetype)sharedInstance {
    static ZegoSettings *s_instance = nil;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_instance = [self new];
    });
    
    return s_instance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _presetVideoQualityList = @[@"超低质量", @"低质量", @"标准质量", @"高质量", @"超高质量", @"自定义"];
        [self loadConfig];
    }
    
    return self;
}


- (NSString *)userID {
    if (_userID.length == 0) {
        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSString *userID = [ud stringForKey:kZegoDemoUserIDKey];
        if (userID.length > 0) {
            _userID = userID;
        } else {
            srand((unsigned)time(0));
            _userID = [NSString stringWithFormat:@"%u", (unsigned)rand()];
            [ud setObject:_userID forKey:kZegoDemoUserIDKey];
        }
    }
    
    return _userID;
}


- (void)setUserID:(NSString *)userID {
    if ([_userID isEqualToString:userID]) {
        return;
    }
    
    if (userID.length > 0) {
        _userID = userID;
        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        [ud setObject:_userID forKey:kZegoDemoUserIDKey];
    }
}


- (NSString *)channelID {
    if (_channelID.length == 0) {
        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSString *channelID = [ud stringForKey:kZegoDemoChannelIDKey];
        if (channelID.length > 0) {
            _channelID = channelID;
        } else {
            _channelID = @"5190";
        }
    }
    
    return _channelID;
}


- (void)setChannelID:(NSString *)channelID {
    if ([_channelID isEqualToString:channelID]) {
        return;
    }
    
    if (channelID.length == 0) {
        channelID = @"5190";
    }
    
    _channelID = channelID;
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setObject:_channelID forKey:kZegoDemoChannelIDKey];
}


- (NSString *)userName {
    if (_userName.length == 0) {
        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        NSString *userName = [ud stringForKey:kZegoDemoUserNameKey];
        if (userName.length > 0) {
            _userName = userName;
        } else {
            srand((unsigned)time(0));
            _userName = [NSString stringWithFormat:@"iOS-%u", (unsigned)rand()];
            [ud setObject:_userName forKey:kZegoDemoUserNameKey];
        }
    }
    
    return _userName;
}


- (void)setUserName:(NSString *)userName {
    if ([_userName isEqualToString:userName]) {
        return;
    }
    
    if (userName.length > 0) {
        _userName = userName;
        NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
        [ud setObject:_userName forKey:kZegoDemoUserNameKey];
    }
}


- (BOOL)selectPresetQuality:(NSInteger)presetIndex {
    if (presetIndex >= self.presetVideoQualityList.count) {
        return NO;
    }
    
    _presetIndex = presetIndex;
    if (_presetIndex < self.presetVideoQualityList.count - 1) {
        _currentConfig = [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)_presetIndex];
    }
    
    [self saveConfig];
    return YES;
}


- (void)setCurrentConfig:(ZegoAVConfig *)currentConfig {
    _presetIndex = self.presetVideoQualityList.count - 1;
    _currentConfig = currentConfig;
    
    [self saveConfig];
}


- (ZegoAVConfigVideoResolution)currentResolution {
    CGSize size = [self.currentConfig getVideoResolution];
    
    ZegoAVConfigVideoResolution r = ZegoAVConfigVideoResolution_640x480;
    switch ((int)size.width) {
        case 320:
            r = (ZegoAVConfigVideoResolution)0;
            break;
        case 352:
            r = (ZegoAVConfigVideoResolution)1;
            break;
        case 640:
            r = (ZegoAVConfigVideoResolution)2;
            break;
        case 960:
            r = (ZegoAVConfigVideoResolution)3;
            break;
        case 1280:
            r = (ZegoAVConfigVideoResolution)4;
            break;
        case 1920:
            r = (ZegoAVConfigVideoResolution)5;
            break;
            
        default:
            r = (ZegoAVConfigVideoResolution)-1;
            break;
    }
    
    return r;
}


- (void)loadConfig {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    id preset = [ud objectForKey:kZegoDemoVideoPresetKey];
    if (preset) {
        _presetIndex = [preset integerValue];
        if (_presetIndex < _presetVideoQualityList.count - 1) {
            _currentConfig = [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)_presetIndex];
            return ;
        }
    } else {
        _presetIndex = ZegoAVConfigPreset_High;
        _currentConfig = [ZegoAVConfig defaultZegoAVConfig:ZegoAVConfigPreset_High];
        return ;
    }
    
    _currentConfig = [ZegoAVConfig defaultZegoAVConfig:ZegoAVConfigPreset_Generic];
    id resolution = [ud objectForKey:kZegoDemoVideoResolutionKey];
    if (resolution) {
        NSInteger resolutionIndex = [resolution integerValue];
        [_currentConfig setVideoResolution:(ZegoAVConfigVideoResolution)resolutionIndex];
    }
    
    id frameRate = [ud objectForKey:kZegoDemoVideoFrameRateKey];
    if (frameRate) {
        [_currentConfig setVideoFPS:(int)[frameRate integerValue]];
    }
    
    id bitRate = [ud objectForKey:kZegoDemoVideoBitRateKey];
    if (bitRate) {
        [_currentConfig setVideoBitrate:(int)[bitRate integerValue]];
    }
}


- (void)saveConfig {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    [ud setObject:@(_presetIndex) forKey:kZegoDemoVideoPresetKey];
    
    if (_presetIndex >= self.presetVideoQualityList.count - 1) {
        ZegoAVConfig *tmpConfig = [[ZegoAVConfig alloc] init];
        for (NSInteger i = 0; i <= 5; i++) {
            [tmpConfig setVideoResolution:(ZegoAVConfigVideoResolution)i];
            CGSize r = [tmpConfig getVideoResolution];
            if (CGSizeEqualToSize(r, [_currentConfig getVideoResolution])) {
                [ud setObject:@(i) forKey:kZegoDemoVideoResolutionKey];
                break;
            }
        }
        
        [ud setObject:@([_currentConfig getVideoFPS]) forKey:kZegoDemoVideoFrameRateKey];
        [ud setObject:@([_currentConfig getVideoBitrate]) forKey:kZegoDemoVideoBitRateKey];
    } else {
        [ud removeObjectForKey:kZegoDemoVideoResolutionKey];
        [ud removeObjectForKey:kZegoDemoVideoFrameRateKey];
        [ud removeObjectForKey:kZegoDemoVideoBitRateKey];
    }
}

- (void)setPublishingStreamID:(NSString *)publishingStreamID {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    
    if (publishingStreamID.length > 0) {
        [ud setObject:publishingStreamID forKey:kZegoDemoPublishingStreamID];
    } else {
        [ud removeObjectForKey:kZegoDemoPublishingStreamID];
    }
}

- (NSString *)publishingStreamID {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kZegoDemoPublishingStreamID];
}

- (void)setPublishingLiveChannel:(NSString *)publishingLiveChannel {
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    if (publishingLiveChannel.length > 0) {
        [ud setObject:publishingLiveChannel forKey:kZegoDemoPublishingLiveID];
    } else {
        [ud removeObjectForKey:kZegoDemoPublishingLiveID];
    }
}

- (NSString *)publishingLiveChannel {
    return [[NSUserDefaults standardUserDefaults] objectForKey:kZegoDemoPublishingLiveID];
}

@end


@interface ZegoSettingViewController ()
@property (weak, nonatomic) IBOutlet UITextField *userID;
@property (weak, nonatomic) IBOutlet UITextField *userName;
@property (weak, nonatomic) IBOutlet UITextField *channelID;

@property (weak, nonatomic) IBOutlet UIPickerView *presetPicker;
@property (weak, nonatomic) IBOutlet UILabel *videoResolution;
@property (weak, nonatomic) IBOutlet UILabel *videoFrameRate;
@property (weak, nonatomic) IBOutlet UILabel *videoBitRate;
@property (weak, nonatomic) IBOutlet UISlider *videoResolutionSlider;
@property (weak, nonatomic) IBOutlet UISlider *videoFrameRateSlider;
@property (weak, nonatomic) IBOutlet UISlider *videoBitRateSlider;
@property (weak, nonatomic) IBOutlet UITextField *appID;
@property (weak, nonatomic) IBOutlet UITextView *appSign;

@property (weak, nonatomic) IBOutlet UISwitch *testEnvSwitch;
@property (weak, nonatomic) IBOutlet UISwitch *hardwareAcceleratedSwitch;

@end


@implementation ZegoSettingViewController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self loadVideoSettings];
    [self loadAccountSettings];
    self.testEnvSwitch.on = isUseingTestEnv();
    self.hardwareAcceleratedSwitch.on = ZegoIsRequireHardwareAccelerated();
    
    if (ZegoGetAppID() != 0) {
        [self.appID setText:[NSString stringWithFormat:@"%u", ZegoGetAppID()]];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [ZegoSettings sharedInstance].userID = self.userID.text;
    [ZegoSettings sharedInstance].userName = self.userName.text;
    [ZegoSettings sharedInstance].channelID = self.channelID.text;
    
    if (self.appID.text.length > 0) {
    
        std::string strAppID = self.appID.text.UTF8String;
        unsigned long appID = std::stoul(strAppID);
        
        ZegoDemoSetCustomAppIDAndSign((uint32)appID, self.appSign.text);
    }
    
    setUseTestEnv(self.testEnvSwitch.on);
    

    
    [super viewWillDisappear:animated];
}

#pragma mark -- UIPickerViewDelegate, UIPickerViewDataSource
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return [ZegoSettings sharedInstance].presetVideoQualityList.count;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    if (row >= [ZegoSettings sharedInstance].presetVideoQualityList.count) {
        return ;
    }
    
    NSLog(@"%s: %@", __func__, [ZegoSettings sharedInstance].presetVideoQualityList[row]);
    
    [[ZegoSettings sharedInstance] selectPresetQuality:row];
    
    [self updateViedoSettingUI];
}

//返回当前行的内容,此处是将数组中数值添加到滚动的那个显示栏上
-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    if (row >= [ZegoSettings sharedInstance].presetVideoQualityList.count) {
        return @"ERROR";
    }
    
    return [[ZegoSettings sharedInstance].presetVideoQualityList objectAtIndex:row];
}


- (void)loadAccountSettings {
    self.userID.text = [ZegoSettings sharedInstance].userID;
    self.userName.text = [ZegoSettings sharedInstance].userName;
    self.channelID.text = [ZegoSettings sharedInstance].channelID;
}

- (void)loadVideoSettings {
    [self.presetPicker selectRow:[ZegoSettings sharedInstance].presetIndex inComponent:0 animated:YES];
    [self updateViedoSettingUI];
}

- (IBAction)sliderDidChange:(id)sender {
    [self.presetPicker selectRow:[ZegoSettings sharedInstance].presetVideoQualityList.count - 1 inComponent:0 animated:YES];
    
    ZegoAVConfig *config = [ZegoSettings sharedInstance].currentConfig;
    
    if (sender == self.videoResolutionSlider) {
        int v = (int)self.videoResolutionSlider.value;
        [config setVideoResolution:(ZegoAVConfigVideoResolution)v];
    } else if (sender == self.videoFrameRateSlider) {
        int v = (int)self.videoFrameRateSlider.value;
        [config setVideoFPS:v];
    } else if (sender == self.videoBitRateSlider) {
        int v = (int)self.videoBitRateSlider.value;
        [config setVideoBitrate:v];
    }
    
    [ZegoSettings sharedInstance].currentConfig = config;
    
    [self updateViedoSettingUI];
}


- (void)updateViedoSettingUI {
    ZegoAVConfig *config = [[ZegoSettings sharedInstance] currentConfig];
    
    self.videoResolutionSlider.value = [ZegoSettings sharedInstance].currentResolution;
    CGSize r = [config getVideoResolution];
    self.videoResolution.text = [NSString stringWithFormat:@"%d X %d", (int)r.width, (int)r.height];
    
    self.videoFrameRateSlider.value = [config getVideoFPS];
    self.videoFrameRate.text = [NSString stringWithFormat:@"%d", [config getVideoFPS]];
    
    self.videoBitRateSlider.value = [config getVideoBitrate];
    self.videoBitRate.text = [NSString stringWithFormat:@"%d", [config getVideoBitrate]];
}


- (BOOL)tableView:(UITableView *)tableView shouldHighlightRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath indexAtPosition:0] == 2 || [indexPath indexAtPosition:0] == 3) {
        return YES;
    }
    return NO;
}

- (IBAction)toggleTestEnv:(id)sender {
    UISwitch *s = (UISwitch *)sender;
    setUseTestEnv(s.on);
}

- (IBAction)toggleHardwareAccelerated:(id)sender {
    UISwitch *s = (UISwitch *)sender;
    ZegoRequireHardwareAccelerated(s.on);
}


@end
