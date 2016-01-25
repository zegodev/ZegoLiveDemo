//
//  PopularViewController.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PopularViewController : UIViewController<UIWebViewDelegate>
@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *loadingIndicator;

@end
