#ifndef ZegoAVDefines_h
#define ZegoAVDefines_h

#define ZEGO_MAX_COMMON_LEN         (512)
#define ZEGO_MAX_LONG_LEN           (4*1024)
#define ZEGO_MAX_URL_COUNT          (10)
#define ZEGO_MAX_EVENT_INFO_COUNT   (10)

#if defined(_MSC_VER) || defined(__BORLANDC__)
#	define _I64_				"I64"
#	define _i64_				"i64"
#	define _64u_				"%I64u"
#	define _I64uw_				L"%llu"L
#	define _i64uw_				L"%llu"L
#else
#	define _I64_				"ll"
#	define _i64_				"ll"
#	define _64u_				"%llu"
#	define _I64w_				"ll"
#	define _I64uw_				L"%llu" L
#	define _i64uw_				L"%llu" L
#	define __int64				long long
#endif

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
        enum RemoteViewIndex
        {
            RemoteViewIndex_First = 0,
            RemoteViewIndex_Second = 1,
            RemoteViewIndex_Third = 2,
        };
        
        enum ZegoVideoViewMode
        {
            ZegoVideoViewModeScaleAspectFit = 0,    ///< 等比缩放，可能有黑边
            ZegoVideoViewModeScaleAspectFill = 1,   ///< 等比缩放填充整View，可能有部分被裁减
            ZegoVideoViewModeScaleToFill = 2,       ///< 填充整个View
        };

        enum AudioDeviceType
        {
            AudioDevice_Input = 0,
            AudioDevice_Output,
        };

        struct DeviceInfo
        {
            char szDeviceId[ZEGO_MAX_COMMON_LEN];
            char szDeviceName[ZEGO_MAX_COMMON_LEN];
        };

        struct ZegoUser
        {
            char szId[ZEGO_MAX_COMMON_LEN];
            char szName[ZEGO_MAX_COMMON_LEN];
        };

        struct ZegoStreamInfo
        {
            ZegoStreamInfo() : uiRtmpURLCount(0), uiHlsURLCount(0), uiFlvURLCount(0)
            {
                szStreamID[0] = '\0';
                szMixStreamID[0] = '\0';
            }
            
            char szStreamID[ZEGO_MAX_COMMON_LEN];       ///< 流ID
            char szMixStreamID[ZEGO_MAX_COMMON_LEN];    ///< 混流ID
            
            char* arrRtmpURLs[ZEGO_MAX_URL_COUNT];      ///< rtmp 播放 url 列表
            unsigned int uiRtmpURLCount;                ///< rtmp url 个数
            
            char* arrFlvRULs[ZEGO_MAX_URL_COUNT];
            unsigned int uiFlvURLCount;
            
            char* arrHlsURLs[ZEGO_MAX_URL_COUNT];
            unsigned int uiHlsURLCount;
        };
        
        enum ZegoAVAPIState
        {
            AVStateBegin = 0,               ///< 直播开始
            AVStateEnd = 1,                 ///< 直播正常停止
            TempBroken = 2,                 ///< 直播异常中断
            FatalError = 3,                 ///< 直播遇到严重的问题

            CreateStreamError = 4,          ///< 创建直播流失败
            FetchStreamError = 5,           ///< 获取流信息失败
            NoStreamError = 6,              ///< 无流信息
            MediaServerNetWorkError = 7,    ///< 媒体服务器连接失败
            DNSResolveError = 8,            ///< DNS 解释失败
            
            NotLoginError = 9,              ///< 未登陆
        };

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

        enum ZegoPublishFlag
        {
            ZEGO_JOIN_PUBLISH   = 1 << 0,
            ZEGO_MIX_STREAM     = 1 << 1
        };
        
        /// \brief 混流图层信息
        struct ZegoMixStreamConfig
        {
            char szStreamID[ZEGO_MAX_COMMON_LEN];   ///< 混流ID
            struct
            {
                int top;
                int left;
                int bottom;
                int right;
            } layout;
            
            /**
             *  原点在左上角，top/bottom/left/right 定义如下：
             *
             *  (left, top)-----------------------
             *  |                                |
             *  |                                |
             *  |                                |
             *  |                                |
             *  -------------------(right, bottom)
             */
        };
        
        enum ZegoLogLevel
        {
            Grievous = 0,
            Error = 1,
            Warning = 2,
            Generic = 3,    ///< 通常在发布产品中使用
            Debug = 4       ///< 调试阶段使用
        };

		struct WindowsDecs
		{
			char szTitle[ZEGO_MAX_COMMON_LEN];
			char szExe[ZEGO_MAX_COMMON_LEN];
			void* wnd;
		};
        
        
        enum EventType
        {
            Play_BeginRetry = 1,
            Play_RetrySuccess = 2,
            
            Publish_BeginRetry = 3,
            Publish_RetrySuccess = 4,
        };
        
        struct EventInfo
        {
            unsigned int uiInfoCount;
            char* arrKeys[ZEGO_MAX_EVENT_INFO_COUNT];
            char* arrValues[ZEGO_MAX_EVENT_INFO_COUNT];
        };
        
    }
}

#endif /* ZegoAVDefines_h */
