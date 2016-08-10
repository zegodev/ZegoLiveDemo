//
//  ZegoPublishViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/29.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoPublishViewController.h"
#import "ZegoAnchorViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"

#define MAX_TITLE_LENGTH    30

@interface ZegoPublishViewController () <UIPickerViewDelegate, UIPickerViewDataSource>

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
    
    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
    [self setRotateFromInterfaceOrientation:orientation];
    
    [self addPreview];
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
    if (self.tabBarController.selectedIndex == 1 && self.presentedViewController == nil)
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
    
    [self startPreview];
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
    
    b = [getZegoAV_ShareInstance() setFilter:[self.filterPicker selectedRowInComponent:0]];
    assert(b);
    
    [getZegoAV_ShareInstance() setLocalViewMode:ZegoVideoViewModeScaleAspectFill];
    
    [getZegoAV_ShareInstance() setLocalView:self.preView];
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

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.destinationViewController isKindOfClass:[ZegoAnchorViewController class]])
    {
        [self.titleField resignFirstResponder];
        
        ZegoAnchorViewController *anchorViewController = (ZegoAnchorViewController *)segue.destinationViewController;
        if (self.titleField.text.length == 0)
            anchorViewController.liveTitle = [NSString stringWithFormat:@"Hello-%@", [ZegoSettings sharedInstance].userName];
        else
        {
            if (self.titleField.text.length > MAX_TITLE_LENGTH)
                anchorViewController.liveTitle = [self.titleField.text substringToIndex:MAX_TITLE_LENGTH];
            else
                anchorViewController.liveTitle = self.titleField.text;
        }
        
        anchorViewController.useFrontCamera = self.switchCamera.on;
        anchorViewController.enableTorch = self.switchTorch.on;
        anchorViewController.beautifyFeature = [self.beautifyPicker selectedRowInComponent:0];
        anchorViewController.filter = [self.filterPicker selectedRowInComponent:0];
        
        [self.preView removeFromSuperview];
        anchorViewController.publishView = self.preView;
        self.preView = nil;
        
        self.titleField.text = nil;
    }
}

- (void)setRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    switch (fromInterfaceOrientation) {
        case UIInterfaceOrientationPortrait:
            [getZegoAV_ShareInstance() setDisplayRotation:CAPTURE_ROTATE_0];
            break;
            
        case UIInterfaceOrientationPortraitUpsideDown:
            [getZegoAV_ShareInstance() setDisplayRotation:CAPTURE_ROTATE_180];
            break;
            
        case UIInterfaceOrientationLandscapeLeft:
            [getZegoAV_ShareInstance() setDisplayRotation:CAPTURE_ROTATE_270];
            break;
            
        case UIInterfaceOrientationLandscapeRight:
            [getZegoAV_ShareInstance() setDisplayRotation:CAPTURE_ROTATE_90];
            break;
            
        default:
            break;
    }
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
        [self setRotateFromInterfaceOrientation:orientation];
    } completion:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        
    }];
    
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self setRotateFromInterfaceOrientation:toInterfaceOrientation];
    
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

@end
