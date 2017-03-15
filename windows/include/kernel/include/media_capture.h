#ifndef ZEGOMediaCapture_h
#define ZEGOMediaCapture_h

#include "./video_capture.h"
#include "./audio_capture.h"

namespace AVE {

class MediaCaptureDevice {
public:
    class Client {
    public:
        virtual ~Client() {}
        
        /// \brief 通知SDK销毁采集回调
        /// \note 调用此方法后，将client对象置空即可，不要做delete操作
        virtual void Destroy() = 0;
        
        virtual void OnError(const char* reason) = 0;
        
        virtual void* GetVideoInterface() = 0;
        virtual void* GetAudioInterface() = 0;
    };
    
public:
    virtual void AllocateAndStart(Client* client) = 0;
    
    virtual void StopAndDeAllocate() = 0;
    
    virtual int StartCapture() = 0;
    
    virtual int StopCapture() = 0;
    
    virtual VideoPixelBufferType SupportBufferType() = 0;
};

class MediaCaptureFactory {
public:
    virtual ~MediaCaptureFactory() {}
    
    virtual MediaCaptureDevice* Create(const char* device_id) = 0;
    
    virtual void Destroy(MediaCaptureDevice *vc) = 0;
};

}

#endif /* ZEGOMediaCapture_h */
