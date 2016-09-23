//
//  ZegoVideoCapture.h
//  zegoavkit
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#if TARGET_OS_IPHONE
#import <UIKit/UIKit.h>
#elif TARGET_OS_MAC
#import <AppKit/AppKit.h>
#endif


@protocol ZegoVideoCaptureClientDelegate <NSObject>
- (void)destroy;
- (void)onIncomingCapturedData:(nullable CVImageBufferRef)image withPresentationTimeStamp:(CMTime)time;
- (void)onError:(nullable NSString*)reason;
- (void)onTakeSnapshot:(nonnull CGImageRef)image;
@end

@protocol ZegoVideoCaptureDevice <NSObject>

@required
- (void)zego_allocateAndStart:(nonnull id<ZegoVideoCaptureClientDelegate>) client;
- (void)zego_stopAndDeAllocate;
- (int)zego_startCapture;
- (int)zego_stopCapture;

@optional
- (int)zego_setFrameRate:(int)framerate;
- (int)zego_setWidth:(int)width andHeight:(int)height;
- (int)zego_setFrontCam:(int)bFront;
#if TARGET_OS_IPHONE
- (int)zego_setView:(UIView* _Nullable )view;
#elif TARGET_OS_MAC
- (int)zego_setView:(NSView* _Nullable )view;
#endif
- (int)zego_setViewMode:(int)mode;
- (int)zego_setViewRotation:(int)rotation;
- (int)zego_setCaptureRotation:(int)rotaion;
- (int)zego_startPreview;
- (int)zego_stopPreview;
- (int)zego_enableTorch:(bool)enable;
- (int)zego_takeSnapshot;
- (int)zego_setPowerlineFreq:(unsigned int)freq;

@end

@protocol ZegoVideoCaptureFactory <NSObject>

@required
- (nonnull id<ZegoVideoCaptureDevice>)create:(nonnull NSString*)deviceId;
- (void)destroy:(nonnull id<ZegoVideoCaptureDevice>)device;

@end

