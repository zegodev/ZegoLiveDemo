//
//  ZegoAPIDefines.h
//  chatroom
//
//  Created by Randy Qiu on 6/4/16.
//  Copyright © 2016 Zego. All rights reserved.
//

#ifndef ZegoAPIDefines_h
#define ZegoAPIDefines_h

#ifdef WIN32
#   ifdef ZEGO_EXPORTS
#       define ZEGO_API __declspec(dllexport)
#   else
#       define ZEGO_API __declspec(dllimport)
#   endif
#else
#   define ZEGO_API __attribute__((visibility("default")))
#endif

#define ZEGO_MAX_COMMON_LEN     (512)
#define ZEGO_MAX_NAME_LEN       (128)

#define ZEGO_MAX_STREAM         (3)

namespace ZEGO
{
    namespace CHATROOM
    {
        enum ZEGONetType
        {
            ZEGO_NT_NONE = 0,
            ZEGO_NT_LINE = 1,
            ZEGO_NT_WIFI = 2,
            ZEGO_NT_2G = 3,
            ZEGO_NT_3G = 4,
            ZEGO_NT_4G = 5,
            ZEGO_NT_UNKNOWN = 32
        };
        
        enum ZegoLogLevel
        {
            Grievous = 0,
            Error = 1,
            Warning = 2,
            Generic = 3,    ///< 通常在发布产品中使用
            Debug = 4       ///< 调试阶段使用
        };
        
        enum ZegoUpdateFlag
        {
            UPDATE_ADDED,
            UPDATE_DELETED,
        };
        
        struct ZegoStream
        {
            unsigned int index;
            char title[ZEGO_MAX_NAME_LEN];
            char streamID[ZEGO_MAX_NAME_LEN];
            char userName[ZEGO_MAX_NAME_LEN];
            //            char streamUrl[ZEGO_MAX_NAME_LEN];
        };
        
        struct ZegoRoomInfo
        {
            unsigned int dwID;
            unsigned int createdTime;
            unsigned int roomKey;
            unsigned int serverKey;
            unsigned int livesCount;
            char firstLiveTitle[ZEGO_MAX_NAME_LEN];
            ZegoStream streamList[ZEGO_MAX_STREAM];
        };
        
        struct ZegoUser
        {
            char userName[ZEGO_MAX_NAME_LEN];
            char userID[ZEGO_MAX_NAME_LEN];
        };
        
        enum ZegoMessageType
        {
            BROADCAST_TEXT_MSG = 1,
            BROADCAST_CUSTOM_MSG = 2,
        };
        
        enum ZegoUserUpdateFlag
        {
            UPDATE_TOTAL = 1,
            UPDATE_INCREASE,
        };
        
        enum ZegoUserFlag
        {
            USER_ADDED = 1,
            USER_DELETED,
            USER_UPDATED,
        };
        
        struct ZegoRoomUserInfo
        {
            char userName[ZEGO_MAX_NAME_LEN];
            char userID[ZEGO_MAX_NAME_LEN];
            unsigned int userIndex;
            ZegoUserFlag updateFlag;
        };
        
        typedef enum {
            EC_NoError,
            EC_Timeout,                 ///< 网络请求超时
            EC_Network,                 ///< 无法发送网络请求包
            
            EC_NotLogout,
            EC_TryingLogin,
            EC_NotLogin,
            EC_AlreadyLogout,
            
            EC_CreateStreamRSPError,
            EC_GetStreamListRSPError,
            
            EC_ParsePBPacketError,
            EC_CannotGetRoomKey,
            
            EC_RoomConnBroken,
            EC_RelayConnBroken,
            
        } ZegoErrorCode;
    }
}



#endif /* ZegoAPIDefines_h */
