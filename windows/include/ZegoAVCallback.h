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
            virtual void OnPlayStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState, const char* pszStreamID) = 0;
            
            virtual void OnPlayQualityUpdate(const char* pszStreamID, int quality) = 0;
            virtual void OnPublishQulityUpdate(const char* pszStreamID, int quality) = 0;

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
        };

    }
}

#endif