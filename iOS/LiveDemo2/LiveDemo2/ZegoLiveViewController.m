//
//  ZegoLiveViewController.m
//  LiveDemo2
//
//  Created by Randy Qiu on 4/10/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ZegoLiveViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettingViewController.h"

#import <CoreTelephony/CTCallCenter.h>
#import <CoreTelephony/CTCall.h>

@interface ZegoDemoAnchorCongif : NSObject

@property BOOL enableMic;
@property BOOL useFrontCamera;
@property NSInteger beautifyFeature;
@property NSInteger filterIndex;

@end

@implementation ZegoDemoAnchorCongif

- (instancetype)init {
    self = [super init];
    if (self) {
        _enableMic = YES;
        _useFrontCamera = YES;
        _beautifyFeature = 0;
    }
    
    return self;
}

@end


const NSString *kZegoDemoViewTypeKey  = @"type";    ///< 1 - publish view, 2 -  play view
const NSString *kZegoDemoVideoViewKey = @"view";                                                                                                                                                                                                                                                                                                                        
const NSString *kZegoDemoViewIndexKey = @"view_idx";
const NSString *kZegoDemoStreamIDKey  = @"stream_id";


@interface ZegoLiveViewController () <ZegoLiveApiDelegate>

@property (weak, nonatomic) IBOutlet UIButton *btnFrontCamera;
@property (weak, nonatomic) IBOutlet UIButton *btnEnableMic;
@property (weak, nonatomic) IBOutlet UIButton *btnJoin;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *anchorToolBoxHeightConstrait;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *statusBoxHeightConstraint;

@property (weak, nonatomic) IBOutlet UIPickerView *beautifyPicker;
@property (weak, nonatomic) IBOutlet UIPickerView *filterPicker;
@property (weak, nonatomic) IBOutlet UILabel *ipInfo;

@property (weak, nonatomic) IBOutlet UIView *bigView;
@property (weak, nonatomic) IBOutlet UIView *smallView1;
@property (weak, nonatomic) IBOutlet UIView *smallView2;

@property (weak, nonatomic) IBOutlet UILabel *liveIDField;
@property (weak, nonatomic) IBOutlet UIView *anchorToolBox;
@property (weak, nonatomic) IBOutlet UILabel *liveStatus;
@property (weak, nonatomic) IBOutlet UILabel *liveInfo;

@property (weak, nonatomic) IBOutlet UIButton *btnShowFilter;

@property (weak, nonatomic) IBOutlet UIView *audienceBox;
@property (weak, nonatomic) IBOutlet UITextField *playStreamID;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *statusBoxBottemSpaceConstraint;

@property (readonly) NSArray *beautifyList;
@property (readonly) NSArray *filterList;

@property (strong) UITapGestureRecognizer *smallView1TapGestureRecognizer;
@property (strong) UITapGestureRecognizer *smallView2TapGestureRecognizer;

@property (nonatomic) BOOL isPublishing;
@property BOOL isPreviewOn;
@property (nonatomic) BOOL isLogin;

@property NSMutableArray<NSDictionary *> *videoViewInfo;

@end

@implementation ZegoLiveViewController
{
    ZegoDemoAnchorCongif *_anchorConfig;
    CTCallCenter *_callCenter;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    _anchorConfig = [[ZegoDemoAnchorCongif alloc] init];
    [self setupAnchorToolBox];
    
    _beautifyList = @[
                      @"无美颜",
                      @"磨皮",
                      @"全屏美白",
                      @"磨皮＋全屏美白",
                      @"磨皮+皮肤美白"
                      ];
    
    _filterList = @[
                    @"无滤镜",
                    @"简洁",
                    @"黑白",
                    @"老化",
                    @"哥特",
                    @"锐色",
                    @"淡雅",
                    @"酒红",
                    @"青柠",
                    @"浪漫",
                    @"光晕",
                    @"蓝调",
                    @"梦幻",
                    @"夜色"
                    ];
    
    if (!_callCenter) {
        _callCenter = [[CTCallCenter alloc] init];
    }
    
    // 监听电话事件，电话来时关闭直播
    __weak ZegoLiveViewController *weakSelf = self;
    _callCenter.callEventHandler = ^(CTCall* call) {
        ZegoLiveViewController *strongSelf = weakSelf;
        [strongSelf handlePhoneCallEvent:call];
    };
    
    [self setupLiveKit];
    
    if (self.isLogin) {
        assert(false);
        return;
    }
    
    [self updateImageProcessInfo];
    
    bool ret = false;
    
    ZegoUser *user = [self userInfo];
    ret = [getZegoAV_ShareInstance() loginChannel:self.liveChannel user:user];
    
    assert(ret);
    NSLog(@"%s, ret: %d", __func__, ret);
    
    self.smallView1.hidden = YES;
    self.smallView2.hidden = YES;
    _videoViewInfo = [NSMutableArray array];
    self.anchorToolBox.hidden = YES;
 
    [self toggleStatusBox:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillDismiss:) name:UIKeyboardWillHideNotification object:nil];
    
    
    [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    
    if (!self.smallView1TapGestureRecognizer) {
        self.smallView1TapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSmallViewTap:)];
    }

    if (!self.smallView2TapGestureRecognizer) {
        self.smallView2TapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSmallViewTap:)];
    }

    [self.smallView1 addGestureRecognizer:self.smallView1TapGestureRecognizer];
    [self.smallView2 addGestureRecognizer:self.smallView2TapGestureRecognizer];
}

- (void)viewWillDisappear:(BOOL)animated {
    
    _callCenter = nil;
    
    [self.smallView1 removeGestureRecognizer:self.smallView1TapGestureRecognizer];
    [self.smallView2 removeGestureRecognizer:self.smallView2TapGestureRecognizer];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
    [super viewWillDisappear:animated];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    CGFloat angle = 0;
    
    switch (self.interfaceOrientation) {
        case UIInterfaceOrientationPortrait:
//            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_0];
            break;
            
        case UIInterfaceOrientationPortraitUpsideDown:
            angle = M_PI;
//            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_180];
            break;
            
        case UIInterfaceOrientationLandscapeLeft:
            angle = M_PI_2;
//            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_90];
            break;
            
        case UIInterfaceOrientationLandscapeRight:
            angle = -M_PI_2;
//            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_270];
            break;
            
        default:
            break;
    }
    
    CGAffineTransform newTransform = CGAffineTransformMakeRotation(angle);
    [self.bigView setTransform:newTransform];
}

- (void)handlePhoneCallEvent:(CTCall *)call {
    NSLog(@"%s, state: %@", __func__, call.callState);
    // 电话来时，关闭直播
    [self leave:self];
}

-(void)handleSmallViewTap:(UIGestureRecognizer*)gestureRecognizer {
    UIView *view = nil;
    if (gestureRecognizer == self.smallView1TapGestureRecognizer) {
        view = self.smallView1;
    } else if (gestureRecognizer == self.smallView2TapGestureRecognizer) {
        view = self.smallView2;
    }
    
    [self switchVideoView:view];
}


- (void)switchVideoView:(UIView *)smallView {
    ZegoLiveApi *api = getZegoAV_ShareInstance();
    
    NSMutableDictionary *firstViewInfo = nil;
    NSMutableDictionary *secondViewInfo = nil;
    
    NSDictionary *infoToBeDeleted1 = nil;
    NSDictionary *infoToBeDeleted2 = nil;
    
    for (NSDictionary *info in self.videoViewInfo) {
        if (info[kZegoDemoVideoViewKey] == smallView) {
            infoToBeDeleted1 = info;
            firstViewInfo = [info mutableCopy];
        } else if (info[kZegoDemoVideoViewKey] == self.bigView) {
            infoToBeDeleted2 = info;
            secondViewInfo = [info mutableCopy];
        }
    }
    
    [self.videoViewInfo removeObject:infoToBeDeleted1];
    [self.videoViewInfo removeObject:infoToBeDeleted2];
    
    NSInteger firstViewType = [firstViewInfo[kZegoDemoViewTypeKey] integerValue];
    NSInteger firstViewIndex = [firstViewInfo[kZegoDemoViewIndexKey] integerValue];
    UIView *firstView = smallView;
    
    NSInteger secondViewType = [secondViewInfo[kZegoDemoViewTypeKey] integerValue];
    NSInteger secondViewIndex = [secondViewInfo[kZegoDemoViewIndexKey] integerValue];
    UIView *secondView = self.bigView;
    
    if (firstViewType == 1) {
        [api setLocalView:nil];
        [api setRemoteView:(RemoteViewIndex)secondViewIndex view:nil];
        
        [api setLocalView:secondView];
        [api setRemoteView:(RemoteViewIndex)secondViewIndex view:firstView];
    } else if (secondViewType == 1) {
        [api setLocalView:nil];
        [api setRemoteView:(RemoteViewIndex)firstViewIndex view:nil];
        
        [api setLocalView:firstView];
        [api setRemoteView:(RemoteViewIndex)firstViewIndex view:secondView];
    } else {
        [api setRemoteView:(RemoteViewIndex)firstViewIndex view:nil];
        [api setRemoteView:(RemoteViewIndex)secondViewIndex view:nil];
        
        [api setRemoteView:(RemoteViewIndex)secondViewIndex view:firstView];
        [api setRemoteView:(RemoteViewIndex)firstViewIndex view:secondView];
    }
    
    firstViewInfo[kZegoDemoVideoViewKey] = secondView;
    secondViewInfo[kZegoDemoVideoViewKey] = firstView;
    
    [self.videoViewInfo addObject:firstViewInfo];
    [self.videoViewInfo addObject:secondViewInfo];
}

- (void)setupAnchorToolBox {
    
    UIColor *hightlightedBGColor = [[UIColor alloc] initWithRed:92.0/255 green:211.0/255 blue:255.0/255 alpha:0.5];
    
    self.btnEnableMic.backgroundColor = _anchorConfig.enableMic ? hightlightedBGColor : [UIColor clearColor];
    self.btnFrontCamera.backgroundColor = _anchorConfig.useFrontCamera ? hightlightedBGColor : [UIColor clearColor];
}


- (IBAction)leave:(id)sender {
    
    if (self.isPublishing) {
        [getZegoAV_ShareInstance() stopPublishing];
    }

    [getZegoAV_ShareInstance() logoutChannel];
    
    // 主动发起的操作，正常关闭
    [ZegoSettings sharedInstance].publishingStreamID = @"";
    [ZegoSettings sharedInstance].publishingLiveChannel = @"";
    
    [self dismissViewControllerAnimated:YES completion:^{
//        releaseZegoAV_ShareInstance();
    }];
}

- (IBAction)switchCamera:(id)sender {
    _anchorConfig.useFrontCamera = !_anchorConfig.useFrontCamera;
    [getZegoAV_ShareInstance() setFrontCam:_anchorConfig.useFrontCamera];
    [self setupAnchorToolBox];
}

- (IBAction)toggleMic:(id)sender {
    _anchorConfig.enableMic = !_anchorConfig.enableMic;
    [getZegoAV_ShareInstance() enableMic:_anchorConfig.enableMic];
    [self setupAnchorToolBox];
}

- (IBAction)toggleTorch:(id)sender {
    static bool on = false;
    [getZegoAV_ShareInstance() enableTorch:on];
    on = !on;
}

- (IBAction)toggleFilterPicker:(id)sender {
    if (self.anchorToolBoxHeightConstrait.constant == 183) {
        self.anchorToolBoxHeightConstrait.constant = 48;
        self.filterPicker.hidden = YES;
        self.beautifyPicker.hidden = YES;
        [self.btnShowFilter setImage:[UIImage imageNamed:@"expand_more"] forState:UIControlStateNormal];
    } else {
        self.anchorToolBoxHeightConstrait.constant = 183;
        self.filterPicker.hidden = NO;
        self.beautifyPicker.hidden = NO;
        [self.btnShowFilter setImage:[UIImage imageNamed:@"expand_less"] forState:UIControlStateNormal];
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
    
    [self.view setNeedsDisplay];
}

- (IBAction)toggleStatusBox:(id)sender {
    if (self.statusBoxHeightConstraint.constant == 32) {
        self.statusBoxHeightConstraint.constant = 100;
        self.liveInfo.hidden = NO;
        [self updateLiveStatus];
    } else {
        self.statusBoxHeightConstraint.constant = 32;
        self.liveInfo.hidden = YES;
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
    
    [self.view setNeedsDisplay];
}

- (IBAction)stopAnchor:(id)sender {
    if (!self.isPublishing) {
        assert(false);
        return;
    }
    
    [getZegoAV_ShareInstance() stopPublishing];
}

- (void)updateLiveStatus {    
    NSMutableString *status = [NSMutableString string];
    for (NSDictionary *viewInfo in self.videoViewInfo) {
        if (status.length > 0) {
            [status appendString:@"\n"];
        }

        if ([viewInfo[kZegoDemoViewTypeKey] isEqual:@(1)]) {
            [status appendString:[NSString stringWithFormat:@"[publish]: %@", self.streamID]];
        } else {
            [status appendString:[NSString stringWithFormat:@"[play]: %@", viewInfo[kZegoDemoStreamIDKey]]];
        }
    }
    
    self.liveInfo.text = status;
}


- (NSDictionary *)videoViewInfoOfStream:(NSString *)streamID {
    __block NSDictionary *info = nil;
    [self.videoViewInfo enumerateObjectsUsingBlock:^(NSDictionary * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj[kZegoDemoStreamIDKey] isEqualToString:streamID]) {
            info = obj;
            *stop = YES;
        }
    }];
    
    return info;
}


- (IBAction)togglePublish:(id)sender {
    
    if (self.isPublishing) {
        [getZegoAV_ShareInstance() stopPublishing];
    } else if (self.videoViewInfo.count < 2) {
        [self enablePreview:YES];
        [getZegoAV_ShareInstance() setLocalViewMode:ZegoVideoViewModeScaleAspectFill];
        bool ret = [getZegoAV_ShareInstance() startPublishingWithTitle:self.liveTitle streamID:[self publishStreamID]];
        NSLog(@"%s, ret: %d", __func__, ret);
        
        self.btnJoin.enabled = NO;
    }
}

- (IBAction)playExtractStream:(id)sender {
    NSString *streamID = self.playStreamID.text;
    if (streamID.length > 0) {
        
        if ([self videoViewInfoOfStream:streamID]) {
            NSLog(@"%s, %@ is being play.", __func__, streamID);
            return;
        }

        UIView *videoView = [self availableVideoView];
        if (videoView == nil) {
            return;
        }
        
        RemoteViewIndex viewIndex = [self availablePlayIndex];
        
        [getZegoAV_ShareInstance() setRemoteView:viewIndex view:videoView];
        [getZegoAV_ShareInstance() setRemoteViewMode:viewIndex mode:ZegoVideoViewModeScaleAspectFill];
        bool ret = [getZegoAV_ShareInstance() startPlayStream:streamID viewIndex:viewIndex];
        assert(ret);
        
        if (ret) {
            videoView.hidden = NO;
            [self addVideoView:videoView type:2 viewIndex:viewIndex streamID:streamID];
        }
    }
}

- (void)handleKeyboardWillShow:(NSNotification *)notification {
    CGSize kbSize = [[notification.userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size;
    if (kbSize.height == 0) {
        return;
    }
    self.statusBoxBottemSpaceConstraint.constant = kbSize.height - self.statusBoxHeightConstraint.constant + 2;
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}


- (void)handleKeyboardWillDismiss:(NSNotification *)notification {
    self.statusBoxBottemSpaceConstraint.constant = 0;
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    return YES;
}

#pragma mark -- UIPickerViewDelegate, UIPickerViewDataSource
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

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    if (pickerView == self.beautifyPicker) {
        _anchorConfig.beautifyFeature = row;
        [getZegoAV_ShareInstance() enableBeautifying:[self rowToFeature:row]];
    } else {
        _anchorConfig.filterIndex = row;
        [getZegoAV_ShareInstance() setFilter:row];
    }
    
    [self updateImageProcessInfo];
}

-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
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

#pragma mark - ZegoLiveApiDelegate

/// \brief 发布直播成功
- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel playUrl:(NSString *)playUrl {
    NSLog(@"%s, stream: %@, url: %@", __func__, streamID, playUrl);
    self.liveStatus.text = streamID;
    self.isPublishing = YES;
    
    assert([channel isEqualToString:self.liveChannel]);
    [self.liveIDField setText:[NSString stringWithFormat:@"Channel: %@", self.liveChannel]];
    self.streamID = streamID;
    self.isLogin = YES;
    
    if (self.liveType == 1) {
        // 作为主播，记录当前的发布信息
        [ZegoSettings sharedInstance].publishingStreamID = streamID;
        [ZegoSettings sharedInstance].publishingLiveChannel = self.liveChannel;
    }
    
    self.btnJoin.enabled = YES;
    
#if TARGET_OS_SIMULATOR
    // testing
//    dispatch_async(dispatch_get_main_queue(), ^{
//        self.playStreamID.text = streamID;
//        [self playExtractStream:self];
//    });
#endif
}

/// \brief 发布直播失败
/// \param err 1 异常结束，2 正常结束
- (void)onPublishStop:(uint32)err stream:(NSString *)streamID channel:(NSString *)channel {
    NSLog(@"%s, stream: %@, err: %u", __func__, streamID, err);
    
    self.isPublishing = NO;
    self.liveStatus.text = [NSString stringWithFormat:@"%@ Stop", streamID];
    
    self.btnJoin.enabled = YES;
}

/// \brief 获取流信息结果
/// \param err 0 成功，进一步等待流信息更新，否则出错
- (void)onLoginChannel:(NSString *)channel error:(uint32)err {
    NSLog(@"%s, err: %u", __func__, err);
    if (err == 0) {
        self.isLogin = YES;
        [self.liveIDField setText:[NSString stringWithFormat:@"Channel: %@", channel]];
        
        if (self.liveType == 1) {
            if (!self.isPublishing) {
                ZegoLiveApi *api = getZegoAV_ShareInstance();
                
                int ret = [api setAVConfig:[ZegoSettings sharedInstance].currentConfig];
                assert(ret == 0);
                
                bool b = [api setFrontCam:_anchorConfig.useFrontCamera];
                assert(b);
                
                b = [api enableMic:_anchorConfig.enableMic];
                assert(b);
                
                b = [api enableBeautifying:[self rowToFeature:_anchorConfig.beautifyFeature]];
                assert(b);
                
                b = [api setFilter:(ZegoFilter)_anchorConfig.filterIndex];
                assert(b);
                
                [self enablePreview:YES];
                [getZegoAV_ShareInstance() setLocalViewMode:ZegoVideoViewModeScaleAspectFill];
                b = [getZegoAV_ShareInstance() startPublishingWithTitle:self.liveTitle streamID:[self publishStreamID]];
                
                assert(b);
                NSLog(@"%s, ret: %d", __func__, ret);
            }
            self.anchorToolBox.hidden = NO;
        } else if (_liveType == 2) {
            // wait for play info update
        }

    } else {
        [self.liveIDField setText:[NSString stringWithFormat:@"Login failed: %u!", err]];
    }
}

/// \brief 观看直播成功
/// \param streamID 直播流的唯一标识
- (void)onPlaySucc:(NSString *)streamID channel:(NSString *)channel {
    NSLog(@"%s, stream: %@", __func__, streamID);
    self.liveStatus.text = [NSString stringWithFormat:@"Play %@ Started.", streamID];
    [self updateLiveStatus];
}

/// \brief 观看直播失败
/// \param err 1 正常结束, 非 1 异常结束
/// \param streamID 直播流的唯一标识
- (void)onPlayStop:(uint32)err streamID:(NSString *)streamID channel:(NSString *)channel {
    NSLog(@"%s, err: %u, stream: %@", __func__, err, streamID);
    
    if (err == 1) {
        self.liveStatus.text = [NSString stringWithFormat:@"Stream %@ stopped.", streamID];
    } else {
        self.liveStatus.text = [NSString stringWithFormat:@"Play %@ err(%u).", streamID, err];
    }
    
    NSDictionary *viewInfo = [self videoViewInfoOfStream:streamID];
    
    if (viewInfo) {
        UIView *videoView = viewInfo[kZegoDemoVideoViewKey];
        if (videoView != self.bigView) {
            videoView.hidden = YES;
        }
        [self.videoViewInfo removeObject:viewInfo];
        NSLog(@"%s, remove video view info", __func__);
    }
    
    [self updateLiveStatus];
}

/// \brief 视频的宽度和高度变化通知,startPlay后，如果视频宽度或者高度发生变化(首次的值也会)，则收到该通知
/// \param streamID 流的唯一标识
/// \param width 宽
/// \param height 高
- (void)onVideoSizeChanged:(NSString *)streamID width:(uint32)width height:(uint32)height {
    NSLog(@"%s, stream: %@, width: %u, height: %u", __func__, streamID, width, height);
}

/// \brief 采集视频的宽度和高度变化通知
/// \param width 宽
/// \param height 高
- (void)onCaptureVideoSizeChangedToWidth:(uint32)width height:(uint32)height {
    NSLog(@"%s, width: %u, height: %u", __func__, width, height);
}

/// \brief 截取观看直播 view 图像结果
/// \param img 图像数据
- (void)onTakeRemoteViewSnapshot:(CGImageRef)img view:(RemoteViewIndex)index {
    NSLog(@"%s", __func__);
}

/// \brief 截取本地预览视频 view 图像结果
/// \param img 图像数据
- (void)onTakeLocalViewSnapshot:(CGImageRef)img {
    NSLog(@"%s", __func__);
}

#pragma mark - Helper
- (ZegoUser *)userInfo {
    ZegoUser *user = [ZegoUser new];
    user.userID = [ZegoSettings sharedInstance].userID;
    user.userName = [ZegoSettings sharedInstance].userName;
    
    return user;
}

- (NSString *)publishStreamID {
    // 使用特殊的方式获取流ID，发布产品中应该尽量保证 streamID 不重复
    NSString *streamID = nil;
    ZegoUser *user = [self userInfo];
    if (user.userID.length > 4) {
        streamID = [user.userID substringFromIndex:user.userID.length - 4];
    } else {
        streamID = user.userID;
    }
    return streamID;
}

- (void)setupLiveKit {
    [getZegoAV_ShareInstance() setDelegate:self];
}


- (void)setIsPublishing:(BOOL)isPublishing {
    _isPublishing = isPublishing;
    [self enablePreview:isPublishing];
    if (_isPublishing) {
        [self.btnJoin setImage:[UIImage imageNamed:@"ic_pause"] forState:UIControlStateNormal];
    } else {
        [self.btnJoin setImage:nil forState:UIControlStateNormal];
    }
    
    self.anchorToolBox.hidden = !_isPublishing;
    [self updateLiveStatus];
}

- (void)enablePreview:(BOOL)enable {
    if (enable == self.isPreviewOn) {
        return;
    }
    
    if (self.isPreviewOn) {
        for (NSDictionary *info in self.videoViewInfo) {
            if ([info[kZegoDemoViewTypeKey] integerValue] == 1) {
                
                UIView *videoView = info[kZegoDemoVideoViewKey];
                if (videoView != self.bigView) {
                    videoView.hidden = YES;
                }
                
                [self.videoViewInfo removeObject:info];
                break;  // break after modifying
            }
        }
        [getZegoAV_ShareInstance() setLocalView:nil];
        [getZegoAV_ShareInstance() stopPreview];
    } else {
        UIView *v = [self availableVideoView];
        if (!v) {
            return;
        }
        
        [self addVideoView:v type:1 viewIndex:0 streamID:nil];
        [getZegoAV_ShareInstance() setLocalView:v];
        [getZegoAV_ShareInstance() startPreview];
        v.hidden = NO;
    }
    
    self.isPreviewOn = enable;
    [self.view endEditing:YES];
}

- (UIView *)availableVideoView {
    if (self.videoViewInfo.count > 2) {
        return nil;
    }
    
    bool foundBigView = false;
    bool foundSmallView1 = false;
    bool foundSmallView2 = false;
    
    for (NSDictionary *info in self.videoViewInfo) {
        UIView *view = info[kZegoDemoVideoViewKey];
        if (view == self.bigView) {
            foundBigView = true;
        } else if (view == self.smallView1) {
            foundSmallView1 = true;
        } else if (view == self.smallView2) {
            foundSmallView2 = true;
        }
    }
    
    if (!foundBigView) {
        return self.bigView;
    } else if (!foundSmallView1) {
        return self.smallView1;
    } else if (!foundSmallView2) {
        return self.smallView2;
    }
    
    assert(false);
    
    return nil;
}

- (RemoteViewIndex)availablePlayIndex {
    bool foundFirst = false;
    bool foundSecond = false;
    bool foundThird = false;
    
    for (NSDictionary *info in self.videoViewInfo) {
        RemoteViewIndex index = (RemoteViewIndex)[info[kZegoDemoViewIndexKey] integerValue];
        if (index == RemoteViewIndex_First) {
            foundFirst = true;
        } else if (index == RemoteViewIndex_Second) {
            foundSecond = true;
        } else if (index == RemoteViewIndex_Third) {
            foundThird = true;
        }
    }
    
    if (!foundFirst) {
        return RemoteViewIndex_First;
    } else if (!foundSecond) {
        return RemoteViewIndex_Second;
    } else if (!foundThird) {
        return RemoteViewIndex_Third;
    }
    
    return -1;
}

- (void)addVideoView:(UIView *)view type:(NSInteger)type viewIndex:(NSInteger)idx streamID:(NSString *)streamID {
    assert(view != nil);
    
    if (!view || self.videoViewInfo.count > 2) {
        return;
    }
    
    if (type == 1) {
        [self.videoViewInfo addObject:@{
                                        kZegoDemoViewTypeKey: @(type),
                                        kZegoDemoVideoViewKey: view
                                        }];
    } else if (type == 2) {
        assert(streamID != nil);
        if (streamID == nil) {
            return;
        }
        
        [self.videoViewInfo addObject:@{
                                        kZegoDemoViewTypeKey: @(type),
                                        kZegoDemoVideoViewKey: view,
                                        kZegoDemoViewIndexKey: @(idx),
                                        kZegoDemoStreamIDKey: streamID
                                        }];
    }
}

- (void)updateImageProcessInfo {
    [self.ipInfo setText:[NSString stringWithFormat:@"[%@] [%@]", self.beautifyList[_anchorConfig.beautifyFeature], self.filterList[_anchorConfig.filterIndex]]];
}

- (int)rowToFeature:(NSInteger)row {
    int feature = ZEGO_BEAUTIFY_NONE;
    switch (row) {
        case 1:
            feature = ZEGO_BEAUTIFY_POLISH;
            break;
        case 2:
            feature = ZEGO_BEAUTIFY_WHITEN;
            break;
        case 3:
            feature = ZEGO_BEAUTIFY_POLISH | ZEGO_BEAUTIFY_WHITEN;
            break;
        case 4:
            feature = ZEGO_BEAUTIFY_POLISH | ZEGO_BEAUTIFY_SKINWHITEN;
            break;
        default:
            break;
    }
    
    return feature;
}

@end
