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
        /// \note 已废弃，请使用 SetViewRotation、SetPreviewRotattion 和 SetCaptureRotation
        ZEGOAVKIT_API bool SetDisplayRotation(int nRotation);
    }
}


#endif /* ZegoAVApi_deprecated_h */
