//
//  ZegoLiveViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/28.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoLiveViewController.h"
#import "ZegoAnchorOptionViewController.h"
#import "ZegoSettings.h"
#import "ZegoChatCommand.h"


@interface ZegoLiveViewController () <UIAlertViewDelegate>

@property (nonatomic, strong) NSMutableDictionary *requestAlertDict;
@property (nonatomic, strong) NSMutableDictionary *requestAlertContextDict;
@property (assign) UIInterfaceOrientation currentOrientation;

@end

@implementation ZegoLiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
//    self.useFrontCamera = YES;
//    self.enableTorch = NO;
//    self.beautifyFeature = ZEGO_BEAUTIFY_NONE;
//    self.filter = ZEGO_FILTER_NONE;
    
    self.enableMicrophone = YES;
    self.enablePreview = YES;
    self.viewMode = ZegoVideoViewModeScaleAspectFill;
    self.enableCamera = YES;
    self.enableSpeaker = YES;
    
    self.logArray = [NSMutableArray array];
    if ([self isDeviceiOS7])
        self.requestAlertContextDict = [NSMutableDictionary dictionary];
    else
        self.requestAlertDict = [NSMutableDictionary dictionary];
    
    // 设置当前的手机姿势
    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
    self.currentOrientation = orientation;
    [self setRotateFromInterfaceOrientation:orientation];
    
    // 监听电话事件
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioSessionWasInterrupted:) name:AVAudioSessionInterruptionNotification object:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    if (self.isBeingDismissed)
        [[NSNotificationCenter defaultCenter] removeObserver:self];
}

//由子类来处理不同的业务逻辑
- (void)audioSessionWasInterrupted:(NSNotification *)notification
{
    
}

#pragma mark option delegate
- (void)onUseFrontCamera:(BOOL)use
{
    self.useFrontCamera = use;
}

- (void)onEnableMicrophone:(BOOL)enabled
{
    self.enableMicrophone = enabled;
}

- (void)onEnableTorch:(BOOL)enable
{
    self.enableTorch = enable;
}

- (void)onSelectedBeautify:(NSInteger)row
{
    self.beautifyFeature = row;
}

- (void)onSelectedFilter:(NSInteger)row
{
    self.filter = row;
}

- (void)onEnableCamera:(BOOL)enabled
{
    self.enableCamera = enabled;
}

- (void)onEanbleSpeaker:(BOOL)enable
{
    self.enableSpeaker = enable;
}

#pragma mark setter
- (void)setBeautifyFeature:(ZegoBeautifyFeature)beautifyFeature
{
    if (_beautifyFeature == beautifyFeature)
        return;
    
    _beautifyFeature = beautifyFeature;
    [getZegoAV_ShareInstance() enableBeautifying:beautifyFeature];
}

- (void)setFilter:(ZegoFilter)filter
{
    if (_filter == filter)
        return;
    
    _filter = filter;
    [getZegoAV_ShareInstance() setFilter:filter];
}

- (void)setUseFrontCamera:(BOOL)useFrontCamera
{
    if (_useFrontCamera == useFrontCamera)
        return;
    
    _useFrontCamera = useFrontCamera;
    [getZegoAV_ShareInstance() setFrontCam:useFrontCamera];
    [self setRotateFromInterfaceOrientation:self.currentOrientation];
}

- (void)setEnableMicrophone:(BOOL)enableMicrophone
{
    if (_enableMicrophone == enableMicrophone)
        return;
    
    _enableMicrophone = enableMicrophone;
    [getZegoAV_ShareInstance() enableMic:enableMicrophone];
}

- (void)setEnableTorch:(BOOL)enableTorch
{
    if (_enableTorch == enableTorch)
        return;
    
    _enableTorch = enableTorch;
    [getZegoAV_ShareInstance() enableTorch:enableTorch];
}

- (void)setEnableCamera:(BOOL)enableCamera
{
    if (_enableCamera == enableCamera)
        return;
    
    _enableCamera = enableCamera;
    [getZegoAV_ShareInstance() enableCamera:enableCamera];
}

- (void)setEnableSpeaker:(BOOL)enableSpeaker
{
    if (_enableSpeaker == enableSpeaker)
        return;
    
    _enableSpeaker = enableSpeaker;
    [getZegoAV_ShareInstance() enableSpeaker:enableSpeaker];
}

- (void)setAnchorConfig:(UIView *)publishView
{
    int ret = [getZegoAV_ShareInstance() setAVConfig:[ZegoSettings sharedInstance].currentConfig];
    assert(ret == 0);
    
    bool b = [getZegoAV_ShareInstance() setFrontCam:self.useFrontCamera];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableMic:self.enableMicrophone];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableBeautifying:self.beautifyFeature];
    assert(b);
    
    [self enablePreview:self.enablePreview LocalView:publishView];
    [getZegoAV_ShareInstance() setLocalViewMode:self.viewMode];
}

- (void)enablePreview:(BOOL)enable LocalView:(UIView *)view
{
    if (enable && view)
    {
        [getZegoAV_ShareInstance() setLocalView:view];
        [getZegoAV_ShareInstance() startPreview];
    }
    else
    {
        [getZegoAV_ShareInstance() setLocalView:nil];
        [getZegoAV_ShareInstance() stopPreview];
    }
}

- (void)addFirstPlayViewConstraints:(UIView *)firstView containerView:(UIView *)containerView
{
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[firstView]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(firstView)]];
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[firstView]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(firstView)]];
}

- (void)addSecondPlayViewConstraints:(UIView *)secondView containerView:(UIView *)containerView
{
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:[secondView(==210)]-(10)-|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(secondView)]];
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:[secondView(==140)]-(10)-|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(secondView)]];
}

- (void)addThirdPlayViewConstraints:(UIView *)thirdView secondView:(UIView *)secondView containerView:(UIView *)containerView
{
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:[thirdView(==210)]-(10)-|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(thirdView)]];
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:[thirdView(==140)]-(10)-[secondView]" options:0 metrics:nil views:NSDictionaryOfVariableBindings(thirdView, secondView)]];
}

- (UIView *)getFirstViewInContainer:(UIView *)containerView
{
    for (UIView *subview in containerView.subviews)
    {
        if (CGRectGetWidth(subview.frame) == CGRectGetWidth(containerView.frame))
            return subview;
    }
    
    return nil;
}

- (UIView *)getSecondViewInContainer:(UIView *)containerView
{
    for (UIView *subView in containerView.subviews)
    {
        if (CGRectGetMaxX(subView.frame) + 10 == CGRectGetMaxX(containerView.frame))
            return subView;
    }
    
    return nil;
}

- (UIView *)getThirdViewInContainer:(UIView *)containerView
{
    for (UIView *subview in containerView.subviews)
    {
        if (CGRectGetMaxX(subview.frame) + CGRectGetWidth(subview.frame) + 20 == CGRectGetMaxX(containerView.frame))
            return subview;
    }
    
    return nil;
}

- (void)reportStreamAction:(BOOL)success streamID:(NSString *)streamID
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    if (success)
        [getBizRoomInstance() reportStreamAction:1 streamID:streamID userID:user.userID];
    else
        [getBizRoomInstance() reportStreamAction:2 streamID:streamID userID:user.userID];
}

- (void)updateContainerConstraintsForTap:(UIView *)tapView containerView:(UIView *)containerView
{
    UIView *bigView = [self getFirstViewInContainer:containerView];
    if (bigView == tapView || tapView == nil)
        return;
    
    UIView *thirdView = [self getThirdViewInContainer:containerView];
    UIView *secondView = [self getSecondViewInContainer:containerView];
    
    [containerView removeConstraints:containerView.constraints];
    
    if (secondView == tapView)
    {
        //第二个和第一个view交换constraints
        [self addFirstPlayViewConstraints:tapView containerView:containerView];
        [self addSecondPlayViewConstraints:bigView containerView:containerView];
        if (thirdView)
            [self addThirdPlayViewConstraints:thirdView secondView:bigView containerView:containerView];
        
        [containerView sendSubviewToBack:tapView];
        
        [UIView animateWithDuration:0.1 animations:^{
            [self.view layoutIfNeeded];
        }];
    }
    else if (tapView == thirdView)
    {
        //第三个view和第一个view交换constraints
        [self addFirstPlayViewConstraints:thirdView containerView:containerView];
        [self addSecondPlayViewConstraints:secondView containerView:containerView];
        [self addThirdPlayViewConstraints:bigView secondView:secondView containerView:containerView];
        
        [containerView sendSubviewToBack:tapView];
        
        [UIView animateWithDuration:0.1 animations:^{
            [self.view layoutIfNeeded];
        }];
    }
}

- (void)updateContainerConstraintsForRemove:(UIView *)removeView containerView:(UIView *)containerView
{
    if (removeView == nil)
        return;
    
    UIView *bigView = [self getFirstViewInContainer:containerView];
    UIView *secondeView = [self getSecondViewInContainer:containerView];
    UIView *thirdView = [self getThirdViewInContainer:containerView];
    
    [containerView removeConstraints:containerView.constraints];
    if (removeView == bigView)
    {
        //删除大图时，更新第二个view为大图
        if (secondeView)
            [self addFirstPlayViewConstraints:secondeView containerView:containerView];
        if (thirdView)
            [self addSecondPlayViewConstraints:thirdView containerView:containerView];
        [containerView sendSubviewToBack:secondeView];
    }
    else if (removeView == secondeView)
    {
        [self addFirstPlayViewConstraints:bigView containerView:containerView];
        if (thirdView)
            [self addSecondPlayViewConstraints:thirdView containerView:containerView];
        [containerView sendSubviewToBack:bigView];
    }
    else if (removeView == thirdView)
    {
        [self addFirstPlayViewConstraints:bigView containerView:containerView];
        [self addSecondPlayViewConstraints:secondeView containerView:containerView];
        [containerView sendSubviewToBack:bigView];
    }
    
    [removeView removeFromSuperview];
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (BOOL)setContainerConstraints:(UIView *)view containerView:(UIView *)containerView viewCount:(NSUInteger)viewCount
{
    if (viewCount == 0)
    {
        [self addFirstPlayViewConstraints:view containerView:containerView];
    }
    else if (viewCount == 1)
    {
        [self addSecondPlayViewConstraints:view containerView:containerView];
    }
    else if (viewCount == 2)
    {
        UIView *secondView = [self getSecondViewInContainer:containerView];
        if (secondView)
        {
            [self addThirdPlayViewConstraints:view secondView:secondView containerView:containerView];
        }
        else
        {
            assert(secondView);
            return NO;
        }
    }
    else
    {
        return NO;
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
    
    return YES;
}

- (BOOL)isDeviceiOS7
{
    if ([[[UIDevice currentDevice] systemVersion] floatValue] < 8.0)
        return YES;
    
    return NO;
}

- (void)showPublishOption
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    ZegoAnchorOptionViewController *optionController = (ZegoAnchorOptionViewController *)[storyboard instantiateViewControllerWithIdentifier:@"anchorOptionID"];
    
    optionController.useFrontCamera = self.useFrontCamera;
    optionController.enableMicrophone = self.enableMicrophone;
    optionController.enableTorch = self.enableTorch;
    optionController.beautifyRow = self.beautifyFeature;
    optionController.filterRow = self.filter;
    optionController.enableCamera = self.enableCamera;
    
    optionController.delegate = self;
    
    self.definesPresentationContext = YES;
    if (![self isDeviceiOS7])
        optionController.modalPresentationStyle = UIModalPresentationOverCurrentContext;
    else
        optionController.modalPresentationStyle = UIModalPresentationCurrentContext;
    
    optionController.view.backgroundColor = [UIColor clearColor];
    [self presentViewController:optionController animated:YES completion:nil];
}

- (void)sendRequestPublishRespond:(BOOL)agreed magicNumber:(NSString *)magicNumber requestPublisher:(ZegoUser *)requestUser
{
    NSArray *userList = nil;
    if (requestUser != nil)
        userList = @[requestUser];
    
    NSString *content = agreed ? kZEGO_CHAT_AGREE_PUBLISH : kZEGO_CHAT_DISAGREE_PUBLISH;
    
    [ZegoChatCommand sendCommand:kZEGO_CHAT_RESPOND_PUBLISH toUsers:userList content:content magicNumber:magicNumber];
    [self.requestAlertDict removeObjectForKey:magicNumber];
}

- (void)dismissAlertView:(NSString *)magicNumber
{
    if ([self isDeviceiOS7])
    {
        UIAlertView *alertView = self.requestAlertContextDict[magicNumber][@"AlertView"];
        if (alertView)
        {
            [alertView dismissWithClickedButtonIndex:-1 animated:YES];
            [self.requestAlertContextDict removeObjectForKey:magicNumber];
        }
    }
    else
    {
        UIAlertController *alertController = self.requestAlertDict[magicNumber];
        if (alertController)
        {
            [alertController dismissViewControllerAnimated:YES completion:nil];
            [self.requestAlertDict removeObjectForKey:magicNumber];
        }
    }
}

- (void)requestPublishAlert:(ZegoUser *)requestUser magicNumber:(NSString *)magicNumber
{
    if (self.presentedViewController)
        [self.presentedViewController dismissViewControllerAnimated:YES completion:nil];
    
    NSString *message = [NSString stringWithFormat:NSLocalizedString(@"%@ 请求直播，是否允许", nil), requestUser.userName];
    if ([self isDeviceiOS7])
    {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:message delegate:self cancelButtonTitle:NSLocalizedString(@"拒绝", nil) otherButtonTitles:NSLocalizedString(@"允许", nil), nil];
        NSDictionary *contextDictionary = @{@"User": requestUser, @"AlertView": alertView};
        self.requestAlertContextDict[magicNumber] = contextDictionary;
        [alertView show];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:message preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"拒绝", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            [self sendRequestPublishRespond:NO magicNumber:magicNumber requestPublisher:requestUser];
        }];
        UIAlertAction *okAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"允许", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self sendRequestPublishRespond:YES magicNumber:magicNumber requestPublisher:requestUser];
        }];
        
        [alertController addAction:cancelAction];
        [alertController addAction:okAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
        self.requestAlertDict[magicNumber] = alertController;
    }
}

- (void)requestPublishResultAlert:(NSString *)fromUserName
{
    if (self.presentedViewController)
        [self.presentedViewController dismissViewControllerAnimated:YES completion:nil];
    
    NSString *message = [NSString stringWithFormat:NSLocalizedString(@"%@ 不允许连麦", nil), fromUserName];
    if ([self isDeviceiOS7])
    {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
        [alertView show];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:message preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        }];
        
        [alertController addAction:cancelAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    }
    
}

- (void)onReceivePublishRequest:(NSDictionary *)receiveInfo
{
    //请求直播用户
    NSString *fromUserName = receiveInfo[KZEGO_CHAT_FROM_USERNAME];
    NSString *fromUserID = receiveInfo[kZEGO_CHAT_FROM_USERID];
    NSString *magicNumber = receiveInfo[kZEGO_CHAT_MAGIC];
    
    if (fromUserID.length == 0)
        return;
    
    ZegoUser *requestUser = [ZegoUser new];
    requestUser.userID = fromUserID;
    requestUser.userName = fromUserName;
    
    [self requestPublishAlert:requestUser magicNumber:magicNumber];
}

- (void)setIdelTimerDisable:(BOOL)disable
{
    [[UIApplication sharedApplication] setIdleTimerDisabled:disable];
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
        self.currentOrientation = orientation;
        [self setRotateFromInterfaceOrientation:orientation];
    } completion:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        
    }];
    
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self setRotateFromInterfaceOrientation:toInterfaceOrientation];
    self.currentOrientation = toInterfaceOrientation;
    
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (NSString *)getCurrentTime
{
    return [NSString stringWithFormat:@"%.0f", [[NSDate date] timeIntervalSince1970] * 1000];
}

- (void)addLogString:(NSString *)logString
{
    if (logString.length != 0)
    {
        NSString *totalString = [NSString stringWithFormat:@"%@: %@", [self getCurrentTime], logString];
        [self.logArray insertObject:totalString atIndex:0];
        
        [[NSNotificationCenter defaultCenter] postNotificationName:@"logUpdateNotification" object:self userInfo:nil];
    }
}

#pragma mark UIAlertViewDelegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    NSString *magicNumber = nil;
    ZegoUser *requestUser = nil;
    for (NSString *key in self.requestAlertContextDict.allKeys)
    {
        NSDictionary *requestInfo = self.requestAlertContextDict[key];
        if (requestInfo[@"AlertView"] == alertView)
        {
            magicNumber = key;
            requestUser = requestInfo[@"User"];
            
            break;
        }
    }
    
    if (magicNumber == nil || requestUser == nil)
        return;
    
    if (buttonIndex == 0)
    {
        //cancel
        [self sendRequestPublishRespond:NO magicNumber:magicNumber requestPublisher:requestUser];
    }
    else if (buttonIndex == 1)
    {
        [self sendRequestPublishRespond:YES magicNumber:magicNumber requestPublisher:requestUser];
    }
    
    [self.requestAlertContextDict removeObjectForKey:magicNumber];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
