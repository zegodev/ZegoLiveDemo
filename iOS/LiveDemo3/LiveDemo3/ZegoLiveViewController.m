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
#import "ZegoInstrument.h"

#import <TencentOpenAPI/QQApiInterface.h>
#import <TencentOpenAPI/QQApiInterfaceObject.h>

@interface ZegoLiveViewController () <UIAlertViewDelegate, ZegoLiveApiAudioRecordDelegate>

@property (assign) UIInterfaceOrientation currentOrientation;

@property (assign) BOOL isTargetOrientationPortrait;    ///< 当次直播是否竖屏

//混流时的数据源
@property (nonatomic, strong) NSData *auxData;
@property (nonatomic, assign) void *pPos;

//处理父view及子view弹框
@property (nonatomic, strong) NSMutableArray *requestAlertList;
@property (nonatomic, strong) NSMutableArray *requestAlertContextList;

@property (nonatomic, strong) NSTimer *usageTimer;
@property (nonatomic, strong) NSString *usageFilePath;

@property (strong) NSMutableData *recordedAudio;

@end

@implementation ZegoLiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
//    self.useFrontCamera = YES;
//    self.enableTorch = NO;
//    self.beautifyFeature = ZEGO_BEAUTIFY_NONE;
//    self.filter = ZEGO_FILTER_NONE;
    
    _viewContainersDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _viewIndexDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _videoSizeDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _streamID2SizeDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    
    self.enableMicrophone = YES;
    self.enablePreview = YES;
    self.viewMode = ZegoVideoViewModeScaleAspectFill;
    self.enableCamera = YES;
    self.enableSpeaker = YES;
    self.enableAux = NO;
    [getZegoAV_ShareInstance() enableAux:NO];
    
    self.logArray = [NSMutableArray array];
    if ([self isDeviceiOS7])
        self.requestAlertContextList = [NSMutableArray array];
    else
        self.requestAlertList = [NSMutableArray array];
    
    // 设置当前的手机姿势
    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
    self.currentOrientation = orientation;
    [self setRotateFromInterfaceOrientation:orientation];
    
    // 监听电话事件
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioSessionWasInterrupted:) name:AVAudioSessionInterruptionNotification object:nil];
    
    self.usageTimer = [NSTimer scheduledTimerWithTimeInterval:10 target:self selector:@selector(onUsage) userInfo:nil repeats:YES];
    
    self.usageFilePath = [self getLogFileName];
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [[NSFileManager defaultManager] removeItemAtPath:self.usageFilePath error:nil];
    });
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.usageFilePath])
    {
        [[NSFileManager defaultManager] createFileAtPath:self.usageFilePath contents:nil attributes:nil];
    }
    
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:self.usageFilePath];
    [fileHandle seekToEndOfFile];
    NSString *content = [NSString stringWithFormat:@"%@\n", NSStringFromClass([self class])];
    [fileHandle writeData:[content dataUsingEncoding:NSUTF8StringEncoding]];
    [fileHandle closeFile];
}

- (NSString *)getLogFileName
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    if (paths.count == 0)
    {
        NSLog(@"Error! Cannot Get Caches Dir.");
        return nil;
    }
    
    NSString *cachesDir = [paths objectAtIndex:0];
    NSString *logName = [cachesDir stringByAppendingPathComponent:@"usage.log"];
    
    return logName;
}

- (NSString *)getCurrentLogTime
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"[yyyy-dd-MM HH:mm:ss]";
    
    return [formatter stringFromDate:[NSDate date]];
}

- (void)onUsage
{
    float cpu = [[ZegoInstrument shareInstance] getCPUUsage];
    float memory = [[ZegoInstrument shareInstance] getMemoryUsage];
    float battery = [[ZegoInstrument shareInstance] getBatteryLevel];
//    printf("cpu %f, memory %f, battery %f\n", cpu, memory, battery);
    NSString *videoInfo = nil;
    if ([NSStringFromClass([self class]) containsString:@"Anchor"])
        videoInfo = [NSString stringWithFormat:@"fps %.3f, kbs %.3f", self.lastPublishFPS, self.lastPublishKBS];
    else if ([NSStringFromClass([self class]) containsString:@"Audience"])
        videoInfo = [NSString stringWithFormat:@"fps %.3f, kbs %.3f", self.lastPlayFPS, self.lastPlayKBS];
    
    NSString *usage = [NSString stringWithFormat:@"cpu %.3f, memory %.3f, battery %.2f %@\n", cpu, memory, battery, videoInfo];
    NSString *content = [NSString stringWithFormat:@"%@ %@", [self getCurrentLogTime], usage];
    
    NSLog(@"onUsage %@", content);
    
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:self.usageFilePath];
    [fileHandle seekToEndOfFile];
    [fileHandle writeData:[content dataUsingEncoding:NSUTF8StringEncoding]];
    [fileHandle closeFile];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    if (self.isBeingDismissed)
    {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        [self.usageTimer invalidate];
        self.usageTimer = nil;
    }
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

- (void)onEnableAux:(BOOL)enabled
{
    self.enableAux = enabled;
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

- (void)setEnableAux:(BOOL)enableAux
{
    if (_enableAux == enableAux)
        return;
    
    _enableAux = enableAux;
    [getZegoAV_ShareInstance() enableAux:enableAux];
    
    if (enableAux == NO)
    {
        self.pPos = NULL;
        self.auxData = nil;
    }
}

- (void)setupDeviceOrientation
{
    //
    // * 在开始直播前，确定直播时的手机姿势，二选一：横屏、竖屏
    //
    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
    
    self.isTargetOrientationPortrait = YES;
    switch (orientation) {
        case UIInterfaceOrientationLandscapeLeft:
        case UIInterfaceOrientationLandscapeRight:
            self.isTargetOrientationPortrait = NO;
            break;
            
        default:
            break;
    }
    
    CGSize resolution = [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution;
    
    //
    // * 修正最终需要的输出分辨率
    // * 保证：横屏姿势时，输出横屏视频，竖屏姿势时，输出竖屏视频
    //
    if (
        (
         self.isTargetOrientationPortrait           // 竖屏
         && resolution.width > resolution.height    // 但是宽比高大
        )
        ||
        (
         !self.isTargetOrientationPortrait          // 横屏
         && resolution.width < resolution.height    // 但是高比宽大
        )
       )
    {
        CGFloat tmp = resolution.height;
        resolution.height = resolution.width;
        resolution.width = tmp;
        [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution = resolution;
    }
    
    int ret = [getZegoAV_ShareInstance() setAVConfig:[ZegoSettings sharedInstance].currentConfig];
    assert(ret == 0);
    
    [self setRotateFromInterfaceOrientation:orientation];
}

- (void)setAnchorConfig:(UIView *)publishView
{
    [self setupDeviceOrientation];
    
    bool b = [getZegoAV_ShareInstance() setFrontCam:self.useFrontCamera];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableMic:self.enableMicrophone];
    assert(b);
    
    b = [getZegoAV_ShareInstance() enableBeautifying:self.beautifyFeature];
    assert(b);
    
    b = [getZegoAV_ShareInstance() setFilter:self.filter];
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
        [getBizRoomInstance() reportStreamAction:1 streamID:streamID userID:user.userID isPublicRoom:YES];
    else
        [getBizRoomInstance() reportStreamAction:2 streamID:streamID userID:user.userID isPublicRoom:YES];
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
    
    [self updateRemoteViewRotation];
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
    optionController.enableAux = self.enableAux;
    
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
}

- (void)dismissAlertView:(NSString *)magicNumber
{
    if ([self isDeviceiOS7])
    {
        NSUInteger index = [self getWaitingRequestListIndex:magicNumber];
        if (index == NSNotFound)
            return;
        
        UIAlertView *alertView = self.requestAlertContextList[index][@"AlertView"];
        if (alertView)
        {
            [alertView dismissWithClickedButtonIndex:-1 animated:YES];
        }
        
        [self.requestAlertContextList removeObjectAtIndex:index];
    }
    else
    {
        NSUInteger index = [self getWaitingRequestListIndex:magicNumber];
        if (index == NSNotFound)
            return;
        
        UIAlertController *alertController = self.requestAlertList[index][@"AlertController"];
        if (alertController == self.presentedViewController)
        {
            [alertController dismissViewControllerAnimated:YES completion:nil];
        }
        
        [self.requestAlertList removeObjectAtIndex:index];
    }
    
    [self continueOtherRequest];
}

- (BOOL)shouldShowPublishAlert
{
    return YES;
}

- (void)continueOtherRequest
{
    if ([self isDeviceiOS7])
    {
        if (self.requestAlertContextList.count == 0)
            return;
        
        if ([self shouldShowPublishAlert])
        {
            NSDictionary *dict = [self.requestAlertContextList firstObject];
            UIAlertView *alertView = dict[@"AlertView"];
            [alertView show];
        }
        else
        {
            for (NSDictionary *dict in self.requestAlertContextList)
                [self sendRequestPublishRespond:NO magicNumber:dict[@"Magic"] requestPublisher:dict[@"User"]];
            
            [self.requestAlertContextList removeAllObjects];
        }
    }
    else
    {
        if (self.requestAlertList.count == 0)
            return;
        
        if ([self shouldShowPublishAlert])
        {
            NSDictionary *dict = [self.requestAlertList firstObject];
            UIAlertController *alertController = dict[@"AlertController"];
            [self presentViewController:alertController animated:YES completion:nil];
        }
        else
        {
            for (NSDictionary *dict in self.requestAlertList)
            {
                [self sendRequestPublishRespond:NO magicNumber:dict[@"Magic"] requestPublisher:dict[@"User"]];
            }
            
            [self.requestAlertList removeAllObjects];
        }
    }
}

- (NSUInteger)getWaitingRequestListIndex:(NSString *)magicNumber
{
    if ([self isDeviceiOS7])
    {
        for (NSDictionary *dict in self.requestAlertContextList)
        {
            if ([dict[@"Magic"] isEqualToString:magicNumber])
                return [self.requestAlertContextList indexOfObject:dict];
        }
        
        return NSNotFound;
    }
    else
    {
        for (NSDictionary *dict in self.requestAlertList)
        {
            if ([dict[@"Magic"] isEqualToString:magicNumber])
                return [self.requestAlertList indexOfObject:dict];
        }
    }
    
    return NSNotFound;
}

- (void)requestPublishAlert:(ZegoUser *)requestUser magicNumber:(NSString *)magicNumber
{
//    if (self.presentedViewController)
//    {
//        [self.presentedViewController dismissViewControllerAnimated:YES completion:nil];
//    }
//    
    NSString *message = [NSString stringWithFormat:NSLocalizedString(@"%@ 请求直播，是否允许", nil), requestUser.userName];
    if ([self isDeviceiOS7])
    {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:message delegate:self cancelButtonTitle:NSLocalizedString(@"拒绝", nil) otherButtonTitles:NSLocalizedString(@"允许", nil), nil];
        NSDictionary *contextDictionary = @{@"Magic": magicNumber, @"User": requestUser, @"AlertView": alertView};
        if (self.requestAlertContextList.count == 0)
            [alertView show];
        [self.requestAlertContextList addObject:contextDictionary];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:message preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"拒绝", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            [self sendRequestPublishRespond:NO magicNumber:magicNumber requestPublisher:requestUser];
            
            NSUInteger index = [self getWaitingRequestListIndex:magicNumber];
            if (index != NSNotFound)
                [self.requestAlertList removeObjectAtIndex:index];
            [self continueOtherRequest];
        }];
        UIAlertAction *okAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"允许", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self sendRequestPublishRespond:YES magicNumber:magicNumber requestPublisher:requestUser];
            
            NSUInteger index = [self getWaitingRequestListIndex:magicNumber];
            if (index != NSNotFound)
                [self.requestAlertList removeObjectAtIndex:index];
            [self continueOtherRequest];
        }];
        
        [alertController addAction:cancelAction];
        [alertController addAction:okAction];
        
        NSDictionary *contextDictionary = @{@"Magic": magicNumber, @"User": requestUser, @"AlertController": alertController};
        [self.requestAlertList addObject:contextDictionary];
        
        if (![self.presentedViewController isKindOfClass:[UIAlertController class]])
        {
            [self.presentedViewController dismissViewControllerAnimated:YES completion:nil];
            
            [self presentViewController:alertController animated:YES completion:nil];
        }
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
            
            [self continueOtherRequest];
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
    //
    // * 手机姿势变化时，更新手机朝向配置
    //
    [getZegoAV_ShareInstance() setAppOrientation:fromInterfaceOrientation];
    
    //
    // * 保证预览为正的
    //
    CAPTURE_ROTATE cr = [self calculatePreviewRotation:fromInterfaceOrientation];
    [getZegoAV_ShareInstance() setLocalViewRotation:cr];
    
    //
    // * 重新计算铺满 view 的旋转角度
    //
    [self updateRemoteViewRotation];
}

/// \breif 计算预览的旋转角度
/// \param fromInterfaceOrientation 手机朝向
/// \return 逆时针旋转角度
- (CAPTURE_ROTATE)calculatePreviewRotation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    int rotate = CAPTURE_ROTATE_0;
    
    switch (fromInterfaceOrientation)
    {
        case UIInterfaceOrientationPortrait:
        case UIInterfaceOrientationPortraitUpsideDown:
            if (!self.isTargetOrientationPortrait)
            {
                rotate = self.useFrontCamera ? CAPTURE_ROTATE_270 : CAPTURE_ROTATE_90;
            }
            break;
            
        case UIInterfaceOrientationLandscapeLeft:
        case UIInterfaceOrientationLandscapeRight:
            if (self.isTargetOrientationPortrait)
            {
                rotate = self.useFrontCamera ? CAPTURE_ROTATE_270 : CAPTURE_ROTATE_90;
            }
            break;
            
        default:
            break;
    }
    
    return (CAPTURE_ROTATE)rotate;
}

- (void)updateRemoteViewRotation
{
    for (NSString *streamID in self.viewIndexDict.allKeys)
    {
        UIView *view = self.viewContainersDict[streamID];
        NSValue *sizeValue = self.streamID2SizeDict[streamID];
        if (sizeValue == nil)
        {
            continue;
        }
        
        CGSize videoSize = [sizeValue CGSizeValue];
        int viewIndex = [self.viewIndexDict[streamID] intValue];
        
        CAPTURE_ROTATE rotate = CAPTURE_ROTATE_0;
        
        if (
            (
             view.bounds.size.height > view.bounds.size.width
              && videoSize.height < videoSize.width
            )
            ||
            (
             view.bounds.size.height < view.bounds.size.width
              && videoSize.height > videoSize.width
            )
           )
        {
            // * 当长宽比不匹配的时候，我们做一个渲染旋转，让视频塞满这个 view
            rotate = CAPTURE_ROTATE_90;
        }
        
        [getZegoAV_ShareInstance() setRemoteViewRotation:rotate viewIndex:viewIndex];
    }
}

- (void)onVideoSizeChanged:(NSString *)streamID width:(uint32)width height:(uint32)height
{
    NSLog(@"%s, streamID %@", __func__, streamID);
    self.streamID2SizeDict[streamID] = [NSValue valueWithCGSize:CGSizeMake(width, height)];
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
//    return [NSString stringWithFormat:@"%.0f", [[NSDate date] timeIntervalSince1970] * 1000];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"[HH-mm-ss:SSS]";
    return [formatter stringFromDate:[NSDate date]];
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
    
    NSUInteger index = NSNotFound;
    for (NSDictionary *dict in self.requestAlertContextList)
    {
        if (dict[@"AlertView"] == alertView)
        {
            magicNumber = dict[@"magic"];
            requestUser = dict[@"User"];
            
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
    
    [self.requestAlertList removeObjectAtIndex:index];
    [self continueOtherRequest];
    
}

- (void)updateQuality:(int)quality view:(UIView *)playerView
{
    if (playerView == nil)
        return;
    
    CALayer *qualityLayer = nil;
    CATextLayer *textLayer = nil;
    
    for (CALayer *layer in playerView.layer.sublayers)
    {
        if ([layer.name isEqualToString:@"quality"])
            qualityLayer = layer;
        
        if ([layer.name isEqualToString:@"indicate"])
            textLayer = (CATextLayer *)layer;
    }
    
    int originQuality = 0;
    if (qualityLayer != nil)
    {
        if (CGColorEqualToColor(qualityLayer.backgroundColor, [UIColor greenColor].CGColor))
            originQuality = 0;
        else if (CGColorEqualToColor(qualityLayer.backgroundColor, [UIColor yellowColor].CGColor))
            originQuality = 1;
        else if (CGColorEqualToColor(qualityLayer.backgroundColor, [UIColor redColor].CGColor))
            originQuality = 2;
        else
            originQuality = 3;
        
        if (quality == originQuality)
            return;
    }
    
    UIFont *textFont = [UIFont systemFontOfSize:13];
    
    if (qualityLayer == nil)
    {
        qualityLayer = [CALayer layer];
        qualityLayer.name = @"quality";
        [playerView.layer addSublayer:qualityLayer];
        qualityLayer.frame = CGRectMake(22, 22, 12, 12);
        qualityLayer.contentsScale = [UIScreen mainScreen].scale;
        qualityLayer.cornerRadius = 6.0f;
    }
    
    if (textLayer == nil)
    {
        textLayer = [CATextLayer layer];
        textLayer.name = @"indicate";
        [playerView.layer addSublayer:textLayer];
        textLayer.backgroundColor = [UIColor clearColor].CGColor;
        textLayer.font = (__bridge CFTypeRef)textFont.fontName;
        textLayer.foregroundColor = [UIColor blackColor].CGColor;
        textLayer.fontSize = textFont.pointSize;
        textLayer.contentsScale = [UIScreen mainScreen].scale;
    }
    
    UIColor *qualityColor = nil;
    NSString *text = nil;
    if (quality == 0)
    {
        qualityColor = [UIColor greenColor];
        text = [NSString stringWithFormat:@"%@%@", NSLocalizedString(@"当前质量:", nil), NSLocalizedString(@"优", nil)];
    }
    else if (quality == 1)
    {
        qualityColor = [UIColor yellowColor];
        text = [NSString stringWithFormat:@"%@%@", NSLocalizedString(@"当前质量:", nil), NSLocalizedString(@"良", nil)];
    }
    else if (quality == 2)
    {
        qualityColor = [UIColor redColor];
        text = [NSString stringWithFormat:@"%@%@", NSLocalizedString(@"当前质量:", nil), NSLocalizedString(@"中", nil)];
    }
    else
    {
        qualityColor = [UIColor grayColor];
        text = [NSString stringWithFormat:@"%@%@", NSLocalizedString(@"当前质量:", nil), NSLocalizedString(@"差", nil)];
    }
    
    qualityLayer.backgroundColor = qualityColor.CGColor;
    CGSize textSize = [text sizeWithAttributes:@{NSFontAttributeName: textFont}];
    CGRect textFrame = CGRectMake(CGRectGetMaxX(qualityLayer.frame) + 3, CGRectGetMinY(qualityLayer.frame) + (CGRectGetHeight(qualityLayer.frame) - ceilf(textSize.height))/2, ceilf(textSize.width), ceilf(textSize.height));
    textLayer.frame = textFrame;
    textLayer.string = text;
}

- (void)auxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount
{
    if (self.auxData == nil)
    {
        //初始化auxData
        NSURL *auxURL = [[NSBundle mainBundle] URLForResource:@"a.pcm" withExtension:nil];
        if (auxURL)
        {
            self.auxData = [NSData dataWithContentsOfURL:auxURL options:0 error:nil];
            self.pPos = (void *)[self.auxData bytes];
        }
    }
    
    if (self.auxData)
    {
        int nLen = (int)[self.auxData length];
        if (self.pPos == 0)
            self.pPos = (void *)[self.auxData bytes];
        
        const void *pAuxData = [self.auxData bytes];
        if (pAuxData == NULL)
            return;
        
        int nLeftLen = (int)(pAuxData + nLen - self.pPos);
        if (nLeftLen < *pDataLen) {
            self.pPos = (void *)pAuxData;
            *pDataLen = 0;
            return;
        }
        
        if (pSampleRate)
            *pSampleRate = 44100;
        
        if (pChannelCount)
            *pChannelCount = 2;
        
        memcpy(pData, self.pPos, *pDataLen);
        self.pPos = self.pPos + *pDataLen;
    }
}

- (void)shareToQQ:(NSString *)hls rtmp:(NSString *)rtmp bizToken:(NSString *)bizToken bizID:(NSString *)bizID streamID:(NSString *)streamID
{
#if !defined(__i386__)
    NSString *urlString = [NSString stringWithFormat:@"http://www.zego.im/share/index?video=%@&rtmp=%@&token=%@&id=%@&stream=%@", [hls stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding], [rtmp stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding], bizToken, bizID, streamID];
    NSURL *url = [NSURL URLWithString:urlString];
    UIImage *logoImage = [UIImage imageNamed:@"zego"];
    NSData *previewImageData = UIImagePNGRepresentation(logoImage);
    
    NSString *title = @"LiveDemo";
    NSString *description = @"快来围观我的直播";
    
    QQApiURLObject *urlObject = [QQApiURLObject objectWithURL:url title:title description:description previewImageData:previewImageData targetContentType:QQApiURLTargetTypeNews];
    SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:urlObject];
    QQApiSendResultCode result = [QQApiInterface sendReq:req];
    
    NSLog(@"share To QQ URL: %@, result %d", urlString, result);

#endif
//    NSURL *shareURL = [NSURL URLWithString:[];
    
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/


#pragma mark - ZegoLiveApiAudioRecordDelegate

- (void)onAudioRecord:(NSData *)audioData sampleRate:(int)sampleRate numOfChannels:(int)numOfChannels bitDepth:(int)bitDepth
{
    if (!self.recordedAudio)
    {
        self.recordedAudio = [NSMutableData data];
    }
    
    [self.recordedAudio appendData:audioData];
}

- (void)enableAudioRecord:(BOOL)enable
{
    //
    // * test audio record
    //
    [getZegoAV_ShareInstance() enableAudioRecord:enable];
    if (enable)
    {
        [getZegoAV_ShareInstance() setAudioRecordDelegate:self];
    }
    else
    {
        [getZegoAV_ShareInstance() setAudioRecordDelegate:nil];
        
        if (self.recordedAudio)
        {
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
            NSString *cachesDir = [paths objectAtIndex:0];
            NSString *auidoFilePathname = [cachesDir stringByAppendingPathComponent:@"recored_audio"];
            
            [self.recordedAudio writeToFile:auidoFilePathname atomically:YES];
            self.recordedAudio = nil;
        }
    }
}

@end
