//
//  ZegoAudienceViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/27.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoMoreAudienceViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"
#import "ZegoStreamInfo.h"
#import "ZegoChatCommand.h"
#import "ZegoLogTableViewController.h"

//loginChannel->onLoginChannel->LoginInRoom->onLoginRoom

@interface ZegoMoreAudienceViewController () <BizRoomStreamDelegate, ZegoLiveApiDelegate, UIAlertViewDelegate>

@property (weak, nonatomic) IBOutlet UIView *playViewContainer;
@property (weak, nonatomic) IBOutlet UIButton *publishButton;
@property (weak, nonatomic) IBOutlet UIButton *optionButton;
@property (weak, nonatomic) IBOutlet UIButton *mutedButton;

@property (weak, nonatomic) IBOutlet UIButton *closeButton;
@property (weak, nonatomic) IBOutlet UIButton *logButton;

@property (weak, nonatomic) IBOutlet UIButton *fullscreenButton;

@property (nonatomic, strong) NSMutableArray<ZegoStreamInfo *> *streamList;

@property (nonatomic, assign) BOOL loginChannelSuccess;
@property (nonatomic, assign) BOOL loginRoomSuccess;

@property (nonatomic, assign) BOOL isPublishing;
@property (nonatomic, copy) NSString *publishTitle;
@property (nonatomic, copy) NSString *publishStreamID;

@property (nonatomic, strong) NSMutableArray *requestingArray;

@property (nonatomic, copy) NSString *liveChannel;

@property (nonatomic, strong) UIColor *defaultButtonColor;

@property (nonatomic, strong) NSMutableDictionary *viewContainersDict;
@property (nonatomic, strong) NSMutableDictionary *viewIndexDict;
@property (nonatomic, strong) NSMutableDictionary *streamID2SizeDict;
@property (nonatomic, strong) NSMutableDictionary *videoSizeDict;

@end

@implementation ZegoMoreAudienceViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.useFrontCamera = YES;
    self.enableTorch = NO;
    self.beautifyFeature = ZEGO_BEAUTIFY_NONE;
    self.filter = ZEGO_FILTER_NONE;
    self.enableMicrophone = YES;
    self.enablePreview = YES;
    self.viewMode = ZegoVideoViewModeScaleAspectFill;
    self.enableCamera = YES;
    
    _streamList = [[NSMutableArray alloc] initWithCapacity:MAX_STREAM_COUNT];
    _viewContainersDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _viewIndexDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _videoSizeDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _streamID2SizeDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    
    _requestingArray = [[NSMutableArray alloc] init];

    self.liveChannel = [[ZegoSettings sharedInstance] getChannelID:self.bizToken bizID:self.bizID];
    
    [self setupLiveKit];
    
    //room信令与channel信令并行处理
    [self loginRoom];
    [self loginChannel];
    
    [self.streamList addObjectsFromArray:self.currentStreamList];
    
    UIImage *backgroundImage = [[ZegoSettings sharedInstance] getBackgroundImage:self.view.bounds.size withText:NSLocalizedString(@"加载中", nil)];
    [self setBackgroundImage:backgroundImage playerView:self.playViewContainer];
    
    [self setButtonHidden:YES];
    
    self.publishButton.enabled = NO;
    self.optionButton.enabled = NO;
    
    self.fullscreenButton.hidden = YES;
}

- (void)setButtonHidden:(BOOL)hidden
{
    if (hidden)
    {
        self.publishButton.alpha = 0;
        self.optionButton.alpha = 0;
        self.mutedButton.alpha = 0;
    }
    else
    {
        self.publishButton.alpha = 1;
        self.optionButton.alpha = 1;
        self.mutedButton.alpha = 1;
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

- (void)setBackgroundImage:(UIImage *)image playerView:(UIView *)playerView
{
    playerView.backgroundColor = [UIColor colorWithPatternImage:image];
}

#pragma mark audiosession
- (void)audioSessionWasInterrupted:(NSNotification *)notification
{
    if (AVAudioSessionInterruptionTypeBegan == [notification.userInfo[AVAudioSessionInterruptionTypeKey] intValue])
    {
        [self clearAllStream];
        
        // 退出channel
        [getZegoAV_ShareInstance() logoutChannel];
        
    }
    else if(AVAudioSessionInterruptionTypeEnded == [notification.userInfo[AVAudioSessionInterruptionTypeKey] intValue])
    {
        // 重新登录channel
        [self loginChannel];
    }
}

#pragma mark onClose action
- (void)clearAllStream
{
    for (ZegoStreamInfo *info in self.streamList)
    {
        [getZegoAV_ShareInstance() stopPlayStream:info.streamID];
        UIView *playView = self.viewContainersDict[info.streamID];
        if (playView)
        {
            [self updateContainerConstraintsForRemove:playView containerView:self.playViewContainer];
            [self.viewContainersDict removeObjectForKey:info.streamID];
            [self.viewIndexDict removeObjectForKey:info.streamID];
        }
    }
    
    [self.viewIndexDict removeAllObjects];
    [self.viewContainersDict removeAllObjects];
    
    if (self.isPublishing)
    {
        [getZegoAV_ShareInstance() stopPreview];
        [getZegoAV_ShareInstance() setLocalView:nil];
        [getZegoAV_ShareInstance() stopPublishing];
        [self removeStreamViewContainer:self.publishStreamID];
        [self reportStreamAction:NO streamID:self.publishStreamID];
    }
}

- (IBAction)onCloseAudience:(id)sender
{
    [self setBackgroundImage:nil playerView:self.playViewContainer];
    
    [self clearAllStream];
    
    if (self.loginChannelSuccess)
        [getZegoAV_ShareInstance() logoutChannel];
    
    if (self.loginRoomSuccess)
        [getBizRoomInstance() leaveLiveRoom:YES];
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark request publish
- (IBAction)onRequestPublish:(id)sender
{
    if (self.isPublishing)
    {
        [getZegoAV_ShareInstance() stopPreview];
        [getZegoAV_ShareInstance() setLocalView:nil];
        [getZegoAV_ShareInstance() stopPublishing];
        
        self.publishButton.enabled = NO;
    }
    else if ([[self.publishButton currentTitle] isEqualToString:NSLocalizedString(@"请求连麦", nil)])
    {
        NSString *magicNumber = [NSString stringWithFormat:@"%d", rand()];
        [ZegoChatCommand sendCommand:kZEGO_CHAT_REQUEST_PUBLISH toUsers:nil content:@"hahaha" magicNumber:magicNumber];
        [self.requestingArray addObject:magicNumber];
        
        self.publishButton.enabled = NO;
    }
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

- (IBAction)onShowPublishOption:(id)sender
{
    [self showPublishOption];
}

- (NSString *)getStreamIDFromView:(UIView *)view
{
    for (NSString *streamID in self.viewContainersDict)
    {
        if (self.viewContainersDict[streamID] == view)
            return streamID;
    }
    
    return nil;
}

#pragma mark fullscreen action
- (void)exitFullScreen:(NSString *)streamID viewIndex:(int)index
{
    BOOL isLandscape = UIInterfaceOrientationIsLandscape([[UIApplication sharedApplication] statusBarOrientation]);
    
    [getZegoAV_ShareInstance() setRemoteViewMode:index mode:ZegoVideoViewModeScaleAspectFit];
    if (isLandscape)
        [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_90 viewIndex:index];
    else
        [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_0 viewIndex:index];
    self.videoSizeDict[streamID] = @(NO);
}

- (void)enterFullScreen:(NSString *)streamID viewIndex:(int)index
{
    BOOL isLandscape = UIInterfaceOrientationIsLandscape([[UIApplication sharedApplication] statusBarOrientation]);
    
    [getZegoAV_ShareInstance() setRemoteViewMode:index mode:ZegoVideoViewModeScaleAspectFill];
    if (isLandscape)
        [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_0 viewIndex:index];
    else
        [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_90 viewIndex:index];
    self.videoSizeDict[streamID] = @(YES);
}

- (void)setRotateFromInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    for (NSString *streamID in self.viewIndexDict.allKeys)
    {
        int viewIndex = [self.viewIndexDict[streamID] intValue];
        
        switch (orientation) {
            case UIInterfaceOrientationPortrait:
                [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_0 viewIndex:viewIndex];
                break;
                
            case UIInterfaceOrientationPortraitUpsideDown:
                [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_180 viewIndex:viewIndex];
                break;
                
            case UIInterfaceOrientationLandscapeLeft:
                [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_270 viewIndex:viewIndex];
                break;
                
            case UIInterfaceOrientationLandscapeRight:
                [getZegoAV_ShareInstance() setRemoteViewRotation:CAPTURE_ROTATE_90 viewIndex:viewIndex];
                break;
                
            default:
                break;
        }
    }
}

- (void)changeFirstViewContent
{
    UIView *view = [self getFirstViewInContainer:self.playViewContainer];
    NSString *streamID = [self getStreamIDFromView:view];
    if (streamID == nil)
        return;
    
    id info = self.videoSizeDict[streamID];
    if (info == nil)
        return;
    
    BOOL isfull = [info boolValue];
    int index = [self.viewIndexDict[streamID] intValue];
    if (isfull)
    {
        [self exitFullScreen:streamID viewIndex:index];
        [self onFullScreenButton:nil];
    }
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    
    [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        
        UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
        [self setRotateFromInterfaceOrientation:orientation];
        [self changeFirstViewContent];
        
    } completion:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        
    }];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
    
    [self setRotateFromInterfaceOrientation:toInterfaceOrientation];
    [self changeFirstViewContent];
}

- (IBAction)onFullScreenButton:(id)sender
{
    UIView *view = [self getFirstViewInContainer:self.playViewContainer];
    NSString *streamID = [self getStreamIDFromView:view];
    if (streamID == nil)
        return;
    
    id info = self.videoSizeDict[streamID];
    if (info == nil)
        return;
    
    BOOL isfull = [info boolValue];
    int index = [self.viewIndexDict[streamID] intValue];
    if (isfull)
    {
        //退出全屏
        [self exitFullScreen:streamID viewIndex:index];
        [self.fullscreenButton setTitle:NSLocalizedString(@"进入全屏", nil) forState:UIControlStateNormal];
    }
    else
    {
        //进入全屏
        [self enterFullScreen:streamID viewIndex:index];
        [self.fullscreenButton setTitle:NSLocalizedString(@"退出全屏", nil) forState:UIControlStateNormal];
    }
}

#pragma mark ZegoAVKit
- (void)setupLiveKit
{
    getBizRoomInstance().streamDelegate = self;
    getZegoAV_ShareInstance().delegate = self;
}

- (void)loginChannel
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    bool ret = [getZegoAV_ShareInstance() loginChannel:self.liveChannel user:user];
    assert(ret);
    
    NSLog(@"%s, ret: %d", __func__, ret);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"开始登录channel, channel:%@", nil), self.liveChannel];
    [self addLogString:logString];
}

#pragma mark ZegoLiveRoom
- (void)loginRoom
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    NSString *userName = [NSString stringWithFormat:@"#m-%@", user.userName];
    [getBizRoomInstance() loginLiveRoom:user.userID userName:userName bizToken:self.bizToken bizID:self.bizID isPublicRoom:YES];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"开始登录房间", nil)];
    [self addLogString:logString];
}

- (void)getStreamList
{
    [getBizRoomInstance() getStreamList:YES];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"开始获取直播流列表", nil)];
    [self addLogString:logString];
}

#pragma mark ZegoLiveApiDelegate
- (void)onLoginChannel:(NSString *)channel error:(uint32)err
{
    NSLog(@"%s, err: %u", __func__, err);
    if (err != 0)
    {
        //TODO: error warning
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录channel失败, error:%d", nil), err];
        [self addLogString:logString];
        return;
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录channel成功", nil)];
        [self addLogString:logString];
    }
    
    self.loginChannelSuccess = YES;
    if (self.streamList.count == 0 && self.loginRoomSuccess)
        [self getStreamList];
    else
    {
        for (ZegoStreamInfo *info in self.streamList)
        {
            if (self.viewContainersDict[info.streamID] != nil)
                continue;
            
            [self addStreamViewContainer:info.streamID];
            
            NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"继续播放之前的流, 流ID:%@", nil), info.streamID];
            [self addLogString:logString];
        }
    }
    
    if (self.publishStreamID.length != 0)
        [getBizRoomInstance() cteateStreamInRoom:self.publishTitle preferredStreamID:self.publishStreamID isPublicRoom:YES];
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

/// \brief 发布直播成功
- (void)onPublishSucc:(NSString *)streamID channel:(NSString *)channel streamInfo:(NSDictionary *)info
{
    NSLog(@"%s, stream: %@", __func__, streamID);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"发布直播成功,流ID:%@", nil), streamID];
    [self addLogString:logString];
    
    //记录当前的发布信息
    self.isPublishing = YES;
    self.publishButton.enabled = YES;
    [self.publishButton setTitle:NSLocalizedString(@"停止直播", nil) forState:UIControlStateNormal];
    self.publishStreamID = streamID;
    self.optionButton.enabled = YES;
    
    //    [ZegoSettings sharedInstance].publishingStreamID = streamID;
    //    [ZegoSettings sharedInstance].publishingLiveChannel = self.liveChannel;
    
    [self reportStreamAction:YES streamID:streamID];
    
    UIView *publishView = self.viewContainersDict[streamID];
    if (publishView)
    {
        [self startCaptureAudioLevel:publishView];
    }
}

/// \brief 发布直播失败
/// \param err 1 异常结束，2 正常结束
- (void)onPublishStop:(uint32)err stream:(NSString *)streamID channel:(NSString *)channel
{
    if (err != 1)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"发布直播失败, 流ID:%@, err:%d", nil), streamID, err];
        [self addLogString:logString];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"发布直播结束, 流ID:%@", nil), streamID];
        [self addLogString:logString];
    }
    
    NSLog(@"%s, stream: %@, err: %u", __func__, streamID, err);
    self.isPublishing = NO;
    self.publishButton.enabled = YES;
    [self.publishButton setTitle:NSLocalizedString(@"请求连麦", nil) forState:UIControlStateNormal];
    //    self.publishStreamID = nil;
    self.optionButton.enabled = NO;
    
    [self reportStreamAction:NO streamID:streamID];
    
    UIView *publishView = self.viewContainersDict[streamID];
    if (publishView)
    {
        [self stopCaptureAudioLevel];
    }
    
    //删除publish的view
    [self removeStreamViewContainer:streamID];
}

- (void)onAuxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount
{
    [self auxCallback:pData dataLen:pDataLen sampleRate:pSampleRate channelCount:pChannelCount];
}

- (void)onVideoSizeChanged:(NSString *)streamID width:(uint32)width height:(uint32)height
{
    NSLog(@"%s, streamID %@", __func__, streamID);
    
    if (self.optionButton.alpha == 0)
    {
        [self setButtonHidden:NO];
        [self setBackgroundImage:nil playerView:self.playViewContainer];
    }
    
    UIView *view = self.viewContainersDict[streamID];
    if (view)
        [self setBackgroundImage:nil playerView:view];
    
    if ([self.publishStreamID isEqualToString:streamID])
        return;
    
    if ([self isStreamIDExist:streamID] && view)
    {
        int index = [self.viewIndexDict[streamID] intValue];
        if (width > height && view.frame.size.width < view.frame.size.height)
        {
            [getZegoAV_ShareInstance() setRemoteViewMode:index mode:ZegoVideoViewModeScaleAspectFit];
            
            self.videoSizeDict[streamID] = @(NO);
            
            if (CGRectEqualToRect(view.frame, self.playViewContainer.bounds))
                self.fullscreenButton.hidden = NO;
        }
        
        self.streamID2SizeDict[streamID] = [NSValue valueWithCGSize:CGSizeMake(width, height)];
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

#pragma mark BizRoomStreamDelegate
- (void)onLoginRoom:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom
{
    NSLog(@"%s, error: %d", __func__, err);
    if (err == 0)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录房间成功. token %d, id %d", nil), bizToken, bizID];
        [self addLogString:logString];
        
        self.loginRoomSuccess = YES;
        
        if (self.viewContainersDict.count < MAX_STREAM_COUNT)
            self.publishButton.enabled = YES;
        
        [self getStreamList];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录房间失败. error: %d", nil), err];
        [self addLogString:logString];
        
        self.loginRoomSuccess = NO;
        [self showNoLivesAlert];
    }
}

- (void)onDisconnected:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken isPublicRoom:(bool)isPublicRoom
{
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"连接失败, error: %d", nil), err];
    [self addLogString:logString];
    
    //    [self onCloseAudience:nil];
}

- (void)onStreamUpdate:(NSArray<NSDictionary *> *)streamList flag:(int)flag isPublicRoom:(bool)isPublicRoom
{
    NSLog(@"%s, flag:%d count %lu", __func__, flag, (unsigned long)streamList.count);
    
    if (!self.loginChannelSuccess)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"流列表有更新, 此时还未登录channel", nil)];
        [self addLogString:logString];
        
        if (flag == 1)
            return;
        
        //先把流缓存起来
        for (NSDictionary *dic in streamList)
        {
            NSString *streamID = dic[kRoomStreamIDKey];
            if ([self isStreamIDExist:streamID])
            {
                continue;
            }
            
            ZegoStreamInfo *streamInfo = [ZegoStreamInfo getStreamInfo:dic];
            [self.streamList addObject:streamInfo];
        }
        
        return;
    }
    
    if (streamList.count == 0)
    {
        //        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"流列表有变化，但是流列表为空！", nil)];
        //        [self addLogString:logString];
        return;
    }
    
    if (flag == 0)
    {
        [self onStreamUpdateForAdd:streamList];
    }
    else if (flag == 1)
    {
        [self onStreamUpdateForDelete:streamList];
    }
}

- (void)onStreamCreate:(NSString *)streamID url:(NSString *)url isPublicRoom:(bool)isPublicRoom
{
    //创建stream成功
    NSLog(@"%s, streamID is %@", __func__, streamID);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流成功, streamID:%@", nil), streamID];
    [self addLogString:logString];
    
    //创建发布view
    UIView *publishView = [self createPublishView:streamID];
    if (publishView)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"开始发布直播", nil)];
        [self addLogString:logString];
        
        [self setAnchorConfig:publishView];
        bool b = [getZegoAV_ShareInstance() startPublishingWithTitle:self.publishTitle streamID:streamID];
        assert(b);
        NSLog(@"%s, ret: %d", __func__, b);
    }
    else
    {
        self.publishButton.enabled = YES;
    }
}

- (void)onReceiveMessage:(NSData *)content messageType:(int)type isPublicRoom:(bool)isPublicRoom
{
    if (type != 2)
        return;
    
    //data
    NSDictionary *receiveInfo = [ZegoChatCommand getRequestPublishRsp:content];
    if (receiveInfo == nil)
        return;
    
    NSString *command = receiveInfo[kZEGO_CHAT_CMD];
    
    if ([self checkMessageMagicNumber:receiveInfo[kZEGO_CHAT_MAGIC]])
    {
        //自己发出的request请求， 只处理respond
        if ([command isEqualToString:kZEGO_CHAT_RESPOND_PUBLISH])
            [self onReceivePublishRespond:receiveInfo];
    }
    else if (self.isPublishing)
    {
        if ([command isEqualToString:kZEGO_CHAT_REQUEST_PUBLISH])
        {
            //自己作为观众进入room，但是请求上台后，自己也在直播
            [self onReceivePublishRequest:receiveInfo];
        }
        else if ([command isEqualToString:kZEGO_CHAT_RESPOND_PUBLISH])
        {
            //自己正在直播，其他用户请求上台->弹框->其他人批准了上台
            //需要把弹框消失
            [self dismissAlertView:receiveInfo[kZEGO_CHAT_MAGIC]];
        }
    }
}

- (BOOL)shouldShowPublishAlert
{
    if (self.viewContainersDict.count < MAX_STREAM_COUNT)
        return YES;
    
    return NO;
}

#pragma mark alert
- (void)showNoLivesAlert
{
    if ([self isDeviceiOS7])
    {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:NSLocalizedString(@"主播已退出", nil) delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
        [alertView show];
    }
    else
    {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:NSLocalizedString(@"主播已退出", nil) preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            [self onCloseAudience:nil];
        }];
        
        [alertController addAction:okAction];
        
        [self presentViewController:alertController animated:YES completion:nil];
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    [self onCloseAudience:nil];
}

#pragma mark stream add & delete
- (BOOL)isStreamIDExist:(NSString *)streamID
{
    if ([self.publishStreamID isEqualToString:streamID])
        return YES;
    
    for (ZegoStreamInfo *info in self.streamList)
    {
        if ([info.streamID isEqualToString:streamID])
            return YES;
    }
    
    return NO;
}

- (int)getRemoteViewIndex
{
    int index = 0;
    for (; index < MAX_STREAM_COUNT; index++)
    {
        if ([self.viewIndexDict allKeysForObject:@(index)].count == 0)
            return index;
    }
    
    if (index == MAX_STREAM_COUNT)
        NSLog(@"cannot find indx to add view");
    
    return index;
}

- (void)onTapView:(UIGestureRecognizer *)gestureRecognizer
{
    UIView *view = gestureRecognizer.view;
    if (view == nil)
        return;
    
    [self updateContainerConstraintsForTap:view containerView:self.playViewContainer];
    
    UIView *firstView = [self getFirstViewInContainer:self.playViewContainer];
    NSString *streamID = [self getStreamIDFromView:firstView];
    if (streamID == nil)
        return;
    
    id info = self.videoSizeDict[streamID];
    if (info == nil)
    {
        self.fullscreenButton.hidden = YES;
    }
    else
    {
        self.fullscreenButton.hidden = NO;
    }
}

- (void)addStreamViewContainer:(NSString *)streamID
{
    if (streamID.length == 0)
        return;
    
    if (self.viewContainersDict[streamID] != nil)
        return;
    
    UIView *bigView = [[UIView alloc] init];
    bigView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.playViewContainer addSubview:bigView];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapView:)];
    [bigView addGestureRecognizer:tapGesture];
    
    int index = [self getRemoteViewIndex];
    RemoteViewIndex remoteIndex = (RemoteViewIndex)index;
    
    BOOL bResult = [self setContainerConstraints:bigView containerView:self.playViewContainer viewCount:self.viewContainersDict.count];
    if (bResult == NO)
    {
        [bigView removeFromSuperview];
        return;
    }
    
    UIImage *backgroundImage = [[ZegoSettings sharedInstance] getBackgroundImage:self.view.bounds.size withText:NSLocalizedString(@"加载中", nil)];
    [self setBackgroundImage:backgroundImage playerView:bigView];
    
    self.viewContainersDict[streamID] = bigView;
    self.viewIndexDict[streamID] = @(index);
    
    [getZegoAV_ShareInstance() setRemoteView:remoteIndex view:bigView];
    [getZegoAV_ShareInstance() setRemoteViewMode:remoteIndex mode:ZegoVideoViewModeScaleAspectFill];
    bool ret = [getZegoAV_ShareInstance() startPlayStream:streamID viewIndex:remoteIndex];
    assert(ret);
}

- (void)removeStreamViewContainer:(NSString *)streamID
{
    if (streamID.length == 0)
        return;
    
    UIView *view = self.viewContainersDict[streamID];
    if (view == nil)
        return;
    
    [self updateContainerConstraintsForRemove:view containerView:self.playViewContainer];
    
    [self.viewContainersDict removeObjectForKey:streamID];
    [self.viewIndexDict removeObjectForKey:streamID];
}

- (void)removeStreamInfo:(NSString *)streamID
{
    NSInteger index = NSNotFound;
    for (ZegoStreamInfo *info in self.streamList)
    {
        if ([info.streamID isEqualToString:streamID])
        {
            index = [self.streamList indexOfObject:info];
            break;
        }
    }
    
    if (index != NSNotFound)
        [self.streamList removeObjectAtIndex:index];
}

- (void)onStreamUpdateForAdd:(NSArray<NSDictionary *> *)streamList
{
    for (NSDictionary *dic in streamList)
    {
        NSString *streamID = dic[kRoomStreamIDKey];
        if (streamID.length == 0)
            continue;
        
        if ([self isStreamIDExist:streamID])
            continue;
        
        if (self.viewContainersDict.count >= MAX_STREAM_COUNT)
            return;
        
        [self.streamList addObject:[ZegoStreamInfo getStreamInfo:dic]];
        [self addStreamViewContainer:streamID];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"新增一条流, 流ID:%@", nil), streamID];
        [self addLogString:logString];
        
        if (self.viewContainersDict.count >= MAX_STREAM_COUNT)
        {
            if (!self.isPublishing)
                self.publishButton.enabled = NO;
        }
        else
            self.publishButton.enabled = YES;
    }
}

- (void)onStreamUpdateForDelete:(NSArray<NSDictionary *> *)streamList
{
    for (NSDictionary *dic in streamList)
    {
        NSString *streamID = dic[kRoomStreamIDKey];
        if (streamID.length == 0)
            continue;
        
        if (![self isStreamIDExist:streamID])
            continue;
        
        [getZegoAV_ShareInstance() stopPlayStream:streamID];
        
        [self removeStreamViewContainer:streamID];
        [self removeStreamInfo:streamID];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"删除一条流, 流ID:%@", nil), streamID];
        [self addLogString:logString];
        
        if (self.viewContainersDict.count < MAX_STREAM_COUNT)
            self.publishButton.enabled = YES;
        else
        {
            if (!self.isPublishing)
                self.publishButton.enabled = NO;
        }
        
        if (self.viewContainersDict.count == 0)
            self.publishButton.enabled = NO;
    }
}

- (BOOL)checkShouldPublish:(NSArray *)toUserList
{
    for (NSDictionary *dic in toUserList)
    {
        NSString *userID = dic[kZEGO_CHAT_TO_USERID];
        
        ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
        if ([user.userID isEqualToString:userID])
            return YES;
    }
    
    return NO;
}

- (UIView *)createPublishView:(NSString *)streamID
{
    if (streamID.length == 0)
        return nil;
    
    UIView *publishView = [[UIView alloc] init];
    publishView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.playViewContainer addSubview:publishView];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapView:)];
    [publishView addGestureRecognizer:tapGesture];
    
    BOOL bResult = [self setContainerConstraints:publishView containerView:self.playViewContainer viewCount:self.viewContainersDict.count];
    if (bResult == NO)
    {
        [publishView removeFromSuperview];
        return nil;
    }
    
    self.viewContainersDict[streamID] = publishView;
    [self.playViewContainer bringSubviewToFront:publishView];
    
    return publishView;
}

- (void)createStream
{
    self.publishTitle = [NSString stringWithFormat:@"Hello-%@", [ZegoSettings sharedInstance].userName];
    [getBizRoomInstance() cteateStreamInRoom:self.publishTitle preferredStreamID:nil isPublicRoom:YES];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流", nil)];
    [self addLogString:logString];
}

- (void)onReceivePublishRespond:(NSDictionary *)receiveInfo
{
    NSArray *toUserList = receiveInfo[kZEGO_CHAT_TO_USER];
    if (![self checkShouldPublish:toUserList])
        return;
    
    NSString *content = receiveInfo[kZEGO_CHAT_CONTENT];
    if ([content isEqualToString:kZEGO_CHAT_DISAGREE_PUBLISH])
    {
        [self requestPublishResultAlert:receiveInfo[KZEGO_CHAT_FROM_USERNAME]];
        self.publishButton.enabled = YES;
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"有主播拒绝请求连麦", nil)];
        [self addLogString:logString];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"有主播同意了你的请求", nil)];
        [self addLogString:logString];
        
        [self createStream];
    }
}

- (BOOL)checkMessageMagicNumber:(NSString *)magicNumber
{
    if (magicNumber.length == 0)
        return NO;
    
    if ([self.requestingArray containsObject:magicNumber])
        return YES;
    
    return NO;
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
