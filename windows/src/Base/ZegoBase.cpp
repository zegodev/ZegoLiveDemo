#include "stdafx.h"
#include "ZegoBase.h"
#include "ZegoAVApi.h"
#include "ZegoChatRoom.h"
#include "ZegoUtility.h"
#include "ZegoSigslotDefine.h"
using namespace ZEGO;

static DWORD g_dwAppID2 = 1;

static BYTE g_bufSignKey2[] =
{
	0x91,0x93,0xcc,0x66,0x2a,0x1c,0xe,0xc1,
	0x35,0xec,0x71,0xfb,0x7,0x19,0x4b,0x38,
	0x15,0xf1,0x43,0xf5,0x7c,0xd2,0xb5,0x9a,
	0xe3,0xdd,0xdb,0xe0,0xf1,0x74,0x36,0xd,
};

LRESULT CALLBACK ZegoCommuExchangeWndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	if (uMsg == WM_ZEGO_SWITCH_THREAD)
	{
		std::function<void(void)>* pFunc = (std::function<void(void)>*)wParam;
		(*pFunc)();
		delete pFunc;
	}

	return DefWindowProc(hWnd, uMsg, wParam, lParam);
}

CZegoBase::CZegoBase(void) : m_dwInitedMask(INIT_NONE)
{
	WCHAR szAppName[MAX_PATH] = {0};
	::GetModuleFileNameW(NULL, szAppName, MAX_PATH);
	CString strAppFullName = szAppName;
	CString strPath = strAppFullName.Left(strAppFullName.ReverseFind('\\') + 1);
	m_strLogPathUTF8 = WStringToUTF8(strPath);

	// 创建隐藏的通信窗体
	WNDCLASSEX wcex = { sizeof(WNDCLASSEX) };
	wcex.hInstance = GetModuleHandle(0);
	wcex.lpszClassName = ZegoCommWndClassName;
	wcex.lpfnWndProc = &ZegoCommuExchangeWndProc;
	wcex.hbrBackground = (HBRUSH)GetStockObject(NULL_BRUSH);
	RegisterClassEx(&wcex);
	m_hCommuWnd = CreateWindowEx(WS_EX_TOOLWINDOW, wcex.lpszClassName, ZegoCommWndName, WS_POPUP, 0, 0, 100, 100,
		NULL, NULL, wcex.hInstance, NULL);
	ShowWindow(m_hCommuWnd, SW_HIDE);

	m_pAVSignal = new CZegoAVSignal;
	m_pChatRoomSignal = new CZegoChatRoomSignal;
}

CZegoBase::~CZegoBase(void)
{
	UninitAVSdk();
	UninitChatRoomSdk();

	delete m_pAVSignal;
	delete m_pChatRoomSignal;

	DestroyWindow(m_hCommuWnd);
	CloseWindow(m_hCommuWnd);
}

bool CZegoBase::InitAVSdk(SettingsPtr pCurSetting)
{
	if (!IsAVSdkInited())
	{
		AV::SetLogLevel(AV::Debug, m_strLogPathUTF8.c_str());
		AV::SetBusinessType(0);
		AV::SetCallback(m_pAVSignal);

		AV::InitSDK(g_dwAppID2, g_bufSignKey2, 32);
	}

	AV::EnableAux(false);
	AV::SetPlayVolume(100);
	if (!pCurSetting->GetMircophoneId().empty())
	{
		AV::SetAudioDevice(AV::AudioDevice_Input, pCurSetting->GetMircophoneId().c_str());
	}

	AV::SetVideoResolution(pCurSetting->GetResolution().cx, pCurSetting->GetResolution().cy);
	AV::SetVideoBitrate(pCurSetting->GetBitrate());
	AV::SetVideoFPS(pCurSetting->GetFps());
	if (!pCurSetting->GetCameraId().empty())
	{
		AV::SetVideoDevice(pCurSetting->GetCameraId().c_str());
	}

	m_dwInitedMask |= INIT_AVSDK;
	return true;
}

void CZegoBase::UninitAVSdk(void)
{
	if (IsAVSdkInited())
	{
		AV::SetCallback(NULL);

		AV::UninitSDK();

		DWORD dwNegation = ~(DWORD)INIT_AVSDK;
		m_dwInitedMask &= dwNegation;
	}
}

bool CZegoBase::IsAVSdkInited(void)
{
	return (m_dwInitedMask & INIT_AVSDK) == INIT_AVSDK;
}

bool CZegoBase::InitChatRoomSdk(void)
{
	if ( IsChatRoomSdkInited() )
		return true;

	CHATROOM::SetNetType(CHATROOM::ZEGO_NT_LINE);

	CHATROOM::SetLogLevel(CHATROOM::Debug, m_strLogPathUTF8.c_str());

	CHATROOM::SetCallback(m_pChatRoomSignal);

	CHATROOM::InitSDK(g_dwAppID2, g_bufSignKey2, 32);

	m_dwInitedMask |= INIT_CRSDK;
	return true;
}

void CZegoBase::UninitChatRoomSdk(void)
{
	if (IsChatRoomSdkInited())
	{
		CHATROOM::SetCallback(NULL);

		CHATROOM::UninitSDK();

		DWORD dwNegation = ~(DWORD)INIT_CRSDK;
		m_dwInitedMask &= dwNegation;
	}
}

bool CZegoBase::IsChatRoomSdkInited(void)
{
	return (m_dwInitedMask & INIT_CRSDK) == INIT_CRSDK;
}

CZegoAVSignal& CZegoBase::GetAVSignal(void)
{
	return *m_pAVSignal;
}

CZegoChatRoomSignal& CZegoBase::GetChatRoomSignal(void)
{
	return *m_pChatRoomSignal;
}