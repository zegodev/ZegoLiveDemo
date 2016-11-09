//
//  ZegoAVApi-advanced.h
//  zegoavkit
//
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoAVApi_advanced_h
#define ZegoAVApi_advanced_h

#include "./ZegoAVDefines.h"

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
        
        /// \brief 设置采集旋转
        /// \param nRotation 选择角度（0/90/180/270）
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetCaptureRotation(int nRotation);
        
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
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API void SetPlayVolume(int volume);
        
        /// \brief 设置监听音量
        /// \param[in] volume 音量 0 ~ 100
        /// \return true: 调用成功; false: 调用失败
        ZEGOAVKIT_API void SetLoopbackVolume(int volume);
        
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
        
        /// \brief void* = HWND
        ZEGOAVKIT_API bool AddWindowCapture(void* hWnd, bool cursor);
        ZEGOAVKIT_API void RemoveWindowCapture(void* hWnd);
        ZEGOAVKIT_API bool RefreshWindowCapture(void* hWnd);
        /// \brief void* = HDC
        ZEGOAVKIT_API bool RenderWindowCapture(void* hWnd, void* hTargetDC, int x, int y, unsigned int width, unsigned int height);
        
        ZEGOAVKIT_API unsigned char* GetWindowCaptureData(void* hWnd, unsigned int& size, unsigned int& width, unsigned int& height, unsigned int& time);
        
#if WIN32
        ZEGOAVKIT_API WindowsDecs* FillWindowList(unsigned int& window_count, bool include_minimized);
        ZEGOAVKIT_API void FreeWindowList(WindowsDecs* window_list);
#endif
        
        ZEGOAVKIT_API void SetUseTestEnv(bool bTestEnv);
        ZEGOAVKIT_API void SetUseAlphaEnv(bool bAlphaEnv);
    }
}

#endif /* ZegoAVApi_advanced_h */
