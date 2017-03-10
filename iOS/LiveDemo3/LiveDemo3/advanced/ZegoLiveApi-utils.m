//
//  ZegoLiveApi-utils.m
//  zegoavkit
//
//  Created by Randy Qiu on 2016/12/9.
//  Copyright © 2016年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ZegoLiveApi-utils.h"

@implementation ZegoLiveApi (Utils)

+ (bool)createPixelBufferPool:(CVPixelBufferPoolRef*)pool width:(int)width height:(int)height
{
    CFDictionaryRef empty; // empty value for attr value.
    CFMutableDictionaryRef attrs;
    
    empty = CFDictionaryCreate(kCFAllocatorDefault,
                               NULL, NULL, 0,
                               &kCFTypeDictionaryKeyCallBacks,
                               &kCFTypeDictionaryValueCallBacks); // our empty IOSurface properties dictionary
    
    SInt32 cvPixelFormatTypeValue = kCVPixelFormatType_32BGRA;
    CFNumberRef cfPixelFormat = CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, (const void*)(&(cvPixelFormatTypeValue)));
    
    SInt32 cvWidthValue = width;
    CFNumberRef cfWidth = CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, (const void*)(&(cvWidthValue)));
    SInt32 cvHeightValue = height;
    CFNumberRef cfHeight = CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, (const void*)(&(cvHeightValue)));
    
    attrs = CFDictionaryCreateMutable(kCFAllocatorDefault,
                                      4,
                                      &kCFTypeDictionaryKeyCallBacks,
                                      &kCFTypeDictionaryValueCallBacks);
    
    CFDictionarySetValue(attrs, kCVPixelBufferIOSurfacePropertiesKey, empty);
    CFDictionarySetValue(attrs, kCVPixelBufferPixelFormatTypeKey, cfPixelFormat);
    CFDictionarySetValue(attrs, kCVPixelBufferWidthKey, cfWidth);
    CFDictionarySetValue(attrs, kCVPixelBufferHeightKey, cfHeight);
    
    CVReturn ret = CVPixelBufferPoolCreate(kCFAllocatorDefault, nil, attrs, pool);
    
    CFRelease(attrs);
    CFRelease(empty);
    CFRelease(cfPixelFormat);
    CFRelease(cfWidth);
    CFRelease(cfHeight);
    
    if (ret != kCVReturnSuccess) {
        return false;
    }
    
    return true;
}

+ (void)destroyPixelBufferPool:(CVPixelBufferPoolRef*)pool {
    CVPixelBufferPoolRelease(*pool);
    *pool = nil;
}

+ (bool)copyPixelBufferFrom:(CVPixelBufferRef)src to:(CVPixelBufferRef)dst {
    bool ret = true;
    
    CVPixelBufferLockBaseAddress(src, kCVPixelBufferLock_ReadOnly);
    
    unsigned char* pb = (unsigned char*)CVPixelBufferGetBaseAddressOfPlane(src, 0);
    int height = (int)CVPixelBufferGetHeight(src);
    int stride = (int)CVPixelBufferGetBytesPerRow(src);
    int size = (int)CVPixelBufferGetDataSize(src);
    
    while (1) {
        CVReturn cvRet = CVPixelBufferLockBaseAddress(dst, 0);
        if (cvRet != kCVReturnSuccess) {
            ret = false;
            break;
        }
        
        int dst_height = (int)CVPixelBufferGetHeight(dst);
        int dst_stride = (int)CVPixelBufferGetBytesPerRow(dst);
        int dst_size = (int)CVPixelBufferGetDataSize(dst);
        
        if (stride == dst_stride && dst_size == size) {
            unsigned char* temp = (unsigned char*)CVPixelBufferGetBaseAddressOfPlane(dst, 0);
            memcpy(temp, pb, size);
        } else {
            int copy_height = height > dst_height ? dst_height : height;
            int copy_stride = stride > dst_stride ? dst_stride : stride;
            
            unsigned char* offset_dst = (unsigned char*)CVPixelBufferGetBaseAddressOfPlane(dst, 0);
            unsigned char* offset_src = pb;
            for (int i = 0; i < copy_height; i++) {
                memcpy(offset_dst, offset_src, copy_stride);
                offset_src += stride;
                offset_dst += dst_stride;
            }
        }
        
        CVPixelBufferUnlockBaseAddress(dst, 0);
        break;
    }
    
    CVPixelBufferUnlockBaseAddress(src, kCVPixelBufferLock_ReadOnly);
    return ret;
}


@end
