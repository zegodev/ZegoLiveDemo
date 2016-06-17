#pragma once
#include <string>
#include <windows.h>
#include "ZegoAVApi.h"

#define WM_CAPTURE_MESSAGE      WM_APP + 2
static const int g_capture_time_id = 100001;


using namespace AVE;


class VideoCaptureImageDevice : public VideoCaptureDevice 
{
public:
	VideoCaptureImageDevice();
	virtual ~VideoCaptureImageDevice();

public:
	virtual void AllocateAndStart(VideoCaptureDevice::Client* client);
	virtual void StopAndDeAllocate();

	virtual int StartCapture();
	virtual int StopCapture();
	virtual int SetFrameRate(int framerate);
	virtual int SetResolution(int width, int height);
	virtual int SetFrontCam(int bFront);
	virtual int SetView(void *view);
	virtual int SetViewMode(int nMode);
	virtual int SetViewRotation(int nRotation);
	virtual int SetCaptureRotation(int nRotation);
	virtual int StartPreview();
	virtual int StopPreview();
	virtual int EnableTorch(bool bEnable);
	virtual int TakeSnapshot();
	virtual int SetPowerlineFreq(unsigned int nFreq);

public:
	void OnRecvWndMsg(WPARAM wParam, LPARAM lParam);
	void OnTimer();

protected:
	int _StartCapture();
	int _StopCapture();
	int _SetFrameRate(int framerate);
	int _SetResolution(int width, int height);
	int _SetFrontCam(int bFront);
	int _SetView(void *view);
	int _SetViewMode(int nMode);
	int _SetViewRotation(int nRotation);
	int _SetCaptureRotation(int nRotation);
	int _StartPreview();
	int _StopPreview();
	int _EnableTorch(bool bEnable);
	int _TakeSnapshot();
	int _SetPowerlineFreq(unsigned int nFreq);

protected:
	void PostMessageToMainWnd(int type, LPARAM lParam = 0);
	void SendMessageToMainWnd(int type, LPARAM lParam = 0);
	HBITMAP ConvertIconToBitmap(HICON hIcon);
	void SetCaptureTimer();
	void KillCaptureTimer();
private:
	VideoCaptureDevice::Client* m_pClient;
	bool m_isCapturing;
	bool m_isPreviewing;

	int m_fps;
	int m_width;
	int m_height;
	bool m_front;
	int m_rotation;
	int m_torch;

	HICON m_hIcon;
};

class CaptureFactoryImpl : public VideoCaptureFactory
{
public:
	CaptureFactoryImpl();
	virtual ~CaptureFactoryImpl();

	virtual VideoCaptureDevice* Create(const char* device_id);
	virtual void Destroy(VideoCaptureDevice *vc);

public:
	void OnRecvWndMsg(WPARAM wParam, LPARAM lParam);
private:
	VideoCaptureImageDevice* m_pDevice;
};
