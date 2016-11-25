//
//  ZegoAnchorViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoAnchorViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"
#import "ZegoAnchorOptionViewController.h"
#import "ZegoChatCommand.h"
#import "ZegoStreamInfo.h"
#import "ZegoLogTableViewController.h"

//主播流程： 进入房间->进入成功->登录channel->登录成功->开始发布

@interface ZegoAnchorViewController () <ZegoLiveApiDelegate, BizRoomStreamDelegate>

//IBOutlet
@property (weak, nonatomic) IBOutlet UIView *playViewContainer;
@property (weak, nonatomic) IBOutlet UIButton *optionButton;
@property (weak, nonatomic) IBOutlet UIButton *stopPublishButton;
@property (weak, nonatomic) IBOutlet UIButton *mutedButton;

//登录房间成功后，拼接出liveChannel
@property (nonatomic, copy) NSString *liveChannel;
//创建stream后，server返回的streamID(当前直播的streamID)
@property (nonatomic, copy) NSString *streamID;

@property (nonatomic, strong) NSMutableDictionary *viewContainersDict;

@property (nonatomic, assign) BOOL isPublishing;

@property (nonatomic, strong) UIColor *defaultButtonColor;
@property (nonatomic, strong) UIColor *disableButtonColor;

@property (nonatomic, copy) NSString *sharedHls;
@property (nonatomic, copy) NSString *sharedRtmp;
@property (nonatomic, copy) NSString *bizToken;
@property (nonatomic, copy) NSString *bizID;

@property (nonatomic, assign) UIInterfaceOrientation orientation;

@end

@implementation ZegoAnchorViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self setupLiveKit];
    [self loginChatRoom];
    
    _viewContainersDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    
    self.stopPublishButton.enabled = NO;
    
    self.mutedButton.enabled = NO;
    self.defaultButtonColor = [self.mutedButton titleColorForState:UIControlStateNormal];
    self.disableButtonColor = [self.mutedButton titleColorForState:UIControlStateDisabled];
    
    self.orientation = [UIApplication sharedApplication].statusBarOrientation;
    
    if (self.publishView)
    {
        [self updatePublishView:self.publishView];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self setIdelTimerDisable:YES];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [self setIdelTimerDisable:NO];
    [super viewWillDisappear:animated];
}

- (BOOL)shouldAutorotate
{
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    if (self.orientation == UIInterfaceOrientationPortrait)
        return UIInterfaceOrientationMaskPortrait;
    else if (self.orientation == UIInterfaceOrientationLandscapeLeft)
        return UIInterfaceOrientationMaskLandscapeLeft;
    else if (self.orientation == UIInterfaceOrientationLandscapeRight)
        return UIInterfaceOrientationMaskLandscapeRight;
    
    return UIInterfaceOrientationMaskPortrait;
}

- (void)audioSessionWasInterrupted:(NSNotification *)notification
{
    if (AVAudioSessionInterruptionTypeBegan == [notification.userInfo[AVAudioSessionInterruptionTypeKey] intValue])
    {
        [self closeAllStream];
        
        // 退出channel
        [getZegoAV_ShareInstance() logoutChannel];
        
    }
    else if(AVAudioSessionInterruptionTypeEnded == [notification.userInfo[AVAudioSessionInterruptionTypeKey] intValue])
    {
        //需要重新创建流
//        [self loginChannel];
        
        [self createStream:self.streamID];
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建断开之前相同的流", nil)];
        [self addLogString:logString];
    }
}

#pragma mark ZegoAVKit
- (void)loginChannel
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    bool ret = [getZegoAV_ShareInstance() loginChannel:self.liveChannel user:user];
    assert(ret);
    
    NSLog(@"%s, ret: %d", __func__, ret);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"Login Channel", nil)];
    [self addLogString:logString];
}

#pragma mark ZegoChatRoom Kit
- (void)loginChatRoom
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    NSString *userName = [NSString stringWithFormat:@"#d-%@", user.userName];
    [getBizRoomInstance() loginLiveRoom:user.userID userName:userName bizToken:0 bizID:0 isPublicRoom:YES];
    
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"开始登录房间", nil)]];
}

- (void)createStream:(NSString *)preferredStreamID
{
    [getBizRoomInstance() cteateStreamInRoom:self.liveTitle preferredStreamID:preferredStreamID isPublicRoom:YES];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流", nil)];
    [self addLogString:logString];
}

#pragma mark ZegoAV interface
- (void)setupLiveKit
{
    getZegoAV_ShareInstance().delegate = self;
    getBizRoomInstance().streamDelegate = self;
}

#pragma mark close publish

- (void)closeAllStream
{
    [getZegoAV_ShareInstance() stopPreview];
    [getZegoAV_ShareInstance() setLocalView:nil];
    [getZegoAV_ShareInstance() stopPublishing];
    [self reportStreamAction:NO streamID:self.streamID];
    [self removeStreamViewContainer:self.streamID];
    [self.viewContainersDict removeAllObjects];
    
    self.publishView = nil;
    self.isPublishing = NO;
}

- (IBAction)onClosePublish:(id)sender
{
    [self closeAllStream];
    
    [getZegoAV_ShareInstance() logoutChannel];
    
    [getBizRoomInstance() leaveLiveRoom:YES];
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark set muted button
- (IBAction)onMutedButton:(id)sender
{
    if (self.enableSpeaker)
    {
        self.enableSpeaker = NO;
        [self.mutedButton setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    }
    else
    {
        self.enableSpeaker = YES;
        [self.mutedButton setTitleColor:self.defaultButtonColor forState:UIControlStateNormal];
    }
}

#pragma mark show more option;
- (IBAction)onShowMoreInfo:(id)sender
{
    [self showPublishOption];
}

#pragma mark stop publish
- (IBAction)onStopPublish:(id)sender
{
    if (self.isPublishing)
    {
        //停止直播
        [getZegoAV_ShareInstance() stopPreview];
        [getZegoAV_ShareInstance() setLocalView:nil];
        [getZegoAV_ShareInstance() stopPublishing];
        
        self.stopPublishButton.enabled = NO;
    }
    else if ([[self.stopPublishButton currentTitle] isEqualToString:NSLocalizedString(@"开始直播", nil)])
    {
        [self createStream:self.streamID];
        self.stopPublishButton.enabled = NO;
    }
}

- (IBAction)onShare:(id)sender
{
    if (self.sharedHls.length == 0)
        return;
    
    [self shareToQQ:self.sharedHls rtmp:self.sharedRtmp bizToken:self.bizToken bizID:self.bizID streamID:self.streamID];
}

#pragma mark PublishView create

- (BOOL)updatePublishView:(UIView *)publishView
{
    publishView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.playViewContainer addSubview:publishView];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapView:)];
    [publishView addGestureRecognizer:tapGesture];
    
    BOOL bResult = [self setContainerConstraints:publishView containerView:self.playViewContainer viewCount:self.playViewContainer.subviews.count - 1];
    if (bResult == NO)
    {
        [publishView removeFromSuperview];
        return NO;
    }
    
    [self.playViewContainer bringSubviewToFront:publishView];
    return YES;
}

- (UIView *)createPublishView
{
    UIView *publishView = [[UIView alloc] init];
    publishView.translatesAutoresizingMaskIntoConstraints = NO;
    
    BOOL result = [self updatePublishView:publishView];
    if (result == NO)
        return nil;
    
    return publishView;
}

#pragma mark ZegoLiveApiDelegate
/// \brief 获取流信息结果
/// \param err 0 成功，进一步等待流信息更新，否则出错
- (void)onLoginChannel:(NSString *)channel error:(uint32)err
{
    NSLog(@"%s, err: %u", __func__, err);
    if (err != 0)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录channel失败, error:%d", nil), err];
        [self addLogString:logString];
        return;
    }
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录channel成功", nil)];
    [self addLogString:logString];
    
    //登录成功后配置直播参数，开始直播 创建publishView
    if (self.publishView.superview == nil)
        self.publishView = nil;
    
    if (self.publishView == nil)
    {
        self.publishView = [self createPublishView];
        if (self.publishView)
        {
            [self setAnchorConfig:self.publishView];
            [getZegoAV_ShareInstance() startPreview];
        }
    }
    
    self.viewContainersDict[self.streamID] = self.publishView;
    
    [getZegoAV_ShareInstance() enableRateControl:YES];
    [getZegoAV_ShareInstance() requireHardwareAccelerated:NO];
    bool b = [getZegoAV_ShareInstance() startPublishingWithTitle:self.liveTitle streamID:self.streamID];
    assert(b);
    NSLog(@"%s, ret: %d", __func__, b);
    
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"开始直播，流ID:%@", nil), self.streamID]];
}

/// \brief 发布直播成功
- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel streamInfo:(NSDictionary *)info
{
    NSLog(@"%s, stream: %@", __func__, streamID);
    self.isPublishing = YES;
    self.stopPublishButton.enabled = YES;
    [self.stopPublishButton setTitle:NSLocalizedString(@"停止直播", nil) forState:UIControlStateNormal];
    
    [self reportStreamAction:YES streamID:self.streamID];
    
    self.sharedHls = [info[kZegoHlsUrlListKey] firstObject];
    self.sharedRtmp = [info[kZegoRtmpUrlListKey] firstObject];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"发布直播成功,流ID:%@", nil), streamID];
    [self addLogString:logString];
    
    //开始监听声音大小
    [self startCaptureAudioLevel:self.publishView];
}

/// \brief 发布直播失败
/// \param err 1 正常结束，2 异常结束
- (void)onPublishStop:(uint32)err stream:(NSString *)streamID channel:(NSString *)channel
{
    NSLog(@"%s, stream: %@, err: %u", __func__, streamID, err);
    self.isPublishing = NO;
    self.stopPublishButton.enabled = NO;
    
    if (err == 1)
    {
        [self.stopPublishButton setTitle:NSLocalizedString(@"开始直播", nil) forState:UIControlStateNormal];
        self.stopPublishButton.enabled = YES;
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"直播结束,流ID:%@", nil), streamID];
        [self addLogString:logString];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"直播结束,流ID：%@, error:%d", nil), streamID, err];
        [self addLogString:logString];
    }
    
    [self reportStreamAction:NO streamID:streamID];
    [self removeStreamViewContainer:streamID];
    self.publishView = nil;
    
    [self stopCaptureAudioLevel];
}

- (void)onPublishQualityUpdate:(int)quality stream:(NSString *)streamID videoFPS:(double)fps videoBitrate:(double)kbs
{
    UIView *view = self.viewContainersDict[streamID];
    if (view)
        [self updateQuality:quality view:view];
    
    self.lastPublishFPS = fps;
    self.lastPublishKBS = kbs;
}

- (void)onAuxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount
{
    [self auxCallback:pData dataLen:pDataLen sampleRate:pSampleRate channelCount:pChannelCount];
}

#pragma mark ZeogStreamRoom delegate
- (void)onLoginRoom:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom
{
    NSLog(@"%s, error: %d", __func__, err);
    if (err == 0)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录房间成功. token %d, id %d", nil), bizToken, bizID];
        [self addLogString:logString];
        
        self.liveChannel = [[ZegoSettings sharedInstance] getChannelID:bizToken bizID:bizID];
        [self createStream:nil];
        self.bizID = [NSString stringWithFormat:@"%u", bizID];
        self.bizToken = [NSString stringWithFormat:@"%u", bizToken];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录房间失败. error: %d", nil), err];
        [self addLogString:logString];
    }
}

- (void)onDisconnected:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom
{
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"连接失败, error: %d", nil), err];
    [self addLogString:logString];
    
//    [self onClosePublish:nil];
}

- (void)onLeaveRoom:(int)err isPublicRoom:(bool)isPublicRoom
{
    NSLog(@"%s, error: %d", __func__, err);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"退出房间, error: %d", nil), err];
    [self addLogString:logString];
}

- (void)onStreamCreate:(NSString *)streamID url:(NSString *)url isPublicRoom:(bool)isPublicRoom
{
    if (streamID.length != 0)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流成功, streamID:%@", nil), streamID];
        [self addLogString:logString];
        
        self.streamID = streamID;
        [self loginChannel];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流失败", nil)];
        [self addLogString:logString];
    }
}

- (void)onTapView:(UIGestureRecognizer *)gestureRecognizer
{
    UIView *view = gestureRecognizer.view;
    if (view == nil)
        return;
    
    [self updateContainerConstraintsForTap:view containerView:self.playViewContainer];
}

- (void)removeStreamViewContainer:(NSString *)streamID
{
    UIView *view = self.viewContainersDict[streamID];
    if (view == nil)
        return;
    
    [self updateContainerConstraintsForRemove:view containerView:self.playViewContainer];
    
    [self.viewContainersDict removeObjectForKey:streamID];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"logSegueIdentifier"])
    {
        UINavigationController *navigationController = [segue destinationViewController];
        ZegoLogTableViewController *logViewController = (ZegoLogTableViewController *)[navigationController.viewControllers firstObject];
        logViewController.logArray = self.logArray;
    }
}

@end
