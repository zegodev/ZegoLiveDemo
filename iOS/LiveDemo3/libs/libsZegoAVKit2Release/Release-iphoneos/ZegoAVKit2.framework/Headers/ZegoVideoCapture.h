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

@protocol ZegoVideoCaptureDelegate
- (void)onIncomingCapturedData:(nullable CVImageBufferRef)image withPresentationTimeStamp:(CMTime)time;
- (void)onTakeSnapshot:(nonnull CGImageRef)image;
@end

@protocol ZegoSupportsVideoCapture
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


@protocol ZegoVideoCaptureClientDelegate <NSObject, ZegoVideoCaptureDelegate>
- (void)destroy;
- (void)onError:(nullable NSString*)reason;
@end

@protocol ZegoVideoCaptureDevice <NSObject, ZegoSupportsVideoCapture>

@required
- (void)zego_allocateAndStart:(nonnull id<ZegoVideoCaptureClientDelegate>) client;
- (void)zego_stopAndDeAllocate;
- (int)zego_startCapture;
- (int)zego_stopCapture;

@end

@protocol ZegoVideoCaptureFactory <NSObject>

@required
- (nonnull id<ZegoVideoCaptureDevice>)zego_create:(nonnull NSString*)deviceId;
- (void)zego_destroy:(nonnull id<ZegoVideoCaptureDevice>)device;

@end

typedef NS_ENUM(NSInteger, ZegoVideoBufferType) {
    ZegoVideoBufferTypeUnknown = 0,
    ZegoVideoBufferTypeAsyncPixelBuffer = 1 << 1,
    ZegoVideoBufferTypeSyncPixelBuffer = 1 << 2,
};

@protocol ZegoVideoBufferPool <NSObject>
- (nullable CVPixelBufferRef)dequeueInputBuffer:(int)width height:(int)height stride:(int)stride;
- (void)queueInputBuffer:(nonnull CVPixelBufferRef)pixel_buffer timestamp:(unsigned long long)timestamp_100n;
@end

@protocol ZegoVideoFilterDelegate <NSObject>
- (void)onProcess:(nonnull CVPixelBufferRef)pixel_buffer withTimeStatmp:(unsigned long long)timestamp_100;
@end

@protocol ZegoVideoFilterClient <NSObject>
- (void)destroy;
@end

@protocol ZegoVideoFilter

@required
- (void)zego_allocateAndStart:(nonnull id<ZegoVideoFilterClient>) client;
- (void)zego_stopAndDeAllocate;
- (ZegoVideoBufferType)supportBufferType;
@end

@protocol ZegoVideoFilterFactory <NSObject>

@required
- (nonnull id<ZegoVideoFilter>)zego_create;
- (void)zego_destroy:(nonnull id<ZegoVideoFilter>)filter;

@end
