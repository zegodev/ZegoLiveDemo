//
//  ZegoVideoCaptureFromImage.m
//  LiveDemo2
//
//  Created by Randy Qiu on 6/10/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ZegoVideoCaptureFromImage.h"
#import <sys/time.h>

@implementation ZegoVideoCaptureFromImage {
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

- (void)allocateAndStart:(id<ZegoVideoCaptureClientDelegate>) client {
    client_ = client;
    is_take_photo_ = false;
}

- (void)stopAndDeAllocate {
    [client_ destroy];
    client_ = nil;
}

NSTimer *g_fps_timer = nil;
static CVPixelBufferRef pb = NULL;

- (int)startCapture {
    if(m_oState.capture) {
        // * already started
        return 0;
    }
    
    m_oState.capture = true;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [g_fps_timer invalidate];
        int fps = m_oSettings.fps > 0 ?: 15;
        fps = 15;
        NSLog(@"%s, fps: %d", __func__, fps);
        
        g_fps_timer = [NSTimer scheduledTimerWithTimeInterval:1.0/fps target:self selector:@selector(handleTick) userInfo:nil repeats:YES];
        
        if (pb) {
            CVPixelBufferRelease(pb);
            pb = NULL;
        }
    });
    return 0;
}

- (int)stopCapture {
    if(!m_oState.capture) {
        // * capture is not started
        return 0;
    }
    
    m_oState.capture = false;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [g_fps_timer invalidate];
        g_fps_timer = nil;
    });
    return 0;
}

- (int)setFrameRate:(int)framerate {
    // * no change
    if(m_oSettings.fps == framerate) {
        return 0;
    }
    
    m_oSettings.fps = framerate;
    dispatch_async(dispatch_get_main_queue(), ^{
        if (g_fps_timer) {
            [g_fps_timer invalidate];
            int fps = m_oSettings.fps > 0 ?: 15;
            g_fps_timer = [NSTimer scheduledTimerWithTimeInterval:1.0/fps target:self selector:@selector(handleTick) userInfo:nil repeats:YES];
        }
    });
    return 0;
}

- (int)setWidth:(int)width andHeight:(int)height {
    // * not changed
    // * little trick here: swap heigh and width
    if ((m_oSettings.width == height) && (m_oSettings.height == width)) {
        return 0;
    }
    
    m_oSettings.width = height;
    m_oSettings.height = width;
    return 0;
}

- (int)setFrontCam:(int)bFront { return 0; }
- (int)setView:(UIView*)view { return 0; }
- (int)setViewMode:(int)mode { return 0; }
- (int)setViewRotation:(int)rotation { return 0; }
- (int)setCaptureRotation:(int)rotaion { return 0; }

- (int)startPreview {
    if(m_oState.preview) {
        // * preview already started
        return 0;
    }
    
    m_oState.preview = true;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self startCapture];
    });
    
    return 0;
}

- (int)stopPreview {
    if(!m_oState.preview) {
        // * preview not started
        return 0;
    }
    
    m_oState.preview = false;
    
    if(!m_oState.capture) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self stopCapture];
        });
    }
    
    return 0;
}

- (int)enableTorch:(bool)enable { return 0; }
- (int)takeSnapshot { return 0; }
- (int)setPowerlineFreq:(unsigned int)freq { return 0; }

- (CGImageRef)CreateBGRAImageFromRGBAImage:(CGImageRef)rgbaImageRef {
    
    if (!rgbaImageRef) {
        return NULL;
    }
    
    const size_t bitsPerPixel = CGImageGetBitsPerPixel(rgbaImageRef);
    const size_t bitsPerComponent = CGImageGetBitsPerComponent(rgbaImageRef);
    
    const size_t channelCount = bitsPerPixel / bitsPerComponent;
    if (bitsPerPixel != 32 || channelCount != 4) {
        assert(false);
        return NULL;
    }
    
    const size_t width = CGImageGetWidth(rgbaImageRef);
    const size_t height = CGImageGetHeight(rgbaImageRef);
    const size_t bytesPerRow = CGImageGetBytesPerRow(rgbaImageRef);
    
    // rgba to bgra: swap blue and red channel
    CFDataRef bgraData = CGDataProviderCopyData(CGImageGetDataProvider(rgbaImageRef));
    UInt8 *pixelData = (UInt8 *)CFDataGetBytePtr(bgraData);
    for (size_t row = 0; row < height; row++) {
        for (size_t col = 0; col < bytesPerRow - 4; col += 4) {
            size_t idx = row * bytesPerRow + col;
            UInt8 tmpByte = pixelData[idx]; // red
            pixelData[idx] = pixelData[idx+2];
            pixelData[idx+2] = tmpByte;
        }
    }
    
    CGColorSpaceRef colorspace = CGImageGetColorSpace(rgbaImageRef);
    CGBitmapInfo bitmapInfo = CGImageGetBitmapInfo(rgbaImageRef);
    
    CGDataProviderRef provider = CGDataProviderCreateWithCFData(bgraData);
    CGImageRef bgraImageRef = CGImageCreate(width, height, bitsPerComponent, bitsPerPixel, bytesPerRow,
                                            colorspace, bitmapInfo, provider,
                                            NULL, true, kCGRenderingIntentDefault);
    
    CFRelease(bgraData);
    CGDataProviderRelease(provider);
    
    return bgraImageRef;
}

- (CVPixelBufferRef)pixelBufferFromCGImage:(CGImageRef)image {
    
    int width = m_oSettings.width;
    int height = m_oSettings.height;
    
    // 要求宽高为 16 的倍数
    int w_m = width % 16;
    if (w_m)
    {
        width += (16 - w_m);
    }
    
    int h_m = height % 16;
    if (h_m)
    {
        height += (16 - h_m);
    }
    
    CVPixelBufferRef pixelBuffer;
    CVReturn status = CVPixelBufferCreate(kCFAllocatorDefault,
                                          width,
                                          height,
                                          kCVPixelFormatType_32BGRA, // no support kCVPixelFormatType_32RGBA?
                                          NULL,
                                          &pixelBuffer);
    
    if (status != kCVReturnSuccess) {
        return NULL;
    }
    
    time_t currentTime = time(0);
    
    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
    void *data = CVPixelBufferGetBaseAddress(pixelBuffer);
    char color[4] = {0};
    
    color[0] = (currentTime * 1) % 0xFF;
    color[1] = (currentTime * 2) % 0xFF;
    color[2] = (currentTime * 3) % 0xFF;
    color[3] = (currentTime * 4) % 0xFF;
    memset_pattern4(data, color, CVPixelBufferGetDataSize(pixelBuffer));
    
    CGColorSpaceRef rgbColorSpace = CGColorSpaceCreateDeviceRGB();
    
    CGContextRef context = CGBitmapContextCreate(data,
                                                 width,
                                                 height,
                                                 8,
                                                 CVPixelBufferGetBytesPerRow(pixelBuffer),
                                                 rgbColorSpace,
                                                 kCGImageAlphaNoneSkipLast);
    
    CGImageRef bgraImage = [self CreateBGRAImageFromRGBAImage:image];
    
    CGFloat imageWith = CGImageGetWidth(image);
    CGFloat imageHeight = CGImageGetHeight(image);
    
    static CGPoint origin = {0, 0};
    static time_t lastTime = 0;
    
    if (lastTime != currentTime) {
        origin.x = rand() % (int)(width - imageWith);
        origin.y = rand() % (int)(height - imageHeight);
        
        lastTime = currentTime;
    }
    
    CGContextDrawImage(context,
                       CGRectMake(origin.x, origin.y, CGImageGetWidth(image), CGImageGetHeight(image)),
                       bgraImage);
    
    CGImageRelease(bgraImage);
    
    CGColorSpaceRelease(rgbColorSpace);
    CGContextRelease(context);
    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
    
    return pixelBuffer;
}

- (void)handleTick {
    if (pb) {
        CVPixelBufferRelease(pb);
        pb = NULL;
    }
    
    if (!pb) {
        UIImage *img = [UIImage imageNamed:@"AppIcon60x60@3x.png"];
        pb = [self pixelBufferFromCGImage:img.CGImage];
    }
    
    struct timeval tv_now;
    gettimeofday(&tv_now, NULL);
    unsigned long long t = (unsigned long long)(tv_now.tv_sec) * 1000 + tv_now.tv_usec / 1000;
    
    CMTime pts = CMTimeMakeWithSeconds(t, 1000);
    [client_ onIncomingCapturedData:pb withPresentationTimeStamp:pts];
}

@end


@implementation ZegoVideoCaptureFactory {
    ZegoVideoCaptureFromImage * g_device_;
}

- (id<ZegoVideoCaptureDevice>)create:(NSString*)deviceId {
    if (g_device_ == nil) {
        g_device_ = [[ZegoVideoCaptureFromImage alloc]init];
    }
    return g_device_;
}

- (void)destroy:(id<ZegoVideoCaptureDevice>)device {
    
}

@end

