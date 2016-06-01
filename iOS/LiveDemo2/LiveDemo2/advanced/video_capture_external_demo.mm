//
//  video_capture_external_demo.cpp
//  ve_demo
//
//  Created by robotding on 16/5/30.
//  Copyright © 2016年 jjams. All rights reserved.
//

#import "video_capture_external_demo.h"

@implementation DemoCaptureLink
{
    AVCaptureVideoDataOutput * output;
    int (*callback)(void *inst, CVPixelBufferRef pb, int64_t ts, int ts_scale);
    void *context;
    dispatch_queue_t queue;
}

-(int) linkOutput:(AVCaptureVideoDataOutput *)o withCallback:(int (*)(void *, CVPixelBufferRef, int64_t, int))cb andContext:(void *)ctx  andQueue: (dispatch_queue_t)q {
    self->output = o;
    self->callback = cb;
    self->context = ctx;
    self->queue = q;
    
    [self->output setSampleBufferDelegate:self queue:self->queue];
    
    return 0;
}

-(int) unlinkOutput:(AVCaptureVideoDataOutput *)output {
    if(self->output != nil)
    {
        [self->output setSampleBufferDelegate:nil queue:dispatch_get_main_queue()];
    }
    self->output = nil;
    self->queue = nil;
    
    return 0;
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection {
    CMTime pts = CMSampleBufferGetPresentationTimeStamp(sampleBuffer);
    CVImageBufferRef img = CMSampleBufferGetImageBuffer(sampleBuffer);
    
    self->callback(self->context, img, pts.value, pts.timescale);
}

@end

namespace demo {
    
VideoCaptureDeviceDemo* g_device_;

VideoCaptureFactoryDemo::VideoCaptureFactoryDemo() {
    g_device_ = new VideoCaptureDeviceDemo();
}

VideoCaptureFactoryDemo::~VideoCaptureFactoryDemo() {
    delete g_device_;
    g_device_ = NULL;
}

ZEGO::AV::VideoCaptureDevice* VideoCaptureFactoryDemo::Create(const char* device_name) {
    return g_device_;
}

void VideoCaptureFactoryDemo::Destroy(ZEGO::AV::VideoCaptureDevice *vc) {
    
}

VideoCaptureDeviceDemo::VideoCaptureDeviceDemo() {
    m_oQueue = dispatch_queue_create("com.zego.ave.vcap.queue", DISPATCH_QUEUE_SERIAL);
}
// Prepares the video capturer for use. StopAndDeAllocate() must be called
// before the object is deleted.
void VideoCaptureDeviceDemo::AllocateAndStart(Client* client) {
    client_ = client;
    is_take_photo_ = false;
    CreateView();
}
void VideoCaptureDeviceDemo::StopAndDeAllocate() {
    client_->Destroy();
    client_ = NULL;
    
    RemoveView();
    dispatch_sync(m_oQueue, ^ {
        return ;
    });
}

int VideoCaptureDeviceDemo::StartCapture() {
    if(m_oState.capture) {
        // * already started
        return 0;
    }
    
    m_oState.capture = true;
    
    if(m_oState.preview) {
        // * cam already started by preview
        return 0;
    }
    
    dispatch_async(m_oQueue, ^{
        Start();
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::StopCapture() {
    if(!m_oState.capture)
    {
        // * capture is not started
        return 0;
    }
    
    m_oState.capture = false;
    
    if(!m_oState.preview)
    {
        // * stop the cam
        dispatch_async(m_oQueue, ^{
            Stop();
        });
    }
    
    return 0;
}

int VideoCaptureDeviceDemo::SetFrameRate(int framerate) {
    // * no change
    if(m_oSettings.fps == framerate)
    {
        return 0;
    }
    
    m_oSettings.fps = framerate;
    
    dispatch_async(m_oQueue, ^{
        if(m_rCapSession) {
            UpdateFrameRate();
        }
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::SetResolution(int width, int height) {
    // * not changed
    if((m_oSettings.width == width) && (m_oSettings.height == height))
    {
        return 0;
    }
    
    m_oSettings.width = width;
    m_oSettings.height = height;
    
    dispatch_async(m_oQueue, ^{
        if(m_rCapSession) {
            UpdateVideoSize();
            UpdateFrameRate();
        }
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::SetFrontCam(int bFront) {
    if(m_oSettings.front == bFront) {
        return 0;
    }
    
    m_oSettings.front = bFront;
    
    dispatch_async(m_oQueue, ^{
        // * just restart capture
        if(m_rCapSession) {
            Stop();
            Start();
        }
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::SetView(void *view) {

    RemoveView();
    CreateView();
    
    dispatch_async(m_oQueue, ^{
        // * restart cam
        Restart();
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::SetViewMode(int nMode) {
    return 0;
}

int VideoCaptureDeviceDemo::SetCaptureRotation(int nRotation) {
    if(m_oSettings.rotation == nRotation) {
        return 0;
    }
    
    m_oSettings.rotation = nRotation;
    
    dispatch_async(m_oQueue, ^{
        if(m_rCapSession) {
            UpdateRotation();
        }
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::CreateView() {
    
    return 0;
}

int VideoCaptureDeviceDemo::RemoveView() {

    return 0;
}

int VideoCaptureDeviceDemo::StartPreview() {
    if(m_oState.preview)
    {
        // * preview already started
        return 0;
    }
    
    m_oState.preview = true;
    
    if(!m_oState.capture)
    {
        // * let's start the cam
        dispatch_async(m_oQueue, ^{
            Start();
        });
    }
    
    return 0;
}

int VideoCaptureDeviceDemo::StopPreview() {
    if(!m_oState.preview)
    {
        // * preview not started
        return 0;
    }
    
    m_oState.preview = false;
    
    if(!m_oState.capture)
    {
        dispatch_async(m_oQueue, ^{
            Stop();
        });
    }
    
    return 0;
}

int VideoCaptureDeviceDemo::EnableTorch(bool bEnable) {
    if (m_oSettings.torch == bEnable) {
        return 0;
    } else {
        m_oSettings.torch = bEnable;
    }
    
    dispatch_async(m_oQueue, ^{
        if (m_rCapSession) {
            UpdateTorch();
        }
    });
    
    return 0;
}

int VideoCaptureDeviceDemo::TakeSnapshot() {
    is_take_photo_ = true;
    return 0;
}

int VideoCaptureDeviceDemo::SetPowerlineFreq(unsigned int nFreq) {
    return 0;
}

int VideoCaptureDeviceDemo::CreateCam() {
    FindDevice();
    
    CreateCapLink();
    
    m_rCapSession = [[AVCaptureSession alloc] init];
    
    [m_rCapSession beginConfiguration];
    
    
    NSError *error = nil;
    m_rDeviceInput = [[AVCaptureDeviceInput alloc] initWithDevice:m_rCapDevice error:&error];
    
    if([m_rCapSession canAddInput:m_rDeviceInput]) {
        [m_rCapSession addInput:m_rDeviceInput];
    }
    
    m_rDataOutput = [[AVCaptureVideoDataOutput alloc] init];
    [m_rDataOutput setAlwaysDiscardsLateVideoFrames:YES];
    
    // * set output to 640x480
    double dWidth = m_oSettings.width;
    double dHeight = m_oSettings.height;
    
    NSDictionary *videoSettings = [NSDictionary dictionaryWithObjectsAndKeys:
                                   [NSNumber numberWithDouble:dWidth], (id)kCVPixelBufferWidthKey,
                                   [NSNumber numberWithDouble:dHeight], (id)kCVPixelBufferHeightKey,
                                   [NSNumber numberWithInt:kCVPixelFormatType_32BGRA], (id)kCVPixelBufferPixelFormatTypeKey,
                                   nil
                                   ];
    
    [m_rDataOutput setVideoSettings:videoSettings];
    
    [m_rCapLink linkOutput:m_rDataOutput withCallback:GOnFrame andContext:this andQueue:m_oQueue];
    
    if([m_rCapSession canAddOutput:m_rDataOutput]) {
        [m_rCapSession addOutput:m_rDataOutput];
    }
    
    NSString *strPreset = FindPreset();
    
    if([m_rCapSession canSetSessionPreset:strPreset]) {
        [m_rCapSession setSessionPreset:strPreset];
    }
    
    AVCaptureConnection *vconn = [m_rDataOutput connectionWithMediaType:AVMediaTypeVideo];
    if([vconn isVideoOrientationSupported]) {
        [vconn setVideoOrientation:FindOrientation()];
    }
    
    [m_rCapSession commitConfiguration];
    
    
    // * set framerate
    UpdateFrameRate();
    
    // * torch
    UpdateTorch();
    
    return 0;
}

int VideoCaptureDeviceDemo::ReleaseCam() {
    if(m_rCapLink && m_rDataOutput) {
        [m_rCapLink unlinkOutput:m_rDataOutput];
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
    
    ReleaseCapLink();
    
    return 0;
}

int VideoCaptureDeviceDemo::Start() {
    // * make sure we are stopped
    Stop();
    
    CreateCam();

    if(m_rCapSession)
    {
        [m_rCapSession startRunning];
    }
    
    return 0;
}

int VideoCaptureDeviceDemo::Stop() {
    if(m_rCapSession) {
        if([m_rCapSession isRunning]) {
            [m_rCapSession stopRunning];
        }
    }
    
    ReleaseCam();
    
    return 0;
}

int VideoCaptureDeviceDemo::Restart() {
    // * stop capture (without releasing capture)
    if(m_rCapSession) {
        if([m_rCapSession isRunning]) {
            [m_rCapSession stopRunning];
        }
    }
    
    // * queue a async start
    if(m_rCapSession)
    {
        [m_rCapSession startRunning];
    }
    
    return 0;
}

int VideoCaptureDeviceDemo::UpdateFrameRate() {
    // * set framerate
    int framerate = m_oSettings.fps;
    CMTime frameDuration;
    if(framerate > 0)
    {
        frameDuration = CMTimeMake(1, framerate);
    }
    else
    {
        frameDuration = kCMTimeInvalid;
    }
    NSError *err = nil;
    [m_rCapDevice lockForConfiguration:&err];
    m_rCapDevice.activeVideoMaxFrameDuration = frameDuration;
    m_rCapDevice.activeVideoMinFrameDuration = frameDuration;
    [m_rCapDevice unlockForConfiguration];
    
    return 0;
}

int VideoCaptureDeviceDemo::UpdateVideoSize() {
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
    
    [m_rDataOutput setVideoSettings:videoSettings];
    
    NSString *strPreset = FindPreset();
    
    if([m_rCapSession canSetSessionPreset:strPreset])
    {
        [m_rCapSession setSessionPreset:strPreset];
    }
    
    [m_rCapSession commitConfiguration];
    
    return 0;
}

int VideoCaptureDeviceDemo::UpdateRotation() {
    [m_rCapSession beginConfiguration];
    
    AVCaptureConnection *vconn = [m_rDataOutput connectionWithMediaType:AVMediaTypeVideo];
    if([vconn isVideoOrientationSupported])
    {
        [vconn setVideoOrientation:FindOrientation()];
    }
    
    [m_rCapSession commitConfiguration];
    
    return 0;
}

int VideoCaptureDeviceDemo::UpdateTorch() {
    AVCaptureTorchMode mode = m_oSettings.torch ? AVCaptureTorchModeOn : AVCaptureTorchModeOff;
    
    if(m_rCapDevice && [m_rCapDevice hasTorch] && [m_rCapDevice isTorchModeSupported:mode])
    {
        [m_rCapDevice lockForConfiguration:nil];
        [m_rCapDevice setTorchMode:mode];
        [m_rCapDevice unlockForConfiguration];
    }
    
    return 0;
}

int VideoCaptureDeviceDemo::FindDevice() {
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    for(AVCaptureDevice *device in devices)
    {
        if(m_oSettings.front)
        {
            if(device.position == AVCaptureDevicePositionFront)
            {
                m_rCapDevice = device;
            }
        }
        else
        {
            if(device.position == AVCaptureDevicePositionBack)
            {
                m_rCapDevice = device;
            }
        }
    }
    
    return 0;
}

NSString* VideoCaptureDeviceDemo::FindPreset() {
    int width = m_oSettings.width;
    int height = m_oSettings.height;
    
    if(0){}
    else if((width >= 1920) || (height >= 1080))
    {
        return AVCaptureSessionPreset1920x1080;
    }
    else if((width >= 1280) || (height >= 720))
    {
        return AVCaptureSessionPreset1280x720;
    }
    else if((width >= 640) || (height >= 480))
    {
        return AVCaptureSessionPreset640x480;
    }
    else if((width >= 352) || (height >= 288))
    {
        return AVCaptureSessionPreset352x288;
    }
    
    // * ??????????
    return AVCaptureSessionPreset640x480;
}

int VideoCaptureDeviceDemo::CreateCapLink() {
    m_rCapLink = [[DemoCaptureLink alloc] init];
    return 0;
}

int VideoCaptureDeviceDemo::ReleaseCapLink() {
    m_rCapLink = nil;
    return 0;
}

AVCaptureVideoOrientation VideoCaptureDeviceDemo::FindOrientation() {
    AVCaptureVideoOrientation nOrientation = AVCaptureVideoOrientationPortrait;
    
    if(m_oSettings.rotation == 0)
    {
        nOrientation = AVCaptureVideoOrientationPortrait;
    }
    else if(m_oSettings.rotation == 90)
    {
        nOrientation = AVCaptureVideoOrientationLandscapeLeft;
    }
    else if(m_oSettings.rotation == 180)
    {
        nOrientation = AVCaptureVideoOrientationPortraitUpsideDown;
    }
    else if(m_oSettings.rotation == 270)
    {
        nOrientation = AVCaptureVideoOrientationLandscapeRight;
    }
    
    return nOrientation;
}

void VideoCaptureDeviceDemo::DoCaptureOutput(CVPixelBufferRef pb, int64_t ts, int ts_scale) {
    
    // not thread-safe
    if (!client_) {
        return;
    }
    
    if (is_take_photo_) {
        CGImageRef img = CreateCGImageFromCVPixelBuffer(pb);
        
        client_->OnTakeSnapshot(img);
        
        CGImageRelease(img);
        is_take_photo_ = false;
    }
    
    int width = (int)CVPixelBufferGetWidth(pb);
    int height = (int)CVPixelBufferGetHeight(pb);
    int stride = (int)CVPixelBufferGetBytesPerRow(pb);
    ZEGO::AV::VideoCaptureFormat frame_format(width, height, ZEGO::AV::PIXEL_FORMAT_BGRA32);
    
    CVPixelBufferLockBaseAddress(pb, kCVPixelBufferLock_ReadOnly);
    const char* data = (const char*)CVPixelBufferGetBaseAddress(pb);
    size_t size = CVPixelBufferGetDataSize(pb);
    client_->OnIncomingCapturedData(data, size, frame_format, ts, ts_scale);
    CVPixelBufferUnlockBaseAddress(pb, kCVPixelBufferLock_ReadOnly);
    
    // add your preview code here
}
    
CGImageRef VideoCaptureDeviceDemo::CreateCGImageFromCVPixelBuffer(CVPixelBufferRef pixels) {
    int width = (int)CVPixelBufferGetWidth(pixels);
    int height = (int)CVPixelBufferGetHeight(pixels);
    int stride = (int)CVPixelBufferGetBytesPerRow(pixels);
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    
    CVPixelBufferLockBaseAddress(pixels, kCVPixelBufferLock_ReadOnly);
    unsigned char *bgra = (unsigned char*)CVPixelBufferGetBaseAddress(pixels);
    CFDataRef data = CFDataCreate(kCFAllocatorDefault, bgra, CVPixelBufferGetBytesPerRow(pixels)*height);
    
    unsigned char *rgba = (unsigned char*)CFDataGetBytePtr(data);
    for(int h=0; h<height; h++)
    {
        unsigned char *line = rgba + (h * stride);
        for(int w=0; w<width; w++)
        {
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
    
}
