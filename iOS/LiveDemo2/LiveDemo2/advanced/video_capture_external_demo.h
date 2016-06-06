//
//  video_capture_external_demo.h
//  ve_demo
//
//  Created by robotding on 16/5/30.
//  Copyright © 2016年 jjams. All rights reserved.
//

#ifndef video_capture_external_demo_h
#define video_capture_external_demo_h

#include <ZegoAVKit2/ZegoVideoCapture.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@interface VideoCaptureDeviceDemo : NSObject<ZegoVideoCaptureDevice, AVCaptureVideoDataOutputSampleBufferDelegate>
@end

@interface VideoCaptureFactoryDemo : NSObject<ZegoVideoCaptureFactory>
@end

#endif /* video_capture_external_demo_h */
