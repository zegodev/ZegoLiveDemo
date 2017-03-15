#ifndef ZEGOVideoCapture_h
#define ZEGOVideoCapture_h

namespace AVE {
    enum VideoPixelFormat {
        PIXEL_FORMAT_UNKNOWN = 0,
        PIXEL_FORMAT_I420 = 1,
        PIXEL_FORMAT_NV12 = 2,
        PIXEL_FORMAT_NV21 = 3,
        PIXEL_FORMAT_BGRA32 = 4,
        PIXEL_FORMAT_RGBA32 = 5,
    };
  
    struct VideoCaptureFormat {
        VideoCaptureFormat() : width(0), height(0), pixel_format(PIXEL_FORMAT_UNKNOWN), rotation(0) {
            strides[0] = strides[1] = strides[2] = strides[3] = 0;
        }
        
        VideoCaptureFormat(int width, int height, VideoPixelFormat pixel_format)
            : width(width), height(height), pixel_format(pixel_format), rotation(0) {
            strides[0] = strides[1] = strides[2] = strides[3] = 0;
        }
        
        bool operator==(const VideoCaptureFormat& other) {
            return (width == other.width && height == other.height &&
                    strides[0] == other.strides[0] && strides[1] == other.strides[1] &&
                    strides[2] == other.strides[2] && strides[3] == other.strides[3] &&
                    rotation == other.rotation && pixel_format == other.pixel_format);
        }
        
        bool operator!=(const VideoCaptureFormat& other) {
            return !(*this == other);
        }
        
        int width;
        int height;
        int strides[4];
        int rotation;
        VideoPixelFormat pixel_format;
    };
    
    class SupportsVideoCapture {
    public:
        /// \brief 设置采集帧率
        /// \param framerate 帧率，一般为10，15，20，30
        /// \note SDK SetVideoFPS异步调用，透传该方法入参
        /// \note 可以不实现
        virtual int SetFrameRate(int framerate) = 0;
        
        /// \brief 设置采集分辨率，采集的分辨率最大不能超过1920*1080
        /// \param width 宽
        /// \param height 高
        /// \note SDK SetVideoResolution异步调用，透传该方法入参
        /// \note 可以不实现
        virtual int SetResolution(int width, int height) = 0;
        
        /// \brief 切换前后摄像头，移动端专用，PC端不需要实现
        /// \param bFront true表示前摄像头，false表示后摄像头
        /// \note SDK SetFrontCam异步调用，透传该方法入参
        /// \note 可以不实现
        virtual int SetFrontCam(int bFront) = 0;
        
        /// \brief 设置采集使用载体
        /// \param view 跨平台预览载体指针
        /// \note SDK SetPreviewView同步调用，透传该方法入参
        /// \note 可以不实现
        virtual int SetView(void *view) = 0;
        
        /// \brief 设置采集预览的模式
        /// \param nMode 取值参考ZegoVideoViewMode
        /// \note SDK SetPreviewViewMode异步调用，透传该方法入参
        /// \note 可以不实现
        virtual int SetViewMode(int nMode) = 0;
        
        /// \brief 设置采集预览的逆时针旋转角度
        /// \param nRotation 值为0,90,180,270
        /// \note SDK SetDisplayRotation 异步调用，主要用于修复移动端的横竖屏旋转问题
        /// \note 可以不实现
        virtual int SetViewRotation(int nRotation) = 0;
        
        /// \brief 设置采集buffer的顺时针旋转角度
        /// \param nRotation 值为0,90,180,270
        /// \note SDK SetDisplayRotation 异步调用，主要用于修复移动端的横竖屏旋转问题
        /// \note 可以不实现
        virtual int SetCaptureRotation(int nRotation) = 0;
        
        /// \brief 启动预览
        /// \note SDK StartPreview异步调用
        /// \note 可以不实现
        virtual int StartPreview() = 0;
        
        /// \brief 停止预览
        /// \note SDK StopPreview异步调用
        /// \note 可以不实现
        virtual int StopPreview() = 0;
        
        /// \brief 打开闪光灯
        /// \param bEnable true表示打开，false表示关闭
        /// \note SDK EnableTorch异步调用
        /// \note 可以不实现
        virtual int EnableTorch(bool bEnable) = 0;
        
        /// \brief 对采集预览进行截图，完成后通过client的OnTakeSnapshot方法通知SDK
        /// \note SDK TakeSnapshotPreview异步调用
        /// \note 可以不实现
        virtual int TakeSnapshot() = 0;
        
        /// \brief 设置采集刷新率
        /// \param nFreq 刷新频率
        /// \note 可以不实现
        virtual int SetPowerlineFreq(unsigned int nFreq) { return 0; }
    };
    
    class VideoCaptureCallback {
    public:
        /// \brief 通知SDK采集到视频数据，SDK会同步拷贝数据，切换到内部线程进行编码，如果缓冲队列不够，SDK会自动丢帧
        /// \param data 采集buffer指针
        /// \param length 采集buffer的长度
        /// \param frame_format 描述buffer的属性，包括了宽高，色彩空间
        /// \param reference_time 采集到该帧的时间戳，用于音画同步，如果采集实现是摄像头，最好使用系统采集回调的原始时间戳，如果不是，最好是生成该帧的UTC时间戳
        /// \param reference_time_scale 采集时间戳单位，毫秒10^3，微妙10^6，纳秒10^9，精度不能低于毫秒
        /// \note SDK不会做额外的裁剪缩放，编码器分辨率以frame_format内的宽高为准
        virtual void OnIncomingCapturedData(const char* data,
                                            int length,
                                            const VideoCaptureFormat& frame_format,
                                            unsigned long long reference_time,
                                            unsigned int reference_time_scale) = 0;
        
        /// \brief 通知SDK截图成功
        /// \param image 图像数据
        virtual void OnTakeSnapshot(void* image) = 0;
    };
    
    class VideoCaptureCVPixelBufferCallback {
    public:
        virtual void OnIncomingCapturedData(void* buffer,
                                            double reference_time_ms) = 0;
    };
    
    class VideoCaptureSurfaceTextureCallback {
    public:
        virtual void* GetSurfaceTexture() = 0;
    };
    
    class VideoCaptureTextureCallback {
    public:
        virtual void OnIncomingCapturedData(int texture_id, int width, int height, double reference_time_ms) = 0;
    };
    
    enum VideoPixelBufferType {
        PIXEL_BUFFER_TYPE_UNKNOWN = 0,
        PIXEL_BUFFER_TYPE_MEM = 1 << 0,
        PIXEL_BUFFER_TYPE_CV_PIXEL_BUFFER = 1 << 1,
        PIXEL_BUFFER_TYPE_SURFACE_TEXTURE = 1 << 2,
        PIXEL_BUFFER_TYPE_GL_TEXTURE_2D = 1 << 3,
    };
    
    class VideoCaptureDeviceBase {
    public:
        class Client {
        public:
            virtual ~Client() {}
            
			/// \brief 通知SDK销毁采集回调
			/// \note 调用此方法后，将client对象置空即可，不要做delete操作
            virtual void Destroy() = 0;
            
            virtual void OnError(const char* reason) = 0;
            
            virtual void* GetInterface() = 0;
        };
        
    public:       
		/// \brief 初始化采集使用的资源，例如启动线程，保存SDK传递的回调
		/// \param client SDK实现回调的对象，一定要保存
		/// \note SDK 第一次调用StartPublish或PlayStream时调用，
        /// \note 接口调用顺序:1、VideoCaptureFactory::Create 2、VideoCaptureDevice::AllocateAndStart
		/// \note 一定要实现
        virtual void AllocateAndStart(Client* client) = 0;
        
		/// \brief 停止并且释放采集占用的资源，同时调用client的Destroy方法，这里的client指的是AllocateAndStart传递的client
		/// \note SDK LogoutChannel时调用，如果是异步实现，实现者必须保证AllocateAndStart()和StopAndDeAllocate()在同一个线程执行
        /// \note 接口调用顺序:1、VideoCaptureDevice::StopAndDeAllocate 2、VideoCaptureFactory::Destroy
		/// \note 一定要实现
        virtual void StopAndDeAllocate() = 0;
        
		/// \brief 启动采集，采集的数据通过client对象的OnIncomingCapturedData通知SDK
		/// \note SDK StartPublish异步调用
        /// \note SDK回调StartCapture后开始推数据才有效，否则数据会被丢弃
		/// \note 一定要实现，不要做丢帧逻辑，SDK内部已经包含了丢帧策略
        virtual int StartCapture() = 0;

		/// \brief 停止采集
		/// \note SDK StopPublish异步调用
        /// \note SDK回调StopCapture后，不再接收任何采集数据
		/// \note 一定要实现
        virtual int StopCapture() = 0;
        
        virtual VideoPixelBufferType SupportBufferType() = 0;
        virtual void* GetInterface() = 0;
    };
    
    class VideoCaptureDevice : public VideoCaptureDeviceBase, public SupportsVideoCapture {
    public:
        class Client : public VideoCaptureDeviceBase::Client, public VideoCaptureCallback {
        public:
            virtual ~Client() {}
            virtual void* GetInterface() {
                return (VideoCaptureCallback*)this;
            }
        };
    
    public :
        virtual void AllocateAndStart(Client* client) = 0;
        
        virtual void AllocateAndStart(VideoCaptureDeviceBase::Client* client) override {
            this->AllocateAndStart((Client*)client);
        }
        
        virtual VideoPixelBufferType SupportBufferType() override {
            return VideoPixelBufferType::PIXEL_BUFFER_TYPE_MEM;
        }
        
        virtual void* GetInterface() override {
            return (SupportsVideoCapture*)this;
        }
    };
    
    class VideoCaptureFactory {
    public:
        virtual ~VideoCaptureFactory() {}
		/// \brief 创建采集设备
		/// \param device_id SDK SetVideoDevice透传的参数
		/// \note SDK第一次StartPublish或者PlayStream时异步调用
		/// \note 一定要实现
        virtual VideoCaptureDeviceBase* Create(const char* device_id) = 0;

		/// \brief 销毁采集设备
		/// \param vc Create方法返回的采集对象
		/// \note SDK LogoutChannel时同步调用
		/// \note 一定要实现
        virtual void Destroy(VideoCaptureDeviceBase *vc) = 0;
    };
    
    enum VideoBufferType {
        BUFFER_TYPE_UNKNOWN = 0,
        BUFFER_TYPE_MEM = 1 << 0,
        BUFFER_TYPE_ASYNC_PIXEL_BUFFER = 1 << 1,
        BUFFER_TYPE_SYNC_PIXEL_BUFFER = 1 << 2,
        BUFFER_TYPE_SURFACE_TEXTURE = 1 << 3,
        BUFFER_TYPE_HYBRID_MEM_GL_TEXTURE_2D = 1 << 4,
    };

    class VideoBufferPool {
    public:
        virtual int DequeueInputBuffer(int width, int height, int stride) = 0;
        virtual void* GetInputBuffer(int index) = 0;
        virtual void QueueInputBuffer(int index, int width, int height, int stride, unsigned long long timestamp_100n) = 0;
    };
    
    class VideoFilterCallback {
    public:
        virtual void OnProcess(void* buffer, int size, int width, int height, int stride, unsigned long long timestamp_100n) = 0;
    };
    
    class VideoFilter {
    public:
        class Client {
        public:
            virtual ~Client() {}
            virtual void Destroy() = 0;
            virtual void* GetInterface() = 0;
        };
        
    public:
        virtual void AllocateAndStart(Client* client) = 0;
        virtual void StopAndDeAllocate() = 0;
        virtual VideoBufferType SupportBufferType() = 0;
        virtual void* GetInterface() = 0;
    };
    
    class VideoFilterFactory {
    public:
        virtual ~VideoFilterFactory() {}
        
        virtual VideoFilter* Create() = 0;
        virtual void Destroy(VideoFilter *vf) = 0;
    };
}
#endif /* ZEGOVideoCapture_h */
