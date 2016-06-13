#ifndef ZegoAVDefines_h
#define ZegoAVDefines_h

#define ZEGO_MAX_COMMON_LEN		(512)
#define ZEGO_MAX_LONG_LEN		(4*1024)
#define ZEGO_MAX_URL_COUNT		(10)

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

namespace ZEGO
{
    namespace AV
    {
        enum RemoteViewIndex
        {
            RemoteViewIndex_First = 0,
            RemoteViewIndex_Second = 1
        };

        enum ZegoVideoViewMode
        {
            ZegoVideoViewModeScaleAspectFit = 0,    ///< 等比缩放，可能有黑边
            ZegoVideoViewModeScaleAspectFill = 1,   ///< 等比缩放填充整View，可能有部分被裁减
            ZegoVideoViewModeScaleToFill = 2,       ///< 填充整个View
        };

        enum PlayListUpdateFlag
        {
            PlayListUpdateFlag_Error = 0,  ///< 获取直播流信息失败
            PlayListUpdateFlag_Add = 1,    ///< 新增流信息
            PlayListUpdateFlag_Remove = 2, ///< 流删除
        };

        enum AudioDeviceType
        {
            AudioDevice_Input = 0,
            AudioDevice_Output,
        };

        struct DeviceInfo
        {
            char				szDeviceId[ZEGO_MAX_COMMON_LEN];
            char				szDeviceName[ZEGO_MAX_COMMON_LEN];
        };

        struct CustomCounter
        {
            unsigned int type;
            unsigned int value;
        };


        struct ZegoUser
        {
            char szId[ZEGO_MAX_COMMON_LEN];
            char szName[ZEGO_MAX_COMMON_LEN];
        };


        /// 直播流信息
        struct ZegoLiveInfo
        {
            unsigned __int64		ddwStreamID;
            char					szTitle[ZEGO_MAX_COMMON_LEN];
            char					szUserId[ZEGO_MAX_COMMON_LEN];
            char					szUserName[ZEGO_MAX_COMMON_LEN];
            char					szScreenShotUrl[ZEGO_MAX_COMMON_LEN];
        };

        //typedef vector<ZegoLiveInfo> ZegoLiveInfoList;

        struct ZegoLiveCustomData
        {
            unsigned int		dwType;
            char				szKey[ZEGO_MAX_COMMON_LEN];
            char				szFile[ZEGO_MAX_COMMON_LEN];
            unsigned char		szValue[ZEGO_MAX_LONG_LEN];
            unsigned int		uiValueSize;
        };

        //typedef vector<ZegoLiveCustomData> ZegoLiveCustomDataList;

        /// 直播间信息
        struct ZegoLiveRoomInfo
        {
            unsigned int uiID;
            unsigned int uiCreatedTime;
            unsigned int uiOnlineNum;
            unsigned int uiZegoID;
            unsigned int uiZegoToken;

            ZegoLiveInfo*	parrLiveInfo;
            unsigned int	uiLiveInfoCount;

            CustomCounter*		parrCustomCounter;
            unsigned int		uiCustomCounterCount;

            ZegoLiveCustomData*	parrLiveCustomData;
            unsigned int		uiLiveCustomDataCount;
        };

        enum ZegoAVAPIState
        {
            AVStateBegin = 0,           ///< 直播开始
            AVStateEnd = 1,             ///< 直播正常停止
            TempBroken = 2,             ///< 直播临时中断
            FatalError = 3,             ///< 直播遇到严重的问题

            CreateStreamError = 4,          ///< 创建直播流自白

            FetchStreamError = 5,           ///< 获取流信息失败
            
            NoStreamError = 6,              ///< 无流信息
            MediaServerNetWorkError = 7,    ///< 媒体服务器连接失败
            DNSResolveError = 8,
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


        enum ZegoLogLevel
        {
            Grievous = 0,
            Error = 1,
            Warning = 2,
            Generic = 3,    ///< 通常在发布产品中使用
            Debug = 4       ///< 调试阶段使用
        };
    }
}

#endif /* ZegoAVDefines_h */
