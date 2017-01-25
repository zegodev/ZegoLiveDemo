#include "stdafx.h"
#include "ZegoSDKSignal.h"
#include "ZegoUtility.h"

CZegoAVSignal::CZegoAVSignal()
{
	m_hCommWnd = ::FindWindow(ZegoCommWndClassName, ZegoCommWndName);
}

CZegoAVSignal::~CZegoAVSignal()
{
}

void CZegoAVSignal::OnLoginChannel(const char* pszUserID, const char* pszChannelID, unsigned int uiErrorCode)
{
	std::string strUserId = pszUserID ? pszUserID : "";
	std::string strChannelId = pszChannelID ? pszChannelID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigLoginChannel(strUserId, strChannelId, uiErrorCode);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnLogoutChannel(const char* pszUserID, const char* pszChannelID, unsigned int nErr)
{
	std::string strUserId = pszUserID ? pszUserID : "";
	std::string strChannelId = pszChannelID ? pszChannelID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigLogoutChannel(strUserId, strChannelId, nErr);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnCountsUpdate(const char* pszUserID, const char* pszChannelID, unsigned int uiOnlineNums, unsigned int uiOnlineCount)
{
	
}

void CZegoAVSignal::OnPublishStateUpdate(const char* pszUserID, const char* pszChannelID, AV::ZegoAVAPIState eState,
	const char* pszStreamID, const AV::ZegoStreamInfo& oStreamInfo)
{
	std::string strUserId = pszUserID ? pszUserID : "";
	std::string strChannelId = pszChannelID ? pszChannelID : "";
	std::string strStreamId = pszStreamID ? pszStreamID : "";

	StreamPtr pStream(new CZegoStreamModel("", strStreamId, strUserId, true));
	for (unsigned int i = 0; i < oStreamInfo.uiRtmpURLCount; i++)
	{
		pStream->m_vecRtmpUrls.push_back(oStreamInfo.arrRtmpURLs[i]);
	}

	for (unsigned int i = 0; i < oStreamInfo.uiFlvURLCount; i++)
	{
		pStream->m_vecFlvUrls.push_back(oStreamInfo.arrFlvRULs[i]);
	}

	for (unsigned int i = 0; i < oStreamInfo.uiHlsURLCount; i++)
	{
		pStream->m_vecHlsUrls.push_back(oStreamInfo.arrHlsURLs[i]);
	}

	ZEGO_SWITCH_THREAD_PRE
		m_sigPublishStateUpdate(strUserId, strChannelId, eState, strStreamId, pStream);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnPlayStateUpdate(const char* pszUserID, const char* pszChannelID, AV::ZegoAVAPIState eState, const char* pszStreamID)
{
	std::string strUserId = pszUserID ? pszUserID : "";
	std::string strChannelId = pszChannelID ? pszChannelID : "";
	std::string strStreamId = pszStreamID ? pszStreamID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigPlayStateUpdate(strUserId, strChannelId, eState, strStreamId);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnPublishQulityUpdate(const char* pszStreamID, int quality, double videoFPS, double videoKBS)
{
	std::string strStreamId = pszStreamID ? pszStreamID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigPublishQualityUpdate(strStreamId, quality);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnPlayQualityUpdate(const char* pszStreamID, int quality, double videoFPS, double videoKBS)
{
	std::string strStreamId = pszStreamID ? pszStreamID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigPlayQualityUpdate(strStreamId, quality);
	ZEGO_SWITCH_THREAD_ING

}

void CZegoAVSignal::OnAuxCallback(unsigned char *pData, int *pDataLen, int *pSampleRate, int *pNumChannels)
{
	m_sigAuxInput(pData, pDataLen, pSampleRate, pNumChannels);
}

void CZegoAVSignal::OnDisconnected(const char* pszUserID, const char* pszChannelID, unsigned int uiErr)
{
	std::string strUserId = pszUserID ? pszUserID : "";
	std::string strChannelId = pszChannelID ? pszChannelID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigDisconnected(strUserId, strChannelId, uiErr);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnReconnected(const char* pszUserID, const char* pszChannelID)
{
	std::string strUserId = pszUserID ? pszUserID : "";
	std::string strChannelId = pszChannelID ? pszChannelID : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigReconnected(strUserId, strChannelId);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnAudioDeviceStateChanged(AudioDeviceType deviceType, DeviceInfo *deviceInfo, DeviceState state)
{
    if (deviceInfo == nullptr)
        return;

    std::string strDeviceId = deviceInfo->szDeviceId;
    std::string strDeviceName = deviceInfo->szDeviceName;

    ZEGO_SWITCH_THREAD_PRE
        m_sigAudioDeviceChanged(deviceType, strDeviceId, strDeviceName, state);
    ZEGO_SWITCH_THREAD_ING
}

void CZegoAVSignal::OnVideoDeviceStateChanged(DeviceInfo *deviceInfo, DeviceState state)
{
    if (deviceInfo == nullptr)
        return;

    std::string strDeviceId = deviceInfo->szDeviceId;
    std::string strDeviceName = deviceInfo->szDeviceName;

    ZEGO_SWITCH_THREAD_PRE
        m_sigVideoDeviceChanged(strDeviceId, strDeviceName, state);
    ZEGO_SWITCH_THREAD_ING
}

//////////////////////////////////////////////////////////////////////////

CZegoChatRoomSignal::CZegoChatRoomSignal()
{
	m_hCommWnd = ::FindWindow(ZegoCommWndClassName, ZegoCommWndName);
}

CZegoChatRoomSignal::~CZegoChatRoomSignal()
{
}

void CZegoChatRoomSignal::OnLoginResult(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool bIsPublicRoom)
{
	ZEGO_SWITCH_THREAD_PRE
		m_sigLoginResult(nErrorCode, dwRoomKey, dwServerKey, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnLeaveRoomResult(int nErrorCode, bool bIsPublicRoom)
{
	ZEGO_SWITCH_THREAD_PRE
		m_sigLeaveResult(nErrorCode, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnDisconnected(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool bIsPublicRoom)
{
	ZEGO_SWITCH_THREAD_PRE
		m_sigDisconnected(nErrorCode, dwRoomKey, dwServerKey, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnKickOut(int nErrorCode, const char* pszMsg, bool bIsPublicRoom)
{
	std::string strMsg = pszMsg != NULL? pszMsg : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigKickOut(nErrorCode, strMsg, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnSendMessage(int nErrorCode, CHATROOM::ZegoMessageType messageType, const char *pszMsg, int msgLen, bool bIsPublicRoom)
{
	std::string strMsg(pszMsg, msgLen);

	ZEGO_SWITCH_THREAD_PRE
		m_sigSendMessage(nErrorCode, strMsg, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnReceiveMessage(CHATROOM::ZegoMessageType messageType, const char *pszMsg, int msgLen, bool bIsPublicRoom)
{
	std::string strMsg(pszMsg, msgLen);

	ZEGO_SWITCH_THREAD_PRE
		m_sigReceiveMessage(strMsg, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnRoomUserUpdate(CHATROOM::ZegoRoomUserInfo *lstUserInfo, int userCount, CHATROOM::ZegoUserUpdateFlag flag, bool bIsPublicRoom)
{
	std::vector<UserPtr> userAddList, userDelList;
	for (int i = 0; i < userCount; i++)
	{
		CHATROOM::ZegoRoomUserInfo userInfo = lstUserInfo[i];
		if (userInfo.userID == NULL || userInfo.userName == NULL)
		{
			continue;
		}

		UserPtr pUser(new CZegoUserModel(UTF8ToWString(userInfo.userID), UTF8ToWString(userInfo.userName)));
		if (userInfo.updateFlag == CHATROOM::USER_ADDED || userInfo.updateFlag == CHATROOM::USER_UPDATED)
		{
			userAddList.push_back(pUser);
		}
		else if (userInfo.updateFlag == CHATROOM::USER_DELETED)
		{
			userDelList.push_back(pUser);
		}
	}

	ZEGO_SWITCH_THREAD_PRE
		m_sigUserList(userAddList, userDelList, flag, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnUserCountUpdate(int nNewUserCount, bool bIsPublicRoom)
{
	ZEGO_SWITCH_THREAD_PRE
		m_sigUserCountUpdate(nNewUserCount, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnGetRoomInfoResult(int nErrorCode, int dwTotalCount, int dwBeginIndex, CHATROOM::ZegoRoomInfo* lstRoomInfo, int roomCount)
{
	std::vector<RoomPtr> roomList;
	for (int i = 0; i < roomCount; i++)
	{
		// 这里还无需关注stream
		CHATROOM::ZegoRoomInfo roomInfo = lstRoomInfo[i];
		RoomPtr pRoom(new CZegoRoomModel(roomInfo.roomKey, roomInfo.serverKey));
		std::wstring strRoomTitle = UTF8ToWString(roomInfo.firstLiveTitle);
		pRoom->SetRoomTitle(strRoomTitle);
		pRoom->SetCreatedTime(roomInfo.createdTime);
		pRoom->SetLivesCount(roomInfo.livesCount);
		roomList.push_back(pRoom);
	}

	ZEGO_SWITCH_THREAD_PRE
		m_sigRoomList(nErrorCode, roomList);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnStreamCreate(const char* pszStreamID, const char* pszUrl, bool bIsPublicRoom)
{
	std::string strStreamId = pszStreamID != NULL ? pszStreamID : "";
	std::string strUrl = pszUrl != NULL ? pszUrl : "";

	ZEGO_SWITCH_THREAD_PRE
		m_sigStreamCreate(strStreamId, strUrl, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}

void CZegoChatRoomSignal::OnStreamUpdate(CHATROOM::ZegoStream *streamList, int streamCount, CHATROOM::ZegoUpdateFlag updateFlag, bool bIsPublicRoom)
{
	std::vector<StreamPtr> vStreamList;
	for (int i = 0; i < streamCount; i++)
	{
		CHATROOM::ZegoStream streamInfo = streamList[i];
		StreamPtr pStream(new CZegoStreamModel(streamInfo.title, streamInfo.streamID, streamInfo.userName));
		vStreamList.push_back(pStream);
	}

	ZEGO_SWITCH_THREAD_PRE
		m_sigStreamList(vStreamList, updateFlag, bIsPublicRoom);
	ZEGO_SWITCH_THREAD_ING
}
