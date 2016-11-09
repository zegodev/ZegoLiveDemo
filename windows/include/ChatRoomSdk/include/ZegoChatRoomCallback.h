//
//  ZegoChatRoomCallback.h
//  chatroom
//
//  Created by Randy Qiu on 6/4/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#ifndef ZegoChatRoomCallback_h
#define ZegoChatRoomCallback_h

#include "./ZegoAPIDefines.h"

namespace ZEGO
{
    namespace CHATROOM
    {
        class IZegoChatRoomCallback
        {
        public:
            virtual ~ IZegoChatRoomCallback() {}
            
            virtual void OnLoginResult(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool bIsPublicRoom) = 0;
            virtual void OnLeaveRoomResult(int nErrorCode, bool bIsPublicRoom) = 0;
            virtual void OnDisconnected(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool bIsPublicRoom) = 0;
            virtual void OnKickOut(int nErrorCode, const char* pszMsg, bool bIsPublicRoom) = 0;
            
//            virtual void OnReceiveBroadcastTextMsg(const char* pszMsg) = 0;
//            virtual void OnReceiveCustomMsg(const char* pszMsg, int len) = 0;
            
            //聊天相关
            virtual void OnSendMessage(int nErrorCode, ZegoMessageType messageType, const char *pszMsg, int msgLen, bool bIsPublicRoom) = 0;
            virtual void OnReceiveMessage(ZegoMessageType messageType, const char *pszMsg, int msgLen, bool bIsPublicRoom) = 0;
            
            /// \brief 成员列表更新
            virtual void OnRoomUserUpdate(ZegoRoomUserInfo *lstUserInfo, int userCount, ZegoUserUpdateFlag flag, bool bIsPublicRoom) = 0;
            virtual void OnUserCountUpdate(int nNewUserCount, bool bIsPublicRoom) = 0;

            /// \brief 从网络拉直播间列表事件
            /// \param lstRoomInfoList 所要求的直播间信息列表
            virtual void OnGetRoomInfoResult(int nErrorCode, int dwTotalCount, int dwBeginIndex, ZegoRoomInfo* lstRoomInfo, int roomCount) = 0;
            
            // stream相关回调
            virtual void OnStreamCreate(const char* pszStreamID, const char* pszUrl, bool bIsPublicRoom) = 0;
            //第一次全量返回Add，之后有变化则动态返回增删的stream
            virtual void OnStreamUpdate(ZegoStream *streamList, int streamCount, ZegoUpdateFlag updateFlag, bool bIsPublicRoom) = 0;
        };
    }
}


#endif /* ZegoChatRoomCallback_h */
