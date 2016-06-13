
// ZegoLiveDemoDlg.h : header file
//

#pragma once

#include "ZegoAVApi.h"
#include "ZegoAVCallback.h"
#include "afxcmn.h"
#include "ZegoAVDefines.h"
#include "afxwin.h"
#include <vector>
#include <memory>
#include "Setting.h"

typedef unsigned int uint32;
typedef unsigned __int64 uint64;

using namespace ZEGO::AV;

// CZegoLiveDemoDlg dialog
class CZegoLiveDemoDlg : public CDialogEx
{
// Construction
public:
	CZegoLiveDemoDlg(CWnd* pParent = NULL);	// standard constructor

	void SetSetting(CSetting* pSetting);

	virtual void OnGetVideoDeviceCallback(DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount);
	virtual void OnGetAudioDeviceCallback(AudioDeviceType deviceType, DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount);

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ZEGOLIVEDEMO_DIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support

	LRESULT OnZegoLiveMessage(WPARAM wParam, LPARAM lParam);
// Implementation
protected:
	// Generated message map functions
	virtual BOOL OnInitDialog();
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnBnClickedCancel();

private:
	BOOL m_bMic;
	BOOL m_bAux;

	DWORD m_dwAppId;
	CString m_strKey;

	int m_nResolutionWidth;
	int m_nResolutionHeight;

	CString m_strAudio;
	CString m_strVideo;

    CComboBox m_cbAudioDevice;
	CComboBox m_cbCameraDevice;

	std::vector<CString> m_vecAudioDeviceIDs;
	std::vector<CString> m_vecVideoDeviceIDs;

	CSetting* m_pSetting;


	HICON m_hIcon;
};
