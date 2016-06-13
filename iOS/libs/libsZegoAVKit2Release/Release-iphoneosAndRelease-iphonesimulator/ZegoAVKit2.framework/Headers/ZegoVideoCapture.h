//
//  ZegoVideoCapture.h
//  zegoavkit
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>


@protocol ZegoVideoCaptureClientDelegate <NSObject>
- (void)destroy;
- (void)onIncomingCapturedData:(CVImageBufferRef)image withPresentationTimeStamp:(CMTime)time;
- (void)onError:(NSString*)reason;
- (void)onTakeSnapshot:(CGImageRef)image;
@end

@protocol ZegoVideoCaptureDevice <NSObject>

- (void)allocateAndStart:(id<ZegoVideoCaptureClientDelegate>) client;
- (void)stopAndDeAllocate;
- (int)startCapture;
- (int)stopCapture;
- (int)setFrameRate:(int)framerate;
- (int)setWidth:(int)width andHeight:(int)height;
- (int)setFrontCam:(int)bFront;
- (int)setView:(UIView*)view;
- (int)setViewMode:(int)mode;
- (int)setViewRotation:(int)rotation;
- (int)setCaptureRotation:(int)rotaion;
- (int)startPreview;
- (int)stopPreview;
- (int)enableTorch:(bool)enable;
- (int)takeSnapshot;
- (int)setPowerlineFreq:(unsigned int)freq;

@end

@protocol ZegoVideoCaptureFactory <NSObject>

- (id<ZegoVideoCaptureDevice>)create:(NSString*)deviceId;
- (void)destroy:(id<ZegoVideoCaptureDevice>)device;

@end

