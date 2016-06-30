#pragma once
#include "stdafx.h"
#include "CaptureFactoryImpl.h"
#include "ZegoLiveDemo.h"
#include <utility>


enum CAPTURE_MESSAGE_TYPE
{
	CAPTURE_MESSAGE_TYPE_StartCapture,
	CAPTURE_MESSAGE_TYPE_StopCapture,
	CAPTURE_MESSAGE_TYPE_SetFrameRate,
	CAPTURE_MESSAGE_TYPE_SetResolution,
	CAPTURE_MESSAGE_TYPE_SetFrontCam,
	CAPTURE_MESSAGE_TYPE_SetView,
	CAPTURE_MESSAGE_TYPE_SetViewMode,
	CAPTURE_MESSAGE_TYPE_SetViewRotation,
	CAPTURE_MESSAGE_TYPE_SetCaptureRotation,
	CAPTURE_MESSAGE_TYPE_StartPreview,
	CAPTURE_MESSAGE_TYPE_StopPreview,
	CAPTURE_MESSAGE_TYPE_EnableTorch,
	CAPTURE_MESSAGE_TYPE_TakeSnapshot,
	CAPTURE_MESSAGE_TYPE_SetPowerlineFreq,
};

VideoCaptureImageDevice* g_pDevice = nullptr;

CaptureFactoryImpl::CaptureFactoryImpl()
{
	m_pDevice = nullptr;
}

CaptureFactoryImpl::~CaptureFactoryImpl()
{
}

VideoCaptureDevice* CaptureFactoryImpl::Create(const char* device_id)
{
	m_pDevice = new VideoCaptureImageDevice;
	g_pDevice = m_pDevice;

	m_pDevice->SetCaptureWindow(m_hWnd);

	return m_pDevice;
}

void CaptureFactoryImpl::Destroy(VideoCaptureDevice *vc)
{
	delete m_pDevice;
	m_pDevice = nullptr;
	g_pDevice = nullptr;
}

void CaptureFactoryImpl::SetCaptureWindow(HWND hWnd)
{
	m_hWnd = hWnd;
	if (g_pDevice)
	{
		g_pDevice->SetCaptureWindow(hWnd);
	}
}

void CaptureFactoryImpl::OnRecvWndMsg(WPARAM wParam, LPARAM lParam)
{
	if (m_pDevice)
	{
		m_pDevice->OnRecvWndMsg(wParam, lParam);
	}
}

VideoCaptureImageDevice::VideoCaptureImageDevice()
{
	m_pClient = nullptr;
	m_isCapturing = false;
	m_isPreviewing = false;

	m_fps = 15;
	m_width = 640;
	m_height = 480;
	m_front = true;
	m_rotation = 0;
	m_torch = 0;

	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

VideoCaptureImageDevice::~VideoCaptureImageDevice()
{

}

void VideoCaptureImageDevice::PostMessageToMainWnd(int type, LPARAM lParam)
{
	CWnd* pMainWnd = theApp.GetMainWnd();
	if (pMainWnd)
	{
		pMainWnd->PostMessage(WM_CAPTURE_MESSAGE, (WPARAM)type, lParam);
	}
}

void VideoCaptureImageDevice::SendMessageToMainWnd(int type, LPARAM lParam)
{
	CWnd* pMainWnd = theApp.GetMainWnd();
	if (pMainWnd)
	{
		pMainWnd->SendMessage(WM_CAPTURE_MESSAGE, (WPARAM)type, lParam);
	}
}


void VideoCaptureImageDevice::OnRecvWndMsg(WPARAM wParam, LPARAM lParam)
{
	int type = (int)wParam;

	switch (type)
	{
	case CAPTURE_MESSAGE_TYPE_StartCapture:
		_StartCapture();
		break;
	case CAPTURE_MESSAGE_TYPE_StopCapture:
		_StopCapture();
		break;
	case CAPTURE_MESSAGE_TYPE_SetFrameRate:
		_SetFrameRate((int)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_SetResolution:
	{
		auto* pParam = (std::pair<int, int>*)lParam;
		_SetResolution(pParam->first, pParam->second);
		delete pParam;
	}
		break;
	case CAPTURE_MESSAGE_TYPE_SetFrontCam:
		SetFrontCam((int)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_SetView:
		_SetView((void*)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_SetViewMode:
		_SetViewMode((int)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_SetViewRotation:
		_SetViewRotation((int)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_SetCaptureRotation:
		_SetCaptureRotation((int)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_StartPreview:
		_StartPreview();
		break;
	case CAPTURE_MESSAGE_TYPE_StopPreview:
		_StopPreview();
		break;
	case CAPTURE_MESSAGE_TYPE_EnableTorch:
		_EnableTorch((bool)lParam);
		break;
	case CAPTURE_MESSAGE_TYPE_TakeSnapshot:
		_TakeSnapshot();
		break;
	case CAPTURE_MESSAGE_TYPE_SetPowerlineFreq:
		_SetPowerlineFreq((unsigned int)lParam);
		break;
	default:
		break;
	}
}


void VideoCaptureImageDevice::SetCaptureWindow(HWND hWnd)
{
	m_hWnd = hWnd;

	ZEGO::AV::AddWindowCapture(m_hWnd, true);
}

void VideoCaptureImageDevice::AllocateAndStart(VideoCaptureDevice::Client* client)
{
	m_pClient = client;
}

void VideoCaptureImageDevice::StopAndDeAllocate()
{
	if (m_pClient)
	{
		m_pClient->Destroy();
		m_pClient = nullptr;
	}
}

int VideoCaptureImageDevice::StartCapture()
{
	if (m_isCapturing)
	{
		return 0;
	}

	if (!::IsWindow(m_hWnd))
	{
		return 0;
	}

	m_isCapturing = true;

	
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StartCapture);

	return 0;
}

int VideoCaptureImageDevice::StopCapture()
{
	m_isCapturing = false;
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture);
	return 0;
}

int VideoCaptureImageDevice::SetFrameRate(int framerate)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)framerate);
	return 0;
}

int VideoCaptureImageDevice::SetResolution(int width, int height)
{
	auto* pParam = new std::pair<int, int>(width, height);
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)pParam);
	return 0;
}

int VideoCaptureImageDevice::SetFrontCam(int bFront)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)bFront);
	return 0;
}

int VideoCaptureImageDevice::SetView(void *view)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)view);
	return 0;
}

int VideoCaptureImageDevice::SetViewMode(int nMode)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)nMode);
	return 0;
}

int VideoCaptureImageDevice::SetViewRotation(int nRotation)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)nRotation);
	return 0;
}

int VideoCaptureImageDevice::SetCaptureRotation(int nRotation)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)nRotation);
	return 0;
}

int VideoCaptureImageDevice::StartPreview()
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture);
	return 0;
}

int VideoCaptureImageDevice::StopPreview()
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture);
	return 0;
}

int VideoCaptureImageDevice::EnableTorch(bool bEnable)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)bEnable);
	return 0;
}

int VideoCaptureImageDevice::TakeSnapshot()
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture);
	return 0;
}

int VideoCaptureImageDevice::SetPowerlineFreq(unsigned int nFreq)
{
	PostMessageToMainWnd(CAPTURE_MESSAGE_TYPE_StopCapture, (LPARAM)nFreq);
	return 0; 
}


//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
//impl
int VideoCaptureImageDevice::_StartCapture()
{
	KillCaptureTimer();
	SetCaptureTimer();
	return 0;
}

int VideoCaptureImageDevice::_StopCapture() 
{
	return 0;
}

int VideoCaptureImageDevice::_SetFrameRate(int framerate)
{
	m_fps = framerate;
	return 0;
}

int VideoCaptureImageDevice::_SetResolution(int width, int height)
{
	m_width = width;
	m_height = height;
	return 0;
}

int VideoCaptureImageDevice::_SetFrontCam(int bFront)
{
	m_front = bFront;
	return 0;
}


int VideoCaptureImageDevice::_SetView(void *view)
{
	return 0;
}


int VideoCaptureImageDevice::_SetViewMode(int nMode)
{
	return 0;
}


int VideoCaptureImageDevice::_SetViewRotation(int nRotation)
{
	return 0;
}


int VideoCaptureImageDevice::_SetCaptureRotation(int nRotation)
{
	return 0;
}


int VideoCaptureImageDevice::_StartPreview()
{
	m_isPreviewing = true;
	_StartCapture();
	return 0;
}


int VideoCaptureImageDevice::_StopPreview()
{
	m_isPreviewing = false;
	_StopCapture();
	return 0;
}


int VideoCaptureImageDevice::_EnableTorch(bool bEnable)
{
	return 0;
}


int VideoCaptureImageDevice::_TakeSnapshot()
{
	return 0;
}


int VideoCaptureImageDevice::_SetPowerlineFreq(unsigned int nFreq)
{
	return 0;
}

void VideoCaptureImageDevice::OnTimer()
{
	if (m_pClient == nullptr)
	{
		return;
	}

	ZEGO::AV::RefreshWindowCapture(m_hWnd);

	unsigned int size = 0;
	unsigned int width = 0;
	unsigned int height = 0;
	unsigned int time = 0;

	
	BYTE* data =ZEGO::AV::GetWindowCaptureData(m_hWnd, size, width, height, time);

	
	VideoCaptureFormat format;
	format.width = width;
	format.height = height;
	format.pixel_format = PIXEL_FORMAT_BGRA32;

	if (m_pClient && data)
	{
		m_pClient->OnIncomingCapturedData((char*)data, size, format, GetTickCount(), 1000);
	}
}

void CALLBACK _OnTimer(HWND, UINT, UINT_PTR, DWORD)
{
	if (g_pDevice)
	{
		g_pDevice->OnTimer();
	}
}

void VideoCaptureImageDevice::SetCaptureTimer()
{
	CWnd* pMainWnd = theApp.GetMainWnd();
	if (pMainWnd)
	{
		int interval = 1000 / m_fps;
		if (interval < 20)
		{
			interval = 20;
		}

		pMainWnd->SetTimer(g_capture_time_id, interval, &_OnTimer);
	}
}

void VideoCaptureImageDevice::KillCaptureTimer()
{
	CWnd* pMainWnd = theApp.GetMainWnd();
	if (pMainWnd)
	{
		pMainWnd->KillTimer(g_capture_time_id);
	}
}
