#pragma once
#include "ZegoSettingsModel.h"
#include "ZegoAVApi.h"
#include "ZegoAVView.h"
#include "ZegoChatRoom.h"
#include "ZegoRoomModel.h"
using namespace ZEGO;

#include <string>
#include <stack>
#include "CGridListCtrlX/CGridListCtrlEx.h"
#include "afxwin.h"

class CAboutDlg : public CDialogEx, public sigslot::has_slots<>
{
public:
	CAboutDlg();

	// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

														// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

// CZegoRoomDlg 对话框

class CZegoRoomDlg : public CDialogEx, public sigslot::has_slots<>
{
	DECLARE_DYNAMIC(CZegoRoomDlg)

public:
	CZegoRoomDlg(SettingsPtr curSettings, CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CZegoRoomDlg();

	void SetRoom(RoomPtr roomModel);

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_DIALOG_ROOM };
#endif

protected:
	virtual BOOL OnInitDialog();
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
	void OnGetMinMaxInfo(MINMAXINFO *lpMMI);
	void OnClose();
	void OnSize(UINT nType, int cx, int cy);
	void OnSysCommand(UINT nID, LPARAM lParam);

	afx_msg void OnBnClickedButtonSend();
	afx_msg void OnBnClickedButtonBack();
	afx_msg void OnBnClickedCheckMicrophone();
	afx_msg void OnBnClickedButtonAux();
	afx_msg void OnIdok();

	// ChatRoomSDK Callback Function Set
	void OnChatRoomUserList(std::vector<UserPtr> userAddList, std::vector<UserPtr> userDelList, CHATROOM::ZegoUserUpdateFlag flag, bool isPublic);
	void OnChatRoomStreamCreate(std::string streamId, std::string url, bool isPublic);
	void OnChatRoomStreamList(std::vector<StreamPtr>, CHATROOM::ZegoUpdateFlag flag, bool isPublic);
	void OnChatRoomDisconnected(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool isPublic);
	void OnChatRoomKickout(int nErrorCode, std::string msg, bool isPublic);
	void OnChatRoomSendMessage(int nErrorCode, std::string msg, bool isPublic);
	void OnChatRoomRecvMessage(std::string msg, bool isPublic);

	// AVKit Callback Function Set
	void OnAVLoginChannel(std::string userId, std::string channelId, unsigned int uErrorCode);
	void OnAVLogoutChannel(std::string userId, std::string channelId, unsigned int uErrorCode);
	void OnAVPublishState(std::string userId, std::string channelId, AV::ZegoAVAPIState estate, std::string streamId, StreamPtr stream);
	void OnAVPlayState(std::string userId, std::string channelId, AV::ZegoAVAPIState estate, std::string streamId);
	void OnAVPublishQuality(std::string streamId, int quality);
	void OnAVPlayQuality(std::string streamId, int quality);
	void OnAVAuxInput(unsigned char *pData, int *pDataLen, int *pSampleRate, int *pNumChannels);
	void OnAVDisconnected(std::string userId, std::string channelId, unsigned int errCode);
	void OnAVReconnected(std::string userId, std::string channelId);

protected:
	void RefreshVisitorList(void);

	void BeginAux(void);
	void EndAux(void);

	void WakeupPendingStream(void);
	void PlayPendingStream(StreamPtr stream);
	void ShutdownPlayingStream(const std::string& streamId);
	void FreeAVView(StreamPtr stream);

	void HandleTextMessage(const std::string& msg, size_t split);
	void HandlePublishRequest(const std::string& msg);

private:
	HICON m_hIcon;

	SettingsPtr m_pAVSettings;

	RoomPtr m_pChatRoom;
	bool m_bLoginChannel;

	unsigned char* m_pAuxData;
	int m_nAuxDataLen;
	int m_nAuxDataPos;

	CGridListCtrlEx m_listVisitors;
	CEdit m_edMessage;
	CListBox m_lbChatContent;
	std::string m_strLastSendMsg;
	CEdit m_edStreamUrl;
	BOOL m_bCKEnableMic;
	CButton m_btnAux;

	CZegoAVView m_primaryAVView;
	std::stack<unsigned int> m_avaliablePrimaryView;
	CZegoAVView m_student1AVView;
	CZegoAVView m_student2AVView;
	CZegoAVView m_student3AVView;
	CZegoAVView m_studeng4AVView;
	CZegoAVView m_student5AVView;
	CZegoAVView m_student6AVView;	
	std::stack<unsigned int> m_avaliableStudentView;
};
