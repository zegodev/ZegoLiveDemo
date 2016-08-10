//
//  ZegoTabBarViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoTabBarViewController.h"
#import "ZegoAnchorViewController.h"
#import "ZegoSettings.h"
#import "ZegoPublishViewController.h"
#import "ZegoRoomViewController.h"

@interface ZegoTabBarViewController () <ZegoRoomViewControllerDelegate>

@end

@implementation ZegoTabBarViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setBarButtonItemTitle];
    
    for (UIViewController *viewController in self.viewControllers)
    {
        if ([viewController isKindOfClass:[ZegoRoomViewController class]])
        {
            ZegoRoomViewController *roomViewController = (ZegoRoomViewController *)viewController;
            roomViewController.delegate = self;
        }
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)tabBar:(UITabBar *)tabBar didSelectItem:(UITabBarItem *)item
{
    if ([tabBar.items indexOfObject:item] == 0)
    {
        [self setBarButtonItemTitle];
    }
    else
    {
        self.navigationItem.rightBarButtonItem = nil;
    }
}

- (void)setBarButtonItemTitle
{
    UIBarButtonItem *rightBarButton = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Refresh", nil) style:UIBarButtonItemStylePlain target:self action:@selector(onRightBarButton:)];
    self.navigationItem.rightBarButtonItem = rightBarButton;
}

- (void)setBarButtonItemCustomView
{
    UIActivityIndicatorView *activityView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    activityView.hidesWhenStopped = YES;
    [activityView startAnimating];
    
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:activityView];
}

- (void)onRightBarButton:(id)sender
{
    [self setBarButtonItemCustomView];
    
    UIViewController *viewController = self.selectedViewController;
    if ([viewController isKindOfClass:[ZegoRoomViewController class]])
    {
        ZegoRoomViewController *roomViewController = (ZegoRoomViewController *)viewController;
        [roomViewController refreshRoomList];
    }
}

- (void)onRefreshRoomListFinished
{
    if (self.navigationItem.rightBarButtonItem != nil)
    {
        [self setBarButtonItemTitle];
    }
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    
}


@end
