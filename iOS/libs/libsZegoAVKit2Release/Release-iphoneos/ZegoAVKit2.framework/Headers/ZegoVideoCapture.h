#ifndef ZEGOVideoCapture_h
#define ZEGOVideoCapture_h

namespace ZEGO {
    namespace AV {
        enum VideoPixelFormat {
            PIXEL_FORMAT_UNKNOWN = 0,  // Unknown or unspecified format value.
            PIXEL_FORMAT_I420 = 1,  // 12bpp YUV planar 1x1 Y, 2x2 UV samples, a.k.a. YU12.
            PIXEL_FORMAT_NV12 = 2,  // 12bpp with Y plane followed by a 2x2 interleaved UV plane.
            PIXEL_FORMAT_NV21 = 3,  // 12bpp with Y plane followed by a 2x2 interleaved VU plane.
            PIXEL_FORMAT_BGRA32 = 4,  // 32bpp BGRA, 1 plane.
            PIXEL_FORMAT_RGBA32 = 5,
        };
        
        // Video capture format specification.
        // This class is used by the video capture device to specify the format of every
        // frame captured and returned to a client.
        struct VideoCaptureFormat {
            VideoCaptureFormat() : width(0), height(0), pixel_format(PIXEL_FORMAT_UNKNOWN) {
            }
            
            VideoCaptureFormat(int width, int height, VideoPixelFormat pixel_format)
                : width(width), height(height), pixel_format(pixel_format) {
            }
                    
            int width;
            int height;
            VideoPixelFormat pixel_format;
        };
        
        class VideoCaptureDevice {
        public:
            class Client {
            public:
                virtual ~Client() {}
                
                virtual void Destroy() = 0;
                
                // Captured a new video frame, data for which is pointed to by |data|.
                //
                // The format of the frame is described by |frame_format|, and is assumed to
                // be tightly packed. This method will try to reserve an output buffer and
                // copy from |data| into the output buffer. If no output buffer is
                // available, the frame will be silently dropped. |reference_time| is
                // system clock time when we detect the capture happens, it is used for
                // Audio/Video sync, not an exact presentation time for playout, because it
                // could contain noise.
                virtual void OnIncomingCapturedData(const char* data,
                                                    int length,
                                                    const VideoCaptureFormat& frame_format,
                                                    unsigned long long reference_time,
                                                    unsigned int reference_time_scale) = 0;
        
                // An error has occurred that cannot be handled and VideoCaptureDevice must
                // be StopAndDeAllocate()-ed. |reason| is a text description of the error.
                virtual void OnError(const char* reason) = 0;
                
                virtual void OnTakeSnapshot(void* image) = 0;
            };
            
        public:
            // Prepares the video capturer for use. StopAndDeAllocate() must be called
            // before the object is deleted.
            virtual void AllocateAndStart(Client* client) = 0;
            
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
            virtual void StopAndDeAllocate() = 0;
            
            virtual int StartCapture() = 0;
            virtual int StopCapture() = 0;
            virtual int SetFrameRate(int framerate) = 0;
            virtual int SetResolution(int width, int height) = 0;
            virtual int SetFrontCam(int bFront) = 0;
            virtual int SetView(void *view) = 0;
            virtual int SetViewMode(int nMode) = 0;
            virtual int SetCaptureRotation(int nRotation) = 0;
            virtual int StartPreview() = 0;
            virtual int StopPreview() = 0;
            virtual int EnableTorch(bool bEnable) = 0;
            virtual int TakeSnapshot() = 0;
            virtual int SetPowerlineFreq(unsigned int nFreq) { return 0; }
        };
        
        class VideoCaptureFactory {
        public:
            virtual ~VideoCaptureFactory() {}
            virtual VideoCaptureDevice* Create(const char* device_id) = 0;
            virtual void Destroy(VideoCaptureDevice *vc) = 0;
        };
    }
}
#endif /* ZEGOVideoCapture_h */
