//
//  ZegoRoomViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoRoomViewController.h"
#import "ZegoRoomInfo.h"
#import "ZegoAVKitManager.h"
#import "ZegoAudienceViewController.h"
#import "ZegoMoreAudienceViewController.h"
#import "ZegoMixStreamAudienceViewController.h"
#import "ZegoStreamInfo.h"
#import "ZegoRenderAudienceViewController.h"

#define ITEM_COUNT_PER_PAGE 20

@implementation ZegoRoomTableViewCell

@end

@interface ZegoRoomViewController () <UITableViewDataSource, UITableViewDelegate, BizRoomInfoDelegate>
@property (weak, nonatomic) IBOutlet UITableView *liveView;

@property (nonatomic, strong) NSMutableArray<ZegoRoomInfo *>* roomList;

@property (nonatomic, strong) UIRefreshControl *refreshControl;

@end

@implementation ZegoRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _roomList = [NSMutableArray array];
    
    _refreshControl = [[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(handleRefresh:) forControlEvents:UIControlEventValueChanged];
    [self.liveView insertSubview:self.refreshControl atIndex:0];
    
    [self setupChatRoomKit];
    [self getLiveRoom];
    
    self.liveView.tableFooterView = [[UIView alloc] init];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onApplicationActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onRoomInstanceClear:) name:@"RoomInstanceClear" object:nil];
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)refreshRoomList
{
    if ([self.refreshControl isRefreshing])
        return;
    
    [self.roomList removeAllObjects];
    [self getLiveRoom];
}

- (void)handleRefresh:(UIRefreshControl *)refreshControl
{
    [self.roomList removeAllObjects];
    [self getLiveRoom];
}

- (void)onApplicationActive:(NSNotification *)notification
{
    [self handleRefresh:self.refreshControl];
}

- (void)onRoomInstanceClear:(NSNotification *)notification
{
    [self setupChatRoomKit];
    [self getLiveRoom];
}

- (void)setupChatRoomKit
{
    getBizRoomInstance().roomDelegate = self;
}

- (void)getLiveRoom
{
    [self.refreshControl beginRefreshing];
    
    bool bRet = [getBizRoomInstance() refreshShowList];
    if (bRet)
        bRet = [getBizRoomInstance() getShowList:(int)self.roomList.count count:ITEM_COUNT_PER_PAGE];
    
    NSLog(@"%s, bRet:%d", __func__, bRet);
}

#pragma mark UITableViewDataSource & UITableViewDelegate
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.roomList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    ZegoRoomTableViewCell *cell = (ZegoRoomTableViewCell *)[tableView dequeueReusableCellWithIdentifier:@"roomListID" forIndexPath:indexPath];
    
    if (indexPath.row >= self.roomList.count)
        return cell;
    
    ZegoRoomInfo *info = self.roomList[indexPath.row];
    
    if (info.firstLiveTitle.length == 0)
        cell.publishTitleLabel.text = NSLocalizedString(@"Empty Title", nil);
    else
        cell.publishTitleLabel.text = info.firstLiveTitle;
    
    if (info.livesCount > 1)
    {
        cell.livesCountLabel.text = [NSString stringWithFormat:NSLocalizedString(@"%lu人正在连麦", nil), (unsigned long)info.livesCount];
        cell.livesCountLabel.hidden = NO;
    }
    else
    {
        cell.livesCountLabel.text = @"";
        cell.livesCountLabel.hidden = YES;
    }
    
    return cell;
}

#pragma mark ZegoChatRoomDelegate
- (BOOL)isBizTokenExist:(NSUInteger)bizToken bizID:(NSUInteger)bizID
{
    for (ZegoRoomInfo *info in self.roomList)
    {
        if (info.bizToken == bizToken && info.bizID == bizID)
            return YES;
    }
    
    return NO;
}

- (void)onGetRoomInfoResult:(int)err totalCount:(int)totalCount beginIndex:(int)beginIndex roomInfoList:(NSArray *)roomInfoList
{
    NSLog(@"%s, error:%d", __func__, err);
    
    if ([self.refreshControl isRefreshing])
        [self.refreshControl endRefreshing];
    
    if ([self.delegate respondsToSelector:@selector(onRefreshRoomListFinished)])
        [self.delegate onRefreshRoomListFinished];
    
    if (err != 0)
    {
        return;
    }
    
    for (NSDictionary *dict in roomInfoList)
    {
        unsigned int bizToken = [dict[kRoomInfoBizTokenKey] unsignedIntValue];
        unsigned int bizID = [dict[kRoomInfoBizIDKey] unsignedIntValue];
        if ([self isBizTokenExist:bizToken bizID:bizID])
            continue;
        
        ZegoRoomInfo *roomInfo = [ZegoRoomInfo new];
        roomInfo.bizToken = bizToken;
        roomInfo.bizID = bizID;
        roomInfo.createTime = [dict[kRoomInfoCreateTimeKey] integerValue];
        roomInfo.livesCount = [dict[kRoomInfoLivesCount] integerValue];
        roomInfo.firstLiveTitle = dict[kRoomInfoFirstLiveTitle];
//        roomInfo.streamIDs = [NSArray arrayWithArray:dict[kRoomInfoLiveStreamIDs]];
        NSArray *streamList = dict[kRoomInfoLiveStreamIDs];
        NSMutableArray *streams = [NSMutableArray arrayWithCapacity:streamList.count];
        for (NSDictionary *dict in streamList)
        {
            ZegoStreamInfo *stream = [ZegoStreamInfo getStreamInfo:dict];
            [streams addObject:stream];
        }
        roomInfo.streamList = [NSArray arrayWithArray:streams];
        
        [self.roomList addObject:roomInfo];
    }
    
    [self.liveView reloadData];

}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    ZegoRoomInfo *info = [self.roomList objectAtIndex:indexPath.row];
    NSUInteger liveType = 0;
    for (ZegoStreamInfo *stream in info.streamList)
    {
        if ([stream.userName hasPrefix:@"#d-"])
        {
            liveType = 1;
            break;
        }
        else if ([stream.userName hasPrefix:@"#m-"])
        {
            liveType = 2;
            break;
        }
        else if ([stream.userName hasPrefix:@"#s-"])
        {
            liveType = 3;
            break;
        }
    }
    
    //兼容老版本，默认play 连麦模式
    if (liveType == 0)
        liveType = 2;
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    if (liveType == 1)
    {
        if (isUsingExternalRender())
        {
            ZegoRenderAudienceViewController *audienceViewController = (ZegoRenderAudienceViewController *)[storyboard instantiateViewControllerWithIdentifier:@"glAudienceID"];
            audienceViewController.bizToken = info.bizToken;
            audienceViewController.bizID = info.bizID;
            audienceViewController.currentStreamList = [NSArray arrayWithArray:info.streamList];
            [self presentViewController:audienceViewController animated:YES completion:nil];
        }
        else
        {
            ZegoAudienceViewController *audienceViewController = (ZegoAudienceViewController *)[storyboard instantiateViewControllerWithIdentifier:@"audienceID"];
            audienceViewController.bizToken = info.bizToken;
            audienceViewController.bizID = info.bizID;
            audienceViewController.currentStreamList = [NSArray arrayWithArray:info.streamList];
            [self presentViewController:audienceViewController animated:YES completion:nil];
        }
    }
    else if (liveType == 2)
    {
        ZegoMoreAudienceViewController *audienceViewController = (ZegoMoreAudienceViewController *)[storyboard instantiateViewControllerWithIdentifier:@"moreAudienceID"];
        audienceViewController.bizToken = info.bizToken;
        audienceViewController.bizID = info.bizID;
        audienceViewController.currentStreamList = [NSArray arrayWithArray:info.streamList];
        [self presentViewController:audienceViewController animated:YES completion:nil];
    }
    else if (liveType == 3)
    {
        ZegoMixStreamAudienceViewController *audienceViewController = (ZegoMixStreamAudienceViewController *)[storyboard instantiateViewControllerWithIdentifier:@"mixStreamAudienceID"];
        audienceViewController.bizToken = info.bizToken;
        audienceViewController.bizID = info.bizID;
        audienceViewController.currentStreamList = [NSArray arrayWithArray:info.streamList];
        [self presentViewController:audienceViewController animated:YES completion:nil];
    }
    
}

#pragma mark - Navigation

/*
// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
//    if ([segue.identifier isEqualToString:@"audienceIdentifier"])
//    {
//        UITableViewCell *cell = (UITableViewCell *)sender;
//        NSIndexPath *indexPath = [self.liveView indexPathForCell:cell];
//        ZegoRoomInfo *info = [self.roomList objectAtIndex:indexPath.row];
//        ZegoAudienceViewController *audienceController = (ZegoAudienceViewController *)[segue destinationViewController];
//        audienceController.zegoToken = (unsigned int)info.zegoToken;
//        audienceController.zegoId = (unsigned int)info.zegoId;
//    }
}
*/

@end
