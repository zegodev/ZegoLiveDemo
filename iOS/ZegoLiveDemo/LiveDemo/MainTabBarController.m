//
//  MainTabBarController.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import "MainTabBarController.h"

@implementation MainTabBarController

- (void)viewDidAppear:(BOOL)animated{
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    if ([ud objectForKey:@"accountID"] == nil){
        [self setSelectedIndex:4];
    }
}


@end


