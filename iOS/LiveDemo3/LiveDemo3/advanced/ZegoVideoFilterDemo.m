//
//  ZegoVideoFilterDemo.m
//  LiveDemo2
//
//  Copyright © 2016 Zego. All rights reserved.
//

#import "ZegoVideoFilterDemo.h"
#import <ZegoImageFilter/ZegoImageFilter.h>
#import "ZegoLiveApi-utils.h"

@interface ZegoVideoFilterDemo()
@property (atomic) int pendingCount;
@end

@implementation ZegoVideoFilterDemo {
    id<ZegoVideoFilterClient> client_;
    id<ZegoVideoBufferPool> buffer_pool_;
    
    dispatch_queue_t queue_;
    int width_;
    int height_;
    int stride_;
    
    CVPixelBufferPoolRef pool_;
    int buffer_count_;
    
    ZegoImageFilter* filter_;
}

- (void)zego_allocateAndStart:(id<ZegoVideoFilterClient>) client {
    client_ = client;
    if ([client_ conformsToProtocol:@protocol(ZegoVideoBufferPool)]) {
        buffer_pool_ = (id<ZegoVideoBufferPool>)client;
    }
    
    width_ = 0;
    height_ = 0;
    stride_ = 0;
    pool_ = nil;
    buffer_count_ = 4;
    self.pendingCount = 0;
    
    queue_ = dispatch_queue_create("video.filter", nil);
    dispatch_async(queue_, ^ {
        filter_ = [[ZegoImageFilter alloc] init];
        [filter_ create];
        [filter_ setCustomizedFilter:ZEGO_FILTER_BLACKWHITE];
    });

}

- (void)zego_stopAndDeAllocate {
    dispatch_sync(queue_, ^ {
        [filter_ destroy];
        filter_ = nil;
    });
    
    if (pool_) {
        [ZegoLiveApi destroyPixelBufferPool:&pool_];
    }
    queue_ = nil;
    
    [client_ destroy];
    client_ = nil;
    buffer_pool_ = nil;
}

- (ZegoVideoBufferType)supportBufferType {
    // * 返回滤镜的类型：此滤镜为异步滤镜
    return ZegoVideoBufferTypeAsyncPixelBuffer;
}

- (CVPixelBufferRef)dequeueInputBuffer:(int)width height:(int)height stride:(int)stride {
    // * 按需创建 CVPixelBufferPool
    if (width_ != width || height_ != height || stride_ != stride) {
        if (pool_) {
            [ZegoLiveApi destroyPixelBufferPool:&pool_];
        }
        
        if ([ZegoLiveApi createPixelBufferPool:&pool_ width:width height:height]) {
            width_ = width;
            height_ = height;
            stride_ = stride;
        } else {
            return nil;
        }
    }
    
    // * 如果处理不及时，未处理帧超过了 pool 的大小，则丢弃该帧
    if (self.pendingCount >= buffer_count_) {
        return nil;
    }
    
    CVPixelBufferRef pixel_buffer = nil;
    CVReturn ret = CVPixelBufferPoolCreatePixelBuffer(kCFAllocatorDefault, pool_, &pixel_buffer);
    
    if (ret != kCVReturnSuccess) {
        return nil;
    } else {
        self.pendingCount = self.pendingCount + 1;
        // * 返回一个可以用于存储采集到的图像的 CVPixelBuffer 实例
        return pixel_buffer;
    }
}

- (void)queueInputBuffer:(CVPixelBufferRef)pixel_buffer timestamp:(unsigned long long)timestamp_100n {
    // * 采集到的图像数据通过这个传进来，这个点需要异步处理
    dispatch_async(queue_, ^ {
        // * 图像滤镜处理
        CVPixelBufferRef output = [filter_ render:pixel_buffer];
        
        int imageWidth = 0;
        int imageHeight = 0;
        int imageStride = 0;
        
        if (output) {
            imageWidth = (int)CVPixelBufferGetWidth(output);
            imageHeight = (int)CVPixelBufferGetHeight(output);
            imageStride = (int)CVPixelBufferGetBytesPerRowOfPlane(output, 0);
        }
        
        // * 处理完图像，需要从 buffer pool 中取出一个 CVPixelBuffer 实例，把处理过的图像数据拷贝到该实例中
        CVPixelBufferRef dst = [buffer_pool_ dequeueInputBuffer:imageWidth height:imageHeight stride:imageStride];
        if (!dst) {
            return ;
        }
        
        if ([ZegoLiveApi copyPixelBufferFrom:output to:dst]) {
            // * 把从 buffer pool 中得到的 CVPixelBuffer 实例传进来
            [buffer_pool_ queueInputBuffer:dst timestamp:timestamp_100n];
        }
        
        self.pendingCount = self.pendingCount - 1;
        
        CVPixelBufferRelease(pixel_buffer);
    });
}
             
@end

@implementation ZegoVideoFilterDemo2 {
    id<ZegoVideoFilterClient> client_;
    id<ZegoVideoFilterDelegate> delegate_;
    ZegoImageFilter* filter_;
}

- (void)zego_allocateAndStart:(id<ZegoVideoFilterClient>) client {
    client_ = client;
    if ([client_ conformsToProtocol:@protocol(ZegoVideoFilterDelegate)]) {
        delegate_ = (id<ZegoVideoFilterDelegate>)client;
    }
    
    filter_ = [[ZegoImageFilter alloc] init];
    [filter_ create];
    [filter_ setCustomizedFilter:ZEGO_FILTER_CUSTOM_BLACKWHITE];
}

- (void)zego_stopAndDeAllocate {
    [filter_ destroy];
    filter_ = nil;

    [client_ destroy];
    client_ = nil;
    delegate_ = nil;
}

- (ZegoVideoBufferType)supportBufferType {
    // * 返回滤镜的类型：此滤镜为同步滤镜
    return ZegoVideoBufferTypeSyncPixelBuffer;
}

- (void)onProcess:(CVPixelBufferRef)pixel_buffer withTimeStatmp:(unsigned long long)timestamp_100 {
    // * 采集到的图像数据通过这个传进来，同步处理完返回处理结果
    CVPixelBufferRef output = [filter_ render:pixel_buffer];
    [delegate_ onProcess:output withTimeStatmp:timestamp_100];
}

@end


@implementation ZegoVideoFilterFactoryDemo {
    id<ZegoVideoFilter> g_filter_;
}

- (id<ZegoVideoFilter>)zego_create {
    if (g_filter_ == nil) {
        bool async = true;
        if (async) {
            g_filter_ = [[ZegoVideoFilterDemo alloc]init];
        } else {
            g_filter_ = [[ZegoVideoFilterDemo2 alloc]init];
        }
    }
    return g_filter_;
}

- (void)zego_destroy:(id<ZegoVideoFilter>)filter {
    if (g_filter_ == filter) {
        g_filter_ = nil;
    }
}

@end
