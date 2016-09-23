//
//  ZegoAudienceViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/27.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoAudienceViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoSettings.h"
#import "ZegoStreamInfo.h"
#import "ZegoChatCommand.h"
#import "ZegoLogTableViewController.h"

//loginChannel->onLoginChannel->LoginInRoom->onLoginRoom

@interface ZegoAudienceViewController () <BizRoomStreamDelegate, ZegoLiveApiDelegate>

@property (weak, nonatomic) IBOutlet UIView *playViewContainer;
@property (weak, nonatomic) IBOutlet UIButton *publishButton;
@property (weak, nonatomic) IBOutlet UIButton *optionButton;
@property (weak, nonatomic) IBOutlet UIButton *mutedButton;

@property (weak, nonatomic) IBOutlet UIButton *closeButton;
@property (weak, nonatomic) IBOutlet UIButton *logButton;

@property (weak, nonatomic) IBOutlet UIButton *fullscreenButton;

@property (nonatomic, strong) NSMutableArray<ZegoStreamInfo *> *streamList;
@property (nonatomic, strong) NSMutableDictionary *viewContainersDict;
@property (nonatomic, strong) NSMutableDictionary *viewIndexDict;

@property (nonatomic, assign) BOOL loginChannelSuccess;
@property (nonatomic, assign) BOOL loginRoomSuccess;

@property (nonatomic, strong) NSMutableArray *requestingArray;

@property (nonatomic, copy) NSString *liveChannel;

@property (nonatomic, strong) UIColor *defaultButtonColor;

@property (nonatomic, strong) NSMutableDictionary *videoSizeDict;

@end

@implementation ZegoAudienceViewController

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
        self.mutedButton.enabled = NO;
    }
    else
    {
        self.mutedButton.enabled = YES;
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

#pragma mark audioSession
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
    NSString *userName = [NSString stringWithFormat:@"#d-%@", user.userName];
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

#pragma mark close
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

#pragma set muted button
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

#pragma mark FullScreen action
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

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    
    [coordinator animateAlongsideTransition:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
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
        
    } completion:^(id<UIViewControllerTransitionCoordinatorContext>  _Nonnull context) {
        
    }];
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

#pragma mark ZegoLiveApiDelegate
- (void)onLoginChannel:(NSString *)channel error:(uint32)err
{
    NSLog(@"%s, err: %u", __func__, err);
    if (err != 0)
    {
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
}

- (void)onPlaySucc:(NSString *)streamID channel:(NSString *)channel
{
    NSLog(@"%s, streamID:%@", __func__, streamID);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"播放流成功, 流ID:%@", nil), streamID];
    [self addLogString:logString];
}

- (void)onPlayStop:(uint32)err streamID:(NSString *)streamID channel:(NSString *)channel
{
    NSLog(@"%s, streamID:%@", __func__, streamID);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"播放流失败, 流ID:%@,  error:%d", nil), streamID, err];
    [self addLogString:logString];
}

- (void)onVideoSizeChanged:(NSString *)streamID width:(uint32)width height:(uint32)height
{
    NSLog(@"%s, streamID %@", __func__, streamID);
    
    [self setButtonHidden:NO];
    [self setBackgroundImage:nil playerView:self.playViewContainer];
    
    UIView *view = self.viewContainersDict[streamID];
    if (view)
        [self setBackgroundImage:nil playerView:view];
    
    if ([self isStreamIDExist:streamID] && view)
    {
        int index = [self.viewIndexDict[streamID] intValue];
        if (width > height)
        {
            [getZegoAV_ShareInstance() setRemoteViewMode:index mode:ZegoVideoViewModeScaleAspectFit];
            
            self.videoSizeDict[streamID] = @(NO);
            
            if (CGRectEqualToRect(view.frame, self.playViewContainer.bounds))
                self.fullscreenButton.hidden = NO;
        }
    }
}

- (void)onPlayQualityUpdate:(int)quality stream:(NSString *)streamID
{
    UIView *view = self.viewContainersDict[streamID];
    if (view)
        [self updateQuality:quality view:view];
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

#pragma mark Stream add & delete function
- (BOOL)isStreamIDExist:(NSString *)streamID
{
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
        
        [self.streamList addObject:[ZegoStreamInfo getStreamInfo:dic]];
        [self addStreamViewContainer:streamID];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"新增一条流, 流ID:%@", nil), streamID];
        [self addLogString:logString];
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
    }
}

#pragma mark Alert
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
