//
//  ZegoRoomViewController.h
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZegoRoomTableViewCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *publishTitleLabel;
@property (nonatomic, weak) IBOutlet UILabel *livesCountLabel;

@end
@protocol ZegoRoomViewControllerDelegate <NSObject>

- (void)onRefreshRoomListFinished;

@end

@interface ZegoRoomViewController : UIViewController

- (void)refreshRoomList;

@property (nonatomic, weak) id<ZegoRoomViewControllerDelegate> delegate;

@end
