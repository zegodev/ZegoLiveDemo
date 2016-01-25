//
//  HomePageTableViewCell.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HomePageTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIImageView *publisherPic;
@property (weak, nonatomic) IBOutlet UILabel *publisherName;
@property (weak, nonatomic) IBOutlet UILabel *publishInfo;
@property (weak, nonatomic) IBOutlet UIImageView *previewImage;
@property (weak, nonatomic) IBOutlet UILabel *publishTitle;
@property (weak, nonatomic) IBOutlet UILabel *favoritesCount;
@property (weak, nonatomic) IBOutlet UILabel *attendeesCount;

@end
