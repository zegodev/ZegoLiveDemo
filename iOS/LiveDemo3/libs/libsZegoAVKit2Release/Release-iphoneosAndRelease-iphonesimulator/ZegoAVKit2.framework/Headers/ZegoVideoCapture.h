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


@protocol ZegoVideoCaptureDelegate <NSObject>

/// \brief 接收视频帧数据
/// \param image 采集到的视频数据
/// \param time 采集时间戳
- (void)onIncomingCapturedData:(nonnull CVImageBufferRef)image withPresentationTimeStamp:(CMTime)time;

@optional
- (void)onTakeSnapshot:(nonnull CGImageRef)image;   // * 已废弃

@end

/// \brief 负责接收视频采集数据的接口
@protocol ZegoVideoCaptureClientDelegate <NSObject, ZegoVideoCaptureDelegate>
- (void)destroy;
- (void)onError:(nullable NSString*)reason;
@end


@protocol ZegoSupportsVideoCapture;

/// \brief 视频采集设备接口
@protocol ZegoVideoCaptureDevice <NSObject, ZegoSupportsVideoCapture>

@required
/// \brief 初始化采集使用的资源，例如启动线程，保存SDK传递的回调
/// \param client SDK 实现回调的对象，一定要保存
/// \note SDK 第一次调用开始预览／推流／拉流时调用
- (void)zego_allocateAndStart:(nonnull id<ZegoVideoCaptureClientDelegate>) client;

/// \brief 停止并且释放采集占用的资源
/// \note 在此之后，不能再调用 client 对象的接口
- (void)zego_stopAndDeAllocate;

/// \brief 启动采集，采集的数据通过 client -onIncomingCapturedData:withPresentationTimeStamp: 通知SDK
/// \note 一定要实现，不要做丢帧逻辑，SDK内部已经包含了丢帧策略
- (int)zego_startCapture;

/// \brief 停止采集
/// \note 一定要实现
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
