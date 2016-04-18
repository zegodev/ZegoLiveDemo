//
//  CategoryViewController.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import "CategoryViewController.h"
//#import <MediaPlayer/MediaPlayer.h>
#import "HomePageTableViewCell.h"
#import "LiveRoomViewController.h"
#import "ProfileViewController.h"


#define ITEM_COUNT_PER_PAGE 20



@interface CategoryViewController ()

@end

@implementation CategoryViewController
{
   // MPMoviePlayerController *moviePlayer;
    UIRefreshControl* refreshControl;
    
    NSMutableArray* listData;
    NSMutableDictionary* dictImageCache;

}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    /*
    NSURL *url= [NSURL URLWithString:@"http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"];

    moviePlayer=[[MPMoviePlayerController alloc]initWithContentURL:url];
    
    CGRect frame = CGRectZero;
    frame = self.view.bounds;
    frame.origin.y -= 100;
    moviePlayer.view.frame=frame;
    
    moviePlayer.controlStyle = MPMovieControlStyleDefault;//MPMovieControlStyleEmbedded;//内嵌的方式
    moviePlayer.scalingMode = MPMovieScalingModeNone;//MPMovieScalingModeAspectFill;
    
    //[self.view insertSubview:moviePlayer.view atIndex:0];
    [self.view addSubview:moviePlayer.view];
    moviePlayer.view.backgroundColor =  [UIColor clearColor];
    
    [moviePlayer play];
    */
    
    [getZegoAV_ShareInstance() setShowListDelegate:self callbackQueue:dispatch_get_main_queue()];
    
    // 集成刷新控件
    [self setupRefresh];
}

- (void) viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    [getZegoAV_ShareInstance() setShowListDelegate:self callbackQueue:dispatch_get_main_queue()];
}

- (void)viewWillDisappear:(BOOL)animated {
    [refreshControl endRefreshing];
    
    [super viewWillDisappear:animated];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - set refresh control
-(void)setupRefresh
{
    //1.添加刷新控件
    UIRefreshControl *control=[[UIRefreshControl alloc]init];
    [control addTarget:self action:@selector(refreshStateChange:) forControlEvents:UIControlEventValueChanged];
    [self.tableView addSubview:control];
    refreshControl = control;
    
    //2.马上进入刷新状态，并不会触发UIControlEventValueChanged事件
    [control beginRefreshing];
    
    // 3.加载数据
    [self refreshStateChange:control];
}

-(void)refreshStateChange:(UIRefreshControl *)control
{
    bool bRet = [getZegoAV_ShareInstance() refreshShowList];
    
    if (bRet) {
        bRet = [getZegoAV_ShareInstance() getReplayList:0 count:ITEM_COUNT_PER_PAGE];
        
    }
    else{
        [control endRefreshing];
        
        NSLog(@"getZegoAV_ShareInstance() refreshShowList return failed!");
    }
    
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return listData.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    HomePageTableViewCell *cell = nil;
    static NSString *CellIdentifier = @"HomePageTableViewCell";
    cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    
    if (listData == nil || indexPath.row >= listData.count) {
        return cell;
    }
    
    NSMutableDictionary * dictRoom = listData[indexPath.row];
    
    if (dictRoom == nil) {
        return cell;
    }
    
    cell.attendeesCount.text = [[NSString alloc] initWithFormat:@"%@", dictRoom[PLAYER_COUNT]];

    
    //LiveRoom使用SYNC_COUNT_0这个自定义字段来存点赞数，从这里取回
    NSNumber *numberSYNC_COUNT_0 = dictRoom[SYNC_COUNT_0];
    cell.favoritesCount.text = [self formatCountString:numberSYNC_COUNT_0.unsignedIntValue];
    
    NSMutableArray * listStreams = dictRoom[SHOW_PUBLISH_LIST];
    
    
    if (listStreams == nil || listStreams.count <= 0) {
        return cell;
    }
    
    cell.publishInfo.text = [self formatPublishInfo:listStreams[0][BEGIN_TIME] endTime:listStreams[0][END_TIME]];
    cell.publisherName.text = listStreams[0][PUBLISHER_NAME];
    cell.publishTitle.text = listStreams[0][SHOW_TITLE];
    
    NSString *strPublisherPic = [self getUserPicPath:listStreams[0][PUBLISHER_ID]];
    [cell.publisherPic setImage:[UIImage imageNamed:strPublisherPic]];
    
    //封面照片，先从自定义的Extra Data取，如果没有设置封面，则这个字段为空，就从动态截屏的字段SHOW_PREVIEW_PIC_URL取
    NSString *imagePath;
    
    NSDictionary *dataExtra;
    NSObject * objExtraDatas = dictRoom[PUBLISH_EXTRA_DATAS];
    if ([objExtraDatas isKindOfClass:[NSDictionary class]]) {
        dataExtra = (NSDictionary *)objExtraDatas;
    }
    
    if (dataExtra != nil) {
        imagePath = dataExtra[COVER_FILE_KEY];
    }

    
    if (imagePath == nil) {
        //从动态截屏的字段SHOW_PREVIEW_PIC_URL取封面照片
        imagePath = listStreams[0][SHOW_PREVIEW_PIC_URL];
    }
    
    
    //加载图片
    if (imagePath != nil && imagePath.length > 0) {
        UIImage *cacheImage = dictImageCache[imagePath][@"imageData"];
        
        if (cacheImage == nil) {        //如果缓存图像不存在
            //使用默认图像
            UIImage *image = [UIImage imageNamed:@"LiveDefault.png"];
            [self setCellPreviewImage:cell.previewImage fromImage:image];
            
            //异步加载网络图片
            [self loadImageAsyncWithIndexPath:indexPath fromImagePath:imagePath];
        }else
        {
            [self setCellPreviewImage:cell.previewImage fromImage:cacheImage];
            
        }
    }
    else{
        //返回的数据没有预览图路径，使用默认图像
        UIImage *image = [UIImage imageNamed:@"LiveDefault.png"];
        [self setCellPreviewImage:cell.previewImage fromImage:image];
    }
    
    return cell;
}
#pragma mark - Table view delegate


//- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
//{
//}
//
//
//-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    /*
//    HomePageTableViewCell *cell = nil;
//    static NSString *CellIdentifier = @"HomePageTableViewCell";
//    cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
//
//    CGRect frame = cell.previewImage.frame;
//    frame.size.height = frame.size.width * cell.previewImage.image.size.height / cell.previewImage.image.size.width;
//    CGRect cellFrame = cell.frame;
//    cellFrame.size.height += frame.size.height - cell.previewImage.frame.size.height;
//    //cell.frame = cellFrame;
//
//    cell.previewImage.frame = frame;
//
//    return cellFrame.size.height;
//     */
//
//    NSMutableDictionary * dictRoom = listData[indexPath.row];
//    NSMutableArray * listStreams = dictRoom[SHOW_PUBLISH_LIST];
//
//
//    NSString *imagePath = listStreams[0][SHOW_PREVIEW_PIC_URL];
//    if (imagePath != nil && imagePath.length > 0) {
//        UIImage *cacheImage = dictImageCache[imagePath][@"imageData"];
//
//        if (cacheImage != nil) {
//            CGFloat height = 200 + cacheImage.size.height * self.view.frame.size.width/cacheImage.size.width;
//            return height;
//        }
//    }
//
//    return 500;
//}



-(void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath
{
    //如果是即将显示最后一项数据，则预先去服务器拉下一页的数据回来
    if (indexPath.row == listData.count-1) {
        UInt32 beginIndex = (UInt32)listData.count;
        [getZegoAV_ShareInstance() getReplayList:beginIndex count:ITEM_COUNT_PER_PAGE];
        
    }
}

-(void)tableView:(UITableView *)tableView didEndDisplayingCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath
{
    
}

#pragma mark 异步加载网络图片
- (void)loadImageAsyncWithIndexPath:(NSIndexPath *)indexPath fromImagePath:(NSString *)imagePath
{
    
    NSLog(@"%@",imagePath);
    
    if (dictImageCache == nil){
        dictImageCache = [[NSMutableDictionary alloc] init];
    }
    
    NSMutableDictionary *dictImageCacheItem = dictImageCache[imagePath];
    if (dictImageCacheItem == nil){
        dictImageCacheItem = [[NSMutableDictionary alloc] init];
        dictImageCache[imagePath] = dictImageCacheItem;
    }
    
    if(dictImageCacheItem[@"imageData"] != nil){ //已经有缓存图片，重新加载数据刷新表格
        [self.tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
        return;
    }
    
    NSMutableArray<NSIndexPath *> *indexList = dictImageCacheItem[@"indexList"];
    if (indexList != nil) { //还没有缓存图片，但已经在下载中，等待下载完重新加载数据刷新表格
        [indexList addObject:indexPath];
        return;
    }
    else{
        indexList = [NSMutableArray arrayWithCapacity:5];
        [indexList addObject:indexPath];
        dictImageCacheItem[@"indexList"] = indexList;
    }
    
    NSURL *imageUrl = [NSURL URLWithString:imagePath];
    
    NSURLRequest *request = [NSURLRequest requestWithURL:imageUrl];
    [NSURLConnection sendAsynchronousRequest:request queue:[NSOperationQueue mainQueue] completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
        
        UIImage *cacheImage = [UIImage imageWithData:data];
        
        if (cacheImage != nil) {
            dictImageCacheItem[@"imageData"] = cacheImage;
            
            //刷新表格对应行
            NSMutableArray<NSIndexPath *> *indexList = dictImageCacheItem[@"indexList"];
            for (int i = 0; i < indexList.count; i++) {
                [self.tableView reloadRowsAtIndexPaths:@[indexList[i]] withRowAnimation:UITableViewRowAnimationFade];
            }
            
        }
        
    }];
}


- (NSString *)formatPublishInfo:(NSString *)timeStamp endTime:(NSString *)endTimeStamp{
    
    NSDate * date = [NSDate dateWithTimeIntervalSince1970:[timeStamp doubleValue]];
    NSTimeInterval timeInterval = [date timeIntervalSinceNow] * -1;
    
    NSString * strPublishInfo = @"发布于";
    
    do{
        int months = (int)(timeInterval/(3600*24*30));
        if(months >= 1){
            strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%d月前", months];
            break;
        }
        
        int days = (int)(timeInterval/(3600*24));
        if(days >= 1){
            strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%d天前", days];
            break;
        }
        
        int hours = (int)(timeInterval/3600);
        if(hours >= 1){
            strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%d小时前", hours];
            break;
        }
        int minutes = (int)(timeInterval/60);
        if(minutes >= 1){
            strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%d分钟前", minutes];
            break;
        }
        int seconds = timeInterval;
        if(seconds >= 30){
            strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%d秒前", seconds];
        }
        else{
            strPublishInfo = @"刚刚发布";
        }
    }while (0);
    
    
    NSTimeInterval timeBegin = timeStamp.longLongValue;
    NSTimeInterval timeEnd = endTimeStamp.longLongValue;
    NSTimeInterval timeDuration = timeEnd - timeBegin;
    timeDuration = MAX(0, timeDuration);
    
    NSInteger s = timeDuration;
    NSInteger m = s / 60;
    NSInteger h = m / 60;
    
    s = s % 60;
    m = m % 60;
    
    strPublishInfo = [strPublishInfo stringByAppendingString:@"  "];
    
    if (h > 0) {
        strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%ld小时", h];
    }
    if (m > 0) {
        strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%ld分", m];
    }
    if (s > 0) {
        strPublishInfo = [strPublishInfo stringByAppendingFormat:@"%ld秒", s];
    }
    
    return strPublishInfo;
}

- (NSString *)getUserPicPath:(NSString *)userID{
    int nHeadIndexBase = 86, nHeadIndexEnd = 132;
    UInt32 nPublisherID = (UInt32)userID.longLongValue;
    return [[NSString alloc] initWithFormat:@"emoji_%03u.png", nPublisherID%(nHeadIndexEnd-nHeadIndexBase+1)+nHeadIndexBase];
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
        strCount = [[NSString alloc] initWithFormat:@"%ld", count];
        
    }
    
    return  strCount;
}

- (void)setCellPreviewImage:(UIImageView *)imageView fromImage:(UIImage *)image{
    [imageView setImage:image];
    [imageView setContentScaleFactor:[[UIScreen mainScreen] scale]];
    [imageView setContentMode:UIViewContentModeScaleAspectFill];
    [imageView setAutoresizingMask:UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight];
    [imageView setClipsToBounds:YES];
}

#pragma mark ZegoVideoDelegate
- (void) onGetReplayListResult:(uint32)result totalCount:(uint32)totalCount beginIndex:(uint32)beginIndex replayList:(NSArray*)list
{
    if (result != 0){
        NSLog(@"%@ 拉取回播列表失败：%d", self, result);

        [refreshControl endRefreshing];

        return;
    }
    
    bool needReload = false;
    
    if (beginIndex == 0) {  //如果是用户下拉刷新，触发Refresh，则返回的数据是从头开始组织的，此时替换所有数据
        listData = nil;
        dictImageCache = nil;
        
        if (list != nil){
            listData = [list mutableCopy];
        }
        
        // 3. 结束刷新
        [refreshControl endRefreshing];
        
        needReload = true;
    }
    else{   //用户上拉页面拉数据，返回的数据要续已有的数据
        if (list != nil && list.count > 0){
            
            if (listData == nil) {
                listData = [list mutableCopy];
            }
            else{
                [listData addObjectsFromArray:list];
            }
            
            needReload = true;
            
        }
    }
    
    if (needReload) {
        //刷新表格
        [self.tableView reloadData];
    }
    
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(nullable id)sender{
    
    if([segue.identifier isEqualToString:@"ReplayInLiveRoom"] == YES) {
        
        long selectedIndex = self.tableView.indexPathForSelectedRow.row;
        NSMutableDictionary * dictRoom = listData[selectedIndex];
        NSMutableArray * listStreams = dictRoom[SHOW_PUBLISH_LIST];
        
        LiveRoomViewController *lr = (LiveRoomViewController *)segue.destinationViewController;
        if ([lr isKindOfClass:[LiveRoomViewController class]]) {
            lr.roomType = LIVEROOM_TYPE_PLAYBACK;
            
            NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
            NSString *strAccountName = [ud stringForKey:@"accountName"];
            NSString *strAccountID = [[NSString alloc] initWithFormat:@"%u", (UInt32)[ud doubleForKey:@"accountID"]];
            NSString *strAccountPic = [ud stringForKey:@"accountPic"];
            
            lr.userName = strAccountName;
            lr.userID = strAccountID;
            lr.userPic = strAccountPic;
            
            if (listStreams.count > 0) {
                lr.roomTitle = listStreams[0][SHOW_TITLE];
                lr.createTime = listStreams[0][BEGIN_TIME];
                lr.endTime = listStreams[0][END_TIME];
                
                lr.publisherID = listStreams[0][PUBLISHER_ID];
                lr.publisherName = listStreams[0][PUBLISHER_NAME];
                
                lr.publisherPic = [self getUserPicPath:lr.publisherID];
                
                lr.replayPath = listStreams[0][REPLAY_URLS][0];//@"http://dianbo.zego.8686c.com/livestream.s-1-8-281476320023941-1107072285995797-8128_20151217151818.flv?wsSecret=bd2351d8bae9a7164bfd013218e2cb74&wsTime=1450960030";//listStreams[0][REPLAY_URLS][0];
            }
        }
    }
}

@end
