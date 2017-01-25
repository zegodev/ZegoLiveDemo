//
//  ZegoMixStreamViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/9/9.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoMixStreamAnchorViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"
#import "ZegoAnchorOptionViewController.h"
#import "ZegoChatCommand.h"
#import "ZegoStreamInfo.h"
#import "ZegoLogTableViewController.h"

@interface ZegoMixStreamAnchorViewController () <ZegoLiveApiDelegate, BizRoomStreamDelegate, UIAlertViewDelegate>

//IBOutlet
@property (weak, nonatomic) IBOutlet UIView *playViewContainer;
@property (weak, nonatomic) IBOutlet UIButton *optionButton;
@property (weak, nonatomic) IBOutlet UIButton *stopPublishButton;
@property (weak, nonatomic) IBOutlet UIButton *mutedButton;
@property (weak, nonatomic) IBOutlet UIButton *mixStreamButton;

//登录房间成功后，拼接出liveChannel
@property (nonatomic, copy) NSString *liveChannel;
//创建stream后，server返回的streamID(当前直播的streamID)
@property (nonatomic, copy) NSString *streamID;

//正在观看房间里的其他直播
@property (nonatomic, assign) BOOL isPlaying;

//正在播放的streamList
@property (nonatomic, strong) NSMutableArray *playStreamList;

@property (nonatomic, strong) NSMutableDictionary *viewContainersDict;
@property (nonatomic, strong) NSMutableDictionary *viewIndexDict;

@property (nonatomic, assign) BOOL isPublishing;

@property (nonatomic, strong) UIColor *defaultButtonColor;
@property (nonatomic, strong) UIColor *disableButtonColor;

@property (nonatomic, assign) BOOL isTryingMixStream;

@property (nonatomic, assign) NSString *afterMixStreamID;

@property (nonatomic, copy) NSString *mixMagicNumber;
@property (nonatomic, strong) ZegoUser *mixRequestUser;
@property (nonatomic, strong) NSMutableArray<ZegoMixStreamInfo*> *mixStreamConfig;

@property (nonatomic, copy) NSString *sharedHls;
@property (nonatomic, copy) NSString *sharedRtmp;
@property (nonatomic, copy) NSString *bizToken;
@property (nonatomic, copy) NSString *bizID;

@property (nonatomic, assign) UIInterfaceOrientation orientation;

@property (nonatomic, strong) NSArray *mixPlayStreamList;

@end

@implementation ZegoMixStreamAnchorViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self setupLiveKit];
    [self loginChatRoom];
    
    _viewContainersDict = [[NSMutableDictionary alloc] initWithCapacity:self.maxStreamCount];
    _viewIndexDict = [[NSMutableDictionary alloc] initWithCapacity:self.maxStreamCount];
    _playStreamList = [[NSMutableArray alloc] init];
    _mixStreamConfig = [[NSMutableArray alloc] init];
//    _mixPlayStreamList = [[NSMutableArray alloc] init];
    
    self.stopPublishButton.enabled = NO;
    
    self.mutedButton.enabled = NO;
    self.defaultButtonColor = [self.mutedButton titleColorForState:UIControlStateNormal];
    self.disableButtonColor = [self.mutedButton titleColorForState:UIControlStateDisabled];
    
    self.orientation = [UIApplication sharedApplication].statusBarOrientation;
    
    if (self.publishView)
    {
        [self updatePublishView:self.publishView];
    }
    
    self.mixStreamButton.enabled = NO;
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

- (void)tryMixStream
{
    self.isTryingMixStream = YES;
    
    [getZegoAV_ShareInstance() stopPublishing];
}

#pragma mark ZegoChatRoom Kit
- (void)loginChatRoom
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    NSString *userName = [NSString stringWithFormat:@"#s-%@", user.userName];
    [getBizRoomInstance() loginLiveRoom:user.userID userName:userName bizToken:0 bizID:0 isPublicRoom:YES];
    
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"开始登录房间", nil)]];
}

- (void)createStream:(NSString *)preferredStreamID
{
    [getBizRoomInstance() cteateStreamInRoom:self.liveTitle preferredStreamID:preferredStreamID isPublicRoom:YES];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流", nil)];
    [self addLogString:logString];
}

#pragma mark audio session
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
    if (self.afterMixStreamID)
    {
        [self reportStreamAction:NO streamID:self.afterMixStreamID];
        [self removeStreamViewContainer:self.afterMixStreamID];
    }
    
    self.publishView = nil;
    
    if (self.isPlaying)
    {
        for (ZegoStreamInfo *info in self.playStreamList)
        {
            [getZegoAV_ShareInstance() stopPlayStream:info.streamID];
            [self removeStreamViewContainer:info.streamID];
        }
    }
    
    [self.viewContainersDict removeAllObjects];
    [self.viewIndexDict removeAllObjects];
    
    self.isPublishing = NO;
    self.isPlaying = NO;
}

- (IBAction)onClosePublish:(id)sender
{
    
    if (self.afterMixStreamID.length > 0)
    {
        // 正在混流，退出时关闭
        [getZegoAV_ShareInstance() updateMixStreamConfig:nil];
    }
    
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
/*
- (IBAction)magic:(id)sender
{
    if (self.afterMixStreamID.length > 0)
    {
        BOOL isPlaying = NO;
        for (ZegoStreamInfo *info in self.playStreamList)
        {
            if ([info.streamID isEqualToString:self.streamID])
            {
                isPlaying = YES;
                break;
            }
        }
        
        if (isPlaying)
        {
            [getZegoAV_ShareInstance() stopPlayStream:self.streamID];
            [self removeStreamViewContainer:self.streamID];
            [self removeStreamInfo:self.streamID];
        }
        else
        {
            ZegoStreamInfo *streamInfo = [ZegoStreamInfo getStreamInfo:@{kRoomStreamIDKey: self.streamID}];
            [self.playStreamList addObject:streamInfo];
            [self createPlayStream:self.streamID];
        }
    }
}
*/

- (IBAction)onPlayMixStream:(id)sender
{
    if (!self.isPublishing)
        return;
    
    if (self.afterMixStreamID.length == 0)
        return;
    
    if ([[self.mixStreamButton currentTitle] isEqualToString:NSLocalizedString(@"播放混流", nil)])
    {
        self.mixPlayStreamList = [NSArray arrayWithArray:self.playStreamList];
        
        for (ZegoStreamInfo *info in self.playStreamList)
        {
            if (![info.streamID isEqualToString:self.streamID])
            {
                [getZegoAV_ShareInstance() stopPlayStream:info.streamID];
                [self removeStreamViewContainer:info.streamID];
                [self removeStreamInfo:info.streamID];
            }
        }
        
        ZegoStreamInfo *streamInfo = [ZegoStreamInfo getStreamInfo:@{kRoomStreamIDKey: self.streamID}];
        [self.playStreamList addObject:streamInfo];
        [self createPlayStream:self.streamID];
        
        [self.mixStreamButton setTitle:NSLocalizedString(@"播放单流", nil) forState:UIControlStateNormal];
    }
    else
    {
        [getZegoAV_ShareInstance() stopPlayStream:self.streamID];
        [self removeStreamViewContainer:self.streamID];
        [self removeStreamInfo:self.streamID];
        
        [self.playStreamList addObjectsFromArray:self.mixPlayStreamList];
        
        for (ZegoStreamInfo *info in self.playStreamList)
        {
            if (![info.streamID isEqualToString:self.streamID])
                [self createPlayStream:info.streamID];
        }
        
        [self.mixStreamButton setTitle:NSLocalizedString(@"播放混流", nil) forState:UIControlStateNormal];
    }
}

- (IBAction)onShare:(id)sender
{
    if (self.sharedHls.length == 0)
        return;
    
    NSString *streamID = self.streamID;
    if (self.afterMixStreamID)
        streamID = self.afterMixStreamID;
    
    [self shareToQQ:self.sharedHls rtmp:self.sharedRtmp bizToken:self.bizToken bizID:self.bizID streamID:streamID];
}

#pragma mark publish view
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

    bool b = [getZegoAV_ShareInstance() startPublishingWithTitle:self.liveTitle streamID:self.streamID];
    assert(b);
    NSLog(@"%s, ret: %d", __func__, b);
    
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"开始直播，流ID:%@", nil), self.streamID]];
    
    if (self.playStreamList.count != 0)
    {
        for (ZegoStreamInfo *info in self.playStreamList)
        {
            NSString *streamID = info.streamID;
            if (self.viewContainersDict[streamID] != nil)
                continue;
            
            [self createPlayStream:streamID];
            
            NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"继续播放之前的流, 流ID:%@", nil), streamID];
            [self addLogString:logString];
        }
    }
}

/// \brief 发布直播成功
- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel streamInfo:(NSDictionary *)info
{
    NSLog(@"%s, stream: %@", __func__, streamID);
    
    self.isPublishing = YES;
    self.stopPublishButton.enabled = YES;
    [self.stopPublishButton setTitle:NSLocalizedString(@"停止直播", nil) forState:UIControlStateNormal];
    
    if (self.isTryingMixStream)
    {
        [self reportStreamAction:YES streamID:self.afterMixStreamID];
        [self sendRequestPublishRespond:YES magicNumber:self.mixMagicNumber requestPublisher:self.mixRequestUser];
        self.isTryingMixStream = NO;
        self.mixRequestUser = nil;
        self.mixMagicNumber = nil;
    }
    else
    {
        [self reportStreamAction:YES streamID:self.streamID];
        
        self.sharedHls = [info[kZegoHlsUrlListKey] firstObject];
        self.sharedRtmp = [info[kZegoRtmpUrlListKey] firstObject];
    }
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"发布直播成功,流ID:%@", nil), streamID];
    [self addLogString:logString];
    
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
    
    if (self.isTryingMixStream)
    {
        [self reportStreamAction:NO streamID:streamID];
        
        [self.viewContainersDict removeObjectForKey:streamID];
        [self.viewIndexDict removeObjectForKey:streamID];
        [self createStream:nil];
    }
    else
    {
        [self reportStreamAction:NO streamID:streamID];
        [self removeStreamViewContainer:streamID];
        self.publishView = nil;
    }
    
    [self stopCaptureAudioLevel];
}

- (void)onPlaySucc:(NSString *)streamID channel:(NSString *)channel
{
    NSLog(@"%s, streamID:%@", __func__, streamID);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"播放流成功, 流ID:%@", nil), streamID];
    [self addLogString:logString];
    
    UIView *playView = self.viewContainersDict[streamID];
    int index = [self.viewIndexDict[streamID] intValue];
    if (playView)
    {
        [self startPlayAudioLevel:playView index:index];
    }
}

- (void)onPlayStop:(uint32)err streamID:(NSString *)streamID channel:(NSString *)channel
{
    NSLog(@"%s, streamID:%@", __func__, streamID);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"播放流失败, 流ID:%@,  error:%d", nil), streamID, err];
    [self addLogString:logString];
    
    UIView *playView = self.viewContainersDict[streamID];
    int index = [self.viewIndexDict[streamID] intValue];
    if (playView)
    {
        [self stopPlayAudioLevel:index];
    }
}

- (void)onPublishQualityUpdate:(int)quality stream:(NSString *)streamID videoFPS:(double)fps videoBitrate:(double)kbs
{
    UIView *view = self.viewContainersDict[streamID];
    if (view)
        [self updateQuality:quality view:view];
    
    self.lastPublishFPS = fps;
    self.lastPublishKBS = kbs;
}

- (void)onPlayQualityUpdate:(int)quality stream:(NSString *)streamID videoFPS:(double)fps videoBitrate:(double)kbs
{
    UIView *view = self.viewContainersDict[streamID];
    if (view)
        [self updateQuality:quality view:view];
    
    self.lastPlayFPS = fps;
    self.lastPlayKBS = kbs;
}

- (void)onAuxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount
{
    [self auxCallback:pData dataLen:pDataLen sampleRate:pSampleRate channelCount:pChannelCount];
}

- (void)onVideoSizeChanged:(NSString *)streamID width:(uint32)width height:(uint32)height
{
    if (![self isStreamIDExist:streamID])
        return;
    
    int index = [self.viewIndexDict[streamID] intValue];
    UIView *view = self.viewContainersDict[streamID];
    if (view == nil)
        return;
    
    if (width > height && view.frame.size.width < view.frame.size.height)
    {
        [getZegoAV_ShareInstance() setRemoteViewMode:index mode:ZegoVideoViewModeScaleAspectFit];
    }
}

- (void)onMixStreamConfigUpdate:(int)errorCode mixStream:(NSString *)mixStreamID streamInfo:(NSDictionary *)info
{
    NSLog(@"%@, errorCode %d", mixStreamID, errorCode);
    
    NSString *rtmpUrl = [info[kZegoRtmpUrlListKey] firstObject];
    NSString *hlsUrl = [info[kZegoHlsUrlListKey] firstObject];
    
    NSLog(@"rtmp %@", rtmpUrl);
    self.sharedHls = hlsUrl;
    self.sharedRtmp = rtmpUrl;
    
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"混流结果: %d", nil), errorCode]];
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"混流rtmp: %@", nil), rtmpUrl]];
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"混流hls: %@", nil), hlsUrl]];
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
        
        if (self.isTryingMixStream)
        {
            if (self.publishView == nil)
            {
                self.publishView = [self createPublishView];
                if (self.publishView)
                {
                    [self setAnchorConfig:self.publishView];
                    [getZegoAV_ShareInstance() startPreview];
                }
            }
            
            self.viewContainersDict[streamID] = self.publishView;
            CGSize videoSize = [ZegoSettings sharedInstance].currentConfig.videoEncodeResolution;

            [getZegoAV_ShareInstance() startPublishingWithTitle:self.liveTitle streamID:streamID mixStreamID:self.streamID mixVideoSize:videoSize flag:ZEGOAPI_MIX_STREAM];
            self.afterMixStreamID = streamID;
            self.mixStreamButton.enabled = YES;
        }
        else
        {
            self.streamID = streamID;
            [self loginChannel];
        }
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流失败", nil)];
        [self addLogString:logString];
    }
}

- (void)onStreamUpdate:(NSArray<NSDictionary *> *)streamList flag:(int)flag isPublicRoom:(bool)isPublicRoom
{
    if (streamList.count == 0)
    {
        return;
    }
    
    if (flag == 0)
        [self onStreamUpdateForAdd:streamList];
    else if (flag == 1)
        [self onStreamUpdateForDelete:streamList];
}

- (void)onReceiveMessage:(NSData *)content messageType:(int)type isPublicRoom:(bool)isPublicRoom
{
    //text
    if (type != 2)
        return;
    
    NSDictionary *receiveInfo = [ZegoChatCommand getRequestPublishRsp:content];
    if (receiveInfo == nil)
        return;
    
    NSString *command = receiveInfo[kZEGO_CHAT_CMD];
    if ([command isEqualToString:kZEGO_CHAT_REQUEST_PUBLISH])
    {
        [self onReceivePublishRequest:receiveInfo];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"有用户请求连麦", nil)];
        [self addLogString:logString];
    }
//    else if ([command isEqualToString:kZEGO_CHAT_RESPOND_PUBLISH])
//    {
//        //有人已经操作请求上台了，弹框消失
//        [self dismissAlertView:receiveInfo[kZEGO_CHAT_MAGIC]];
//    }
}

- (BOOL)shouldShowPublishAlert
{
    if (self.viewContainersDict.count < self.maxStreamCount)
        return YES;
    
    return NO;
}

#pragma mark request mixstream
- (void)requestPublishAlert:(ZegoUser *)requestUser magicNumber:(NSString *)magicNumber
{
    //已经开始混流走原来逻辑
    if (self.afterMixStreamID.length != 0)
        return [super requestPublishAlert:requestUser magicNumber:magicNumber];
    
    if (self.presentedViewController)
        [self.presentedViewController dismissViewControllerAnimated:YES completion:nil];
    
    //在与第二条流混流成功前拒绝其他连麦请求
    if (self.mixMagicNumber != nil)
        [self sendRequestPublishRespond:NO magicNumber:magicNumber requestPublisher:requestUser];
    
    NSString *message = [NSString stringWithFormat:NSLocalizedString(@"%@ 请求直播，是否允许", nil), requestUser.userName];
    if ([self isDeviceiOS7])
    {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:message delegate:self cancelButtonTitle:NSLocalizedString(@"拒绝", nil) otherButtonTitles:NSLocalizedString(@"允许", nil), nil];
        
        self.mixMagicNumber = magicNumber;
        self.mixRequestUser = requestUser;
        
        [alertView show];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:message preferredStyle:UIAlertControllerStyleAlert];
        
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"拒绝", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            [self sendRequestPublishRespond:NO magicNumber:magicNumber requestPublisher:requestUser];
        }];
        UIAlertAction *okAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"允许", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self tryMixStream];
            self.mixMagicNumber = magicNumber;
            self.mixRequestUser = requestUser;
        }];
        
        [alertController addAction:cancelAction];
        [alertController addAction:okAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    }
}

#pragma mark stream update & delete
- (void)onStreamUpdateForAdd:(NSArray<NSDictionary *> *)streamList
{
    for (NSDictionary *dic in streamList)
    {
        NSString *streamID = dic[kRoomStreamIDKey];
        if (streamID.length == 0)
            continue;
        
        if ([self isStreamIDExist:streamID])
        {
            continue;
        }
        
        ZegoStreamInfo *streamInfo = [ZegoStreamInfo getStreamInfo:dic];
        [self.playStreamList addObject:streamInfo];
        [self createPlayStream:streamID];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"新增一条流, 流ID:%@", nil), streamID];
        [self addLogString:logString];
        
        //更新布局
        CGFloat height = [[ZegoSettings sharedInstance] currentConfig].videoEncodeResolution.height;
        CGFloat width = [[ZegoSettings sharedInstance] currentConfig].videoEncodeResolution.width;
        if (self.mixStreamConfig.count == 0)
        {
            ZegoMixStreamInfo *info = [[ZegoMixStreamInfo alloc] init];
            info.streamID = self.afterMixStreamID;
            info.top = 0;
            info.left = 0;
            info.bottom = height;
            info.right = width;
            [self.mixStreamConfig addObject:info];
        }
        
        if (self.mixStreamConfig.count == 1)
        {
            ZegoMixStreamInfo *info = [[ZegoMixStreamInfo alloc] init];
            info.streamID = streamID;
            info.top = ceilf(height * 2 / 3);
            info.left = ceilf(width * 2 / 3);
            info.bottom = height;
            info.right = width;
            [self.mixStreamConfig addObject:info];
        }
        else if (self.mixStreamConfig.count == 2)
        {
            ZegoMixStreamInfo *info = [[ZegoMixStreamInfo alloc] init];
            info.streamID = streamID;
            info.top = ceilf(height * 2 / 3);
            info.left = 0;
            info.bottom = height;
            info.right = ceilf(width / 3);
            [self.mixStreamConfig addObject:info];
        }
        
        [getZegoAV_ShareInstance() updateMixStreamConfig:self.mixStreamConfig];
        
    }
    
    self.mutedButton.enabled = YES;
}

- (void)onStreamUpdateForDelete:(NSArray<NSDictionary *> *)streamList
{
    for (NSDictionary *dic in streamList)
    {
        NSString *streamID = dic[kRoomStreamIDKey];
        if (![self isStreamIDExist:streamID])
            continue;
        
        [getZegoAV_ShareInstance() stopPlayStream:streamID];
        [self removeStreamViewContainer:streamID];
        [self removeStreamInfo:streamID];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"删除一条流, 流ID:%@", nil), streamID];
        [self addLogString:logString];
        
        for (ZegoMixStreamInfo *info in self.mixStreamConfig)
        {
            if ([info.streamID isEqualToString:streamID])
            {
                [self.mixStreamConfig removeObject:info];
                break;
            }
        }
        
        [getZegoAV_ShareInstance() updateMixStreamConfig:self.mixStreamConfig];
        
    }
    
    if (self.playStreamList.count == 0)
    {
        self.mutedButton.enabled = NO;
        [self.mutedButton setTitleColor:self.disableButtonColor forState:UIControlStateDisabled];
    }
}

- (BOOL)isStreamIDExist:(NSString *)streamID
{
    if ([self.streamID isEqualToString:streamID])
        return YES;
    
    for (ZegoStreamInfo *info in self.playStreamList)
    {
        if ([info.streamID isEqualToString:streamID])
            return YES;
    }
    
    return NO;
}

- (void)removeStreamInfo:(NSString *)streamID
{
    NSInteger index = NSNotFound;
    for (ZegoStreamInfo *info in self.playStreamList)
    {
        if ([info.streamID isEqualToString:streamID])
        {
            index = [self.playStreamList indexOfObject:info];
            break;
        }
    }
    
    if (index != NSNotFound)
        [self.playStreamList removeObjectAtIndex:index];
}

- (int)getRemoteViewIndex
{
    int index = 0;
    for (; index < self.maxStreamCount; index++)
    {
        if ([self.viewIndexDict allKeysForObject:@(index)].count == 0)
            return index;
    }
    
    if (index == self.maxStreamCount)
        NSLog(@"cannot find indx to add view");
    
    return index;
}

- (void)createPlayStream:(NSString *)streamID
{
    if (self.viewContainersDict[streamID] != nil)
        return;
    
    UIView *playView = [self createPlayView:streamID];
    
    RemoteViewIndex index = (RemoteViewIndex)[self getRemoteViewIndex];
    self.viewIndexDict[streamID] = @(index);
    
    [getZegoAV_ShareInstance() setRemoteView:index view:playView];
    [getZegoAV_ShareInstance() setRemoteViewMode:index mode:ZegoVideoViewModeScaleAspectFill];
    bool ret = [getZegoAV_ShareInstance() startPlayStream:streamID viewIndex:index];
    assert(ret);
}

- (UIView *)createPlayView:(NSString *)streamID
{
    UIView *playView = [[UIView alloc] init];
    playView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.playViewContainer addSubview:playView];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapView:)];
    [playView addGestureRecognizer:tapGesture];
    
    BOOL bResult = [self setContainerConstraints:playView containerView:self.playViewContainer viewCount:self.viewContainersDict.count];
    if (bResult == NO)
    {
        [playView removeFromSuperview];
        return nil;
    }
    
    self.viewContainersDict[streamID] = playView;
    [self.playViewContainer bringSubviewToFront:playView];
    
    return playView;
    
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
    [self.viewIndexDict removeObjectForKey:streamID];
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

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0)
    {
        //cancel
        [self sendRequestPublishRespond:NO magicNumber:self.mixMagicNumber requestPublisher:self.mixRequestUser];
        self.mixRequestUser = nil;
        self.mixMagicNumber = nil;
    }
    else if (buttonIndex == 1)
    {
        //        [self sendRequestPublishRespond:YES magicNumber:magicNumber requestPublisher:requestUser];
        [self tryMixStream];
    }
}

@end

