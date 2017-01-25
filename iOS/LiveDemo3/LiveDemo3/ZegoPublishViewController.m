//
//  ZegoPublishViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/29.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoPublishViewController.h"
#import "ZegoAnchorViewController.h"
#import "ZegoMoreAnchorViewController.h"
#import "ZegoMixStreamAnchorViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"
#import <AVFoundation/AVFoundation.h>

#define MAX_TITLE_LENGTH    30

@interface ZegoPublishViewController () <UIPickerViewDelegate, UIPickerViewDataSource, UIActionSheetDelegate, ZegoDeviceEventDelegate>

@property (nonatomic, weak) IBOutlet UISwitch *switchCamera;
@property (nonatomic, weak) IBOutlet UISwitch *switchTorch;
@property (nonatomic, weak) IBOutlet UIPickerView *beautifyPicker;
@property (nonatomic, weak) IBOutlet UIPickerView *filterPicker;
@property (nonatomic, weak) IBOutlet UITextField *titleField;
@property (nonatomic, weak) IBOutlet UIButton *publishButton;

@property (nonatomic, weak) IBOutlet UIView *settingView;
@property (nonatomic, weak) IBOutlet UIView *boxView;

@property (readonly) NSArray *beautifyList;
@property (readonly) NSArray *filterList;

@property (nonatomic, strong) UIView *preView;

@property (nonatomic, assign) CGSize videoSize;

@end

@implementation ZegoPublishViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _beautifyList = @[
                      NSLocalizedString(@"无美颜", nil),
                      NSLocalizedString(@"磨皮", nil),
                      NSLocalizedString(@"全屏美白", nil),
                      NSLocalizedString(@"磨皮＋全屏美白", nil),
                      NSLocalizedString(@"磨皮+皮肤美白", nil)
                      ];
    
    _filterList = @[
                    NSLocalizedString(@"无滤镜", nil),
                    NSLocalizedString(@"简洁", nil),
                    NSLocalizedString(@"黑白", nil),
                    NSLocalizedString(@"老化", nil),
                    NSLocalizedString(@"哥特", nil),
                    NSLocalizedString(@"锐色", nil),
                    NSLocalizedString(@"淡雅", nil),
                    NSLocalizedString(@"酒红", nil),
                    NSLocalizedString(@"青柠", nil),
                    NSLocalizedString(@"浪漫", nil),
                    NSLocalizedString(@"光晕", nil),
                    NSLocalizedString(@"蓝调", nil),
                    NSLocalizedString(@"梦幻", nil),
                    NSLocalizedString(@"夜色", nil)
                    ];
    
    self.switchCamera.on = YES;
    self.switchTorch.on = NO;
    self.switchTorch.enabled = NO;
    
    self.settingView.backgroundColor = [UIColor clearColor];
    self.publishButton.layer.cornerRadius = 4.0f;
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapView:)];
    [self.settingView addGestureRecognizer:tapGesture];
    
    UITapGestureRecognizer *boxTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapView:)];
    [self.boxView addGestureRecognizer:boxTapGesture];
    
    self.videoSize = [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution;
    
//    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
//    [self setRotateFromInterfaceOrientation:orientation];
    
    [self.beautifyPicker selectRow:3 inComponent:0 animated:NO];
    
    [self addPreview];
    
}

- (void)showAuthorizationAlert:(NSString *)message title:(NSString *)title
{
    if ([[[UIDevice currentDevice] systemVersion] floatValue] < 8.0)
    {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:title message:message delegate:self cancelButtonTitle:NSLocalizedString(@"取消", nil) otherButtonTitles:NSLocalizedString(@"设置权限", nil), nil];
        [alertView show];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            self.publishButton.enabled = NO;
        }];
        UIAlertAction *settingAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"设置权限", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self openSetting];
        }];
        
        [alertController addAction:settingAction];
        [alertController addAction:cancelAction];
        
        alertController.preferredAction = settingAction;
        
        [self presentViewController:alertController animated:YES completion:nil];
    }
}

#pragma mark alert view delegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0)
        self.publishButton.enabled = NO;
    else
        [self openSetting];
}

- (void)openSetting
{
    NSURL *settingURL = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    if ([[UIApplication sharedApplication] canOpenURL:settingURL])
        [[UIApplication sharedApplication] openURL:settingURL];
}

//检查相机权限
- (BOOL)checkVideoAuthorization
{
    AVAuthorizationStatus videoAuthStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (videoAuthStatus == AVAuthorizationStatusDenied || videoAuthStatus == AVAuthorizationStatusRestricted)
        return NO;
    if (videoAuthStatus == AVAuthorizationStatusNotDetermined)
    {
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            if (granted == NO)
                self.publishButton.enabled = NO;
        }];
    }
    return YES;
}

- (BOOL)checkAudioAuthorization
{
    AVAuthorizationStatus audioAuthStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeAudio];
    if (audioAuthStatus == AVAuthorizationStatusDenied || audioAuthStatus == AVAuthorizationStatusRestricted)
        return NO;
    if (audioAuthStatus == AVAuthorizationStatusNotDetermined)
    {
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeAudio completionHandler:^(BOOL granted) {
            if (granted == NO)
                self.publishButton.enabled = NO;
        }];
    }
    
    return YES;
}

- (void)addPreview
{
    _preView = [[UIView alloc] init];
    self.preView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:self.preView];
    [self.view sendSubviewToBack:self.preView];
    
    [self addPreviewConstraints];
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (void)addPreviewConstraints
{
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[_preView]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(_preView)]];
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[_preView]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(_preView)]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)onApplicationActive:(NSNotification *)notification
{
    if (self.tabBarController.selectedIndex == 1 && self.presentedViewController == nil && self.preView != nil)
    {
        [self stopPreview];
        [self startPreview];
    }
}

- (void)onApplicationDeactive:(NSNotification *)notification
{
    if (self.tabBarController.selectedIndex == 1 && self.presentedViewController == nil)
        [self stopPreview];
}

- (void)onTapView:(UIGestureRecognizer *)recognizer
{
    [self.titleField resignFirstResponder];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onApplicationActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onApplicationDeactive:) name:UIApplicationWillResignActiveNotification object:nil];
    
    if (self.preView == nil)
        [self addPreview];
    
    BOOL videoAuthorization = [self checkVideoAuthorization];
    BOOL audioAuthorization = [self checkAudioAuthorization];
    
    if (videoAuthorization == YES)
    {
        [self startPreview];
        [getZegoAV_ShareInstance() setAppOrientation:[UIApplication sharedApplication].statusBarOrientation];
        
        if (audioAuthorization == NO)
        {
            [self showAuthorizationAlert:NSLocalizedString(@"直播视频,访问麦克风", nil) title:NSLocalizedString(@"需要访问麦克风", nil)];
        }
    }
    else
    {
        [self showAuthorizationAlert:NSLocalizedString(@"直播视频,访问相机", nil) title:NSLocalizedString(@"需要访问相机", nil)];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    if (self.preView)
        [self stopPreview];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [super viewWillDisappear:animated];
}

- (void)startPreview
{
    if (UIInterfaceOrientationIsLandscape([UIApplication sharedApplication].statusBarOrientation))
    {
        [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution = CGSizeMake(self.videoSize.height, self.videoSize.width);
    }
    else
    {
        [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution = self.videoSize;
    }
    
    int ret = [getZegoAV_ShareInstance() setAVConfig:[ZegoSettings sharedInstance].currentConfig];
    assert(ret == 0);
    
    bool b = [getZegoAV_ShareInstance() setFrontCam:self.switchCamera.on];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableMic:YES];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableTorch:self.switchTorch.on];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableBeautifying:[self.beautifyPicker selectedRowInComponent:0]];
    assert(b);
    
    
    [getZegoAV_ShareInstance() setPolishFactor:4.0];
    [getZegoAV_ShareInstance() setPolishStep:4.0];
    [getZegoAV_ShareInstance() setWhitenFactor:0.6];
    
    
    b = [getZegoAV_ShareInstance() setFilter:[self.filterPicker selectedRowInComponent:0]];
    assert(b);
    

    {
        [getZegoAV_ShareInstance() setWaterMarkImagePath:@"asset:watermark"];
        
        [getZegoAV_ShareInstance() setPreviewWaterMarkRect:CGRectMake(10, 40, 100, 100)];
        [getZegoAV_ShareInstance() setPublishWaterMarkRect:CGRectMake(10, 40, 50, 50)];
    }
    
    [getZegoAV_ShareInstance() setLocalViewMode:ZegoVideoViewModeScaleAspectFill];
    
    [getZegoAV_ShareInstance() setLocalView:self.preView];
    [getZegoAV_ShareInstance() setDeviceEventDelegate:self];
    [getZegoAV_ShareInstance() startPreview];
}


- (void)stopPreview
{
    [getZegoAV_ShareInstance() setLocalView:nil];
    [getZegoAV_ShareInstance() stopPreview];
}

#pragma mark UIPickerDelegate
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    if (pickerView == self.beautifyPicker) {
        return self.beautifyList.count;
    } else {
        return self.filterList.count;
    }
}

- (NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    NSArray *dataList = nil;
    if (pickerView == self.beautifyPicker) {
        dataList = self.beautifyList;
    } else {
        dataList = _filterList;
    }
    
    if (row >= dataList.count) {
        return @"Error";
    }
    
    return [dataList objectAtIndex:row];
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    [self.titleField resignFirstResponder];
    
    if (pickerView == self.beautifyPicker)
    {
        [getZegoAV_ShareInstance() enableBeautifying:row];
    }
    else
    {
        [getZegoAV_ShareInstance() setFilter:row];
    }
}

- (IBAction)onSwitchCamer:(id)sender
{
    [self.titleField resignFirstResponder];
    
    [getZegoAV_ShareInstance() setFrontCam:self.switchCamera.on];
    if (self.switchCamera.on)
        self.switchTorch.enabled = NO;
    else
        self.switchTorch.enabled = YES;
}

- (IBAction)onSwitchTorch:(id)sender
{
    [self.titleField resignFirstResponder];
    
    [getZegoAV_ShareInstance() enableTorch:self.switchTorch.on];
}


- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    return YES;
}

- (BOOL)isDeviceiOS7
{
    if ([[[UIDevice currentDevice] systemVersion] floatValue] < 8.0)
        return YES;
    
    return NO;
}

- (IBAction)onStartPublish:(id)sender
{
    if ([self isDeviceiOS7])
    {
        UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:NSLocalizedString(@"请选择直播模式", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"取消", nil) destructiveButtonTitle:nil otherButtonTitles:NSLocalizedString(@"无连麦模式", nil), NSLocalizedString(@"连麦模式", nil), NSLocalizedString(@"混流模式", nil), nil];
        actionSheet.actionSheetStyle = UIActionSheetStyleDefault;
        [actionSheet showInView:self.view];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil message:NSLocalizedString(@"请选择直播模式", nil) preferredStyle:UIAlertControllerStyleActionSheet];
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            
        }];
        
        UIAlertAction *anchorAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"无连麦模式", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self performSegueWithIdentifier:@"anchorSegueIdentifier" sender:nil];
        }];
        
        UIAlertAction *moreAnchorAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"连麦模式", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self performSegueWithIdentifier:@"moreAnchorSegueIdentifier" sender:nil];
        }];
        
        UIAlertAction *mixStreamAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"混流模式", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self performSegueWithIdentifier:@"mixStreamAnchorSegueIdentifier" sender:nil];
        }];
        
        [alertController addAction:cancelAction];
        [alertController addAction:anchorAction];
        [alertController addAction:moreAnchorAction];
        [alertController addAction:mixStreamAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    }
}

#pragma mark ActionSheetDelegate
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0)
        [self performSegueWithIdentifier:@"anchorSegueIdentifier" sender:nil];
    else if (buttonIndex == 1)
        [self performSegueWithIdentifier:@"moreAnchorSegueIdentifier" sender:nil];
    else if (buttonIndex == 2)
        [self performSegueWithIdentifier:@"mixStreamAnchorSegueIdentifier" sender:nil];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    [self.titleField resignFirstResponder];
    NSString *liveTitle = nil;
    if (self.titleField.text.length == 0)
        liveTitle = [NSString stringWithFormat:@"Hello-%@", [ZegoSettings sharedInstance].userName];
    else
    {
        if (self.titleField.text.length > MAX_TITLE_LENGTH)
            liveTitle = [self.titleField.text substringToIndex:MAX_TITLE_LENGTH];
        else
            liveTitle = self.titleField.text;
    }

    
    if ([segue.identifier isEqualToString:@"anchorSegueIdentifier"])
    {
        ZegoAnchorViewController *anchorViewController = (ZegoAnchorViewController *)segue.destinationViewController;
        
        anchorViewController.liveTitle = liveTitle;
        anchorViewController.useFrontCamera = self.switchCamera.on;
        anchorViewController.enableTorch = self.switchTorch.on;
        anchorViewController.beautifyFeature = [self.beautifyPicker selectedRowInComponent:0];
        anchorViewController.filter = [self.filterPicker selectedRowInComponent:0];
        
        [self.preView removeFromSuperview];
        anchorViewController.publishView = self.preView;
        self.preView = nil;
    }
    else if ([segue.identifier isEqualToString:@"moreAnchorSegueIdentifier"])
    {
        ZegoMoreAnchorViewController *anchorViewController = (ZegoMoreAnchorViewController *)segue.destinationViewController;
        
        anchorViewController.liveTitle = liveTitle;
        anchorViewController.useFrontCamera = self.switchCamera.on;
        anchorViewController.enableTorch = self.switchTorch.on;
        anchorViewController.beautifyFeature = [self.beautifyPicker selectedRowInComponent:0];
        anchorViewController.filter = [self.filterPicker selectedRowInComponent:0];
        
        [self.preView removeFromSuperview];
        anchorViewController.publishView = self.preView;
        self.preView = nil;
    }
    else if ([segue.identifier isEqualToString:@"mixStreamAnchorSegueIdentifier"])
    {
        ZegoMixStreamAnchorViewController *anchorViewController = (ZegoMixStreamAnchorViewController *)segue.destinationViewController;
        
        anchorViewController.liveTitle = liveTitle;
        anchorViewController.useFrontCamera = self.switchCamera.on;
        anchorViewController.enableTorch = self.switchTorch.on;
        anchorViewController.beautifyFeature = [self.beautifyPicker selectedRowInComponent:0];
        anchorViewController.filter = [self.filterPicker selectedRowInComponent:0];
        
        [self.preView removeFromSuperview];
        anchorViewController.publishView = self.preView;
        self.preView = nil;
    }
    
    self.titleField.text = nil;
}

- (void)setRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [self stopPreview];
    [getZegoAV_ShareInstance() setAppOrientation:fromInterfaceOrientation];
    [self startPreview];
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    if (self.presentedViewController == nil && self.isViewLoaded && self.tabBarController.selectedIndex == 1)
    {
        [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
            UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
            [self setRotateFromInterfaceOrientation:orientation];
        } completion:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
            
        }];
    }
    
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self setRotateFromInterfaceOrientation:toInterfaceOrientation];
    
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

#pragma mark - ZegoDeviceEventDelegate

- (void)zego_onDevice:(NSString *)deviceName error:(int)errorCode
{
    NSLog(@"device name: %@, error code: %d", deviceName, errorCode);
}

@end
