#ifndef ZegoAVApi_h
#define ZegoAVApi_h

#include "./ZegoAVDefines.h"
#include "./ZegoAVCallback.h"
#include "./ZegoAVApi-advanced.h"
#include "./ZegoAVApi-deprecated.h"

namespace ZEGO
{
	namespace AV
	{
        /// \brief 启动log，设置log级别和log输出目录
        /// \param[in] nLevel log级别，参考 enum ZegoLogLevel
        /// \param[in] strLogDir log输出目录
        /// \note 在InitSDK之前调用，这样才不会丢掉初始化的log，程序生命期中只调用一次
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetLogLevel(ZegoLogLevel level, const char* pszLogDir);
		
		/// \brief 初始化SDK
		/// \param[in] appID Zego派发的数字ID, 各个开发者的唯一标识
		/// \param[in] appSignature Zego派发的签名, 用来校验对应appID的合法性
		/// \return true: 调用成功; false: 调用失败
		ZEGOAVKIT_API bool InitSDK(unsigned int uiAppID, unsigned char* pBufAppSignature, int nSignatureSize);

		/// \brief 反初始化SDK
		/// \return true: 调用成功; false: 调用失败
        /// \note Windows 平台会等到真正停止才返回
		ZEGOAVKIT_API bool UninitSDK();

        /// \brief 设置Zego房间相关信息通知的回调
        /// \param[in] pCallback 回调对象指针
        /// \note 可以在InitSDK之前调用，但只能调用一次
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetCallback(IZegoLiveCallback* pCallback);

        /// \brief 设置音频视频设备变化的回调
        /// \param[in] pCallback 回调对象指针
        ZEGOAVKIT_API void SetDeviceStateCallback(IZegoDeviceStateCallback *pCallback);

        /// \brief 获取直播流列表
		/// \param userID 用户 ID，每个用户唯一
		/// \param userName 用户名
		/// \param pszChannelID 频道 ID
		/// \return true 成功，等待异步结果回调，否则失败
		ZEGOAVKIT_API bool LoginChannel(const char* pszUserID, const char* pszUserName, const char* pszChannelID, int nNetType);

        /// \brief 退出直播频道
        /// \note 会停止所有的推拉流
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool LogoutChannel();

		/// \brief 播放
		/// \param pszStreamID 流 ID
		/// \param uiChnIndex 播放通道
		/// \return true 成功，等待异步结果回调，否则失败
		ZEGOAVKIT_API bool PlayStream(const char* pszStreamID, unsigned int uiChnIndex);

		/// \brief 停止播放
		/// \param streamID 流 ID
		/// \return true 成功，等待异步结果回调，否则失败
		ZEGOAVKIT_API bool StopPlayStream(const char* pszStreamID);

		/// \brief 设置视频播放载体 view
        /// \param[in] pView view 指针
        /// \param[in] uiChnIndex 播放通道
        /// \return true: 调用成功; false: 调用失败
		ZEGOAVKIT_API bool SetView(void* pView, unsigned int uiChnIndex = 0);

        /// \breif 设置播放视频模式
        /// \param[in] mode 模式
        /// \param[in] uiChnIndex 播放通道
        /// \return true: 调用成功; false: 调用失败
		ZEGOAVKIT_API bool SetViewMode(ZegoVideoViewMode mode, unsigned int uiChnIndex);
        
        /// \brief 手机外放开关
        /// \param bEnalbe 是否开启，true 声音从外放播放
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetSpeakerPhoneOn(bool bOn);
        
        /// \brief 播放声音开关
        /// \param bEnalbe 是否开启
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool EnableSpk(bool bEnable);

        /// \brief 设置视频预览载体 view
        /// \param[in] pView view 指针
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetPreviewView(void* pView);

        /// \breif 设置预览视频模式
        /// \param[in] mode 模式
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetPreviewViewMode(ZegoVideoViewMode mode);
        
        /// \brief 开启预览
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool StartPreview();
        
        /// \brief 关闭预览
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool StopPreview();
        
        /// \brief 开始直播
        /// \param[in] pszTitle 直播的名称
        /// \param[in] pszStreamID 流ID
        /// \param[in] pszMixStreamID 混流ID
        /// \param[in] nMixVideoWidth 混流后视频的宽
        /// \param[in] nMixVideoHeight 混流后视频的高
        /// \param[in] flag 直播属性 参考 enum ZegoPublishFlag 定义
        /// \return true:调用成功，IZegoVideoCallback中的OnPublishSucc或OnPublishStop回调直播结果；false:调用失败
        ZEGOAVKIT_API bool StartPublish(const char*  pszTitle, const char* pszStreamID, const char* pszMixStreamID = 0, int nMixVideoWidth = 0, int nMixVideoHeight = 0, int flag = 0);
        
        /// \brief 停止直播
        /// \param flag 保留字段
        /// \param pszMsg 自定义信息，server对接流结束回调包含此字段内容
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool StopPublish(int flag = 0, const char* pszMsg = 0);
        
        /// \brief 设置视频码率
        /// \param[in] nBitrate 码率
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool SetVideoBitrate(int nBitrate);
        
        /// \brief 设置视频帧率
        /// \param[in] nFps 帧率
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool SetVideoFPS(int nFps);

        /// \brief 设置手机姿势，用于校正主播输出视频朝向
        /// \param nOrientation 手机姿势
        ZEGOAVKIT_API bool SetAppOrientation(int nOrientation);
        
        /// \brief 设置视频编码输出分辨率
        /// \param[in] nWidth 宽
        /// \param[in] nHeight 高
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetVideoEncodeResolution(int nWidth, int nHeight);
        
        /// \brief 设置视频采集分辨率
        /// \param[in] nWidth 宽
        /// \param[in] nHeight 高
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetVideoCaptureResolution(int nWidth, int nHeight);
        
        /// \brief 前摄像头开关
        /// \param bFront true 前摄像头, false 后摄像头
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool SetFrontCam(bool bFront);

        /// \brief 音频采集开关
        /// \param bEnalbe 是否开启
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool EnableMic(bool bEnable);
        
        /// \brief 摄像头开关
        /// \param bEnalbe 是否开启
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool EnableCamera(bool bEnable);

        /// \brief 手机手电筒开关
        /// \param bEnalbe 是否开启
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool EnableTorch(bool bEnable);
        
#ifndef WIN32
        /// \brief 美颜配置
        /// \param nFeature 美颜配置
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool EnableBeautifying(int nFeature);
        
        /// \brief 滤镜配置
        /// \param nIndex 滤镜索引
        /// \return true 成功，false 失败
		ZEGOAVKIT_API bool SetFilter(int nIndex);
#endif
        
        /// \brief 截预览图
        /// \return true 成功，通过回调返回结果，false 失败
		ZEGOAVKIT_API bool TakeSnapshotPreview();
        
        /// \brief 截播放图
        /// \return true 成功，通过回调返回结果，false 失败
		ZEGOAVKIT_API bool TakeSnapshotRender(int channelIdx);

        /// \brief 硬件编解码开关
        /// \param bRequired 开关
        /// \note 打开硬件编解码，只有条件满足的情况下才成功
        ZEGOAVKIT_API bool RequireHardwareAccelerated(bool bRequired);
        
		/// \brief 出发上传log
		ZEGOAVKIT_API bool UploadLog();
        
        /// \brief 调试信息
        ZEGOAVKIT_API void SetVerbose(bool bVerbose);
	}
}

#endif
