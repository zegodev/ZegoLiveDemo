//
//  ZegoAVApi-deprecated.h
//  zegoavkit
//
//  Copyright © 2016年 Zego. All rights reserved.
//

#ifndef ZegoAVApi_deprecated_h
#define ZegoAVApi_deprecated_h

#include "./ZegoAVDefines.h"

namespace ZEGO
{
    namespace AV
    {
        /// \brief 启动 SDK 服务
        /// \return true: 调用成功; false: 调用失败
        /// \note 已废弃，实际上为无效调用
        ZEGOAVKIT_API bool StartSDK();
        
        /// \brief 停止 SDK 服务
        /// \return true: 调用成功; false: 调用失败
        /// \note 已废弃，实际上为无效调用
        ZEGOAVKIT_API bool StopSDK();
        
        /// \brief 设置视频分辨率
        /// \param[in] nWidth 宽
        /// \param[in] nHeight 高
        /// \return true 成功，false 失败
        /// \note 已废弃，请使用 SetVideoEncodeResolution 和 SetVideoCaptureResolution
        ZEGOAVKIT_API bool SetVideoResolution(int nWidth, int nHeight);
        
        /// \brief 设置渲染视频旋转角度
        /// \param nRotation 旋转角度(0/90/180/270)
        /// \return true 成功，false 失败
        /// \note 已废弃，请使用 SetViewRotation、SetPreviewRotattion
        ZEGOAVKIT_API bool SetDisplayRotation(int nRotation);
        
        /// \brief 设置采集旋转
        /// \param nRotation 选择角度（0/90/180/270）
        /// \return true 成功，false 失败
        ZEGOAVKIT_API bool SetCaptureRotation(int nRotation);

        /// \brief 获取麦克风音量
        /// \return -1: 获取失败 0 ~100 麦克风音量
        /// \note 切换麦克风后需要重新获取麦克风音量
        /// \note 已废弃，请使用 GetMicDeviceVolume 替代
        ZEGOAVKIT_API int GetMicrophoneVolume();

        /// \brief 设置麦克风音量
        /// \param[in] volume 音量 0 ~ 100
        /// \note 已废弃，请使用 SetMicDeviceVolume 替代
        ZEGOAVKIT_API void SetMicrophoneVolume(int volume);

        /// \brief 获取扬声器音量
        /// \return -1: 获取失败 0 ~100 扬声器音量
        /// \note 切换扬声器后需要重新获取音量
        /// \note 已废弃，请使用 GetSpeakerDeviceVolume 替代
        ZEGOAVKIT_API int GetSpeakerVolume();

        /// \brief 设置扬声器音量
        /// \param[in] volume 音量 0 ~ 100
        /// \note 已废弃，请使用 SetSpeakerDeviceVolume 替代
        ZEGOAVKIT_API void SetSpeakerVolume(int volume);
    }
}


#endif /* ZegoAVApi_deprecated_h */
