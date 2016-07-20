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

//正在观看房间里的其他直播
@property (nonatomic, assign) BOOL isPlaying;

//正在播放的streamList
@property (nonatomic, strong) NSMutableArray *playStreamList;

@property (nonatomic, strong) NSMutableDictionary *viewContainersDict;
@property (nonatomic, strong) NSMutableDictionary *viewIndexDict;

@property (nonatomic, assign) BOOL isPublishing;

@property (nonatomic, strong) UIColor *defaultButtonColor;
@property (nonatomic, strong) UIColor *disableButtonColor;

@end

@implementation ZegoAnchorViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self setupLiveKit];
    [self loginChatRoom];
    
    _viewContainersDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _viewIndexDict = [[NSMutableDictionary alloc] initWithCapacity:MAX_STREAM_COUNT];
    _playStreamList = [[NSMutableArray alloc] init];
    
    
    self.stopPublishButton.enabled = NO;
    self.mutedButton.enabled = NO;
    self.defaultButtonColor = [self.mutedButton titleColorForState:UIControlStateNormal];
    self.disableButtonColor = [self.mutedButton titleColorForState:UIControlStateDisabled];
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

- (void)loginChannel
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];
    bool ret = [getZegoAV_ShareInstance() loginChannel:self.liveChannel user:user];
    assert(ret);
    
    NSLog(@"%s, ret: %d", __func__, ret);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"Login Channel", nil)];
    [self addLogString:logString];
}

- (void)closeAllStream
{
    [getZegoAV_ShareInstance() stopPreview];
    [getZegoAV_ShareInstance() setLocalView:nil];
    [getZegoAV_ShareInstance() stopPublishing];
    [self reportStreamAction:NO streamID:self.streamID];
    [self removeStreamViewContainer:self.streamID];
        
    if (self.isPlaying)
    {
        for (ZegoStreamInfo *info in self.playStreamList)
        {
            [getZegoAV_ShareInstance() stopPlayStream:info.streamID];
            [self removeStreamViewContainer:info.streamID];
        }
    }
    
//    self.streamID = nil;
//    [self.playStreamList removeAllObjects];
    
    [self.viewContainersDict removeAllObjects];
    [self.viewIndexDict removeAllObjects];
    self.isPublishing = NO;
    self.isPlaying = NO;
}

#pragma mark ZegoAV interface
- (void)setupLiveKit
{
    getZegoAV_ShareInstance().delegate = self;
    getBizRoomInstance().streamDelegate = self;
}

#pragma mark close publish
- (IBAction)onClosePublish:(id)sender
{
    [self reportStreamAction:NO streamID:self.streamID];

    [self closeAllStream];
    
    [getZegoAV_ShareInstance() logoutChannel];
    
    [getBizRoomInstance() leaveLiveRoom];
    
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

#pragma mark show more option;
- (IBAction)onShowMoreInfo:(id)sender
{
    [self showPublishOption];
}

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

#pragma mark ZegoLiveApiDelegate
/// \brief 获取流信息结果
/// \param err 0 成功，进一步等待流信息更新，否则出错
- (void)onLoginChannel:(NSString *)channel error:(uint32)err
{
    NSLog(@"%s, err: %u", __func__, err);
    if (err != 0)
    {
        //TODO: error warning
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录channel失败, error:%d", nil), err];
        [self addLogString:logString];
    }
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录channel成功", nil)];
    [self addLogString:logString];
    
    //登录成功后配置直播参数，开始直播 创建publishView
    UIView *publishView = [self createPublishView:self.streamID];
    if (publishView)
    {
        [self setAnchorConfig:publishView];
        bool b = [getZegoAV_ShareInstance() startPublishingWithTitle:self.liveTitle streamID:self.streamID];
        assert(b);
        NSLog(@"%s, ret: %d", __func__, b);
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"开始直播，流ID:%@", nil), self.streamID];
        [self addLogString:logString];
    }
    
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
    
    //记录当前的发布信息
//    [ZegoSettings sharedInstance].publishingStreamID = streamID;
//    [ZegoSettings sharedInstance].publishingLiveChannel = self.liveChannel;
    
    [self reportStreamAction:YES streamID:self.streamID];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"发布直播成功,流ID:%@", nil), streamID];
    [self addLogString:logString];
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
    
    [self reportStreamAction:NO streamID:self.streamID];
    [self removeStreamViewContainer:streamID];
    
//    self.streamID = nil;
}

#pragma mark ZegoChatRoom Kit
- (void)loginChatRoom
{
    ZegoUser *user = [[ZegoSettings sharedInstance] getZegoUser];    
    [getBizRoomInstance() loginLiveRoom:user.userID userName:user.userName bizToken:0 bizID:0];
    
    [self addLogString:[NSString stringWithFormat:NSLocalizedString(@"开始登录房间", nil)]];
}

- (void)createStream:(NSString *)preferredStreamID
{
    [getBizRoomInstance() cteateStreamInRoom:self.liveTitle preferredStreamID:preferredStreamID];
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"创建流", nil)];
    [self addLogString:logString];
}

#pragma mark ZeogStreamRoom delegate
- (void)onLoginRoom:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken
{
    NSLog(@"%s, error: %d", __func__, err);
    if (err == 0)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录房间成功. token %d, id %d", nil), bizToken, bizID];
        [self addLogString:logString];
        
        self.liveChannel = [[ZegoSettings sharedInstance] getChannelID:bizToken bizID:bizID];
        [self createStream:nil];
    }
    else
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"登录房间失败. error: %d", nil), err];
        [self addLogString:logString];
    }
}

- (void)onDisconnected:(int)err bizID:(unsigned int)bizID bizToken:(unsigned int)bizToken
{
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"连接失败, error: %d", nil), err];
    [self addLogString:logString];
    
    [self onClosePublish:nil];
}

- (void)onLeaveRoom:(int)err
{
    NSLog(@"%s, error: %d", __func__, err);
    
    NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"退出房间, error: %d", nil), err];
    [self addLogString:logString];
}

- (void)onStreamCreate:(NSString *)streamID url:(NSString *)url
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

- (void)onReceiveMessage:(NSData *)content messageType:(int)type
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
    else if ([command isEqualToString:kZEGO_CHAT_RESPOND_PUBLISH])
    {
        //有人已经操作请求上台了，弹框消失
        [self dismissAlertView:receiveInfo[kZEGO_CHAT_MAGIC]];
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
        
        [self removeStreamViewContainer:streamID];
        [self removeStreamInfo:streamID];
        
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"删除一条流, 流ID:%@", nil), streamID];
        [self addLogString:logString];
    }
    
    if (self.playStreamList.count == 0)
    {
        self.mutedButton.enabled = NO;
        [self.mutedButton setTitleColor:self.disableButtonColor forState:UIControlStateDisabled];
    }
}

- (void)onStreamUpdate:(NSArray<NSDictionary *> *)streamList flag:(int)flag
{
    if (streamList.count == 0)
    {
        NSString *logString = [NSString stringWithFormat:NSLocalizedString(@"流列表有变化，但是流列表为空！", nil)];
        [self addLogString:logString];
        return;
    }
    
    if (flag == 0)
        [self onStreamUpdateForAdd:streamList];
    else if (flag == 1)
        [self onStreamUpdateForDelete:streamList];
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

@end
