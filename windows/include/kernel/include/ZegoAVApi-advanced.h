//
//  ZegoAVApi-advanced.h
//  zegoavkit
//
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoAVApi_advanced_h
#define ZegoAVApi_advanced_h

#include "./ZegoAVDefines.h"

namespace AVE
{
    class VideoCaptureFactory;
    class MediaCaptureFactory;
    class VideoFilterFactory;
}

namespace ZEGO
{
    namespace AV
    {
        /// \brief 初始化引擎
        /// \note jvm 与 ctx 仅用于 android
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool InitPlatform(void* jvm, void* ctx);
        
        /// \brief 设置业务类型
        /// \param nType 类型，默认为 0
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetBusinessType(int nType);
        
        /// \brief 设置外部采集模块
        /// \param
        /// \note 必须在 InitSDK 前调用，并且不能置空
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API void SetVideoCaptureFactory(AVE::VideoCaptureFactory* factory);
        
        /// \brief 设置外部采集模块(包含音频)
        /// \param
        /// \note 必须在 InitSDK 前调用，并且不能置空
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API void SetMediaCaptureFactory(AVE::MediaCaptureFactory* factory);
        
        /// \brief 设置音频前处理函数
        /// \param prep 前处理函数指针
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API void SetAudioPrep(void(*prep)(const short* inData, int inSamples, int sampleRate, short *outData));
        
        /// \brief 更新混流配置
        /// \param pConfigList 输入流配置数组首元素指针
        /// \param size 输入流个数
        ZEGOAVKIT_API bool UpdateMixStreamConfig(ZegoMixStreamConfig* pConfigList, int size);
        
        /// \brief 设置播放渲染朝向
        /// \param[in] nRotation 旋转角度(0/90/180/270)
        /// \param[in] nChannelIndex 播放通道
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetViewRotation(int nRotation, int nChannelIndex);
        
        /// \brief 设置预览渲染朝向
        /// \param nRotation 旋转角度(0/90/180/270)
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetPreviewRotation(int nRotation);
        
        /// \brief 设置App的朝向，确定进行横竖屏采集
        /// \param orientation app orientation
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetAppOrientation(int nOrientation);
        
        /// \brief 混音开关
        /// \param bEable true 启用混音输入；false 关闭混音输入
        ZEGOAVKIT_API bool EnableAux(bool bEnable);
        
        /// \brief 音频录制回调开关
        /// \param bEnable true 启用音频录制回调；false 关闭音频录制回调
        ZEGOAVKIT_API bool EnableAudioRecord(bool bEnable);
        
        /// \brief 设置音频录制回调
        ZEGOAVKIT_API bool SetAudioRecordCallback(IZegoAudioRecordCallback* pCB);
        
        /// \brief 混音输入播放静音开关
        /// \param bMute true: aux 输入播放静音；false: 不静音
        ZEGOAVKIT_API bool MuteLocalBackground(bool bMute);
        
        /// \brief 音频采集监听开关
        /// \param bEnalbe 是否开启
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool EnableLoopback(bool bEnable);
        
        /// \brief 音频采集降噪开关
        /// \param bEnalbe 是否开启
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool EnableNoiseSuppress(bool bEnable);
        
        /// \brief 设置播放音量
        /// \param[in] volume 音量 0 ~ 100
        ZEGOAVKIT_API void SetPlayVolume(int volume);
        
        /// \brief 设置监听音量
        /// \param[in] volume 音量 0 ~ 100
        ZEGOAVKIT_API void SetLoopbackVolume(int volume);
        
#ifdef WIN32
        /// \brief 系统声卡声音采集开关
        /// \param[in] bEnable 是否打开
        ZEGOAVKIT_API void EnableMixSystemPlayout(bool bEnable);
        
        /// \brief 获取麦克风音量
        /// \param[in] deviceid 麦克风deviceid
        /// \return -1: 获取失败 0 ~100 麦克风音量
        /// \note 切换麦克风后需要重新获取麦克风音量
        ZEGOAVKIT_API int GetMicDeviceVolume(const char *deviceId);

        /// \brief 设置麦克风音量
        /// \param[in] deviceid 麦克风deviceid volume 音量 0 ~ 100
        ZEGOAVKIT_API void SetMicDeviceVolume(const char *deviceId, int volume);

        /// \brief 获取扬声器音量
        /// \param[in] deviceid 杨声器deviceid
        /// \return -1: 获取失败 0 ~100 扬声器音量
        /// \note 切换扬声器后需要重新获取音量
        ZEGOAVKIT_API int GetSpeakerDeviceVolume(const char *deviceId);

        /// \brief 设置扬声器音量
        /// \param[in] deviceid 杨声器deviceid volume 音量 0 ~ 100
        ZEGOAVKIT_API void SetSpeakerDeviceVolume(const char *deviceId, int volume);
#endif
        
        /// \brief 获取当前采集的音量
        /// \return 当前采集音量大小
        ZEGOAVKIT_API float GetCaptureSoundLevel();
        
        /// \brief 获取当前播放视频的音量
        /// \param[in] channelIndex 播放通道
        /// \return channelIndex对应视频的音量
        ZEGOAVKIT_API float GetRemoteSoundLevel(int channelIndex);
        
        /// \brief 设置平台信息
        /// \param[in] pszInfo 平台信息
        ZEGOAVKIT_API void SetPlatformInfo(const char* pszInfo);
        
        /// \brief 获取 SDK 版本1
        /// \return SDK 版本信息1
        ZEGOAVKIT_API const char* GetSDKVersion();

        /// \brief 获取 SDK 版本2
        /// \return SDK 版本信息2
        ZEGOAVKIT_API const char* GetSDKVersion2();
        
        /// \brief 设置是否使用外部视频渲染
        ZEGOAVKIT_API void SetExternalRender(bool bExternalRender);
        
        ZEGOAVKIT_API void SetExternalRenderCallback(IZegoVideoRenderCallback* pCB);
        
        ZEGOAVKIT_API void SetLiveEventCallback(IZegoLiveEventCallback* pCB);
        
#ifndef WIN32
        ZEGOAVKIT_API bool SetPolishStep(float step);
        ZEGOAVKIT_API bool SetPolishFactor(float factor);
        ZEGOAVKIT_API bool SetWhitenFactor(float factor);
        ZEGOAVKIT_API bool SetSharpenFactor(float factor);
#endif
        /// \brief 设置网络联通状态
        /// \param[in] bNetworkConnected true 网络可用, false 网络不可用
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API void SetNetworkConnected(bool bNetworkConnected);
        
        /// \brief 获取音频设备列表
        /// \param[in] deviceType 设备类型
        /// \return 音频设备列表
        ZEGOAVKIT_API DeviceInfo* GetAudioDeviceList(AudioDeviceType deviceType, int& device_count);

        /// \brief 设置选用音频设备
        /// \param[in] deviceType 设备类型
        /// \param[in] pszDeviceID 设备 ID
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetAudioDevice(AudioDeviceType deviceType, const char* pszDeviceID);

        /// \brief 获取视频设备列表
        /// \return 视频频设备列表
        ZEGOAVKIT_API DeviceInfo* GetVideoDeviceList(int& device_count);

        ZEGOAVKIT_API void FreeDeviceList(DeviceInfo* parrDeviceList);

        /// \brief 设置选用视频设备
        /// \param[in] pszDeviceID 设备 ID
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API bool SetVideoDevice(const char* pszDeviceID);
        
        
        /// \brief 设置编码器码率控制策略
        /// \param strategy 策略配置，参考 ZegoVideoEncoderRateControlStrategy
        /// \param encoderCRF 当策略为恒定质量（ZEGO_RC_VBR/ZEGO_RC_CRF）有效，取值范围 [0~51]，越小质量越好，建议取值范围 [18, 28]
        ZEGOAVKIT_API void SetVideoEncoderRateControlConfig(int strategy, int encoderCRF);
        
        /// \brief 设置外部滤镜模块
        /// \param factory 工厂对象
        /// \note 必须在 InitSDK 前调用，并且不能置空
        ZEGOAVKIT_API void SetVideoFilterFactory(AVE::VideoFilterFactory* factory);
        
#if WIN32
        ZEGOAVKIT_API WindowsDecs* FillWindowList(unsigned int& window_count, bool include_minimized);
        ZEGOAVKIT_API void FreeWindowList(WindowsDecs* window_list);
#endif

        /// \brief 设置拉流质量监控周期
        /// \param timeInMS 时间周期，单位为毫秒，取值范围：(500, 60000)
        /// \return true 设置成功，否则失败
        ZEGOAVKIT_API bool SetPlayQualityMoniterCycle(unsigned int timeInMS);
        
        ZEGOAVKIT_API void SetUseTestEnv(bool bTestEnv);
        ZEGOAVKIT_API void SetUseAlphaEnv(bool bAlphaEnv);
    }
}

#endif /* ZegoAVApi_advanced_h */
