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

//混流时的数据源
@property (nonatomic, strong) NSData *auxData;
@property (nonatomic, assign) void *pPos;

//处理父view及子view弹框
@property (nonatomic, strong) NSMutableArray *requestAlertList;
@property (nonatomic, strong) NSMutableArray *requestAlertContextList;

@property (nonatomic, assign) CGSize videoSize;
@property (nonatomic, strong) NSTimer *usageTimer;
@property (nonatomic, strong) NSString *usageFilePath;

@property (strong) NSMutableData *recordedAudio;

@property (nonatomic, strong) NSTimer *captureTimer;
@property (nonatomic, strong) NSMutableDictionary *playTimerDictionary;

@property (nonatomic, assign) NSUInteger subViewSpace;
@property (nonatomic, assign) NSUInteger subViewWidth;
@property (nonatomic, assign) NSUInteger subViewHeight;
@property (nonatomic, assign) NSUInteger subViewPerRow;

@end

@implementation ZegoLiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
//    self.useFrontCamera = YES;
//    self.enableTorch = NO;
//    self.beautifyFeature = ZEGO_BEAUTIFY_NONE;
//    self.filter = ZEGO_FILTER_NONE;

    _maxStreamCount = [ZegoLiveApi getMaxPlayChannelCount];
    self.subViewSpace = 10;
    if (self.maxStreamCount <= 3)
    {
        self.subViewWidth = 140;
        self.subViewHeight = 210;
        self.subViewPerRow = 2;
    }
    else
    {
        self.subViewWidth = 90;
        self.subViewHeight = 135;
        self.subViewPerRow = 3;
    }
    
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
//    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
//    self.currentOrientation = orientation;
//    [self setRotateFromInterfaceOrientation:orientation];
    
    self.videoSize = [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution;
    
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
    
    self.playTimerDictionary = [NSMutableDictionary dictionaryWithCapacity:self.maxStreamCount];
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
        
        [self stopCaptureAudioLevel];
        for (NSNumber *number in self.playTimerDictionary.allKeys)
        {
            [self stopPlayAudioLevel:[number intValue]];
        }
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
//    [self setRotateFromInterfaceOrientation:self.currentOrientation];
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

- (void)setAnchorConfig:(UIView *)publishView
{
    [getZegoAV_ShareInstance() setAppOrientation:[UIApplication sharedApplication].statusBarOrientation];
    
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

- (void)reportStreamAction:(BOOL)success streamID:(NSString *)streamID
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    if (success)
        [getBizRoomInstance() reportStreamAction:1 streamID:streamID userID:user.userID isPublicRoom:YES];
    else
        [getBizRoomInstance() reportStreamAction:2 streamID:streamID userID:user.userID isPublicRoom:YES];
}

- (void)addFirstPlayViewConstraints:(UIView *)firstView containerView:(UIView *)containerView
{
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[firstView]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(firstView)]];
    [containerView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[firstView]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(firstView)]];
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

- (void)addPlayViewConstraints:(UIView *)view containerView:(UIView *)containerView viewIndex:(NSUInteger)viewIndex
{
    if (viewIndex == 0)
        [self addFirstPlayViewConstraints:view containerView:containerView];
    else
    {
        NSUInteger xIndex = (viewIndex - 1) % self.subViewPerRow;
        NSUInteger yIndex = (viewIndex - 1) / self.subViewPerRow;
        
        CGFloat xToLeftConstraints = xIndex * (self.subViewSpace + self.subViewWidth) + self.subViewSpace;
        CGFloat yToTobottomConstraints = yIndex * (self.subViewSpace + self.subViewHeight) + self.subViewSpace;
        
        NSLayoutConstraint *widthConstraints = [NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeWidth multiplier:1.0 constant:self.subViewWidth];
        NSLayoutConstraint *heightConstraints = [NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeHeight multiplier:1.0 constant:self.subViewHeight];
        NSLayoutConstraint *leftConstraints = [NSLayoutConstraint constraintWithItem:containerView attribute:NSLayoutAttributeTrailing relatedBy:NSLayoutRelationEqual toItem:view attribute:NSLayoutAttributeTrailing multiplier:1.0 constant:xToLeftConstraints];
        NSLayoutConstraint *bottomConstraints = [NSLayoutConstraint constraintWithItem:containerView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:view attribute:NSLayoutAttributeBottom multiplier:1.0 constant:yToTobottomConstraints];
        
        [containerView addConstraints:@[widthConstraints, heightConstraints, leftConstraints, bottomConstraints]];
    }
}

- (NSUInteger)getViewIndex:(UIView *)view containerView:(UIView *)containerView
{
    if (CGRectGetWidth(view.frame) == CGRectGetWidth(containerView.frame) &&
        CGRectGetHeight(view.frame) == CGRectGetHeight(containerView.frame))
        return 0;
    else
    {
        CGFloat deltaHeight = CGRectGetHeight(containerView.frame) - CGRectGetMaxY(view.frame) - self.subViewSpace;
        CGFloat deltaWidth = CGRectGetWidth(containerView.frame) - CGRectGetMaxX(view.frame) - self.subViewSpace;
        
        NSUInteger xIndex = deltaWidth / (self.subViewSpace + self.subViewWidth);
        NSUInteger yIndex = deltaHeight / (self.subViewSpace + self.subViewHeight);
        
        return yIndex * self.subViewPerRow + xIndex + 1;
    }
}

- (void)updateContainerConstraintsForTap:(UIView *)tapView containerView:(UIView *)containerView
{
    UIView *bigView = [self getFirstViewInContainer:containerView];
    if (bigView == tapView || tapView == nil)
        return;
    
    NSUInteger tapIndex = [self getViewIndex:tapView containerView:containerView];
    [containerView removeConstraints:containerView.constraints];
    [containerView exchangeSubviewAtIndex:0 withSubviewAtIndex:tapIndex];
    
    for (int i = 0; i < containerView.subviews.count; i++)
    {
        UIView *view = containerView.subviews[i];
        [self addPlayViewConstraints:view containerView:containerView viewIndex:i];
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
    
}

- (void)updateContainerConstraintsForRemove:(UIView *)removeView containerView:(UIView *)containerView
{
    if (removeView == nil)
        return;
    
    NSUInteger removeIndex = [self getViewIndex:removeView containerView:containerView];
    [containerView removeConstraints:containerView.constraints];
    
    for (UIView *view in containerView.subviews)
    {
        NSUInteger viewIndex = [self getViewIndex:view containerView:containerView];
        if (viewIndex == 0 && removeIndex != 0)
            [self addFirstPlayViewConstraints:view containerView:containerView];
        else if (viewIndex > removeIndex)
            [self addPlayViewConstraints:view containerView:containerView viewIndex:viewIndex - 1];
        else if (viewIndex < removeIndex)
            [self addPlayViewConstraints:view containerView:containerView viewIndex:viewIndex];
    }
    
    [removeView removeFromSuperview];
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (BOOL)setContainerConstraints:(UIView *)view containerView:(UIView *)containerView viewCount:(NSUInteger)viewCount
{
    [self addPlayViewConstraints:view containerView:containerView viewIndex:viewCount];
    
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
    
    UIFont *textFont = [UIFont systemFontOfSize:10];
    
    if (qualityLayer == nil)
    {
        qualityLayer = [CALayer layer];
        qualityLayer.name = @"quality";
        [playerView.layer addSublayer:qualityLayer];
        qualityLayer.frame = CGRectMake(12, 22, 10, 10);
        qualityLayer.contentsScale = [UIScreen mainScreen].scale;
        qualityLayer.cornerRadius = 5.0f;
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

- (void)updateAudioLevel:(float)level view:(UIView *)view isPlayView:(BOOL)isPlayView
{
    if (![view viewWithTag:10002])
    {
        UIProgressView *progressView = [UIProgressView new];
        progressView.progress = 0;
        if (isPlayView)
            progressView.progressTintColor = [UIColor greenColor];
        progressView.tag = 10002;
        progressView.translatesAutoresizingMaskIntoConstraints = NO;
        [view addSubview:progressView];
        
        [view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-(10)-[progressView(==50)]" options:0 metrics:nil views:NSDictionaryOfVariableBindings(progressView)]];
        [view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:[progressView(==2)]-(20)-|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(progressView)]];
    }

    if (level < 0)
        level = 0;
    
    UIProgressView *progressView = (UIProgressView *)[view viewWithTag:10002];
    [progressView setProgress:level/ 100.0 animated:YES];
}

- (void)getCaptureAudioLevel:(NSTimer *)timer
{
    NSDictionary *userInfo = timer.userInfo;
    if (userInfo == nil)
        return;
    
    UIView *publishView = userInfo[@"view"];
    if (publishView == nil)
        return;
    
    float level = [getZegoAV_ShareInstance() getCaptureSoundLevel];
    [self updateAudioLevel:level view:publishView isPlayView:NO];
}

- (void)startCaptureAudioLevel:(UIView *)publishView
{
    [self stopCaptureAudioLevel];
    
    self.captureTimer = [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(getCaptureAudioLevel:) userInfo:@{@"view":publishView} repeats:YES];
}

- (void)stopCaptureAudioLevel
{
    if (self.captureTimer)
    {
        [self.captureTimer invalidate];
        self.captureTimer = nil;
    }
}

- (void)getPlayAudioLevel:(NSTimer *)timer
{
    NSDictionary *userInfo = timer.userInfo;
    if (userInfo == nil)
        return;
    
    UIView *playView = userInfo[@"view"];
    int index = [userInfo[@"index"] intValue];
    if (playView == nil)
        return;
    
    CGFloat level = [getZegoAV_ShareInstance() getRemoteSoundLevel:index];
    [self updateAudioLevel:level view:playView isPlayView:YES];
}

- (void)startPlayAudioLevel:(UIView *)playView index:(int)index
{
    [self stopPlayAudioLevel:index];
    
    NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(getPlayAudioLevel:) userInfo:@{@"view":playView, @"index":@(index)} repeats:YES];
    
    [self.playTimerDictionary setObject:timer forKey:@(index)];
}

- (void)stopPlayAudioLevel:(int)index
{
    NSTimer *timer = self.playTimerDictionary[@(index)];
    if (timer)
    {
        [self.playTimerDictionary removeObjectForKey:@(index)];
        [timer invalidate];
        timer = nil;
    }
}

@end
