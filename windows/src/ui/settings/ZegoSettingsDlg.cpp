// ZegoSettingsDlg.cpp : implementation file
//

#include "stdafx.h"
#include "ZegoSettingsDlg.h"
#include "ZegoLiveDemo.h"

#include <tuple>
#include "ZegoAVApi.h"
#include "ZegoAVApi-advanced.h"
#include "ZegoAVDefines.h"
using namespace ZEGO::AV;

#include "ZegoUtility.h"
#include "ZegoSettingsModel.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

CZegoSettingsDlg::CZegoSettingsDlg(SettingsPtr pDefaultSettings, CWnd* pParent /*=NULL*/)
	: m_pSettingsModel(pDefaultSettings), CDialogEx(IDD_ZEGOLIVEDEMO_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CZegoSettingsDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);

	DDX_Control(pDX, IDC_COMBO_RESOLUTION, m_cbResolution);
	DDX_Control(pDX, IDC_COMBO_BITRATE, m_cbBitrate);
	DDX_Control(pDX, IDC_COMBO_FPS, m_cbFPS);
	DDX_Control(pDX, IDC_COMBO_AUDIODEVICE, m_cbAudioDevice);
	DDX_Control(pDX, IDC_COMBO_VIDEODEVICE, m_cbCameraDevice);
}

BEGIN_MESSAGE_MAP(CZegoSettingsDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_CLOSE()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDOK, &CZegoSettingsDlg::OnBnClickedOk)
END_MESSAGE_MAP()


// CZegoSettingsDlg message handlers

BOOL CZegoSettingsDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	SetIcon(m_hIcon, TRUE); // 设置大图标
	SetIcon(m_hIcon, FALSE);// 设置小图标

	int nCBCurSel(0);

	SIZE defResolution = m_pSettingsModel->GetResolution();
	for (int i = 0; i < sizeof(g_Resolution)/sizeof(g_Resolution[0]); ++i)
	{
		CString strText;
		strText.Format(_T("%d×%d"), g_Resolution[i].cx, g_Resolution[i].cy);
		m_cbResolution.AddString(strText);

		if (defResolution.cx == g_Resolution[i].cx && defResolution.cy == g_Resolution[i].cy)
			nCBCurSel = i;
	}
	m_cbResolution.SetCurSel(nCBCurSel);

	nCBCurSel = 0;
	int defBitrate = m_pSettingsModel->GetBitrate();
	for (int i = 0; i < sizeof(g_Bitrate)/sizeof(g_Bitrate[0]); ++i)
	{
		CString strText;
		int m = g_Bitrate[i]/(1024 * 1024);
		m > 1 ? strText.Format(_T("%dm"), m) : strText.Format(_T("%dk"), g_Bitrate[i] / 1024);
		m_cbBitrate.AddString(strText);

		if (defBitrate == g_Bitrate[i])
			nCBCurSel = i;
	}
	m_cbBitrate.SetCurSel(nCBCurSel);

	nCBCurSel = 0;
	int defFps = m_pSettingsModel->GetFps();
	for (int i = 0; i<sizeof(g_Fps)/sizeof(g_Fps[0]); ++i)
	{
		CString strText;
		strText.Format(_T("%d"), g_Fps[i]);
		m_cbFPS.AddString(strText);

		if (defFps == g_Fps[i])
			nCBCurSel = i;
	}
	m_cbFPS.SetCurSel(nCBCurSel);

	int nDeviceCount = 0;
	DeviceInfo* pDeviceList(NULL);

	nCBCurSel = 0;
	pDeviceList = ZEGO::AV::GetAudioDeviceList(AudioDevice_Input, nDeviceCount);
	for (int i = 0; i < nDeviceCount; ++i)
	{
		m_cbAudioDevice.AddString(UTF8ToWString(pDeviceList[i].szDeviceName).c_str());
		m_vecAudioDeviceIDs.push_back(pDeviceList[i].szDeviceId);
		if (m_pSettingsModel->GetMircophoneId() == std::string(pDeviceList[i].szDeviceId))
			nCBCurSel = i;
	}
	m_cbAudioDevice.SetCurSel(nCBCurSel);
	ZEGO::AV::FreeDeviceList(pDeviceList);
	pDeviceList = NULL;

	nCBCurSel = 0;
	pDeviceList = ZEGO::AV::GetVideoDeviceList(nDeviceCount);
	for (int i = 0; i < nDeviceCount; ++i)
	{
		m_cbCameraDevice.AddString(UTF8ToWString(pDeviceList[i].szDeviceName).c_str());
		m_vecVideoDeviceIDs.push_back(pDeviceList[i].szDeviceId);
		if (m_pSettingsModel->GetCameraId() == std::string(pDeviceList[i].szDeviceId))
			nCBCurSel = i;
	}
	m_cbCameraDevice.SetCurSel(nCBCurSel);
	ZEGO::AV::FreeDeviceList(pDeviceList);
	pDeviceList = NULL;

    return TRUE;
}

void CZegoSettingsDlg::OnBnClickedOk()
{
	UpdateData(TRUE);

	int nCurResolutionSel = m_cbResolution.GetCurSel() >= 0 ? m_cbResolution.GetCurSel() : 0;
	m_pSettingsModel->SetResolution(g_Resolution[nCurResolutionSel]);

	int nCurBitrateSel = m_cbBitrate.GetCurSel() >= 0 ? m_cbBitrate.GetCurSel() : 0;
	m_pSettingsModel->SetBitrate(g_Bitrate[nCurBitrateSel]);

	int nCurFpsSel = m_cbFPS.GetCurSel() >= 0 ? m_cbFPS.GetCurSel() : 0;
	m_pSettingsModel->SetFps(g_Fps[nCurFpsSel]);

	int nCurAudioSel = m_cbAudioDevice.GetCurSel();
	if (nCurAudioSel >= 0 && nCurAudioSel < (int)m_vecAudioDeviceIDs.size())
	{
		m_pSettingsModel->SetMicrophoneId(m_vecAudioDeviceIDs[nCurAudioSel]);
	}
	else
	{
		m_pSettingsModel->SetMicrophoneId("");
	}

	int nCurVideoSel = m_cbCameraDevice.GetCurSel();
	if (nCurVideoSel >= 0 && nCurVideoSel < (int)m_vecVideoDeviceIDs.size())
	{
		m_pSettingsModel->SetCameraId(m_vecVideoDeviceIDs[nCurVideoSel]);
	}
	else
	{
		m_pSettingsModel->SetCameraId("");
	}

	OnOK();
}

void CZegoSettingsDlg::OnBnClickedCancel()
{
	OnCancel();
}
