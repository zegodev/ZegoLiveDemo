//
//  CategoryViewController.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ZegoAVKitManager.h"


@interface CategoryViewController : UIViewController<UITableViewDataSource,UITableViewDelegate, ZegoShowListDelegate>
@property (weak, nonatomic) IBOutlet UITableView *tableView;


@end

