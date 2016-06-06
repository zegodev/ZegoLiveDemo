//
//  video_capture_external_demo.cpp
//  ve_demo
//
//  Created by robotding on 16/5/30.
//  Copyright © 2016年 jjams. All rights reserved.
//

#import "video_capture_external_demo.h"

@interface VideoCaptureDeviceDemo ()
- (int)start;
- (int)stop;
- (int)restart;

- (int)updateFrameRate;
- (int)updateVideoSize;
- (int)updateRotation;
- (int)updateTorch;
- (int)findDevice;
- (NSString*)findPreset;

- (int)createCam;
- (int)releaseCam;

- (AVCaptureVideoOrientation)findOrientation;

- (CGImageRef)createCGImageFromCVPixelBuffer:(CVPixelBufferRef)pixels ;
@end

@implementation VideoCaptureDeviceDemo {
    dispatch_queue_t m_oQueue;
    
    AVCaptureSession *m_rCapSession;
    AVCaptureDevice *m_rCapDevice;
    AVCaptureDeviceInput *m_rDeviceInput;
    AVCaptureVideoDataOutput *m_rDataOutput;
    
    struct {
        int fps;
        int width;
        int height;
        bool front;
        int rotation;
        int torch;
    } m_oSettings;
    
    struct RunningState {
        bool preview;
        bool capture;
    } m_oState;
    
    id<ZegoVideoCaptureClientDelegate> client_;
    
    bool is_take_photo_;
}

-(id)init{
    self = [super init];
    if(nil != self){
        m_oQueue = dispatch_queue_create("com.zego.ave.vcap.queue", DISPATCH_QUEUE_SERIAL);
    }
    return self;  
}

- (void)allocateAndStart:(id<ZegoVideoCaptureClientDelegate>) client {
    client_ = client;
    is_take_photo_ = false;
}

- (void)stopAndDeAllocate {
    [client_ destroy];
    client_ = nil;
    
    dispatch_sync(m_oQueue, ^ {
        return ;
    });
}

- (int)startCapture {
    if(m_oState.capture) {
        // * already started
        return 0;
    }
    
    m_oState.capture = true;
    
    if(!m_oState.preview) {
        dispatch_async(m_oQueue, ^{
            [self start];
        });
    }
    
    return 0;
}

- (int)stopCapture {
    if(!m_oState.capture) {
        // * capture is not started
        return 0;
    }
    
    m_oState.capture = false;
    
    if(!m_oState.preview) {
        // * stop the cam
        dispatch_async(m_oQueue, ^{
            [self stop];
        });
    }
    
    return 0;
}

- (int)setFrameRate:(int)framerate {
    // * no change
    if(m_oSettings.fps == framerate) {
        return 0;
    }
    
    m_oSettings.fps = framerate;
    
    dispatch_async(m_oQueue, ^{
        if(m_rCapSession) {
            [self updateFrameRate];
        }
    });
    
    return 0;
}

- (int)setWidth:(int)width andHeight:(int)height {
    // * not changed
    if ((m_oSettings.width == width) && (m_oSettings.height == height)) {
        return 0;
    }
    
    m_oSettings.width = width;
    m_oSettings.height = height;
    
    dispatch_async(m_oQueue, ^{
        if(m_rCapSession) {
            [self updateVideoSize];
            [self updateFrameRate];
        }
    });
    
    return 0;
}

- (int)setFrontCam:(int)bFront {
    if(m_oSettings.front == bFront) {
        return 0;
    }
    
    m_oSettings.front = bFront;
    
    dispatch_async(m_oQueue, ^{
        // * just restart capture
        if(m_rCapSession) {
            [self stop];
            [self start];
        }
    });
    
    return 0;
}

- (int)setView:(UIView*)view {
    dispatch_async(m_oQueue, ^{
        // * restart cam
        [self restart];
    });
    
    return 0;
}

- (int)setViewMode:(int)mode {
    return 0;
}

- (int)setCaptureRotation:(int)rotaion {
    if(m_oSettings.rotation == rotaion) {
        return 0;
    }
    
    m_oSettings.rotation = rotaion;
    
    dispatch_async(m_oQueue, ^{
        if(m_rCapSession) {
            [self updateRotation];
        }
    });
    
    return 0;
}

- (int)startPreview {
    if(m_oState.preview) {
        // * preview already started
        return 0;
    }
    
    m_oState.preview = true;
    
    if(!m_oState.capture) {
        // * let's start the cam
        dispatch_async(m_oQueue, ^{
            [self start];
        });
    }
    
    return 0;
}

- (int)stopPreview {
    if(!m_oState.preview) {
        // * preview not started
        return 0;
    }
    
    m_oState.preview = false;
    
    if(!m_oState.capture) {
        dispatch_async(m_oQueue, ^{
            [self stop];
        });
    }
    
    return 0;
}

- (int)enableTorch:(bool)enable {
    if (m_oSettings.torch == enable) {
        return 0;
    } else {
        m_oSettings.torch = enable;
    }
    
    dispatch_async(m_oQueue, ^{
        if (m_rCapSession) {
            [self updateTorch];
        }
    });
    
    return 0;
}

- (int)takeSnapshot {
    is_take_photo_ = true;
    return 0;
}

- (int)setPowerlineFreq:(unsigned int)freq {
    return 0;
}


- (int)createCam {
    [self findDevice];
    
    m_rCapSession = [[AVCaptureSession alloc] init];
    
    [m_rCapSession beginConfiguration];
    
    NSError *error = nil;
    m_rDeviceInput = [[AVCaptureDeviceInput alloc] initWithDevice:m_rCapDevice error:&error];
    
    if([m_rCapSession canAddInput:m_rDeviceInput]) {
        [m_rCapSession addInput:m_rDeviceInput];
    }
    
    m_rDataOutput = [[AVCaptureVideoDataOutput alloc] init];
    // * [m_rDataOutput setAlwaysDiscardsLateVideoFrames:NO];
    [m_rDataOutput setAlwaysDiscardsLateVideoFrames:YES];
    // * [m_rDataOutput setVideoSettings:@{(NSString*)kCVPixelBufferPixelFormatTypeKey:@(kCVPixelFormatType_32BGRA)}];
    
    // * set output to 640x480
    double dWidth = m_oSettings.width;
    double dHeight = m_oSettings.height;
    
    NSDictionary *videoSettings = [NSDictionary dictionaryWithObjectsAndKeys:
                                   [NSNumber numberWithDouble:dWidth], (id)kCVPixelBufferWidthKey,
                                   [NSNumber numberWithDouble:dHeight], (id)kCVPixelBufferHeightKey,
                                   [NSNumber numberWithInt:kCVPixelFormatType_32BGRA], (id)kCVPixelBufferPixelFormatTypeKey,
                                   nil
                                   ];
    
    // * [datOutput setVideoSettings: [NSDictionary dictionaryWithObject:[NSNumber numberWithInt:kCVPixelFormatType_32BGRA] forKey:(id)kCVPixelBufferPixelFormatTypeKey]];
    [m_rDataOutput setVideoSettings:videoSettings];

    [m_rDataOutput setSampleBufferDelegate:self queue:m_oQueue];
    
    if([m_rCapSession canAddOutput:m_rDataOutput]) {
        [m_rCapSession addOutput:m_rDataOutput];
    }
    
    // * [capSession setSessionPreset:AVCaptureSessionPresetMedium];
    NSString *strPreset = [self findPreset];
    
    if([m_rCapSession canSetSessionPreset:strPreset]) {
        [m_rCapSession setSessionPreset:strPreset];
    }
    
    // * set video orientation
    AVCaptureConnection *vconn = [m_rDataOutput connectionWithMediaType:AVMediaTypeVideo];
    if([vconn isVideoOrientationSupported]) {
        [vconn setVideoOrientation:[self findOrientation]];
    }
    
    [m_rCapSession commitConfiguration];
    
    // * set framerate
    [self updateFrameRate];
    
    // * torch
    [self updateTorch];
    
    return 0;
}

- (int)releaseCam {
    if(m_rDataOutput) {
        [m_rDataOutput setSampleBufferDelegate:nil queue:dispatch_get_main_queue()];
    }
    
    if(m_rCapSession) {
        if(m_rDeviceInput) {
            [m_rCapSession removeInput:m_rDeviceInput];
        }
        
        if(m_rDataOutput) {
            [m_rCapSession removeOutput:m_rDataOutput];
        }
    }
    
    m_rDataOutput = nil;
    m_rDeviceInput = nil;
    m_rCapDevice = nil;
    m_rCapSession = nil;
    
    return 0;
}

- (int)start {
    // * make sure we are stopped
    [self stop];
    
    [self createCam];
    
    // * [m_rCapSession startRunning];
    // * queue a async start
    
    // * lock again
    if(m_rCapSession)
    {
        [m_rCapSession startRunning];
    }
    
    return 0;
}

- (int)stop {
    if (m_rCapSession) {
        if([m_rCapSession isRunning]) {
            [m_rCapSession stopRunning];
        }
    }
    
    [self releaseCam];
    
    return 0;
}

- (int)restart {
    // * stop capture (without releasing capture)
    if (m_rCapSession) {
        if([m_rCapSession isRunning]) {
            [m_rCapSession stopRunning];
        }
    }
    
    // * queue a async start
    if(m_rCapSession) {
        [m_rCapSession startRunning];
    }
    
    return 0;
}

- (int)updateFrameRate {
    // * set framerate
    int framerate = m_oSettings.fps;
    CMTime frameDuration;
    if(framerate > 0) {
        frameDuration = CMTimeMake(1, framerate);
    } else {
        frameDuration = kCMTimeInvalid;
    }
    NSError *err = nil;
    [m_rCapDevice lockForConfiguration:&err];
    m_rCapDevice.activeVideoMaxFrameDuration = frameDuration;
    m_rCapDevice.activeVideoMinFrameDuration = frameDuration;
    [m_rCapDevice unlockForConfiguration];
    // * for(AVCaptureConnection *conn in datOutput.connections)
    // * {
    // * 	if([conn respondsToSelector:@selector(setVideoMinFrameDuration:)])
    // * 	{
    // * 		[conn setVideoMinFrameDuration:frameDuration];
    // * 	}
    // * }
    
    return 0;
}

- (int)updateVideoSize {
    [m_rCapSession beginConfiguration];
    
    // * set output to 640x480
    double dWidth = m_oSettings.width;
    double dHeight = m_oSettings.height;
    
    NSDictionary *videoSettings = [NSDictionary dictionaryWithObjectsAndKeys:
                                   [NSNumber numberWithDouble:dWidth], (id)kCVPixelBufferWidthKey,
                                   [NSNumber numberWithDouble:dHeight], (id)kCVPixelBufferHeightKey,
                                   [NSNumber numberWithInt:kCVPixelFormatType_32BGRA], (id)kCVPixelBufferPixelFormatTypeKey,
                                   nil
                                   ];
    
    // * [datOutput setVideoSettings: [NSDictionary dictionaryWithObject:[NSNumber numberWithInt:kCVPixelFormatType_32BGRA] forKey:(id)kCVPixelBufferPixelFormatTypeKey]];
    [m_rDataOutput setVideoSettings:videoSettings];
    
    NSString *strPreset = [self findPreset];
    
    if([m_rCapSession canSetSessionPreset:strPreset])
    {
        [m_rCapSession setSessionPreset:strPreset];
    }
    
    [m_rCapSession commitConfiguration];
    
    return 0;
}

- (int)updateRotation {
    [m_rCapSession beginConfiguration];
    
    AVCaptureConnection *vconn = [m_rDataOutput connectionWithMediaType:AVMediaTypeVideo];
    if([vconn isVideoOrientationSupported]) {
        [vconn setVideoOrientation:[self findOrientation]];
    }
    
    [m_rCapSession commitConfiguration];
    
    return 0;
}

- (int)updateTorch {
    AVCaptureTorchMode mode = m_oSettings.torch ? AVCaptureTorchModeOn : AVCaptureTorchModeOff;
    
    if(m_rCapDevice && [m_rCapDevice hasTorch] && [m_rCapDevice isTorchModeSupported:mode]) {
        [m_rCapDevice lockForConfiguration:nil];
        [m_rCapDevice setTorchMode:mode];
        [m_rCapDevice unlockForConfiguration];
    }
    
    return 0;
}

- (int)findDevice {
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    for(AVCaptureDevice *device in devices) {
        if(m_oSettings.front) {
            if(device.position == AVCaptureDevicePositionFront) {
                m_rCapDevice = device;
            }
        } else {
            if(device.position == AVCaptureDevicePositionBack) {
                m_rCapDevice = device;
            }
        }
    }
    
    return 0;
}

- (NSString*)findPreset {
    int width = m_oSettings.width;
    int height = m_oSettings.height;
    
    if((width >= 1920) || (height >= 1080))  {
        return AVCaptureSessionPreset1920x1080;
    } else if((width >= 1280) || (height >= 720)) {
        return AVCaptureSessionPreset1280x720;
    }
    // * else if((width >= 960) || (height >= 540))
    // * {
    // * 	return AVCaptureSessionPreset960x540;
    // * }
    else if((width >= 640) || (height >= 480))
    {
        return AVCaptureSessionPreset640x480;
    } else if((width >= 352) || (height >= 288)) {
        return AVCaptureSessionPreset352x288;
    }
    // * else if((width >= 320) || (height >= 240))
    // * {
    // * 	return AVCaptureSessionPreset320x240;
    // * }
    
    // * ??????????
    return AVCaptureSessionPreset640x480;
}

- (AVCaptureVideoOrientation)findOrientation {
    AVCaptureVideoOrientation nOrientation = AVCaptureVideoOrientationPortrait;
    
    if(m_oSettings.rotation == 0) {
        nOrientation = AVCaptureVideoOrientationPortrait;
    } else if(m_oSettings.rotation == 90) {
        nOrientation = AVCaptureVideoOrientationLandscapeLeft;
    } else if(m_oSettings.rotation == 180) {
        nOrientation = AVCaptureVideoOrientationPortraitUpsideDown;
    } else if(m_oSettings.rotation == 270) {
        nOrientation = AVCaptureVideoOrientationLandscapeRight;
    }
    
    return nOrientation;
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection {
    CMTime pts = CMSampleBufferGetPresentationTimeStamp(sampleBuffer);
    CVImageBufferRef buffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    
    if (is_take_photo_) {
        CGImageRef img = [self createCGImageFromCVPixelBuffer:buffer];
        
        [client_ onTakeSnapshot:img];
        
        CGImageRelease(img);
        is_take_photo_ = false;
    }
    
    [client_ onIncomingCapturedData:buffer withPresentationTimeStamp:pts];
}

- (CGImageRef)createCGImageFromCVPixelBuffer:(CVPixelBufferRef)pixels {
    int width = (int)CVPixelBufferGetWidth(pixels);
    int height = (int)CVPixelBufferGetHeight(pixels);
    int stride = (int)CVPixelBufferGetBytesPerRow(pixels);
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    
    CVPixelBufferLockBaseAddress(pixels, kCVPixelBufferLock_ReadOnly);
    unsigned char *bgra = (unsigned char*)CVPixelBufferGetBaseAddress(pixels);
    CFDataRef data = CFDataCreate(kCFAllocatorDefault, bgra, CVPixelBufferGetBytesPerRow(pixels)*height);
    
    unsigned char *rgba = (unsigned char*)CFDataGetBytePtr(data);
    for(int h=0; h<height; h++) {
        unsigned char *line = rgba + (h * stride);
        for(int w=0; w<width; w++) {
            unsigned char tmp = line[4*w+0];
            line[4*w+0] = line[4*w+2];
            line[4*w+2] = tmp;
        }
    }
    
    CGDataProviderRef dp = CGDataProviderCreateWithCFData(data);
    CGImageRef image = CGImageCreate(width, height, 8, 32, CVPixelBufferGetBytesPerRow(pixels), colorSpace, kCGImageAlphaPremultipliedLast, dp, NULL, NO, kCGRenderingIntentDefault);
    
    CGDataProviderRelease(dp);
    CFRelease(data);
    CGColorSpaceRelease(colorSpace);
    CVPixelBufferUnlockBaseAddress(pixels, kCVPixelBufferLock_ReadOnly);
    
    return image;
}

@end

@implementation VideoCaptureFactoryDemo {
    VideoCaptureDeviceDemo* g_device_;
}

- (id<ZegoVideoCaptureDevice>)create:(NSString*)deviceId {
    if (g_device_ == nil) {
        g_device_ = [[VideoCaptureDeviceDemo alloc]init];
    }
    return g_device_;
}

- (void)destroy:(id<ZegoVideoCaptureDevice>)device {
    
}

@end