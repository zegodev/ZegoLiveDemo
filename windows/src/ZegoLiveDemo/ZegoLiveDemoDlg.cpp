
// ZegoLiveDemoDlg.cpp : implementation file
//

#include "stdafx.h"
#include "ZegoLiveDemo.h"
#include "ZegoLiveDemoDlg.h"
#include "afxdialogex.h"
#include "AvRoomAppUtility.h"
#include <tuple>
#include "AvRoomAppUtility.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CZegoLiveDemoDlg dialog

#define WM_ZEGOLIVE_MESSAGE      WM_APP + 1

enum ZEGOLIVE_MESSAGE_TYPE
{
	ZEGOLIVE_MESSAGE_TYPE_Unknown,
	ZEGOLIVE_MESSAGE_TYPE_OnGetinRoomResult,
	ZEGOLIVE_MESSAGE_TYPE_OnGetAudioDevices,
	ZEGOLIVE_MESSAGE_TYPE_OnGetVideoDevices,

	ZEGOLIVE_MESSAGE_TYPE_OnPublishStateUpdate,
	ZEGOLIVE_MESSAGE_TYPE_OnPublishStop,
	ZEGOLIVE_MESSAGE_TYPE_OnPlaySucc,
	ZEGOLIVE_MESSAGE_TYPE_OnPlayStop,
};


CZegoLiveDemoDlg::CZegoLiveDemoDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_ZEGOLIVEDEMO_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);

	m_pSetting = NULL;
	m_nResolutionWidth = 640;
	m_nResolutionHeight = 480;
	m_bMic = TRUE;
	m_bAux = TRUE;
	m_dwAppId = 1;
	m_strKey = L"0x91,0x93,0xcc,0x66,0x2a,0x1c,0xe,0xc1,0x35, 0xec, 0x71, 0xfb, 0x7, 0x19, 0x4b, 0x38,0x15, 0xf1, 0x43, 0xf5, 0x7c, 0xd2, 0xb5, 0x9a,0xe3, 0xdd, 0xdb, 0xe0, 0xf1, 0x74, 0x36, 0xd";
}

void CZegoLiveDemoDlg::SetSetting(CSetting* pSetting)
{
	m_pSetting = pSetting;
}

void CZegoLiveDemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_AUDIODEVICE, m_cbAudioDevice);
	DDX_Control(pDX, IDC_COMBO_VIDEODEVICE, m_cbCameraDevice);
	DDX_Text(pDX, IDC_EDIT_APP_ID, m_dwAppId);
	DDX_Text(pDX, IDC_EDIT_KEY, m_strKey);
	DDX_Check(pDX, IDC_CHECK_MIC, m_bMic);
	DDX_Check(pDX, IDC_CHECK_AUX, m_bAux);
	DDX_Text(pDX, IDC_EDIT_RESOLUTION_WIDTH, m_nResolutionWidth);
	DDX_Text(pDX, IDC_EDIT_RESOLUTION_HEIGHT, m_nResolutionHeight);
	DDX_Text(pDX, IDC_COMBO_AUDIODEVICE, m_strAudio);
	DDX_Text(pDX, IDC_COMBO_VIDEODEVICE, m_strVideo);
}

BEGIN_MESSAGE_MAP(CZegoLiveDemoDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_MESSAGE(WM_ZEGOLIVE_MESSAGE, &CZegoLiveDemoDlg::OnZegoLiveMessage)
	ON_BN_CLICKED(IDOK, &CZegoLiveDemoDlg::OnBnClickedOk)
	ON_WM_CLOSE()
END_MESSAGE_MAP()


// CZegoLiveDemoDlg message handlers

BOOL CZegoLiveDemoDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	ZEGO::AV::GetAudioDeviceList(AudioDevice_Input);
	ZEGO::AV::GetVideoDeviceList();

	if (m_pSetting)
	{
		m_nResolutionWidth = m_pSetting->m_nResolutionWidth;
		m_nResolutionHeight = m_pSetting->m_nResolutionHeight;
		m_bMic = m_pSetting->m_bMic;
		m_bAux = m_pSetting->m_bAux;
		m_dwAppId = m_pSetting->m_dwAppId;

		m_strKey.Empty();

		for (int i = 0; i < 32; ++i)
		{
			WCHAR buf[8] = {};
			swprintf(buf, L"0x%x", m_pSetting->m_signkey[i]);
			m_strKey.Append(buf);
			if (i != 31)
			{
				m_strKey.Append(L",");
			}
		}
	}

	UpdateData(FALSE);
    return TRUE;
}

void CZegoLiveDemoDlg::OnGetVideoDeviceCallback(DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount)
{
	DeviceInfo* parrDevices = NULL;
	if (parrDeviceInfo && uiDeviceCount > 0)
	{
		parrDevices = new DeviceInfo[uiDeviceCount];
		memcpy(parrDevices, parrDeviceInfo, sizeof(DeviceInfo)*uiDeviceCount);
	}

	std::tuple<DeviceInfo*, unsigned int>* pParam = new std::tuple<DeviceInfo*, unsigned int>(parrDevices, uiDeviceCount);
	PostMessage(WM_ZEGOLIVE_MESSAGE, ZEGOLIVE_MESSAGE_TYPE_OnGetVideoDevices, (LPARAM)pParam);
}

void CZegoLiveDemoDlg::OnGetAudioDeviceCallback(AudioDeviceType deviceType, DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount)
{
	DeviceInfo* parrDevices = NULL;
	if (parrDeviceInfo && uiDeviceCount > 0)
	{
		parrDevices = new DeviceInfo[uiDeviceCount];
		memcpy(parrDevices, parrDeviceInfo, sizeof(DeviceInfo)*uiDeviceCount);
	}

	std::tuple<AudioDeviceType, DeviceInfo*, unsigned int>* pParam = new std::tuple<AudioDeviceType, DeviceInfo*, unsigned int>(deviceType, parrDevices, uiDeviceCount);
	PostMessage(WM_ZEGOLIVE_MESSAGE, ZEGOLIVE_MESSAGE_TYPE_OnGetAudioDevices, (LPARAM)pParam);

}


LRESULT CZegoLiveDemoDlg::OnZegoLiveMessage(WPARAM wParam, LPARAM lParam)
{
	ZEGOLIVE_MESSAGE_TYPE type = (ZEGOLIVE_MESSAGE_TYPE)wParam;
	if (type == ZEGOLIVE_MESSAGE_TYPE_OnGetVideoDevices)
	{
		std::tuple<DeviceInfo*, unsigned int>* pParam = (std::tuple<DeviceInfo*, unsigned int>*)lParam;

		DeviceInfo* parrDeviceInfo = std::get<0>(*pParam);
		unsigned int nDeviceCount = std::get<1>(*pParam);

		m_vecVideoDeviceIDs.clear();

		for (unsigned int i = 0; i < nDeviceCount; ++i)
		{
			m_cbCameraDevice.AddString(AvRoomApp::Utility::UTF8ToWString(parrDeviceInfo[i].szDeviceName).c_str());
			m_vecVideoDeviceIDs.push_back(AvRoomApp::Utility::UTF8ToWString(parrDeviceInfo[i].szDeviceId).c_str());
		}

		m_cbCameraDevice.SetCurSel(0);

		delete[] parrDeviceInfo;
		delete pParam;


	}
	else if (type == ZEGOLIVE_MESSAGE_TYPE_OnGetAudioDevices)
	{
		std::tuple<AudioDeviceType, DeviceInfo*, unsigned int>* pParam = (std::tuple<AudioDeviceType, DeviceInfo*, unsigned int>*)lParam;

		AudioDeviceType deviceType = std::get<0>(*pParam);
		DeviceInfo* parrDeviceInfo = std::get<1>(*pParam);
		unsigned int nDeviceCount = std::get<2>(*pParam);

		for (unsigned int i = 0; i < nDeviceCount; ++i)
		{
			m_cbAudioDevice.AddString(AvRoomApp::Utility::UTF8ToWString(parrDeviceInfo[i].szDeviceName).c_str());
			m_vecAudioDeviceIDs.push_back(AvRoomApp::Utility::UTF8ToWString(parrDeviceInfo[i].szDeviceId).c_str());
		}

		m_cbAudioDevice.SetCurSel(0);

		delete[] parrDeviceInfo;
		delete pParam;
	}

	return 0;
}



void split(std::string s, std::string delim, std::vector< std::string >* ret)
{
	size_t last = 0;
	size_t index = s.find_first_of(delim, last);
	while (index != std::string::npos)
	{
		ret->push_back(s.substr(last, index - last));
		last = index + 1;
		index = s.find_first_of(delim, last);
	}

	if (index - last > 0)
	{
		ret->push_back(s.substr(last, index - last));
	}
}

void CZegoLiveDemoDlg::OnBnClickedOk()
{
	UpdateData(TRUE);
	if (m_pSetting)
	{
		m_pSetting->m_bAux = m_bAux;
		m_pSetting->m_bMic = m_bMic;
		m_pSetting->m_dwAppId = m_dwAppId;
		m_pSetting->m_nResolutionHeight = m_nResolutionHeight;
		m_pSetting->m_nResolutionWidth = m_nResolutionWidth;

		m_pSetting->m_strCam.Empty();
		m_pSetting->m_strMic.Empty();

		int curAudio = m_cbAudioDevice.GetCurSel();
		if (curAudio > -1 && curAudio < m_vecAudioDeviceIDs.size())
		{
			m_pSetting->m_strMic = m_vecAudioDeviceIDs[curAudio];
		}

		int curVedio = m_cbCameraDevice.GetCurSel();
		if (curVedio > -1 && curVedio < m_vecVideoDeviceIDs.size())
		{
			m_pSetting->m_strCam = m_vecVideoDeviceIDs[curVedio];
		}

		std::vector< std::string > vecKey;
		split(AvRoomApp::Utility::WStringToUTF8(m_strKey), ",", &vecKey);

		if (vecKey.size() == 32)
		{
			for (int i = 0; i < 32; ++i)
			{
				int key = 0;
				sscanf(vecKey[i].c_str(), "%x", &key);
				m_pSetting->m_signkey[i] = key;
			}
		}
	}

	OnOK();
}

void CZegoLiveDemoDlg::OnBnClickedCancel()
{
	// TODO: Add your control notification handler code here
	OnCancel();
}
