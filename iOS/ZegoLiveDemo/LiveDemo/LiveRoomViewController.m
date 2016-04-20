//
//  LiveRoomViewController.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import "LiveRoomViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoAVKit/ZegoUser.h"
#import <ZegoAVKit/ZegoChatDelegate.h>
#import <ZegoAVKit/ZegoVideoDelegate.h>
#import <ZegoAVKit/ZegoAVApi.h>
#import <ZegoAVKit/ZegoAVConfig.h>



typedef enum{
    CUSTOM_BROADCAST_MSG_TYPE_LIKEACTION = 1,
    CUSTOM_BROADCAST_MSG_TYPE_GOPUBLISH = 2
}CUSTOM_BROADCAST_MSG_TYPE;

typedef struct {
    UInt32 type;
    UInt32 cmd;
}CUSTOM_BROADCAST_MSG_HEAD;

typedef enum{
    CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_APPLY = 0,
    CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_CANCEL = 1,
    CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_APPROVE = 2
}CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD;

static NSString *CellIdentifier = @"LiveRoomTableViewCell";

#define VIEW_INFO_KEY_View              @"View"
#define VIEW_INFO_KEY_StreamID          @"StreamID"
#define VIEW_INFO_KEY_Index             @"Index"
#define VIEW_INFO_KEY_FullScreen        @"FullScreen"
#define VIEW_INFO_KEY_Type              @"Type"
#define VIEW_INFO_KEY_Weight            @"Weight"
#define VIEW_INFO_KEY_TimeStamp         @"TimeStamp"

enum{
    VIEW_INFO_KEY_Type_Idle,
    VIEW_INFO_KEY_Type_Playing,
    VIEW_INFO_KEY_Type_Publishing
};

enum{
    VIEW_INFO_KEY_Weight_Low = 1,
    VIEW_INFO_KEY_Weight_High = 10,
    VIEW_INFO_KEY_Weight_Supper = 100
};



@interface LiveRoomViewController ()

@property (strong) UIAlertView *alert;
@property (weak, nonatomic) IBOutlet UIView *beautyBox;
@property (weak, nonatomic) IBOutlet UIView *filterBox;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *beautifyBoxHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *beautifyBoxWidth;
@property (weak, nonatomic) IBOutlet UIButton *btnBeautify;
@property (weak, nonatomic) IBOutlet UIPickerView *beautifyPicker;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *filterBoxHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *filterBoxWidth;
@property (weak, nonatomic) IBOutlet UIPickerView *filterPicker;
@property (weak, nonatomic) IBOutlet UIButton *btnFilter;


@property (readonly) NSArray* beautifyFeatureList;

@end

@implementation LiveRoomViewController
{
    bool _playing;
    bool _publishing;
    bool _displayChat;
    
    UInt32 _likeActionAmount, _likeActionCount, _likeActionCombo;
    
    NSMutableArray *_playerList;
    
    ZegoMoviePlayer *_moviePlayer;
    
    NSMutableArray *_candidateList;
    
    NSMutableArray *_viewInfoList;
    
    ZegoShareApi *shareApi;
    
    NSArray *_filterList;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // * disable idle timer
    [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    
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
    
    _beautifyFeatureList = @[@"无美颜", @"磨皮", @"全屏美白", @"磨皮＋全屏美白", @"磨皮+皮肤美白"];
}


-(void)viewWillAppear:(BOOL)animated{
    
    [self registerNotifications];
    
    _displayChat = YES;

    self.beautyBox.hidden = YES;
    self.filterBox.hidden = YES;
    
    if ([_roomType isEqualToString:LIVEROOM_TYPE_PLAYBACK]) {
        
        if (_replayPath == nil || _replayPath.length <= 0) {
            self.alert = [[UIAlertView alloc] initWithTitle:@"回播失败" message:@"回播地址为空或者长度为0" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [self.alert show];
            return;
        }
        
        NSString *path = _replayPath;
        NSMutableDictionary *parameters = [NSMutableDictionary dictionary];
       
        parameters[ZegoMovieParameterScalingMode] = @(ZegoMovieScalingModeAspectFill);
        
        _moviePlayer = [ZegoMoviePlayer movieControllerWithContentPath:path
                                                          presentView:self.view
                                                             autoplay:YES
                                                           parameters:parameters];
        
        [_moviePlayer setDelegate:self callBackQueue:dispatch_get_main_queue()];

        
        self.replayProgress.continuous = NO;
        self.replayCtrlView.hidden = NO;
        self.inputMsgEdit.hidden = YES;
        self.likeBtn.hidden = YES;
        self.goPublishBtn.hidden = YES;
        self.switchViewBtn.hidden = YES;
        
        [self displayPublisherUI];
        self.roomStatus.text = @"回播中";
        
        
        NSTimeInterval timeBegin = self.createTime.longLongValue;
        NSTimeInterval timeEnd = self.endTime.longLongValue;
        NSTimeInterval timeDuration = timeEnd - timeBegin;

        self.liveDuration.text = formatTimeInterval(timeDuration, NO);

        return;
    }
    
    self.replayCtrlView.hidden = YES;
    self.inputMsgEdit.hidden = NO;
    self.likeBtn.hidden = NO;
    
    
    _candidateList = [[NSMutableArray alloc] init];

    UIView *firstRemoteView = self.firstStreamView;
    UIView *secondRemoteView = self.secondStreamView;
    
    _viewInfoList = [[NSMutableArray alloc] init];
    NSMutableDictionary *viewInfo1 = [[NSMutableDictionary alloc] init];
    viewInfo1[VIEW_INFO_KEY_View] = firstRemoteView;
    viewInfo1[VIEW_INFO_KEY_Index] = @(RemoteViewIndex_First);
    viewInfo1[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
    
    NSMutableDictionary *viewInfo2 = [[NSMutableDictionary alloc] init];
    viewInfo2[VIEW_INFO_KEY_View] = secondRemoteView;
    viewInfo2[VIEW_INFO_KEY_Index] = @(RemoteViewIndex_Second);
    viewInfo2[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
  
    [_viewInfoList addObject:viewInfo1];
    [_viewInfoList addObject:viewInfo2];
    
    [self fullScreenView:viewInfo1];    //let first view full screen

    
    [getZegoAV_ShareInstance() setRemoteView:RemoteViewIndex_First view:firstRemoteView];
    [getZegoAV_ShareInstance() setRemoteView:RemoteViewIndex_Second view:secondRemoteView];
    
    [getZegoAV_ShareInstance() setLocalViewMode:ZegoVideoViewModeScaleAspectFit];
    [getZegoAV_ShareInstance() setRemoteViewMode:RemoteViewIndex_First mode:ZegoVideoViewModeScaleAspectFit];
    [getZegoAV_ShareInstance() setRemoteViewMode:RemoteViewIndex_Second mode:ZegoVideoViewModeScaleAspectFit];
    
    //设置回调代理
    [getZegoAV_ShareInstance() setVideoDelegate:self callbackQueue:dispatch_get_main_queue()];
    [getZegoAV_ShareInstance() setChatDelegate:self callbackQueue:dispatch_get_main_queue()];
    
    //进入聊天室
    ZegoUser * user = [ZegoUser new];
    user.userID = _userID;
    user.userName = _userName;
    
    UInt32 nRoomToken = [_roomToken intValue];
    UInt32 nRoomNum = [_roomNumber intValue];
    
    [getZegoAV_ShareInstance() getInChatRoom:user zegoToken:nRoomToken zegoId:nRoomNum];
    

    //设置视频参数
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    NSInteger avConfigPreset = [ud integerForKey:@"avConfigPreset"];
    
    ZegoAVConfig *zegoAVConfig;
    if (avConfigPreset < 0) {
        //用户自定义过各种参数
        NSInteger nResolutionEnum = [ud integerForKey:@"avConfigResolution"];
        NSInteger nFPS = [ud integerForKey:@"avConfigFPS"];
        NSInteger nBitrate = [ud integerForKey:@"avConfigBitrate"];
        
        zegoAVConfig = [ZegoAVConfig new];
        
        [zegoAVConfig setVideoFPS:(int)nFPS];
        [zegoAVConfig setVideoBitrate:(int)nBitrate];
        [zegoAVConfig setVideoResolution:(int)nResolutionEnum];
    }
    else{
        zegoAVConfig = [ZegoAVConfig defaultZegoAVConfig:(ZegoAVConfigPreset)avConfigPreset];
    }
    [getZegoAV_ShareInstance() setAVConfig:zegoAVConfig];
    
    [getZegoAV_ShareInstance() setFrontCam:_useFrontCamera];
    
    if ([_roomType isEqualToString:LIVEROOM_TYPE_PUBLISH]) {
    
        _publisherID = _userID;
        _publisherName = _userName;
        _publisherPic = _userPic;
                
        self.goPublishBtn.hidden = YES;
        self.beautyBox.hidden = NO;
        self.filterBox.hidden = NO;
        
        [self.btnFilter setTitle:_filterList[0] forState:UIControlStateNormal];
        [self.btnBeautify setTitle:self.beautifyFeatureList[0] forState:UIControlStateNormal];
    }
    [self displayPublisherUI];
    
}

-(void)viewWillDisappear:(BOOL)animated{

    [_viewInfoList removeAllObjects];
    _viewInfoList = nil;
    
    if ([_roomType isEqualToString:LIVEROOM_TYPE_PLAYBACK]) {
        
        [_moviePlayer pause];
        [_moviePlayer setDelegate:nil callBackQueue:nil];
        _moviePlayer = nil;
        
    }
    else{
        
        [getZegoAV_ShareInstance() leaveChatRoom];
        releaseZegoAV_ShareInstance();
        _playing = NO;
    }
    
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    // * enable idle timer
    [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
}


- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    switch (self.interfaceOrientation) {
        case UIInterfaceOrientationPortrait:
            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_0];
            break;
            
        case UIInterfaceOrientationPortraitUpsideDown:
            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_180];
            break;
            
        case UIInterfaceOrientationLandscapeLeft:
            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_90];
            break;
            
        case UIInterfaceOrientationLandscapeRight:
            [getZegoAV_ShareInstance() setCaptureRotation:CAPTURE_ROTATE_270];
            break;
            
        default:
            break;
    }
}


- (IBAction)LeaveRoom:(id)sender {
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)SendChatMessage:(id)sender {
    UITextField *input = sender;
    if (input.text.length != 0)
    {
        [getZegoAV_ShareInstance() sendBroadcastTextMsgInChatRoom:input.text];
        
        NSTimeInterval sendTime = [[NSDate dateWithTimeIntervalSinceNow:0] timeIntervalSince1970];
        NSString *strSendTime =  [self formatMsgTime:sendTime];
        NSString *strMsg = [[NSString alloc ]initWithFormat:@"\n%@ %@ \n  %@", self.userName, strSendTime, input.text];
        [self appendMessageBoardText:strMsg];
    }
    
    //释放
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];

    
    self.inputMsgEdit.text = @"";
}

- (IBAction)switchCameraBtnClicked:(UIButton *)sender {
    _useFrontCamera = !_useFrontCamera;

    [getZegoAV_ShareInstance() setFrontCam:_useFrontCamera];
    
    [UIView animateWithDuration:1 delay:0 options:UIViewAnimationOptionTransitionNone animations:^{
        CGAffineTransform newTransform = CGAffineTransformMakeScale(-1, 1);
        
        [self.firstStreamView setTransform:newTransform];
        
        CGAffineTransform newTransform2 = CGAffineTransformMakeScale(1, 1);
        
        [self.firstStreamView setTransform:newTransform2];

        
    }
                     completion:nil
                         
     ];
}

- (IBAction)publishControlBtnClicked:(UIButton *)sender {
    NSString *strBtnBackgroundImagePath;
    if (_publishing) {
        //直播中，停止发布直播
        strBtnBackgroundImagePath = @"aio_voice_operate_listen_nor";
        _publishControlBtn.backgroundColor = [UIColor whiteColor];
        
        [getZegoAV_ShareInstance() stopPublishInChatRoom];
        [getZegoAV_ShareInstance() stopPreview];
        
        
        for (int i = 0; i < _viewInfoList.count; i++) {
            NSMutableDictionary *viewInfo = _viewInfoList[i];
            
            long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
            if (type == VIEW_INFO_KEY_Type_Publishing) {
                viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
                
                break;
            }
        }
        
        self.roomStatus.text = @"直播暂停";
        
        //[_publishControlBtn setEnabled:NO];
        [self updateStreamViews];
        
    } else {
        //重新发布直播
        
        NSMutableDictionary *viewInfo = [self needIdleView];
        
        UIView *previewView = (UIView *)viewInfo[VIEW_INFO_KEY_View];
        
        
        viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Publishing);
        viewInfo[VIEW_INFO_KEY_TimeStamp] = @([[NSDate date] timeIntervalSince1970]);
        viewInfo[VIEW_INFO_KEY_Weight] = @(VIEW_INFO_KEY_Weight_High); //直播者权重取一个较大值

        strBtnBackgroundImagePath = @"fanpaizi_closed_nor";
        _publishControlBtn.backgroundColor = [UIColor clearColor];
        
        [getZegoAV_ShareInstance() setLocalView:previewView];
        [getZegoAV_ShareInstance() startPreview];
        [getZegoAV_ShareInstance() startPublishInChatRoom:self.roomTitle];
        
        self.roomStatus.text = @"直播中";
        
        //[_publishControlBtn setEnabled:NO];
        [self updateStreamViews];
        [self fullScreenView:viewInfo];
    }
    
    _publishing = !_publishing;
    [_publishControlBtn setBackgroundImage:[UIImage imageNamed:strBtnBackgroundImagePath] forState:UIControlStateNormal];
}

- (IBAction)chatDisplayCtrlBtnClicked:(UIButton *)sender {

    _displayChat = !_displayChat;

    NSString *strBtnBackgroundImagePath;
    if (_displayChat) {
        _messageBoard.hidden = NO;
        strBtnBackgroundImagePath = @"tab_recent_nor.png";
    }
    else{
        _messageBoard.hidden = YES;
        strBtnBackgroundImagePath = @"tab_recent_press.png";
    }
    [_chatDisplayCtrlBtn setBackgroundImage:[UIImage imageNamed:strBtnBackgroundImagePath] forState:UIControlStateNormal];
}

- (IBAction)viewTouchDown:(UIControl *)sender {
    [[UIApplication sharedApplication] sendAction:@selector(resignFirstResponder) to:nil from:nil forEvent:nil];

}

#define  LIKE_ACTION_COMBO_FREQ 8
#define  LIKE_ACTION_COMBO_CHALLENG 20

- (IBAction)likeBtnClicked:(UIButton *)sender {
    
    _likeActionCount ++;
    _likeActionCombo ++;
    _likeActionAmount ++;
    
    _likeCountLabel.text = [self formatCountString:_likeActionAmount];

    NSString *imagePath;
    if (_likeActionCombo < LIKE_ACTION_COMBO_CHALLENG) {
        int imageIndex = (self.userID.longLongValue)%9;
        imagePath = [[NSString alloc] initWithFormat:@"balloon_%d.png", imageIndex];
    }
    else{
        imagePath = @"XmasTree.png";
    }
    
    [self startFlyImage:1 imagePath:imagePath];
    
    if (_likeActionCount == 1) {
        
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC);
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            [self sendLikeAction];
        });
    }
}


- (IBAction)goPublishBtnClicked:(UIButton *)sender {
    
    UInt32 cmd = (sender.tag == 0 ? CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_APPLY:CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_CANCEL);
    NSMutableDictionary *msgContainer = [[NSMutableDictionary alloc] init];
    
    UInt32 dataBytes[2] = { htonl(CUSTOM_BROADCAST_MSG_TYPE_GOPUBLISH), htonl(cmd)};
    NSData *data = [[NSData alloc] initWithBytes:dataBytes length:sizeof(dataBytes)];
    
    msgContainer[CUSTOM_DATA] = data;
    long long nSentResult = [getZegoAV_ShareInstance() sendBroadcastCustomMsgInChatRoom:msgContainer];

    if (nSentResult <= 0) {
        self.alert = [[UIAlertView alloc] initWithTitle:@"操作失败" message:@"操作太频繁，请您稍后重试！" delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [self.alert show];
        
        return;
    }
    
    
    if (sender.tag == 0) {
        sender.tag = 1;
        
        [sender setTitle:@"取消上台" forState:UIControlStateNormal];
        
        ZegoUser * user = [ZegoUser new];
        user.userID = self.userID;
        user.userName = self.userName;
        [_candidateList addObject:user];
        
        [self.candidateTableView reloadData];

    }
    else{
        sender.tag = 0;
        
        [sender setTitle:@"申请上台" forState:UIControlStateNormal];
        
        //remove from candidate list
        for (int i = 0; i < _candidateList.count; i++) {
            ZegoUser * user = _candidateList[i];
            if([user.userID isEqualToString:self.userID]){
                [_candidateList removeObjectAtIndex:i];
                break;
            }
        }
        
        [self.candidateTableView reloadData];
        
        if (_publishing) {
            [getZegoAV_ShareInstance() stopPreview];
            [getZegoAV_ShareInstance() stopPublishInChatRoom];
            
            for (int i = 0; i < _viewInfoList.count; i++) {
                NSMutableDictionary *viewInfo = _viewInfoList[i];
                
                long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
                if (type == VIEW_INFO_KEY_Type_Publishing) {
                    viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
                    
                    break;
                }
            }
            
            _publishing = NO;

            [self updateStreamViews];
        }

    }
    
}

- (IBAction)replayCtrlClicked:(UIButton *)sender {
    if ([_moviePlayer playing]) {
        [_moviePlayer pause];
    }
    else{
        [_moviePlayer restorePlay];
    }
}

- (IBAction)replayProgressChanged:(UISlider *)sender {
    CGFloat pos = sender.value;
    [_moviePlayer seek:pos];
    
    NSLog(@"replayProgressChanged: %f", pos);
}



- (void)sendLikeAction{
    
    NSLog(@"send _likeActionAmount:%u, _likeActionCount:%u, _likeActionCombo:%u", _likeActionAmount, _likeActionCount, _likeActionCombo);

    //sendBroadcastCustomMsgInChatRoom 支持系统统计计数，共8个计数器，
    //在字典中使用SYNC_COUNT_0...SYNC_COUNT_7字段，后台服务器会做计数，然后，同步给其他用户
    //使用CUSTOM_DATA这个字段，可以放用户自定义的数据，
    //通常在这个字段至少放入一个type以区分不同的业务数据，方便接收方做处理
    
    NSMutableDictionary *msgContainer = [[NSMutableDictionary alloc] init];

    //组织自定义数据，其中用type区分是点赞这个数据包，_likeActionCombo是点赞互动中另一个自定义的业务数据，在这里是连击数
    UInt32 dataBytes[2] = { htonl(CUSTOM_BROADCAST_MSG_TYPE_LIKEACTION), htonl(_likeActionCombo)};
    //CustomMSG_LikeAction *msg = (CustomMSG_LikeAction *)dataBytes;
    NSData *data = [[NSData alloc] initWithBytes:dataBytes length:sizeof(dataBytes)];
    
    msgContainer[CUSTOM_DATA] = data;
    
    //点赞数，使用SYNC_COUNT_0字段，所有用户点赞数会在后台做累计
    msgContainer[SYNC_COUNT_0] = @(_likeActionCount);

    [getZegoAV_ShareInstance() sendBroadcastCustomMsgInChatRoom:msgContainer];
    
    if (_likeActionCount < LIKE_ACTION_COMBO_FREQ) {
        _likeActionCombo = 0;
    }
    _likeActionCount = 0;
}

- (void)startFlyImage:(int)imageCount imagePath:(NSString *)imagePath{
    
    srand(arc4random());
    
    double delay = 0.0;
    
    for (int i = 0; i < imageCount; i++) {
        
        CGRect frame = _likeBtn.frame;
        frame.origin.y -= 2 * frame.size.height + 4 + rand()%20;
        frame.origin.x -= frame.size.width + rand()%20;
        frame.size.height *= 1.6;
        frame.size.width *= 1.6;
        
        UIImageView *imageView = [[UIImageView alloc] initWithFrame:frame];
        imageView.hidden = YES;
        [self.view addSubview:imageView];

        [imageView setImage:[UIImage imageNamed:imagePath]];
    
        double xOffset = (double)(rand() % (int)(frame.size.width));
        double yOffset = self.view.frame.size.height * (0.6+(double)(rand()%3)/10);

        [self doFlyImage:imageView xOffset:xOffset yOffset:yOffset delay:delay];

        delay += 0.05;
    }
}

- (void)doFlyImage:(UIImageView *)imageView xOffset:(double)xOffset yOffset:(double)yOffset delay:(NSTimeInterval)delay{
    
    [UIView animateWithDuration:4 delay:delay options:UIViewAnimationOptionTransitionFlipFromLeft animations:^{
         CGAffineTransform newTransform = CGAffineTransformMakeScale(0.3, 0.3);
         [imageView setTransform:newTransform];
         [imageView setAlpha:0.2];
         CGRect frame = imageView.frame;
         frame.origin.x -= xOffset;
         frame.origin.y -= yOffset;
         [imageView setFrame:frame];
        
         imageView.hidden = NO;

        }
            completion:^(BOOL finished){
                            [imageView removeFromSuperview];
                      }
      ];
     
 }

- (NSMutableDictionary *)needIdleView{
    
    NSMutableDictionary *idleViewInfo = nil;
    
    NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
    double tsWeight = timeStamp * VIEW_INFO_KEY_Weight_Supper;
    int indexNeedToRelease = 0;

    for (int i = 0; i < _viewInfoList.count; i++) {
        NSMutableDictionary *viewInfo = _viewInfoList[i];
        
        long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
        if (type == VIEW_INFO_KEY_Type_Idle) {
            idleViewInfo = viewInfo;
            break;
        }
        else{
            NSTimeInterval ts = [viewInfo[VIEW_INFO_KEY_TimeStamp] doubleValue];
            NSInteger weight = [viewInfo[VIEW_INFO_KEY_Weight] integerValue];
            
            if (ts*weight < tsWeight)
            {
                tsWeight = ts*weight;
                indexNeedToRelease = i;
            }
        }
    }
    
    if (idleViewInfo == nil) {
        //两个view都已经被占用，需要释放一个时间戳加权后较小的
        NSMutableDictionary *viewInfo = _viewInfoList[indexNeedToRelease];
        long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
        viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
        
        if (type == VIEW_INFO_KEY_Type_Playing) {
            long long streamID = [viewInfo[VIEW_INFO_KEY_StreamID] longLongValue];
            [getZegoAV_ShareInstance() stopPlayInChatRoom:streamID];
        }
        else{
            [getZegoAV_ShareInstance() stopPreview];
            [getZegoAV_ShareInstance() stopPublishInChatRoom];
            _publishing = NO;
        }
        
        idleViewInfo = viewInfo;
    }
    
    return idleViewInfo;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return _candidateList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    
   
    ZegoUser *user = _candidateList[indexPath.row];
    NSString *strUserPic = [self getUserPicPath:user.userID];
    [cell.imageView setImage:[UIImage imageNamed:strUserPic]];
    cell.textLabel.text = user.userName;
    
    if ([self.roomType isEqualToString:LIVEROOM_TYPE_PUBLISH]) {
        UIButton *opBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        opBtn.frame = CGRectMake(0, 0, 30, 24);
        [opBtn setTitle:@"同意" forState:UIControlStateNormal];
        //[opBtn setBackgroundImage:[UIImage imageNamed:@"message"] forState:UIControlStateNormal];
        [opBtn addTarget:self action:@selector(candidateOPBtnClick:event:) forControlEvents:UIControlEventTouchUpInside];
        cell.accessoryView = opBtn;
    }

    
    return cell;
}


-(void)candidateOPBtnClick:(id)sender event:(id)event
{
    NSSet *touches = [event allTouches];
    UITouch *touch = [touches anyObject];
    CGPoint currentTouchPosition = [touch locationInView:self.candidateTableView];
    
    NSIndexPath *indexPath = [self.candidateTableView indexPathForRowAtPoint:currentTouchPosition];
    if(indexPath !=nil)
    {
        [self tableView:self.candidateTableView accessoryButtonTappedForRowWithIndexPath:indexPath];
    }
    
}
-(void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
    //主播批准了 申请上台
    ZegoUser *user = _candidateList[indexPath.row];
    UInt32 userID = (UInt32)user.userID.integerValue;
    
    UInt32 cmd = CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_APPROVE;
    NSMutableDictionary *msgContainer = [[NSMutableDictionary alloc] init];
    
    UInt32 dataBytes[3] = { htonl(CUSTOM_BROADCAST_MSG_TYPE_GOPUBLISH), htonl(cmd), htonl(userID)};
    NSData *data = [[NSData alloc] initWithBytes:dataBytes length:sizeof(dataBytes)];
    
    msgContainer[CUSTOM_DATA] = data;
    long long nSentResult = [getZegoAV_ShareInstance() sendBroadcastCustomMsgInChatRoom:msgContainer];
    if (nSentResult <= 0) {
        self.alert = [[UIAlertView alloc] initWithTitle:@"操作失败" message:@"操作太频繁，请您稍后重试！" delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [self.alert show];
        
        return;
    }
    
    //remove from candidate list
    [_candidateList removeObjectAtIndex:indexPath.row];
    [self.candidateTableView reloadData];

}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
}


#pragma mark - UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
//    if (buttonIndex == 1) {
//        //用户想继续直播
//        _playing = YES;
//        [getZegoAV_ShareInstance() startPlayInChatRoom];
//    }
}

#pragma mark - ZegoVideoDelegate

- (void) onPublishSucc:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title{
    //self.messageBoard.text = [self.messageBoard.text stringByAppendingString:@"\n启动直播成功，直播进行中..."];
    [self appendMessageBoardText:@"\n启动直播成功，直播进行中..."];

    _publishing = YES;

    self.roomStatus.text = @"直播中";
    
    self.switchCameraBtn.hidden = NO;
    
    if ([self.roomType isEqualToString:LIVEROOM_TYPE_PUBLISH]) {
        
        self.publishControlBtn.hidden = NO;
        
        NSDate *createTime = [[NSDate alloc] initWithTimeIntervalSinceNow:0];
        
        self.createTime = [[NSString alloc] initWithFormat:@"%u", (unsigned int)[createTime timeIntervalSince1970]];
        [self updateLiveDurationLabel];
        
        if (self.coverPath != nil || self.coverPath.length > 0) {
            [getZegoAV_ShareInstance() setPublishExtraData:CustomDataType_file dataKey:COVER_FILE_KEY data:nil file:self.coverPath];
        }
    }
    
    
    //[_publishControlBtn setEnabled:YES];
}

- (void) onPublishStop:(ShowErrCode)err zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title {

    if(err == ShowErrCode_Temp_Broken){
        self.roomStatus.text = @"网络优化中";
        [self appendMessageBoardText:@"\n直播已经被停止！"];

        //临时中断，尝试重新启动发布直播
        [getZegoAV_ShareInstance() startPreview];
        [getZegoAV_ShareInstance() startPublishInChatRoom:self.roomTitle];
    }
    else if(err == ShowErrCode_End){
        //发布流正常结束
        _publishing = NO;
    }
    
    //[_publishControlBtn setEnabled:YES];
}

- (void) onPlaySucc:(long long)streamID zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title{
    NSLog(@"%s", __FUNCTION__);
    
    if (!_playing) {
        _playing = YES;
    }
    
    self.roomStatus.text = @"直播中";
    [self updateLiveDurationLabel];
}

- (void) onPlayStop:(uint32)err streamID:(long long)streamID zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId title:(NSString*)title{
    NSLog(@"%s", __FUNCTION__);
    
    if(err == ShowErrCode_Temp_Broken){
        self.roomStatus.text = @"网络优化中";
        
        //临时中断，尝试重新启动播放
        for (int i = 0; i < _viewInfoList.count; i++) {
            NSMutableDictionary *viewInfo = _viewInfoList[i];
            
            long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
            if (type == VIEW_INFO_KEY_Type_Playing) {
                
                long long sid = [viewInfo[VIEW_INFO_KEY_StreamID] longLongValue];
                if (sid == streamID) {
                    RemoteViewIndex viewIndex = (RemoteViewIndex)[viewInfo[VIEW_INFO_KEY_Index] integerValue];
                    [getZegoAV_ShareInstance() startPlayInChatRoom:viewIndex streamID:streamID];
                    break;
                }
            }
        }
        
    }
    else if(err == ShowErrCode_End){
        
        BOOL allPlayStreamStopped = YES;
        for (int i = 0; i < _viewInfoList.count; i++) {
            NSMutableDictionary *viewInfo = _viewInfoList[i];
            
            long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
            if (type == VIEW_INFO_KEY_Type_Playing) {
                
                long long sid = [viewInfo[VIEW_INFO_KEY_StreamID] longLongValue];
                if (sid == streamID) {
                    viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
                    continue;
                }
                else if (type != VIEW_INFO_KEY_Type_Idle){
                    allPlayStreamStopped = NO;
                }
            }
        }

        
        if (allPlayStreamStopped) {
            _playing = NO;
        }
        
        if (!_playing && !_publishing) {
            
            self.roomStatus.text = @"直播结束";
            [self notifyStoppedWithMsg:@"直播正常结束"];
        }
        
        [self updateStreamViews];
    }
}

- (void)updateStreamViews{
    NSMutableDictionary *notIdleViewInfo = nil;
    int notIdleViewCount = 0;
    for (int i = 0; i < _viewInfoList.count; i++) {
        NSMutableDictionary *viewInfo = _viewInfoList[i];
        
        long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
        UIView *view = (UIView *)viewInfo[VIEW_INFO_KEY_View];
        if (type == VIEW_INFO_KEY_Type_Idle) {
            view.hidden = YES;
        }
        else{
            notIdleViewCount ++;
            notIdleViewInfo = viewInfo;
            view.hidden = NO;
        }
        
        viewInfo[VIEW_INFO_KEY_FullScreen] = @(NO);
    }
    
    if (notIdleViewCount == 1) {
        //只剩下一个view，将它全屏
        [self fullScreenView:notIdleViewInfo];
    }
}

- (void) fullScreenView:(NSMutableDictionary *)targetViewInfo{
    
    UIView *preView = nil;
    UIView *targetView = nil;
    
    for (int i = 0; i < _viewInfoList.count; i++) {
        NSMutableDictionary *viewInfo = _viewInfoList[i];
        
        UIView *view = (UIView *)viewInfo[VIEW_INFO_KEY_View];
        if (viewInfo == targetViewInfo) {
            
            [self removeConstraintsWithSubView:view];
            [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[view]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(view)]];
            [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[view]|" options:0 metrics:nil views:NSDictionaryOfVariableBindings(view)]];
            
            viewInfo[VIEW_INFO_KEY_FullScreen] = @(YES);
            targetView = view;
        }
        else{
            
            [self removeConstraintsWithSubView:view];

            [self.view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeWidth multiplier:0.25 constant:0]];
            [self.view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeHeight multiplier:0.25 constant:0]];
            
            [self.view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeBottom multiplier:1.0 constant:-50]];

            if (preView != nil) {
                [self.view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:preView attribute:NSLayoutAttributeLeft multiplier:1.0 constant:-4]];
            }
            else{
                [self.view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeRightMargin multiplier:1.0 constant:0]];
                preView = view;
            }
            
            viewInfo[VIEW_INFO_KEY_FullScreen] = @(NO);
        }
    }
    
    [self.view sendSubviewToBack:targetView];
}

-(void)removeConstraintsWithSubView:(UIView *)subView{
    for(NSLayoutConstraint *l in self.view.constraints){
        if(l.firstItem == subView || l.secondItem == subView){
            [self.view removeConstraint:l];
        }
    }
}

- (void)notifyStoppedWithMsg:(NSString *)msg {
    ////////////////
    
    if (_publishing) {
        if ([_roomType isEqualToString:LIVEROOM_TYPE_PUBLISH]) {
            //[getZegoAV_ShareInstance() stopPublish];
            [self publishControlBtnClicked:self.publishControlBtn];
        }
        else if ([_roomType isEqualToString:LIVEROOM_TYPE_PLAY]) {
            [self goPublishBtnClicked:self.goPublishBtn];
        }

    }
    
    [self stopAllPlayStreams];
    
    [self.alert dismissWithClickedButtonIndex:-1 animated:NO];
    self.alert = [[UIAlertView alloc] initWithTitle:@"直播终止" message:msg delegate:self cancelButtonTitle:@"返回" otherButtonTitles:nil];
    [self.alert show];
    
    //////////////////////////
}

- (void)stopAllPlayStreams{
    
    for (int i = 0; i < _viewInfoList.count; i++) {
        NSMutableDictionary *viewInfo = _viewInfoList[i];
        
        long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
        if (type == VIEW_INFO_KEY_Type_Playing) {
            
            long long streamID = [viewInfo[VIEW_INFO_KEY_StreamID] longLongValue];
            viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
            [getZegoAV_ShareInstance() stopPlayInChatRoom:streamID];
        }
    }

    _playing = NO;
    
    [self updateStreamViews];
}


- (void) onPlayListUpdate:(PlayListUpdateFlag)flag playList:(NSArray*)list{

    if (flag == PlayListUpdateFlag_Error || list.count <= 0) {
//        self.roomStatus.text = @"直播出错";
//        
//        [self notifyStoppedWithMsg:@"无法拉取到直播信息！请退出重进！"];
        return;
    }

    
    if (flag == PlayListUpdateFlag_Remove) {
        NSDictionary * dictStream = list[0];
        if ([[dictStream objectForKey:PUBLISHER_ID] isEqualToString:self.userID]) {
            return;     //是自己停止直播的消息，应该在停止时处理过相关逻辑，这里不再处理
        }
        
        BOOL allPlayStreamsStopped = YES;
        long long streamID = [[dictStream objectForKey:STREAM_ID] longLongValue];
        
        for (int i = 0; i < _viewInfoList.count; i++) {
            NSMutableDictionary *viewInfo = _viewInfoList[i];
            
            long type = [viewInfo[VIEW_INFO_KEY_Type] integerValue];
            if (type == VIEW_INFO_KEY_Type_Playing) {
                
                long long sid = [viewInfo[VIEW_INFO_KEY_StreamID] longLongValue];
                if (streamID == sid) {
                    viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Idle);
                    [getZegoAV_ShareInstance() stopPlayInChatRoom:streamID];
                }
                else{
                    allPlayStreamsStopped = NO;
                }
            }
        }
        
        [self updateStreamViews];
        
        if (allPlayStreamsStopped) {
            _playing = NO;
        }
        
        if (!_playing && !_publishing) {
            
            self.roomStatus.text = @"直播结束";
            [self notifyStoppedWithMsg:@"没人直播了！"];
        }
        
    }
    else{
        
        if(flag == PlayListUpdateFlag_UpdateAll){
            [self stopAllPlayStreams];
        }
        
        for (NSUInteger i = 0; i < list.count; i++) {
            
            NSDictionary * dictStream = list[i];

            if ([[dictStream objectForKey:PUBLISHER_ID] isEqualToString:self.userID]) {
                continue;     //是自己发布直播的消息，应该在发布时处理过相关逻辑，这里不再处理
            }
            
            //有新流加入，找到一个空闲的view来播放，如果已经有两路播放，则停止比较老的流，播放新流
            NSMutableDictionary *viewInfo = [self needIdleView];
            
            long long newStreamID = [[dictStream objectForKey:STREAM_ID] longLongValue];
            RemoteViewIndex newStreamIndex = (RemoteViewIndex)[viewInfo[VIEW_INFO_KEY_Index] integerValue];

            viewInfo[VIEW_INFO_KEY_StreamID] = @(newStreamID);
            viewInfo[VIEW_INFO_KEY_TimeStamp] = @([[NSDate date] timeIntervalSince1970]);
            viewInfo[VIEW_INFO_KEY_Weight] = @(VIEW_INFO_KEY_Weight_Low);
            viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Playing);
            
            [getZegoAV_ShareInstance() startPlayInChatRoom:newStreamIndex streamID:newStreamID];
            _playing = YES;
            
            if ([[dictStream objectForKey:PUBLISHER_ID] isEqualToString:self.publisherID]) {
                viewInfo[VIEW_INFO_KEY_Weight] = @(VIEW_INFO_KEY_Weight_High); //直播者权重取一个较大值
                [self fullScreenView:viewInfo];
            }
        }
        
        [self updateStreamViews];
    }
    
}

- (void) onPlayerCountUpdate:(uint32)userCount{
    [_playerCountBtn setTitle:[self formatCountString:userCount] forState:UIControlStateNormal];
}

#pragma mark - ZegoChatDelegate
- (void) onGetInChatRoomResult:(uint32)result zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId {
    NSLog(@"%s, result: %d, token: %d, id: %d", __func__, result, zegoToken, zegoId);
    
    if(result == 0){
        [self appendMessageBoardText:@"\n进入聊天室成功，开始启动直播..."];
    }
    else{
        [self appendMessageBoardText:@"\n进入聊天室失败"];
        
        return;
    }
    
    ZegoUser * user = [ZegoUser new];
    user.userID = self.userID;
    user.userName = self.userName;
    
    if ([self.roomType isEqualToString:LIVEROOM_TYPE_PUBLISH]) {
        
        NSMutableDictionary *viewInfo = [self needIdleView];
        
        UIView *previewView = (UIView *)viewInfo[VIEW_INFO_KEY_View];
        
        
        viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Publishing);
        viewInfo[VIEW_INFO_KEY_TimeStamp] = @([[NSDate date] timeIntervalSince1970]);
        viewInfo[VIEW_INFO_KEY_Weight] = @(VIEW_INFO_KEY_Weight_High); //直播者权重取一个较大值

        
        [getZegoAV_ShareInstance() setLocalView:previewView];
        [getZegoAV_ShareInstance() startPreview];
        [getZegoAV_ShareInstance() startPublishInChatRoom:self.roomTitle];
        
        self.roomStatus.text = @"直播中";
        _publishing = YES;
        
        //[_publishControlBtn setEnabled:NO];
        [self updateStreamViews];
        [self fullScreenView:viewInfo];
    }
}

- (void) onChatRoomDisconnected:(uint32)err{
    NSString *strMsg = [[NSString alloc ]initWithFormat:@"\n已经从聊天室断开了:%u", err];
    [self appendMessageBoardText:strMsg];
    
    self.roomStatus.text = @"直播终止";
    
    [self notifyStoppedWithMsg:strMsg];

}

//最多显示100个头像
#define PLAYER_LIST_MAX_COUNT 100
//@optional
- (void) onChatRoomUsersUpdate:(NSArray*) arrNewUsers leftUsers:(NSArray*)arrLeftUsers{
    NSLog(@"%s, new: %@\nleft: %@", __FUNCTION__, arrNewUsers, arrLeftUsers);
    
    
    if (_playerList == nil) {
        _playerList = [[NSMutableArray<NSMutableCopying> alloc] initWithCapacity:PLAYER_LIST_MAX_COUNT];
    }
    NSMutableIndexSet *removeIndexes = [[NSMutableIndexSet alloc] init];
    
    for (int i = 0; i < arrLeftUsers.count; i++) {
        ZegoUser *user = arrLeftUsers[i];
        //self.messageBoard.text = [self.messageBoard.text stringByAppendingFormat:@"\n%@(%@) 走了", user.userName, user.userID];
        NSString *strMsg = [[NSString alloc ]initWithFormat:@"\n%@(%@) 走了", user.userName, user.userID];
        [self appendMessageBoardText:strMsg];
        
        for (int j = 0; j < _playerList.count; j++) {
            ZegoUser *player = _playerList[j];
            if (player && [player.userID isEqualToString:user.userID]) {
                [removeIndexes  addIndex:j];
            }
        }
    }
    
    for (int i = 0; i < arrNewUsers.count; i++) {
        ZegoUser *user = arrNewUsers[i];
        //self.messageBoard.text = [self.messageBoard.text stringByAppendingFormat:@"\n%@(%@) 来了", user.userName, user.userID];
        NSString *strMsg = [[NSString alloc ]initWithFormat:@"\n%@(%@) 来了", user.userName, user.userID];
        [self appendMessageBoardText:strMsg];
    }
    
    //先删除列表中已离开的播放者
    [_playerList removeObjectsAtIndexes:removeIndexes];
    
    //如果新来的人数加上剩下的播放者超过了总数，则清除若干最久的
    NSInteger totalCount =  _playerList.count + arrNewUsers.count;
    
    int nNewAdded = MIN((int)arrNewUsers.count, PLAYER_LIST_MAX_COUNT);
    
    int nNeedRemoveMore = nNewAdded - (int)(PLAYER_LIST_MAX_COUNT - _playerList.count);
    if (nNeedRemoveMore > 0) {
        NSRange range = NSMakeRange(_playerList.count-nNeedRemoveMore, nNeedRemoveMore);
        [_playerList removeObjectsInRange:range];
    }
    
    //加上新来的
    for (int i = 0; i < nNewAdded; i++) {
        [_playerList insertObject:arrNewUsers[i] atIndex:0];
    }
   
    [self displayPlayerList:totalCount];
}

- (void) onChatRoomUserUpdateAll:(NSArray*) arrUsers{
    
    //最多显示PLAYER_LIST_MAX_COUNT个播放者头像，只需要存下最后PLAYER_LIST_MAX_COUNT个（可能是最新的）
    int nNewAdded = MIN((int)arrUsers.count, PLAYER_LIST_MAX_COUNT);

    if (_playerList != nil) {
        [_playerList removeAllObjects];
    }
    
    _playerList = [[NSMutableArray<NSMutableCopying> alloc] initWithCapacity:PLAYER_LIST_MAX_COUNT];

    for (int i = 0; i < nNewAdded; i++) {
        [_playerList addObject:arrUsers[(int)arrUsers.count-1-i]];
    }


    NSInteger totalCount = arrUsers.count;
    [self displayPlayerList:totalCount];
}

- (void) onSendBroadcastTextMsgResult:(uint32)result msg:(NSString*)msg msgSeq:(long long)msgSeq {
    
}

- (void) onSendBroadcastCustomMsgResult:(uint32)result msg:(NSDictionary*)msg msgSeq:(long long)msgSeq{

}

/// \brief 收到广播自定义消息
/// \param customMsg 收到的消息结构，包括发送者，发送时间，发送内容
- (void) onReceiveBroadcastCustomMsg:(CustomMsg*)customMsg{
    
    NSDictionary *msgContainer = customMsg.msg;
    
    //使用CUSTOM_DATA这个字段应该存有一个type以区分不同的业务数据

    NSData *data = msgContainer[CUSTOM_DATA];
    if (data == nil || data.length < sizeof(CUSTOM_BROADCAST_MSG_HEAD)) {
        return;
    }
    
    CUSTOM_BROADCAST_MSG_HEAD *header = (CUSTOM_BROADCAST_MSG_HEAD *)data.bytes;
    UInt32 msgType = ntohl(header->type);
    
    
    if(msgType == CUSTOM_BROADCAST_MSG_TYPE_LIKEACTION){
        
        UInt32 nCombo = ntohl(header->cmd);

        //从SYNC_COUNT_0取出点赞数
        UInt32 nCount = 0;
        NSNumber *numberSYNC_COUNT_0 = msgContainer[SYNC_COUNT_0];
        if (numberSYNC_COUNT_0 != nil) {
            nCount =numberSYNC_COUNT_0.unsignedIntValue;
        }
        
        NSLog(@"receive _likeActionCount:%u, likeActionComob:%u", (unsigned int)nCount, (unsigned int)nCombo);
        
        NSString *imagePath;
        
        if (nCombo >= LIKE_ACTION_COMBO_CHALLENG) {

            if (nCount > nCombo) {
                int imageIndex = (customMsg.user.userID.longLongValue)%9;
                imagePath = [[NSString alloc] initWithFormat:@"balloon_%d.png", imageIndex];
                
                [self startFlyImage:nCount-nCombo imagePath:imagePath];
                
                imagePath = @"XmasTree.png";
                [self startFlyImage:nCombo imagePath:imagePath];
            }
            else{
                imagePath = @"XmasTree.png";

                [self startFlyImage:nCount imagePath:imagePath];
            }
        }
        else{
            int imageIndex = ((unsigned long long)(customMsg.user.userID.longLongValue))%9;
            imagePath = [[NSString alloc] initWithFormat:@"balloon_%d.png", imageIndex];
            
            [self startFlyImage:nCount imagePath:imagePath];
        }
    }
    else if(msgType == CUSTOM_BROADCAST_MSG_TYPE_GOPUBLISH){
        UInt32 cmd = ntohl(header->cmd);

        if (cmd == CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_CANCEL) {
            //remove from candidate list
            for (int i = 0; i < _candidateList.count; i++) {
                ZegoUser * user = _candidateList[i];
                if([user.userID isEqualToString:customMsg.user.userID]){
                    [_candidateList removeObjectAtIndex:i];
                    break;
                }
            }
            
            NSString *strNote = [[NSString alloc] initWithFormat:@"\n%@ 取消上台", customMsg.user.userName];
            [self appendMessageBoardText:strNote];
        }
        else if (cmd == CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_APPLY){
            ZegoUser * user = [ZegoUser new];
            user.userID = [customMsg.user.userID copy];
            user.userName = [customMsg.user.userName copy];
            [_candidateList addObject:user];
            
            NSString *strNote = [[NSString alloc] initWithFormat:@"\n%@ 申请上台", user.userName];
            [self appendMessageBoardText:strNote];
        }
        else if (cmd == CUSTOM_BROADCAST_MSG_GOPUBLISH_CMD_APPROVE){
            //publisher (room admin) allow to publish
            ////////
            UInt32 *userIDVal = (UInt32 *)(data.bytes+sizeof(CUSTOM_BROADCAST_MSG_HEAD));
            NSString *userID = [[NSString alloc] initWithFormat:@"%u", (UInt32)ntohl(*userIDVal)];
            NSString *userName = userID;
            
            //remove from candidate list
            for (int i = 0; i < _candidateList.count; i++) {
                ZegoUser * user = _candidateList[i];
                if([user.userID isEqualToString:userID]){
                    userName = user.userName;
                    [_candidateList removeObjectAtIndex:i];
                    break;
                }
            }
            
            if (![self.userID isEqualToString:userID]){
                NSString *strNote = [[NSString alloc] initWithFormat:@"\n%@ 被批准上台", userName];
                [self appendMessageBoardText:strNote];
            }
            else {
                //自己的申请被批准
                
                NSString *strNote = @"\n你被批准上台，正在启动视频发布";
                [self appendMessageBoardText:strNote];
                
                NSMutableDictionary *viewInfo = [self needIdleView];
                
                UIView *previewView = (UIView *)viewInfo[VIEW_INFO_KEY_View];
                
                
                viewInfo[VIEW_INFO_KEY_Type] = @(VIEW_INFO_KEY_Type_Publishing);
                viewInfo[VIEW_INFO_KEY_TimeStamp] = @([[NSDate date] timeIntervalSince1970]);
                viewInfo[VIEW_INFO_KEY_Weight] = @(VIEW_INFO_KEY_Weight_Low);


                [getZegoAV_ShareInstance() setLocalView:previewView];
                [getZegoAV_ShareInstance() startPreview];
                [getZegoAV_ShareInstance() startPublishInChatRoom:self.roomTitle];
                
                _publishing = YES;
                
                [self updateStreamViews];
                [self fullScreenView:viewInfo];
            }
            
        }
        
        [self.candidateTableView reloadData];

    }
}


- (void) onReceiveBroadcastTextMsg:(TextMsg*)textMsg{
    NSString *strSendTime =  [self formatMsgTime:textMsg.sendTime];
    NSString *strMsg = [[NSString alloc ]initWithFormat:@"\n%@ %@ \n  %@", textMsg.user.userName, strSendTime, textMsg.msg];
    //self.messageBoard.text = [self.messageBoard.text stringByAppendingString:strMsg];
    [self appendMessageBoardText:strMsg];
}

- (void) onKickOut:(uint32) reason msg:(NSString*)msg{
    //self.messageBoard.text = [self.messageBoard.text stringByAppendingFormat:@"\n已经被踢出聊天室 （%d:%@）", reason, msg];
    NSString *strMsg = [[NSString alloc ]initWithFormat:@"\n已经被踢出聊天室 （%d:%@）", reason, msg];
    [self appendMessageBoardText:strMsg];
    
    self.roomStatus.text = @"直播终止";
    
    NSString *detail = strMsg;
    [self notifyStoppedWithMsg:detail];
}


- (void) onSyncTotalCountUpdate:(uint32)zegoToken zegoId:(uint32)zegoId counts:(NSDictionary*)counts{

    NSNumber * numberSYNC_COUNT_0 = counts[SYNC_COUNT_0];
    if (numberSYNC_COUNT_0 == nil) {
        return;
    }
    
    //更新点赞数
    _likeActionAmount = numberSYNC_COUNT_0.unsignedIntValue;
    _likeCountLabel.text = [self formatCountString:_likeActionAmount];
}

- (void) onSetPublishExtraDataResult:(uint32)errCode zegoToken:(uint32)zegoToken zegoId:(uint32)zegoId dataKey:(NSString*)strDataKey{
    
    if (errCode != 0) {
        
        if ([strDataKey isEqualToString:COVER_FILE_KEY]) {
            
            NSString *strError = [[NSString alloc] initWithFormat:@"%@ onSetPublishExtraDataResult: errCode %u", self, errCode];
            
            NSLog(@"%@", strError);
            
            self.alert = [[UIAlertView alloc] initWithTitle:@"设置封面照片失败" message:strError delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [self.alert show];
        }
    }
}

- (void) onVideoSizeChanged:(long long)streamID width:(uint32)width height:(uint32)height{
    NSLog(@"%@ onVideoSizeChanged width: %u height:%u", self, width, height);
}


- (NSString *) formatMsgTime:(time_t)msgTime{
    NSDate *time = [[NSDate alloc] initWithTimeIntervalSince1970:msgTime];
    NSDateFormatter *outputFormatter = [[NSDateFormatter alloc] init];
    [outputFormatter setLocale:[NSLocale currentLocale]];
    [outputFormatter setDateFormat:@"HH:mm:ss"];
    return [outputFormatter stringFromDate:time];
}

- (void) updateLiveDurationLabel{
    if (!_playing && !_publishing) {
        return;
    }
    
    NSTimeInterval timeStamp = self.createTime.longLongValue;
    NSDate *createTime = [[NSDate alloc] initWithTimeIntervalSince1970:timeStamp];
    NSTimeInterval timeInterval = [createTime timeIntervalSinceNow] * -1;
    
    int hours = (int)(timeInterval/3600);
    int minutes = (int)((timeInterval-3600*hours)/60);
    int seconds = (int)(timeInterval-3600*hours-minutes*60);
    
    self.liveDuration.text = [[NSString alloc ]initWithFormat:@"%02d:%02d:%02d", hours, minutes, seconds];
    
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        [self updateLiveDurationLabel];
    });

}

- (void) displayPublisherUI{
   
    //切成圆图
    //_publisherPicView.layer.masksToBounds = YES;
    //_publisherPicView.layer.cornerRadius = 20;
    

    [_publisherPicView setImage:[UIImage imageNamed:_publisherPic]];
    //点击头像复制分享链接到剪贴板
    _publisherPicView.userInteractionEnabled = YES;
    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleTap:)];
    [_publisherPicView addGestureRecognizer:singleTap];
}

- (void)handleSingleTap:(UIGestureRecognizer *)gestureRecognizer {
    shareApi = [ZegoShareApi new];
    [shareApi setDelegate:self callbackQueue:dispatch_get_main_queue()];
    NSDictionary *dict =[getZegoAV_ShareInstance() currentPublishInfo];
    [shareApi initApi];
    [shareApi getShareUrlByBase:@"http://api.zego.im" stream:dict[kZegoPublishStreamAliasKey] title:@"ZegoTest"];
}

- (void)onGetStreamShareUrl:(int)errorCode info:(NSDictionary *)info
{
    NSString* strShareUrl = info[kZegoShareUrlKey];
    UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
    [pasteboard setString:strShareUrl];
    [shareApi setDelegate:nil callbackQueue:nil];
    [shareApi uninitApi];
    shareApi = nil;
}

- (void)onGetStreamPlayUrl:(int)errorCode info:(NSDictionary *)info
{
    
}

- (void) displayPlayerList:(NSInteger)totalCount{
    
    for (long i = self.playerListView.subviews.count-1; i >= 0; i--) {
        UIImageView *imageView = self.playerListView.subviews[i];
        [imageView removeFromSuperview];
        imageView = nil;
    }
    
    CGRect frame = CGRectZero;
    frame.size = _playerCountBtn.frame.size;
    double playerListViewWidth = 0.0;
    
    for (int i = 0; i < _playerList.count; i++) {
        
        UIImageView *imageView = [[UIImageView alloc] initWithFrame:frame];
        
        [self.playerListView addSubview:imageView];
        
        ZegoUser *player = _playerList[i];
        NSString *strUserPic = [self getUserPicPath:player.userID];
        [imageView setImage:[UIImage imageNamed:strUserPic]];
            
        frame.origin.x += frame.size.width + 2;
    }
    
    playerListViewWidth = frame.origin.x - 2;

    if (totalCount > _playerList.count) {
        frame.size.width *= 4;
        UILabel *moreLabel = [[UILabel alloc] initWithFrame:frame];
        moreLabel.text = [[NSString alloc] initWithFormat:@"还有%u位", (unsigned int)(totalCount-_playerList.count)];
        [self.playerListView addSubview:moreLabel];
        
        playerListViewWidth += 2 + frame.size.width;
    }
    
    self.playerListView.contentSize = CGSizeMake(playerListViewWidth, frame.size.height);

}

- (NSString *)getUserPicPath:(NSString *)userID{
    int nHeadIndexBase = 86, nHeadIndexEnd = 132;
    UInt32 nPublisherID = (UInt32)userID.longLongValue;
    return [[NSString alloc] initWithFormat:@"emoji_%03u.png", (unsigned int)nPublisherID%(nHeadIndexEnd-nHeadIndexBase+1)+nHeadIndexBase];
}

- (NSString *)formatCountString:(NSInteger)count{
    NSString *strCount;
    if (count > 10000) {
        strCount = [[NSString alloc] initWithFormat:@"%0.1fw", (float)count/10000];
    }
    else if(count > 1000){
        strCount = [[NSString alloc] initWithFormat:@"%0.1fk", (float)count/1000];
    }
    else{
        strCount = [[NSString alloc] initWithFormat:@"%ld", (long)count];
        
    }
    
    return  strCount;
}

- (void) appendMessageBoardText:(NSString*)text
{
    NSString* oldText = self.messageBoard.text;
    int nLen = self.messageBoard.contentSize.height;
    if (nLen >150)
    {
        NSRange range;
        for (int i = 0; i < 3; i++) {
            range = [oldText rangeOfString:@"\n"];
            if (range.length > 0){
                oldText = [oldText substringFromIndex:(range.location+1)];
            }
            else
                break;
        }
    }
    oldText = [oldText stringByAppendingFormat:@"%@", text];
    self.messageBoard.text = oldText;
}

- (void)registerNotifications
{
    //使用NSNotificationCenter
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardDidShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardDismiss:) name:UIKeyboardDidHideNotification object:nil];
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidEnterBackground:)
                                                 name:UIApplicationDidEnterBackgroundNotification object:nil]; //监听是否触发home键挂起程序.
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationWillEnterForeground:)
                                                 name:UIApplicationWillEnterForegroundNotification object:nil]; //监听是否重新进入程序程序.
    
}

//实现当键盘出现的时候计算键盘的高度大小。用于输入框显示位置
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size;
    CGRect edFrame = self.view.frame;
    edFrame.size.height = [UIScreen mainScreen].applicationFrame.size.height - kbSize.height;
    self.view.frame = edFrame;
}

- (void)handleKeyboardDismiss:(NSNotification *)notification {    
    CGRect edFrame = self.view.frame;
    edFrame.size.height = [UIScreen mainScreen].applicationFrame.size.height;
    self.view.frame = edFrame;
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    //[self stopAllPlayStreams];
    //[getZegoAV_ShareInstance() stopPublishInChatRoom];
    //[self LeaveRoom:nil];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    //should have left room and dismissed this UI when applicationDidEnterBackground!!
    ////Do Nothing Here!!
}

static NSString * formatTimeInterval(CGFloat seconds, BOOL isLeft)
{
    seconds = MAX(0, seconds);
    
    NSInteger s = seconds;
    NSInteger m = s / 60;
    NSInteger h = m / 60;
    
    s = s % 60;
    m = m % 60;
    
    return [NSString stringWithFormat:@"%@%ld:%0.2ld:%0.2ld", isLeft ? @"-" : @"", h,m,s];
}

#pragma mark -- Zego_moviePlayerDelegate

- (void) onZegoMoviePlayerUpdatePlayerStatus:(ZegoMoviePlayerStatus)status{

    NSLog(@"%s %d", __FUNCTION__, status);

    
    switch (status) {
            
        case ZegoMoviePlayerStatus_Loading:
            self.roomStatus.text = @"正在加载";

            break;
            
        case ZegoMoviePlayerStatus_ReadyToPlay:
            //如果设置了autoplay，这个事件不会触发，可以播放时会直接进行播放
            //如果没有设置autoplay，可以在这个事件触发时，调用play
            /////could play now if not init player with autoplay
            /////[_moviePlayer play];
            self.roomStatus.text = @"准备就绪";

            break;
       
        case ZegoMoviePlayerStatus_Playing:
            self.roomStatus.text = @"回播中";
            
            [self.replayCtrlBtn setTitle:@"||" forState:UIControlStateNormal];
            break;
        
        case ZegoMoviePlayerStatus_Paused:
            self.roomStatus.text = @"回播暂停";

            [self.replayCtrlBtn setTitle:@">" forState:UIControlStateNormal];
            break;
        
        case ZegoMoviePlayerStatus_Buffering:
            self.roomStatus.text = @"缓冲中";
            
            break;
        
        case ZegoMoviePlayerStatus_FinishBuffering:
            self.roomStatus.text = @"回播中";

            break;
        
        case ZegoMoviePlayerStatus_Stopping:
            self.roomStatus.text = @"正在停止回播";

            break;

        case ZegoMoviePlayerStatus_Stopped:
            self.roomStatus.text = @"回播结束";

            self.alert = [[UIAlertView alloc] initWithTitle:@"回播结束" message:@"回播正常结束" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [self.alert show];
            break;
            
        default:
            break;
    }
}

- (void) onZegoMoviePlayerNaturalSizeAvairable:(CGSize)naturalSize{
    NSLog(@"%@ %s: %f %f", self, __FUNCTION__, naturalSize.width, naturalSize.height);
    
    //here to set scaling mode to fit naturalSize
    CGFloat ratioWH = naturalSize.width/naturalSize.height;

    if (ratioWH > 1) {
        [_moviePlayer setScalingMode:ZegoMovieScalingModeAspectFit];
    }
}
- (void) onZegoMoviePlayerDurationAvairable:(CGFloat)duration{
    NSLog(@"%@ %s: %f", self, __FUNCTION__, duration);
    
    int hours = (int)(duration/3600);
    int minutes = (int)((duration-3600*hours)/60);
    int seconds = (int)(duration-3600*hours-minutes*60);
    
    self.liveDuration.text = [[NSString alloc ]initWithFormat:@"%02d:%02d:%02d", hours, minutes, seconds];
}

- (void) onZegoMoviePlayerUpdateProgress:(CGFloat)position playableDuration:(CGFloat)playableDuration{
    self.replayProgress.value = position / _moviePlayer.duration;
    self.replayDurationLabel.text = formatTimeInterval(position, NO);
}

- (void) onZegoMoviePlayerError:(NSError *)error{
    
    self.alert = [[UIAlertView alloc] initWithTitle:@"回播失败" message:error.description delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [self.alert show];
}

- (IBAction)switchViewBtnClicked:(UIButton *)sender {
    
    for (int i = 0; i < _viewInfoList.count; i++) {
        NSMutableDictionary *viewInfo = _viewInfoList[i];
        
        UIView *view = (UIView *)viewInfo[VIEW_INFO_KEY_View];
        BOOL fullScreen = [viewInfo[VIEW_INFO_KEY_FullScreen] boolValue];
        if (!view.hidden && !fullScreen) {
            [self fullScreenView:viewInfo];
            break;
        }
    }
}


#pragma mark -- UIPickerViewDelegate, UIPickerViewDataSource
// pickerView 列数
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// pickerView 每列个数
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    if (pickerView == self.beautifyPicker) {
        return self.beautifyFeatureList.count;
    } else {
        return _filterList.count;
    }
}


// 返回选中的行
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    if (pickerView == self.beautifyPicker) {
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
        
        [getZegoAV_ShareInstance() enableBeautifying:feature];

        [self.btnBeautify setTitle:self.beautifyFeatureList[row] forState:UIControlStateNormal];
        
    } else {
        [getZegoAV_ShareInstance() setFilter:row];
        [self.btnFilter setTitle:_filterList[row] forState:UIControlStateNormal];
    }
}

//返回当前行的内容,此处是将数组中数值添加到滚动的那个显示栏上
-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    NSArray *dataList = nil;
    if (pickerView == self.beautifyPicker) {
        dataList = self.beautifyFeatureList;
    } else {
        dataList = _filterList;
    }
    
    if (row >= dataList.count) {
        return @"Error";
    }
    
    return [dataList objectAtIndex:row];
}


- (IBAction)enableBeautify:(id)sender {
    
    if (self.beautifyBoxWidth.constant == 150) {
        self.beautifyBoxHeight.constant = 46;
        self.beautifyBoxWidth.constant = 120;
        self.beautifyPicker.hidden = YES;
    } else {
        self.beautifyBoxHeight.constant = 128;
        self.beautifyBoxWidth.constant = 150;
        self.beautifyPicker.hidden = NO;
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}

- (IBAction)filterClicked:(id)sender {
    if (self.filterBoxWidth.constant == 130) {
        self.filterBoxHeight.constant = 46;
        self.filterBoxWidth.constant = 60;
        self.filterPicker.hidden = YES;
    } else {
        self.filterBoxHeight.constant = 128;
        self.filterBoxWidth.constant = 130;
        self.filterPicker.hidden = NO;
    }
    
    [UIView animateWithDuration:0.1 animations:^{
        [self.view layoutIfNeeded];
    }];
}


@end
