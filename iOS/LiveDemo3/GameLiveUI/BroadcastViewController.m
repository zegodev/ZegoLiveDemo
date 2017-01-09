//
//  BroadcastViewController.m
//  GameLiveUI
//
//  Created by Strong on 2016/10/10.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "BroadcastViewController.h"

@interface BroadcastViewController ()
@property (nonatomic, weak) IBOutlet UIButton *startLiveButton;
@property (nonatomic, weak) IBOutlet UIButton *cancelButton;
@property (nonatomic, weak) IBOutlet UITextField *liveTitle;

@end

@implementation BroadcastViewController

// Called when the user has finished interacting with the view controller and a broadcast stream can start
- (void)userDidFinishSetup {
    
    // Broadcast url that will be returned to the application
    NSURL *broadcastURL = [NSURL URLWithString:@"http://broadcastURL_example/stream1"];
    
    // Service specific broadcast data example which will be supplied to the process extension during broadcast
//    NSString *userID = @"user1";
//    NSString *endpointURL = @"http://broadcastURL_example/stream1/upload";
//    NSDictionary *setupInfo = @{ @"userID" : userID, @"endpointURL" : endpointURL };
    CGSize screenSize = [[UIScreen mainScreen] bounds].size;
//    CGFloat screenScale = [[UIScreen mainScreen] scale];
    CGFloat screenScale = 1.0;
    
    NSDictionary *setupInfo = @{@"title": self.liveTitle.text, @"width": @(screenSize.width * screenScale), @"height": @(screenSize.height * screenScale)};
    
    // Set broadcast settings
    RPBroadcastConfiguration *broadcastConfig = [[RPBroadcastConfiguration alloc] init];
    broadcastConfig.clipDuration = 5.0; // deliver movie clips every 5 seconds
    
    // Tell ReplayKit that the extension is finished setting up and can begin broadcasting
    [self.extensionContext completeRequestWithBroadcastURL:broadcastURL broadcastConfiguration:broadcastConfig setupInfo:setupInfo];
}

- (void)userDidCancelSetup {
    // Tell ReplayKit that the extension was cancelled by the user
    [self.extensionContext cancelRequestWithError:[NSError errorWithDomain:@"YourAppDomain" code:-1     userInfo:nil]];
}

- (IBAction)onStartLive:(id)sender
{
    [self userDidFinishSetup];
    
    
}

- (IBAction)onCancel:(id)sender
{
    [self userDidCancelSetup];
}
@end
