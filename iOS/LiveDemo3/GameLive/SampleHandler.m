//
//  SampleHandler.m
//  GameLive
//
//  Created by Strong on 2016/10/10.
//  Copyright © 2016年 ZEGO. All rights reserved.
//


#import "SampleHandler.h"
#import "ZegoAVKitManager.h"

//  To handle samples with a subclass of RPBroadcastSampleHandler set the following in the extension's Info.plist file:
//  - RPBroadcastProcessMode should be set to RPBroadcastProcessModeSampleBuffer
//  - NSExtensionPrincipalClass should be set to this class

@implementation SampleHandler

- (void)broadcastStartedWithSetupInfo:(NSDictionary<NSString *,NSObject *> *)setupInfo {
    // User has requested to start the broadcast. Setup info from the UI extension will be supplied.
    NSString *liveTitle = (NSString *)setupInfo[@"title"];
    CGFloat videoWidth = [(NSNumber *)setupInfo[@"width"] floatValue];
    CGFloat videoHeight = [(NSNumber *)setupInfo[@"height"] floatValue];
    
    [[ZegoAVKitManager sharedInstance] startLiveWithTitle:liveTitle videoSize:CGSizeMake(videoWidth, videoHeight)];
}

- (void)broadcastPaused {
    // User has requested to pause the broadcast. Samples will stop being delivered.
}

- (void)broadcastResumed {
    // User has requested to resume the broadcast. Samples delivery will resume.
}

- (void)broadcastFinished {
    // User has requested to finish the broadcast.
    [[ZegoAVKitManager sharedInstance] stopLive];
}

- (void)processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType {
    
    switch (sampleBufferType) {
        case RPSampleBufferTypeVideo:
            // Handle audio sample buffer
            [[ZegoAVKitManager sharedInstance] handleVideoInputSampleBuffer:sampleBuffer];
            break;
        case RPSampleBufferTypeAudioApp:
            // Handle audio sample buffer for app audio
            [[ZegoAVKitManager sharedInstance] handleAudioInputSampleBuffer:sampleBuffer withType:RPSampleBufferTypeAudioApp];
            break;
        case RPSampleBufferTypeAudioMic:
            [[ZegoAVKitManager sharedInstance] handleAudioInputSampleBuffer:sampleBuffer withType:RPSampleBufferTypeAudioMic];
            // Handle audio sample buffer for mic audio
            break;
            
        default:
            break;
    }
}

@end
