#ifndef ZEGOAudioCapture_h
#define ZEGOAudioCapture_h

namespace AVE {
    enum AudioBufferType {
        BufferTypeAudioApp = 0, 
        BufferTypeAudioMic,
    };
    
    struct AudioCaptureFormat {
        AudioCaptureFormat() : sample_rate(0), num_channels(0), bit_depth(0), is_big_endian(false) {
        }
        
        int sample_rate;
        int num_channels;
        int bit_depth;
        bool is_big_endian;
    };
    
    class SupportsAudioCapture {
    public:
        
    };
    
    class AudioCaptureCallback {
    public:
        virtual void OnCapturedAudioData(const char *pData, int data_len,
                                         const AudioCaptureFormat& format,
                                         AudioBufferType buffer_type,
                                         double timestamp_ms) = 0;
    };
}
#endif /* ZEGOAudioCapture_h */
