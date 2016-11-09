
// ZegoSettingsDlg.h : header file
//

#pragma once

#include <vector>
#include <memory>
#include "ZegoSettingsModel.h"

// CZegoSettingsDlg dialog
class CZegoSettingsDlg : public CDialogEx
{
// Construction
public:
	CZegoSettingsDlg(SettingsPtr pDefaultSettings, CWnd* pParent = NULL);	// standard constructor

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ZEGOLIVEDEMO_DIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support

// Implementation
protected:
	// Generated message map functions
	virtual BOOL OnInitDialog();
	DECLARE_MESSAGE_MAP()
	afx_msg void OnBnClickedOk();
	afx_msg void OnBnClickedCancel();

private:
	HICON m_hIcon;

	CComboBox m_cbResolution;
	CComboBox m_cbBitrate;
	CComboBox m_cbFPS;
    CComboBox m_cbAudioDevice;
	CComboBox m_cbCameraDevice;
	std::vector<std::string> m_vecAudioDeviceIDs;
	std::vector<std::string> m_vecVideoDeviceIDs;

	SettingsPtr m_pSettingsModel;
};
