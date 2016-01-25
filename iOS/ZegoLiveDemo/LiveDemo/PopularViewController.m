//
//  PopularViewController.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import "PopularViewController.h"

@interface PopularViewController ()

@end

@implementation PopularViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.webView.delegate = self;
    
    NSURL *pageUrl = [NSURL URLWithString:@"http://www.zego.im"];
    NSURLRequest *request = [NSURLRequest requestWithURL:pageUrl];

    [self.webView loadRequest:request];
    
    self.loadingIndicator.hidesWhenStopped = true;
    [self.loadingIndicator startAnimating];
}

#pragma mark -- UIWebViewDelegate
- (void)webViewDidStartLoad:(UIWebView *)webView{
}
- (void)webViewDidFinishLoad:(UIWebView *)webView{
    [self.loadingIndicator stopAnimating];
    self.loadingIndicator.hidden = true;
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(nullable NSError *)error{
    [self.loadingIndicator stopAnimating];
    self.loadingIndicator.hidden = true;
}

@end

