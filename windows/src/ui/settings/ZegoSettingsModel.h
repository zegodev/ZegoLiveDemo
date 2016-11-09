#pragma once

#include <string>
#include <memory>

typedef enum _VideoQuality
{
	VQ_SuperLow = 0,
	VQ_Low = 1,
	VQ_Middle = 2,
	VQ_High = 3,
	VQ_SuperHigh = 4,
	VQ_SelfDef = 5,
}VideoQuality;

static SIZE g_Resolution[] =
{
	{ 1920, 1200 },{ 1728, 1080 },{ 1644, 1028 },{ 1440, 900 },{ 1280, 800 },{ 1152, 720 },
	{ 1044, 654 },{ 720, 640 },{ 640, 480 },{ 480, 320 },{ 320, 240 },{ 160, 120 },
};

static int g_Bitrate[] =
{
	2200 * 1024, 2000 * 1024, 1800 * 1024, 1500 * 1024, 1000 * 1024,
	800 * 1024, 700 * 1024, 650 * 1024, 600 * 1024, 550 * 1024,
	500 * 1024, 450 * 1024, 400 * 1024, 350 * 1024, 300 * 1024,
	250 * 1024, 180 * 1024, 150 * 1024, 120 * 1024, 100 * 1024,
};

static int g_Fps[] = { 30, 25, 20, 15, 10, 5 };

struct IndexSet
{
	unsigned int indexResolution;
	unsigned int indexBitrate;
	unsigned int indexFps;

	bool operator ==(const IndexSet& otherSet)
	{
		return indexResolution == otherSet.indexResolution &&
			indexBitrate == otherSet.indexBitrate &&
			indexFps == otherSet.indexFps;
	}
};

class CZegoSettingsModel
{
public:
	CZegoSettingsModel();
	CZegoSettingsModel(const IndexSet& index);

	VideoQuality GetQuality(bool primary);
	void SetQuality(bool primary, VideoQuality quality);

	SIZE GetResolution(void);
	void SetResolution(SIZE resolution);
	int  GetBitrate(void);
	void SetBitrate(int bitrate);
	int  GetFps(void);
	void SetFps(int fps);
	std::string GetCameraId(void);
	void SetCameraId(const std::string& cameraId);
	std::string GetMircophoneId(void);
	void SetMicrophoneId(const std::string& microphoneId);

private:
	void InitByIndex(void);
	void InitDeviceId(void);
	
	IndexSet m_index;
	SIZE m_sizeResolution;
	int m_nBitrate;
	int m_nFps;
	std::string m_strCameraId;
	std::string m_strMicrophoneId;
};

using SettingsPtr = std::shared_ptr<CZegoSettingsModel>;