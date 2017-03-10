//
//  ZegoVideoCaptureFromImage.h
//  LiveDemo2
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <ZegoAVKit2/ZegoVideoCapture.h>

@interface ZegoVideoFilterDemo : NSObject<ZegoVideoFilter, ZegoVideoBufferPool>

@end

@interface ZegoVideoFilterDemo2 : NSObject<ZegoVideoFilter, ZegoVideoFilterDelegate>

@end

@interface ZegoVideoFilterFactoryDemo : NSObject<ZegoVideoFilterFactory>

@end
