#ifndef ZegoAVCallback_h
#define ZegoAVCallback_h

#include "ZegoAVDefines.h"

namespace ZEGO
{
    namespace AV
    {
        class IZegoLiveCallback
        {
        public:
            virtual void OnVideoSizeChanged(const char* pStreamID, int nWidth, int nHeight) = 0;
            virtual void OnCaptureVideoSizeChanged(int nWidth, int nHeight) = 0;
            virtual void OnPreviewSnapshot(void *pImage) = 0;
            virtual void OnRenderSnapshot(void *pImage, int nChannelIdx) = 0;

            virtual void OnLoginChannel(const char* pszUserID, const char* pszChannelID, unsigned int uiErrorCode) = 0;
			virtual void OnLogoutChannel(const char* pszUserID, const char* pszChannelID, unsigned int nErr) = 0;

            virtual void OnCountsUpdate(const char* pszUserID, const char* pszChannelID, unsigned int uiOnlineNums, unsigned int uiOnlineCount) = 0;

            virtual void OnPublishStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState,
				const char* pszStreamID, const ZegoStreamInfo& oStreamInfo) = 0;
            virtual void OnUpdateMixStreamConfig(unsigned int uiErrorCode, const char* pszMixStreamID, const ZegoStreamInfo& oStreamInfo) = 0;
            
            virtual void OnPlayStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState, const char* pszStreamID) = 0;
            
			/// \brief 观看质量更新
			/// \param quality: 0 ~ 3 分别对应优良中差
			/// \param streamID 观看流ID
            /// \param videoFPS 帧率
            /// \param videoKBS 码率
            virtual void OnPlayQualityUpdate(const char* pszStreamID, int quality, double videoFPS, double videoKBS) = 0;

			/// \brief 发布质量更新
			/// \param quality: 0 ~ 3 分别对应优良中差
			/// \param streamID 发布流ID
            /// \param videoFPS 帧率
            /// \param videoKBS 码率
            virtual void OnPublishQulityUpdate(const char* pszStreamID, int quality, double videoFPS, double videoKBS) = 0;

            virtual void OnDisconnected(const char* pszUserID, const char* pszChannelID, unsigned int uiErr) = 0;
            virtual void OnReconnected(const char* pszUserID, const char* pszChannelID) = 0;

            virtual void OnGetVideoDeviceCallback(DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount) = 0;
            virtual void OnGetAudioDeviceCallback(AudioDeviceType deviceType, DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount) = 0;

			/// \brief 混音数据输入回调
			/// \param pData 数据缓存起始地址
			/// \param pDataLen [in] 缓冲区长度；[out]实际填充长度，必须为 0 或是缓冲区长度，代表有无混音数据
			/// \param pSampleRate 混音数据采样率
			/// \param pNumChannels 混音数据声道数
			/// \note 混音数据 bit depth 必须为 16
            virtual void OnAuxCallback(unsigned char *pData, int *pDataLen, int *pSampleRate, int *pNumChannels) = 0;
            
            virtual void OnAVEngineStop() = 0;
        };

        class IZegoVideoRenderCallback
        {
        public:
            virtual void OnVideoDataCallback(const unsigned char *pData, int dataLen, int nChannelIdx, int width, int height, int strides[4]) = 0;
        };
        
        class IZegoAudioRecordCallback
        {
        public:
            virtual void OnAudioRecordCallback(const unsigned char *pData, int data_len, int sample_rate, int num_channels, int bit_depth) = 0;
        };
        
        class IZegoLiveEventCallback
        {
        public:
            virtual void OnAVKitEvent(int event, EventInfo* pInfo) = 0;
        };
    }
}

#endif
