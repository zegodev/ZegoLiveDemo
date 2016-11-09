//
//  ZegoChatRoom.h
//  chatroom
//
//  Created by Randy Qiu on 6/4/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#ifndef ZegoChatRoom_h
#define ZegoChatRoom_h

#include "./ZegoAPIDefines.h"
#include "./ZegoChatRoomCallback.h"


namespace ZEGO
{
    namespace CHATROOM
    {
        /// \brief 启动log，设置log级别和log输出目录
        /// \param nLevel，log级别，参考enum ZegoLogLevel
        /// \param strLogDir, log输出目录
        // @note	在InitSDK之前调用，这样才不会丢掉初始化的log，程序生命期中只调用一次
        ZEGO_API bool SetLogLevel(ZegoLogLevel level, const char* pszLogDir);
        
        
        ZEGO_API bool SetNetType(ZEGONetType nt);
        
        /**
         * 初始化SDK
         * @param[in]    appID    Zego派发的数字ID，各个开发者的唯一标识
         * @param[in]    appSignature    Zego派发的签名,用来校验对应appID的合法性
         * @return       true:调用成功；false:调用失败
         */
        ZEGO_API bool InitSDK(unsigned int uiAppID, unsigned char* pBufAppSignature, int nSignatureSize);
        
        /**
         * 反初始化SDK
         * @return      true:调用成功；false:调用失败
         */
        ZEGO_API bool UninitSDK();

        /**
         * 设置Zego房间相关信息通知的回调
         * @param[in]    pCallback    回调对象指针
         * @note	可以在InitSDK之前调用，但只能调用一次
         */
        ZEGO_API bool SetCallback(IZegoChatRoomCallback* pCallback);
        
        
        /// \brief 进入一个由 SDK 使用者维护的房间
        /// \note 如果存在实体则直接登录，否则创建实体后登录
        ZEGO_API bool GetInCustomRoom(unsigned int dwRoomKey, const char* pszUserID, const char* pszUserName);
        
        /// \brief 进入由 server 维护的房间
        /// \note 房间必须存在
        ZEGO_API bool GetInExistedRoom(unsigned int dwRoomKey, unsigned int dwServerKey, const char* pszUserID, const char* pszUserName);
        
        /// \brief 创建一个房间，并进入该房间
        ZEGO_API bool CreateRoomAndGetIn(const char* pszUserID, const char* pszUserName);
        
        /// \brief 离开直播间
        ZEGO_API bool LeaveRoom();
        
        ZEGO_API bool GetInCustomPrivateRoom(unsigned int dwRoomKey, const char *pszUserID, const char *pszUserName);
        ZEGO_API bool GetInExistedPrivateRoom(unsigned int dwRoomKey, unsigned int dwServerKey, const char* pszUserID, const char *pszUserName);
        ZEGO_API bool CreatePrivateRoomAndGetIn(const char *pszUserID, const char *pszUserName);
        ZEGO_API bool LeavePrivateRoom();
        
        /// \brief 给全员发送一个文本消息
        /// \note 该函数返回消息序列号，应用开发者可以保存起来返回值，以方便在OnSendBroadcastTextMsgResult做定位
        /// \return 负数表示失败，将来可以通过返回的各种负数知道各种错误，暂时可以先实现为一种错误号（-1）
        ZEGO_API long long SendBroadcastTextMsg(const char* pszMsg, bool isPublicRoom);
        
        ZEGO_API long long SendRelayBroadcastTextMsg(const char* pszMsg, bool isPublicRoom);
        ZEGO_API long long SendRelayBroadcastCustomMsg(const char* pszMsg, int nSize, bool isPublicRoom);
        
        /// \brief 丢弃本地缓存 room info
        ZEGO_API bool DropRoomInfoCache();
        
        /// \brief 获取在线房间信息，通过 OnGetRoomInfoResult 返回结果
        /// \param dwBeginIndex 起始直播间号，从 0 开始计数
        /// \param dwCount 期望获取的直播间数
        ZEGO_API bool GetRoomInfoList(unsigned int dwBeginIndex, unsigned int dwCount);

        ZEGO_API bool DropReplayInfoCache();
        ZEGO_API bool GetReplayInfoList(unsigned int dwBeginIndex, unsigned int dwCount);
        
        ZEGO_API bool SetTestServer(const char* pszIP, int nPort);
        ZEGO_API bool SetTestBaseUrl(const char* pszBaseUrl);
        ZEGO_API bool SetTestEnv(bool bTestEnv);
        
        ZEGO_API bool CreateStreamInRoom(const char* strTitle, const char *preferredStreamID, bool isPublicRoom);
        ZEGO_API bool GetStreamList(bool isPublicRoom);
        ZEGO_API bool ReportStreamAction(int actionType, const char *strStreamID, const char *strUserId, bool isPublicRoom);
    }
}


#endif /* ZegoChatRoom_h */
