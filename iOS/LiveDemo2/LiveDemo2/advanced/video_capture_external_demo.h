//
//  video_capture_external_demo.h
//  ve_demo
//
//  Created by robotding on 16/5/30.
//  Copyright © 2016年 jjams. All rights reserved.
//

#ifndef video_capture_external_demo_h
#define video_capture_external_demo_h

#import <ZegoAVKit2/ZegoVideoCapture.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@interface DemoCaptureLink : NSObject<AVCaptureVideoDataOutputSampleBufferDelegate>
-(int) linkOutput: (AVCaptureVideoDataOutput*)output withCallback: (int (*)(void *inst, CVPixelBufferRef pb, int64_t ts, int ts_scale))cb andContext: (void*)ctx andQueue: (dispatch_queue_t)queue;
-(int) unlinkOutput: (AVCaptureVideoDataOutput*)output;
@end

namespace demo {
    class VideoCaptureFactoryDemo : public ZEGO::AV::VideoCaptureFactory {
    public:
        VideoCaptureFactoryDemo();
        virtual ~VideoCaptureFactoryDemo();
        virtual ZEGO::AV::VideoCaptureDevice* Create(const char* device_name) override;
        virtual void Destroy(ZEGO::AV::VideoCaptureDevice *vc) override;
    };
    
    class VideoCaptureDeviceDemo : public ZEGO::AV::VideoCaptureDevice {
    public:
        VideoCaptureDeviceDemo();
        void DoCaptureOutput(CVPixelBufferRef pb, int64_t ts, int ts_scale);
        
    public:
        // Prepares the video capturer for use. StopAndDeAllocate() must be called
        // before the object is deleted.
        virtual void AllocateAndStart(Client* client) override;
        
        // Deallocates the video capturer, possibly asynchronously.
        //
        // This call requires the device to do the following things, eventually: put
        // hardware into a state where other applications could use it, free the
        // memory associated with capture, and call |client| Destroy() which passed into
        // AllocateAndStart.
        //
        // If deallocation is done asynchronously, then the device implementation must
        // ensure that a subsequent AllocateAndStart() operation targeting the same ID
        // would be sequenced through the same task runner, so that deallocation
        // happens first.
        virtual void StopAndDeAllocate() override;
        
        virtual int StartCapture() override;
        virtual int StopCapture() override;
        virtual int SetFrameRate(int framerate) override;
        virtual int SetResolution(int width, int height) override;
        virtual int SetFrontCam(int bFront) override;
        virtual int SetView(void *view) override;
        virtual int SetViewMode(int nMode) override;
        virtual int SetCaptureRotation(int nRotation) override;
        virtual int StartPreview() override;
        virtual int StopPreview() override;
        virtual int EnableTorch(bool bEnable) override;
        virtual int TakeSnapshot() override;
        virtual int SetPowerlineFreq(unsigned int nFreq) override;
    protected:
        int CreateView();
        int RemoveView();
        int Start();
        int Stop();
        int Restart();
        int UpdateFrameRate();
        int UpdateVideoSize();
        int UpdateRotation();
        int UpdateTorch();
        int FindDevice();
        NSString* FindPreset();
        int CreateCapLink();
        int ReleaseCapLink();
        AVCaptureVideoOrientation FindOrientation();
        
        int CreateCam();
        int ReleaseCam();
        
        CGImageRef CreateCGImageFromCVPixelBuffer(CVPixelBufferRef pixels);
        
        static int GOnFrame(void *ctx, CVPixelBufferRef pb, int64_t ts, int ts_scale) {
            VideoCaptureDeviceDemo *pthis = (VideoCaptureDeviceDemo*)ctx;
            if(pthis) {
                pthis->DoCaptureOutput(pb, ts, ts_scale);
            }
            return 0;
        }
        
    protected:
        dispatch_queue_t m_oQueue = nil;
        
        AVCaptureSession *m_rCapSession = nil;
        AVCaptureDevice *m_rCapDevice = nil;
        AVCaptureDeviceInput *m_rDeviceInput = nil;
        AVCaptureVideoDataOutput *m_rDataOutput = nil;
        DemoCaptureLink *m_rCapLink = nil;
                
        struct
        {
            int fps = 15;
            int width = 640;
            int height = 480;
            bool front = false;
            int rotation = 0;
            int torch = false;
        } m_oSettings;
        
        struct RunningState
        {
            bool preview;
            bool capture;
        } m_oState;
        
        ZEGO::AV::VideoCaptureDevice::Client* client_;
        
        bool is_take_photo_;
    };
}

#endif /* video_capture_external_demo_h */
