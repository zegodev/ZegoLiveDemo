#pragma once

#include "sigslot.h"
#include "ZegoSigslotDefine.h"

#include "ZegoAVCallback.h"
#include "ZegoChatRoomCallback.h"
#include "ZegoRoomModel.h"
using namespace ZEGO;
using namespace ZEGO::AV;

class CZegoAVSignal : public AV::IZegoLiveCallback, public AV::IZegoDeviceStateCallback
{
public:
	CZegoAVSignal();
	~CZegoAVSignal();

	ZEGO_MAKE_SIGNAL(LoginChannel, 3, std::string, std::string, unsigned int)
	ZEGO_MAKE_SIGNAL(LogoutChannel, 3, std::string, std::string, unsigned int)
	ZEGO_MAKE_SIGNAL(PublishStateUpdate, 5, std::string, std::string, AV::ZegoAVAPIState, std::string, StreamPtr)
	ZEGO_MAKE_SIGNAL(PlayStateUpdate, 4, std::string, std::string, AV::ZegoAVAPIState, std::string)
	ZEGO_MAKE_SIGNAL(PublishQualityUpdate, 2, std::string, int)
	ZEGO_MAKE_SIGNAL(PlayQualityUpdate, 2, std::string, int)
	ZEGO_MAKE_SIGNAL(AuxInput, 4, unsigned char*, int*, int*, int*)
	ZEGO_MAKE_SIGNAL(Disconnected, 3, std::string, std::string, unsigned int)
	ZEGO_MAKE_SIGNAL(Reconnected, 2, std::string, std::string)

    ZEGO_MAKE_SIGNAL(AudioDeviceChanged, 4, AudioDeviceType, std::string, std::string, DeviceState)
    ZEGO_MAKE_SIGNAL(VideoDeviceChanged, 3, std::string, std::string, DeviceState)

protected:
	void OnVideoSizeChanged(const char* pStreamID, int nWidth, int nHeight) {}
	void OnCaptureVideoSizeChanged(int nWidth, int nHeight) {}
	void OnPreviewSnapshot(void *pImage) {}
	void OnRenderSnapshot(void *pImage, int nChannelIdx) {}
	void OnLoginChannel(const char* pszUserID, const char* pszChannelID, unsigned int uiErrorCode);
	void OnLogoutChannel(const char* pszUserID, const char* pszChannelID, unsigned int nErr);
	void OnCountsUpdate(const char* pszUserID, const char* pszChannelID, unsigned int uiOnlineNums, unsigned int uiOnlineCount);
	void OnPublishStateUpdate(const char* pszUserID, const char* pszChannelID, AV::ZegoAVAPIState eState,
		const char* pszStreamID, const AV::ZegoStreamInfo& oStreamInfo);
	void OnPlayStateUpdate(const char* pszUserID, const char* pszChannelID, AV::ZegoAVAPIState eState, const char* pszStreamID);
	void OnPlayQualityUpdate(const char* pszStreamID, int quality, double videoFPS, double videoKBS);
	void OnPublishQulityUpdate(const char* pszStreamID, int quality, double videoFPS, double videoKBS);
	void OnDisconnected(const char* pszUserID, const char* pszChannelID, unsigned int uiErr);
	void OnReconnected(const char* pszUserID, const char* pszChannelID);
	void OnGetVideoDeviceCallback(AV::DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount) {}
	void OnGetAudioDeviceCallback(AV::AudioDeviceType deviceType, AV::DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount) {}
	void OnAuxCallback(unsigned char *pData, int *pDataLen, int *pSampleRate, int *pNumChannels);
	void OnAVEngineStop() {}
    void OnUpdateMixStreamConfig(unsigned int uiErrorCode, const char* pszMixStreamID, const AV::ZegoStreamInfo& oStreamInfo) {}

    void OnAudioDeviceStateChanged(AudioDeviceType deviceType, DeviceInfo *deviceInfo, DeviceState state);
    void OnVideoDeviceStateChanged(DeviceInfo *deviceInfo, DeviceState state);

private:
	HWND m_hCommWnd;
};

class CZegoChatRoomSignal : public CHATROOM::IZegoChatRoomCallback
{
public:
	CZegoChatRoomSignal();
	~CZegoChatRoomSignal();

	ZEGO_MAKE_SIGNAL(LoginResult, 4, int, unsigned int, unsigned int, bool)
	ZEGO_MAKE_SIGNAL(LeaveResult, 2, int, bool)
	ZEGO_MAKE_SIGNAL(Disconnected, 4, int, unsigned int, unsigned int, bool)
	ZEGO_MAKE_SIGNAL(KickOut, 3, int, std::string, bool)
	ZEGO_MAKE_SIGNAL(RoomList, 2, int, std::vector<RoomPtr>)
	ZEGO_MAKE_SIGNAL(SendMessage, 3, int, std::string, bool)
	ZEGO_MAKE_SIGNAL(ReceiveMessage, 2, std::string, bool)
	ZEGO_MAKE_SIGNAL(UserCountUpdate, 2, int, bool)
	ZEGO_MAKE_SIGNAL(UserList, 4, std::vector<UserPtr>, std::vector<UserPtr>, CHATROOM::ZegoUserUpdateFlag, bool)
	ZEGO_MAKE_SIGNAL(StreamList, 3, std::vector<StreamPtr>, CHATROOM::ZegoUpdateFlag, bool)
	ZEGO_MAKE_SIGNAL(StreamCreate, 3, std::string, std::string, bool)

protected:
	virtual void OnLoginResult(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool bIsPublicRoom);
	virtual void OnLeaveRoomResult(int nErrorCode, bool bIsPublicRoom);
	virtual void OnDisconnected(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool bIsPublicRoom);
	virtual void OnKickOut(int nErrorCode, const char* pszMsg, bool bIsPublicRoom);
	virtual void OnSendMessage(int nErrorCode, CHATROOM::ZegoMessageType messageType, const char *pszMsg, int msgLen, bool bIsPublicRoom);
	virtual void OnReceiveMessage(CHATROOM::ZegoMessageType messageType, const char *pszMsg, int msgLen, bool bIsPublicRoom);
	virtual void OnRoomUserUpdate(CHATROOM::ZegoRoomUserInfo *lstUserInfo, int userCount, CHATROOM::ZegoUserUpdateFlag flag, bool bIsPublicRoom);
	virtual void OnUserCountUpdate(int nNewUserCount, bool bIsPublicRoom);
	virtual void OnGetRoomInfoResult(int nErrorCode, int dwTotalCount, int dwBeginIndex, CHATROOM::ZegoRoomInfo* lstRoomInfo, int roomCount);
	virtual void OnStreamCreate(const char* pszStreamID, const char* pszUrl, bool bIsPublicRoom);
	virtual void OnStreamUpdate(CHATROOM::ZegoStream *streamList, int streamCount, CHATROOM::ZegoUpdateFlag updateFlag, bool bIsPublicRoom);

private:
	HWND m_hCommWnd;
};
