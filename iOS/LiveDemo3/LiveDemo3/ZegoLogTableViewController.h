//
//  ZegoLogTableViewController.h
//  LiveDemo3
//
//  Created by Strong on 16/7/1.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZegoLogTableViewCell: UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *logLabel;

@end


@interface ZegoLogTableViewController : UITableViewController

@property (nonatomic, strong) NSArray *logArray;

@end
