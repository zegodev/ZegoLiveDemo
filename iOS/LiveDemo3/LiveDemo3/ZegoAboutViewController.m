//
//  ZegoAbouViewController.m
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoAboutViewController.h"

@interface ZegoAboutViewController () <UIWebViewDelegate>

@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIProgressView *progressView;

@property (nonatomic, assign) BOOL isFininshedLoad;
@property (nonatomic, strong) NSTimer *progressTimer;

@end

@implementation ZegoAboutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    NSURLRequest *req = [NSURLRequest requestWithURL:[NSURL URLWithString:@"https://www.zego.im"]];
    [self.webView loadRequest:req];
    self.progressView.progress = 0;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    self.progressView.progress = 0;
    self.isFininshedLoad = NO;
    
    self.progressTimer = [NSTimer scheduledTimerWithTimeInterval:0.01667 target:self selector:@selector(timerCallback) userInfo:nil repeats:YES];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    self.isFininshedLoad = YES;
}

- (void)timerCallback
{
    if (self.isFininshedLoad)
    {
        if (self.progressView.progress >= 1)
        {
            self.progressView.hidden = YES;
            [self.progressTimer invalidate];
        }
        else
        {
            self.progressView.progress += 0.1;
        }
    }
    else
    {
        self.progressView.progress += 0.05;
        if (self.progressView.progress >= 0.95)
            self.progressView.progress = 0.95;
    }
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
