#ifndef ZegoAVApi_h
#define ZegoAVApi_h

#include "./ZegoAVDefines.h"
#include "./ZegoAVCallback.h"

#ifdef WIN32
    #ifdef ZEGOAVKIT_EXPORTS
    #define ZEGOAVKIT_API __declspec(dllexport)
    #else
    #define ZEGOAVKIT_API __declspec(dllimport)
    #endif
#else
    #define ZEGOAVKIT_API __attribute__((visibility("default")))
#endif

namespace ZEGO
{
	namespace AV
	{
		/// \brief 初始化引擎
		/// \note jvm 与 ctx 仅用于 android
		ZEGOAVKIT_API bool InitPlatform(void* jvm, void* ctx);

        
        
        /// \brief 设置外部采集模块
        /// \param
        /// \note 必须在 InitSDK 前调用，并且不能置空
        ZEGOAVKIT_API void SetVideoCaptureFactory(void* factory);
        
        
        /// \brief 启动log，设置log级别和log输出目录
        /// \param nLevel，log级别，参考enum ZegoLogLevel 
        /// \param strLogDir, log输出目录
        // @note	在InitSDK之前调用，这样才不会丢掉初始化的log，程序生命期中只调用一次
        ZEGOAVKIT_API bool SetLogLevel(ZegoLogLevel level, const char* pszLogDir);


		/**
		* 初始化SDK
		* @param[in]    appID    Zego派发的数字ID，各个开发者的唯一标识
		* @param[in]    appSignature    Zego派发的签名,用来校验对应appID的合法性
		* @return       true:调用成功；false:调用失败
		*/
		ZEGOAVKIT_API bool InitSDK(unsigned int uiAppID, unsigned char* pBufAppSignature, int nSignatureSize);


		/**
		* 反初始化SDK
		* @return      true:调用成功；false:调用失败
		*/
		ZEGOAVKIT_API bool UninitSDK();

        /// \brief 启动 SDK 服务
        ZEGOAVKIT_API bool StartSDK();
        
        /// \brief 停止 SDK 服务
        ZEGOAVKIT_API bool StopSDK();

        /**
        * 设置Zego房间相关信息通知的回调
        * @param[in]    pCallback    回调对象指针
        * @note	可以在InitSDK之前调用，但只能调用一次
        */
        ZEGOAVKIT_API bool SetCallback(IZegoLiveCallback* pCallback);

		/**
		* 开始直播
		* @param[in]   title 直播的名称
		* @return      true:调用成功，IZegoVideoCallback中的OnPublishSucc或OnPublishStop回调直播结果；false:调用失败
		*/
		ZEGOAVKIT_API bool StartPublish(const char*  pszTitle, const char* pszStreamID);

		/**
		* 停止直播
		* @return      true:停止成功；false:停止失败
		*/
		ZEGOAVKIT_API bool StopPublish();

		/// \breif 获取直播流列表
		/// \param userID 用户 ID，每个用户唯一
		/// \param userName 用户名
		/// \param liveID 频道 ID
		/// \return 0 成功，等待异步结果回调，否则失败
		ZEGOAVKIT_API bool LoginChannel(const char* pszUserID, const char* pszUserName, const char* pszLiveID, int nNetType);

		ZEGOAVKIT_API bool LogoutChannel();

		/// \brief 播放
		/// \param streamID 流 ID
		/// \param chn 播放通道
		/// \return 0 成功，等待异步结果回调，否则失败
		ZEGOAVKIT_API bool PlayStream(const char* pszStreamID, unsigned int uiChannelID);

		/// \brief 停止播放
		/// \param streamID 流 ID
		/// \return 0 成功，等待异步结果回调，否则失败
		ZEGOAVKIT_API bool StopPlayStream(const char* pszStreamID);



		/// \brief 设置视频载体 view，没有考虑多主播展示情况，引擎持有该对象
		ZEGOAVKIT_API bool SetView(void* pView, unsigned int uiChannelIndex = 0);
		ZEGOAVKIT_API bool SetPreviewView(void* pView);

		ZEGOAVKIT_API bool SetViewMode(ZegoVideoViewMode mode, unsigned int uiChannelIndex);
		ZEGOAVKIT_API bool SetPreviewViewMode(ZegoVideoViewMode mode);

        /// \brief 设置渲染视频旋转角度
        ZEGOAVKIT_API bool SetDisplayRotation(int nRotation);
        
		ZEGOAVKIT_API bool StartPreview();
		ZEGOAVKIT_API bool StopPreview();

		ZEGOAVKIT_API bool SetVideoBitrate(int nBitrate);
		ZEGOAVKIT_API bool SetVideoFPS(int nFps);
		ZEGOAVKIT_API bool SetVideoResolution(int nWidth, int nHeight);
		ZEGOAVKIT_API bool SetFrontCam(bool bFront);

		ZEGOAVKIT_API bool GetAudioDeviceList(AudioDeviceType deviceType);
		ZEGOAVKIT_API bool SetAudioDevice(AudioDeviceType deviceType, const char* pszDeviceID);

		ZEGOAVKIT_API bool GetVideoDeviceList();
		ZEGOAVKIT_API bool SetVideoDevice(const char* pszDeviceID);

        ZEGOAVKIT_API bool EnableNoiseSuppress(bool bEnable);
		ZEGOAVKIT_API bool EnableMic(bool bEnable);
        ZEGOAVKIT_API bool EnableCamera(bool bEnable);
		ZEGOAVKIT_API bool SetSpeakerPhoneOn(bool bOn);
		ZEGOAVKIT_API bool EnableSpk(bool bEnable);
		ZEGOAVKIT_API bool EnableTorch(bool bEnable);
		ZEGOAVKIT_API bool EnableBeautifying(int nFeature);
		ZEGOAVKIT_API bool SetFilter(int nIndex);
		ZEGOAVKIT_API bool SetCaptureRotation(int nRotation);  ///< AVE::CEngine::CAPTURE_ROTATE_xxx

		ZEGOAVKIT_API bool TakeSnapshotPreview();
		ZEGOAVKIT_API bool TakeSnapshotRender(RemoteViewIndex channelIdx);

		/// \brief 混音开关
		/// \param bEable true 启用混音输入；false 关闭混音输入
		ZEGOAVKIT_API bool EnableAux(bool bEable);

        /// \brief 混音输入播放静音开关
        /// \param bMute true: aux 输入播放静音；false: 不静音
        ZEGOAVKIT_API bool MuteLocalBackground(bool bMute);
        
        /// \brief 硬件编解码开关
        /// \param bRequired 开关
        /// \note 打开硬件编解码，只有条件满足的情况下才成功
        ZEGOAVKIT_API bool RequireHardwareAccelerated(bool bRequired);
        
		/// \brief 出发上传log
		ZEGOAVKIT_API bool UploadLog();
        
        ZEGOAVKIT_API void SetPlatformInfo(const char* pszInfo);
        ZEGOAVKIT_API const char* GetSDKVersion();
        
        ZEGOAVKIT_API void SetUseTestEnv(bool bTestEnv);
	}
}

#endif