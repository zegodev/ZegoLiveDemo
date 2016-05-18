//
//  ZegoAboutViewController.m
//  LiveDemo2
//
//  Created by Randy Qiu on 4/11/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ZegoAboutViewController.h"

@implementation ZegoAboutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    NSURLRequest *req = [NSURLRequest requestWithURL:[NSURL URLWithString:@"http://www.zego.im"]];
    [self.webView loadRequest:req];
}

@end
