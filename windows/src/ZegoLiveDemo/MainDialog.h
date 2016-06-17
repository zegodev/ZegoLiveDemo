#pragma once
#include "ZegoAVApi.h"
#include "ZegoAVCallback.h"
#include "ZegoAVDefines.h"
#include "Setting.h"
#include "ZegoLiveDemoDlg.h"
#include "CaptureFactoryImpl.h"

using namespace ZEGO::AV;
// CMainDialog dialog

class CMainDialog : public CDialogEx, public IZegoLiveCallback
{
	DECLARE_DYNAMIC(CMainDialog)

public:
	CMainDialog(CWnd* pParent = NULL);   // standard constructor
	virtual ~CMainDialog();

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_DIALOG_MAIN };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSetting();

protected:
	LRESULT OnZegoLiveMessage(WPARAM wParam, LPARAM lParam);
	LRESULT OnCaptureMessage(WPARAM wParam, LPARAM lParam);
	virtual BOOL OnInitDialog();

public:
	virtual void OnCaptureVideoSizeChanged(int nWidth, int nHeight) {};
	virtual void OnVideoSizeChanged(const char* pStreamID, int nWidth, int nHeight);
	virtual void OnPreviewSnapshot(void *pImage);
	virtual void OnRenderSnapshot(void *pImage, int nChannelIdx);

	virtual void OnLoginChannel(const char* pszUserID, const char* pszChannelID, unsigned int uiErrorCode);
	virtual void OnLogoutChannel(const char* pszUserID, const char* pszChannelID, unsigned int nErr);

	virtual void OnCountsUpdate(const char* pszUserID, const char* pszChannelID, unsigned int uiOnlineNums, unsigned int uiOnlineCount);

	virtual void OnPublishStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState,
		const char* pszStreamID, const char* pszPlayUrl);
	virtual void OnPlayStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState, const char* pszStreamID);

	virtual void OnDisconnected(const char* pszUserID, const char* pszChannelID, unsigned int uiErr);
	virtual void OnReconnected(const char* pszUserID, const char* pszChannelID);

	virtual void OnGetVideoDeviceCallback(DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount);
	virtual void OnGetAudioDeviceCallback(AudioDeviceType deviceType, DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount);

	/// IZegoAuxCallback
	virtual void OnAuxCallback(unsigned char *pData, int *pDataLen, int *pSampleRate, int *pNumChannels);

protected:
	void LoadAuxData();
	void InitSdk();
	void UninitSdk();

private:
	unsigned char* m_pAuxData ;
	int m_dwDataLen;
	int m_dwPos;

	bool m_hasInitSdk;

	CString m_strUserID;
	CString m_strUserName;
	CString m_strChannelID;


	CString m_strTitle;
	CString m_strStreamID;

	CString m_strPublishState;
	CString m_strLoginState;

	CString m_strPlayStreamId0;
	CString m_strPlayStreamId1;
	CString m_strPlayStatus;

	bool m_bLogin;
	bool m_bIsPublishing;

	CZegoLiveDemoDlg* m_pDlg;
	CSetting m_setting;

	HICON m_hIcon;
	CaptureFactoryImpl m_CaptureFactoryImpl;
public:
	afx_msg void OnBnClickedButtonGetinroom();
	afx_msg void OnBnClickedButtonLeaveChannel();
	afx_msg void OnBnClickedPublish();
	afx_msg void OnBnClickedButtonPlay();
	afx_msg void OnBnClickedButtonLog();
	afx_msg void OnBnClickedButtonPlay2();
	afx_msg void OnBnClickedButtonStoppub();
	afx_msg void OnClose();
    afx_msg void OnBnClickedButtonInitsdk();
    afx_msg void OnBnClickedButtonUninitsdk();
	afx_msg void OnBnClickedButtonCapture();
};
