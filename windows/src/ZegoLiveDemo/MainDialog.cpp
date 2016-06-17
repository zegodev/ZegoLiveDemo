// MainDialog.cpp : implementation file
//

#include "stdafx.h"
#include "ZegoLiveDemo.h"
#include "MainDialog.h"
#include "afxdialogex.h"
#include "ZegoLiveDemoDlg.h"
#include <tuple>
#include "AvRoomAppUtility.h"

#define WM_ZEGOLIVE_MESSAGE      WM_APP + 1
enum ZEGOLIVE_MESSAGE_TYPE
{
	ZEGOLIVE_MESSAGE_TYPE_Unknown,
	ZEGOLIVE_MESSAGE_TYPE_OnGetinRoomResult,
	ZEGOLIVE_MESSAGE_TYPE_OnPlayListUpdate,
	ZEGOLIVE_MESSAGE_TYPE_OnGetShowListResult,
	ZEGOLIVE_MESSAGE_TYPE_OnGetReplayListResult,

	ZEGOLIVE_MESSAGE_TYPE_OnPublishStateUpdate,
	ZEGOLIVE_MESSAGE_TYPE_OnPlaySucc,
	ZEGOLIVE_MESSAGE_TYPE_OnPlayStop,
};


// CMainDialog dialog

IMPLEMENT_DYNAMIC(CMainDialog, CDialogEx)

CMainDialog::CMainDialog(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_DIALOG_MAIN, pParent)
{
	m_bLogin = false;
	m_bIsPublishing = false;

	m_pAuxData = NULL;
	m_dwDataLen = 0;
	m_dwPos = 0;

	m_hasInitSdk = false;

	m_pDlg = NULL;

	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

CMainDialog::~CMainDialog()
{
}

void CMainDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);

	DDX_Text(pDX, IDC_EDIT_USERID, m_strUserID);
	DDX_Text(pDX, IDC_EDIT_USERNAME, m_strUserName);
	DDX_Text(pDX, IDC_EDIT_CHANNEL_ID, m_strChannelID);
	DDX_Text(pDX, IDC_EDIT_LIVE_TITLE, m_strTitle);
	DDX_Text(pDX, IDC_EDIT_STREAM_ID, m_strStreamID);
	DDX_Text(pDX, IDC_STATIC_GETINROOM, m_strLoginState);
	DDX_Text(pDX, IDC_EDIT_PLAY, m_strPlayStreamId0);
	DDX_Text(pDX, IDC_EDIT_PLAY2, m_strPlayStreamId1);
	DDX_Text(pDX, IDC_STATIC_PLAY_STATUS, m_strPlayStatus);
	DDX_Text(pDX, IDC_EDIT_PUBLISH_URL, m_strPublishState);
}


BEGIN_MESSAGE_MAP(CMainDialog, CDialogEx)
	ON_BN_CLICKED(IDC_BUTTON_SETTING, &CMainDialog::OnBnClickedButtonSetting)
	ON_BN_CLICKED(IDC_BUTTON_GETINROOM, &CMainDialog::OnBnClickedButtonGetinroom)
	ON_BN_CLICKED(IDC_BUTTON_LEAVE_CHANNEL, &CMainDialog::OnBnClickedButtonLeaveChannel)
	ON_BN_CLICKED(IDC_Publish, &CMainDialog::OnBnClickedPublish)
	ON_MESSAGE(WM_ZEGOLIVE_MESSAGE, &CMainDialog::OnZegoLiveMessage)
	ON_MESSAGE(WM_CAPTURE_MESSAGE, &CMainDialog::OnCaptureMessage)
	ON_BN_CLICKED(IDC_BUTTON_PLAY, &CMainDialog::OnBnClickedButtonPlay)
	ON_BN_CLICKED(IDC_BUTTON_LOG, &CMainDialog::OnBnClickedButtonLog)
	ON_BN_CLICKED(IIDC_BUTTON_PLAY2, &CMainDialog::OnBnClickedButtonPlay2)
	ON_BN_CLICKED(IDC_BUTTON_STOPPUB, &CMainDialog::OnBnClickedButtonStoppub)
	ON_WM_CLOSE()
    ON_BN_CLICKED(IDC_BUTTON_INITSDK, &CMainDialog::OnBnClickedButtonInitsdk)
    ON_BN_CLICKED(IDC_BUTTON_UNINITSDK, &CMainDialog::OnBnClickedButtonUninitsdk)
	ON_BN_CLICKED(IDC_BUTTON_CAPTURE, &CMainDialog::OnBnClickedButtonCapture)
END_MESSAGE_MAP()


// CMainDialog message handlers

BOOL CMainDialog::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}


	HWND hMainView = ::GetDlgItem(GetSafeHwnd(), IDC_STATIC_MAIN_PLAYER);
	LONG style = GetWindowLong(hMainView, GWL_STYLE);
	style = style | WS_CLIPSIBLINGS;
	SetWindowLong(hMainView, GWL_STYLE, style);

	srand((unsigned int)time(0));
	int x = rand();
	m_strUserID.Format(L"%d", x);

	m_strUserName = L"Windows SDK Demo";
	m_strChannelID = L"5190";

	m_strTitle = L"Hello Win32";

	m_strPlayStreamId0 = L"2015";
	m_strPlayStreamId1 = L"7840";

	UpdateData(FALSE);


	WCHAR szAppName[MAX_PATH];
	::GetModuleFileNameW(NULL, szAppName, MAX_PATH);
	CString strAppFullName = szAppName;
	CString strPath = strAppFullName.Left(strAppFullName.ReverseFind('\\') + 1);
	ZEGO::AV::SetLogLevel(Debug, AvRoomApp::Utility::WStringToUTF8(strPath).c_str());

	ZEGO::AV::SetCallback(this);

	return TRUE;
}


void CMainDialog::LoadAuxData()
{
	if (m_pAuxData)
	{
		return;
	}


	char bufPath[MAX_PATH] = { 0 };
	GetModuleFileNameA(NULL, bufPath, MAX_PATH);

	CStringA str = bufPath;
	auto idx = str.ReverseFind('\\');
	str.Delete(idx, str.GetLength() - idx);
	str.AppendFormat("\\%s", "a.pcm");

	FILE* f = fopen(str, "rb");
	if (!f)
	{
		return;
	}

	fseek(f, 0, SEEK_END);
	m_dwDataLen = ftell(f);
	if (m_dwDataLen > 0)
	{
		m_pAuxData = new unsigned char[m_dwDataLen];
		memset(m_pAuxData, 0, m_dwDataLen);
	}
	fseek(f, 0, 0);

	int c = fread(m_pAuxData, sizeof(unsigned char), m_dwDataLen, f);
	int b = c;

	fclose(f);
	ASSERT(c == m_dwDataLen);
}


void CMainDialog::OnAuxCallback(unsigned char *pData, int *pDataLen, int *pSampleRate, int *pNumChannels)
{
	if (m_pAuxData && (*pDataLen < m_dwDataLen))
	{
		*pSampleRate = 44100;
		*pNumChannels = 2;

		if (m_dwPos + *pDataLen > m_dwDataLen)
		{
			m_dwPos = 0;
		}

		memcpy(pData, m_pAuxData + m_dwPos, *pDataLen);

		m_dwPos += *pDataLen;
	}
	else
	{
		*pDataLen = 0;
	}
}

void CMainDialog::InitSdk()
{
	UninitSdk();

	m_hasInitSdk = true;

	ZEGO::AV::InitSDK(m_setting.m_dwAppId, m_setting.m_signkey, 32);
	//ZEGO::AV::SetCallback(this);
	ZEGO::AV::EnableAux(m_setting.m_bAux);
	ZEGO::AV::EnableMic(m_setting.m_bMic);

	ZEGO::AV::SetVideoResolution(m_setting.m_nResolutionWidth, m_setting.m_nResolutionHeight);
	ZEGO::AV::SetVideoBitrate(m_setting.m_nBitrate);
	ZEGO::AV::SetVideoFPS(m_setting.m_nFPS);

	if (!m_setting.m_strCam.IsEmpty())
	{
		ZEGO::AV::SetVideoDevice(AvRoomApp::Utility::WStringToUTF8(m_setting.m_strCam.GetBuffer()).c_str());
	}

	if (!m_setting.m_strMic.IsEmpty())
	{
		ZEGO::AV::SetAudioDevice(ZEGO::AV::AudioDevice_Input, AvRoomApp::Utility::WStringToUTF8(m_setting.m_strMic.GetBuffer()).c_str());
	}
}


void CMainDialog::UninitSdk()
{
	if (m_hasInitSdk)
	{
		ZEGO::AV::StopPreview();
		ZEGO::AV::StopPlayStream(AvRoomApp::Utility::WStringToUTF8(m_strPlayStreamId0).c_str());
		ZEGO::AV::StopPlayStream(AvRoomApp::Utility::WStringToUTF8(m_strPlayStreamId1).c_str());
		ZEGO::AV::StopPublish();
		ZEGO::AV::UninitSDK();
		ZEGO::AV::LogoutChannel();
		m_hasInitSdk = false;
	}
}

void CMainDialog::OnBnClickedButtonSetting()
{
	m_pDlg = new CZegoLiveDemoDlg;
	m_pDlg->SetSetting(&m_setting);

	if (IDOK == m_pDlg->DoModal())
	{
		InitSdk();
	}

	delete m_pDlg;
	m_pDlg = NULL;
}

LRESULT CMainDialog::OnCaptureMessage(WPARAM wParam, LPARAM lParam)
{
	m_CaptureFactoryImpl.OnRecvWndMsg(wParam, lParam);
	return 0;
}


LRESULT CMainDialog::OnZegoLiveMessage(WPARAM wParam, LPARAM lParam)
{
	ZEGOLIVE_MESSAGE_TYPE type = (ZEGOLIVE_MESSAGE_TYPE)wParam;
	switch (type)
	{
	case ZEGOLIVE_MESSAGE_TYPE_OnGetinRoomResult:
	{
		unsigned int result = (unsigned int)lParam;

		m_bLogin = result == 0;

		if (result == 0)
		{
			m_strLoginState = L"进入房间成功";
		}
		else if (result == -1024)
		{
			m_strLoginState = L"退出了房间";
		}
		else
		{
			m_strLoginState.Format(L"进入房间是吧, 错误码:%u", result);
		}

		UpdateData(FALSE);
	}
	break;

	case ZEGOLIVE_MESSAGE_TYPE_OnPlayListUpdate:
	{
		//std::tuple<PlayListUpdateFlag, ZegoLiveInfo*, unsigned int>* pParam = (std::tuple<PlayListUpdateFlag, ZegoLiveInfo*, unsigned int>*)lParam;
		//PlayListUpdateFlag flag = std::get<0>(*pParam);
		//ZegoLiveInfo* parrLiveInfo = std::get<1>(*pParam);
		//unsigned int uiLiveInfoCount = std::get<2>(*pParam);
	}
	break;
	case ZEGOLIVE_MESSAGE_TYPE_OnPublishStateUpdate:
	{
		auto* pParam = (std::tuple<std::string, ZegoAVAPIState>*)(lParam);

		std::string strPublishUrl = std::get<0>(*pParam);
		ZegoAVAPIState state = std::get<1>(*pParam);

		m_bIsPublishing = state == AVStateBegin;

		CString str;
		if (state == AVStateBegin)
		{
			std::wstring strURL = AvRoomApp::Utility::UTF8ToWString(strPublishUrl.c_str());
			strURL += L"\t";

			str.Format(L"%s", strURL.c_str());
		}
		else
		{
			str.Format(L"Publish Failed.");
		}

		m_strPublishState = str;
		UpdateData(FALSE);
		delete pParam;
	}
	break;
	case ZEGOLIVE_MESSAGE_TYPE_OnPlaySucc:
	{
		std::tuple<uint64, uint32, uint32>* pParam = (std::tuple<uint64, uint32, uint32>*)lParam;
		uint64 ddwStreamID = std::get<0>(*pParam);
		uint32 dwZegoToken = std::get<1>(*pParam);
		uint32 dwZegoId = std::get<2>(*pParam);


		m_strPlayStatus = L"播放开始";
	}
	break;
	case ZEGOLIVE_MESSAGE_TYPE_OnPlayStop:
	{
		std::tuple<uint32, uint64, uint32, uint32>* pParam = (std::tuple<uint32, uint64, uint32, uint32>*)lParam;
		uint32 dwErrorCode = std::get<0>(*pParam);
		uint64 ddwStreamID = std::get<1>(*pParam);
		uint32 dwZegoToken = std::get<3>(*pParam);
		uint32 dwZegoId = std::get<3>(*pParam);

		m_strPlayStatus = L"播放停止";
	}
	break;
	default:
		break;
	}
	return 0;
}


void CMainDialog::OnVideoSizeChanged(const char* pStreamID, int width, int height)
{

}

void CMainDialog::OnPreviewSnapshot(void *image)
{

}

void CMainDialog::OnRenderSnapshot(void *image, int nChannelIdx)
{

}

void CMainDialog::OnLoginChannel(const char* pszUserID, const char* pszChannelID, unsigned int dwErrorCode)
{
	if (IsWindow(m_hWnd))
	{
		PostMessage(WM_ZEGOLIVE_MESSAGE, (WPARAM)ZEGOLIVE_MESSAGE_TYPE_OnGetinRoomResult, (LPARAM)dwErrorCode);
	}
}


void CMainDialog::OnCountsUpdate(const char* pszUserID, const char* pszChannelID, unsigned int onlineNums, unsigned int onlineCount)
{

}

void CMainDialog::OnPublishStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState,
	const char* streamID, const char* pszPlayUrl)
{
	if (IsWindow(m_hWnd))
	{
		std::string url;
		if (pszPlayUrl)
		{
			url = pszPlayUrl;
		}

		auto* pParam = new std::tuple<std::string, ZegoAVAPIState>(url, eState);
		PostMessage(WM_ZEGOLIVE_MESSAGE, (WPARAM)ZEGOLIVE_MESSAGE_TYPE_OnPublishStateUpdate, (LPARAM)pParam);
	}
}

void CMainDialog::OnPlayStateUpdate(const char* pszUserID, const char* pszChannelID, ZegoAVAPIState eState, const char* ddwStreamID)
{

}

void CMainDialog::OnLogoutChannel(const char* pszUserID, const char* pszChannelID, unsigned int err)
{
	if (IsWindow(m_hWnd))
	{
		PostMessage(WM_ZEGOLIVE_MESSAGE, (WPARAM)ZEGOLIVE_MESSAGE_TYPE_OnGetinRoomResult, (LPARAM)-1024);
	}
}

void CMainDialog::OnDisconnected(const char* pszUserID, const char* pszChannelID, unsigned int err)
{

}

void CMainDialog::OnReconnected(const char* pszUserID, const char* pszChannelID)
{

}

void CMainDialog::OnGetVideoDeviceCallback(DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount)
{
	if (m_pDlg)
	{
		m_pDlg->OnGetVideoDeviceCallback(parrDeviceInfo, uiDeviceCount);
	}
}

void CMainDialog::OnGetAudioDeviceCallback(AudioDeviceType deviceType, DeviceInfo* parrDeviceInfo, unsigned int uiDeviceCount)
{
	if (m_pDlg)
	{
		m_pDlg->OnGetAudioDeviceCallback(deviceType, parrDeviceInfo, uiDeviceCount);
	}
}


void CMainDialog::OnBnClickedButtonGetinroom()
{
	UpdateData(TRUE);
	InitSdk();
	int result = ZEGO::AV::LoginChannel(AvRoomApp::Utility::WStringToUTF8(m_strUserID).c_str(), AvRoomApp::Utility::WStringToUTF8(m_strUserName).c_str(), AvRoomApp::Utility::WStringToUTF8(m_strChannelID).c_str(), 1);
}


void CMainDialog::OnBnClickedButtonLeaveChannel()
{
	ZEGO::AV::LogoutChannel();
}


void CMainDialog::OnBnClickedPublish()
{
	if (m_bIsPublishing) 
	{
		AfxMessageBox(L"Already publishing!");
		return;
	}

	HWND hView = ::GetDlgItem(GetSafeHwnd(), IDC_STATIC_LOCALVIEW);

	ZEGO::AV::SetPreviewView(hView);
	ZEGO::AV::StartPreview();

	UpdateData(TRUE);

	ZEGO::AV::StartPublish(AvRoomApp::Utility::WStringToUTF8(m_strTitle).c_str(), AvRoomApp::Utility::WStringToUTF8(m_strStreamID).c_str());
}



void CMainDialog::OnBnClickedButtonPlay()
{
	UpdateData(TRUE);

	HWND hMainView = ::GetDlgItem(GetSafeHwnd(), IDC_STATIC_MAIN_PLAYER);
	::ShowWindow(hMainView, SW_NORMAL);

	if (!m_strPlayStreamId0.IsEmpty())
	{
		ZEGO::AV::SetView(hMainView, 0);
		ZEGO::AV::PlayStream(AvRoomApp::Utility::WStringToUTF8(m_strPlayStreamId0).c_str(), 0);
	}
}


void CMainDialog::OnBnClickedButtonPlay2()
{
	UpdateData(TRUE);
	HWND hSubsView = ::GetDlgItem(GetSafeHwnd(), IDC_STATIC_SUB_PLAYER);
	::ShowWindow(hSubsView, SW_NORMAL);
	if (!m_strPlayStreamId1.IsEmpty())
	{
		ZEGO::AV::SetView(hSubsView, 1);
		ZEGO::AV::PlayStream(AvRoomApp::Utility::WStringToUTF8(m_strPlayStreamId1).c_str(), 1);
	}
}


void CMainDialog::OnBnClickedButtonLog()
{
	ZEGO::AV::UploadLog();
}

void CMainDialog::OnBnClickedButtonStoppub()
{
	ZEGO::AV::StopPublish();
}


void CMainDialog::OnClose()
{
	UninitSdk();
	__super::OnClose();
}


void CMainDialog::OnBnClickedButtonInitsdk()
{
    InitSdk();
}


void CMainDialog::OnBnClickedButtonUninitsdk()
{
	UninitSdk();
}


void CMainDialog::OnBnClickedButtonCapture()
{
	ZEGO::AV::SetVideoCaptureFactory(&m_CaptureFactoryImpl);
}
