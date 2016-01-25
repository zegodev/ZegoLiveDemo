//
//  HomePageViewController.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZegoAVKitManager.h"


@interface HomePageViewController : UIViewController<UITableViewDataSource,UITableViewDelegate, ZegoShowListDelegate>

//@property(nonatomic,strong) NSMutableArray * listData;
@property(nonatomic,weak) IBOutlet UITableView * tableView;

@end

