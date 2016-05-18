//
//  ZegoNavigationController.m
//  LiveDemo2
//
//  Created by Randy Qiu on 4/13/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ZegoNavigationController.h"

@implementation ZegoNavigationController

- (BOOL)shouldAutorotate {
    return [self.visibleViewController shouldAutorotate];
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return [self.visibleViewController supportedInterfaceOrientations];
}

@end
