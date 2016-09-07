//
//  AppDelegate.m
//  LiveDemo3
//
//  Created by Strong on 16/6/22.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "AppDelegate.h"
#import "ZegoAVKitManager.h"
#import "ZegoAnchorViewController.h"
#import "ZegoAudienceViewController.h"
#import "ZegoStreamInfo.h"

#import <TencentOpenAPI/QQApiInterface.h>
#import <TencentOpenAPI/TencentOAuth.h>

@interface AppDelegate () <UISplitViewControllerDelegate>

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    getZegoAV_ShareInstance();
    
#if !defined(__i386__)
    [[TencentOAuth alloc] initWithAppId:@"1105666430" andDelegate:nil];
#endif
    
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
{
    if ([url.absoluteString hasPrefix:@"tencent"])
    {
#if !defined(__i386__)
        return [QQApiInterface handleOpenURL:url delegate:nil];
#else
        return NO;
#endif
    }
    else if ([url.absoluteString hasPrefix:@"ZegoLiveShare"])
        return [self handleOpenLive:url];
    
    return NO;
}

- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options
{
    if ([url.absoluteString hasPrefix:@"tencent"])
    {
#if !defined(__i386__)
        return [QQApiInterface handleOpenURL:url delegate:nil];
#else
        return NO;
#endif
    }
    else if ([url.absoluteString hasPrefix:@"zegoliveshare"])
        return [self handleOpenLive:url];
    
    return NO;
}

- (BOOL)handleOpenLive:(NSURL *)url
{
    UINavigationController *navigationController = (UINavigationController *)self.window.rootViewController;
    if ([navigationController.topViewController isKindOfClass:[ZegoAnchorViewController class]] ||
        [navigationController.topViewController isKindOfClass:[ZegoAudienceViewController class]])
        return YES;
    
    NSMutableDictionary *queryStringDict = [NSMutableDictionary dictionary];
    NSString *urlParams = [[url.absoluteString componentsSeparatedByString:@"?"] lastObject];
    NSArray *urlComponents = [urlParams componentsSeparatedByString:@"&"];
    
    for (NSString *keyValuePair in urlComponents)
    {
        NSArray *pairComponents = [keyValuePair componentsSeparatedByString:@"="];
        NSString *key = [[pairComponents firstObject] stringByRemovingPercentEncoding];
        NSString *value = [[pairComponents lastObject] stringByRemovingPercentEncoding];
        
        [queryStringDict setObject:value forKey:key];
    }
    
    NSLog(@"%@", queryStringDict);
    
    unsigned int bizToken = (unsigned int)[queryStringDict[@"token"] longLongValue];
    unsigned int bizID = (unsigned int)[queryStringDict[@"id"] longLongValue];
    if (bizToken == 0 && bizID == 0)
        return YES;
    
    NSString *streamID = queryStringDict[@"stream"];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    ZegoAudienceViewController *audienceViewController = (ZegoAudienceViewController *)[storyboard instantiateViewControllerWithIdentifier:@"audienceID"];
    audienceViewController.bizToken = bizToken;
    audienceViewController.bizID = bizID;
    if (streamID.length != 0)
    {
        ZegoStreamInfo *streamInfo = [ZegoStreamInfo new];
        streamInfo.streamID = streamID;
        
        audienceViewController.currentStreamList = @[streamInfo];
    }
    
    [self.window.rootViewController presentViewController:audienceViewController animated:YES completion:nil];
    
    return YES;
}
@end
