#include "stdafx.h"
#include "ZegoSettingsModel.h"
#include "ZegoAVApi.h"

static IndexSet g_PrimaryDefConf[] = { { 9,15,3 },{ 8,12,3 },{ 7,10,3 },{ 6,8,3 },{ 4,5,3 } };
static IndexSet g_StudentDefConf[] = { { 11,16,3 },{ 10,14,3 },{ 9,15,3 },{ 8,12,3 },{ 7,10,3 } };

CZegoSettingsModel::CZegoSettingsModel()
{
	m_index = { 8, 12, 3 };
	InitDeviceId();
}

CZegoSettingsModel::CZegoSettingsModel(const IndexSet& index)
{
	m_index = index;
	InitByIndex();
	InitDeviceId();
}

void CZegoSettingsModel::SetResolution(SIZE resolution)
{
	m_sizeResolution = resolution;
	for (int i = 0; i < sizeof(g_Resolution) / sizeof(g_Resolution[0]); i++)
	{
		if (g_Resolution[i].cx == resolution.cx &&
			g_Resolution[i].cy == resolution.cy)
		{
			m_index.indexResolution = i;
			break;
		}
	}
}

SIZE CZegoSettingsModel::GetResolution(void)
{
	return m_sizeResolution;
}

void CZegoSettingsModel::SetBitrate(int bitrate)
{
	m_nBitrate = bitrate;
	for (int i = 0; i < sizeof(g_Bitrate) / sizeof(g_Bitrate[0]); i++)
	{
		if (g_Bitrate[i] == bitrate)
		{
			m_index.indexBitrate = i;
			break;
		}
	}
}

int  CZegoSettingsModel::GetBitrate(void)
{
	return m_nBitrate;
}

void CZegoSettingsModel::SetFps(int fps)
{
	m_nFps = fps;
	for (int i = 0; i < sizeof(g_Fps) / sizeof(g_Fps[0]); i++)
	{
		if (g_Fps[i] == fps)
		{
			m_index.indexFps = i;
			break;
		}
	}
}

int  CZegoSettingsModel::GetFps(void)
{
	return m_nFps;
}

void CZegoSettingsModel::SetCameraId(const std::string& cameraId)
{
	m_strCameraId = cameraId;
}

std::string CZegoSettingsModel::GetCameraId(void)
{
	return m_strCameraId;
}

void CZegoSettingsModel::SetMicrophoneId(const std::string& microphoneId)
{
	m_strMicrophoneId = microphoneId;
}

std::string CZegoSettingsModel::GetMircophoneId(void)
{
	return m_strMicrophoneId;
}

void CZegoSettingsModel::InitByIndex(void)
{
	m_sizeResolution = g_Resolution[m_index.indexResolution];
	m_nBitrate = g_Bitrate[m_index.indexBitrate];
	m_nFps = g_Fps[m_index.indexFps];
}

void CZegoSettingsModel::InitDeviceId(void)
{
	int nDeviceCount(0);
	ZEGO::AV::DeviceInfo* pDeviceList(NULL);

	pDeviceList = ZEGO::AV::GetAudioDeviceList(ZEGO::AV::AudioDevice_Input, nDeviceCount);
	if (nDeviceCount > 0 && pDeviceList != NULL)
		m_strMicrophoneId = pDeviceList[0].szDeviceId;
	ZEGO::AV::FreeDeviceList(pDeviceList);
	pDeviceList = NULL;

	pDeviceList = ZEGO::AV::GetVideoDeviceList(nDeviceCount);
	if (nDeviceCount > 0 && pDeviceList != NULL)
		m_strCameraId = pDeviceList[0].szDeviceId;
	ZEGO::AV::FreeDeviceList(pDeviceList);
	pDeviceList = NULL;
}

VideoQuality CZegoSettingsModel::GetQuality(bool primary)
{
	VideoQuality quality = VQ_SelfDef;

	if (primary)
	{
		for (int i = 0; i < sizeof(g_PrimaryDefConf) / sizeof(g_PrimaryDefConf[0]); i++)
		{
			if (g_PrimaryDefConf[i] == m_index)
			{
				quality = (VideoQuality)i;
				break;
			}
		}
	}
	else
	{
		for (int i = 0; i < sizeof(g_StudentDefConf) / sizeof(g_StudentDefConf[0]); i++)
		{
			if (g_StudentDefConf[i] == m_index)
			{
				quality = (VideoQuality)i;
				break;
			}
		}
	}

	return quality;
}

void CZegoSettingsModel::SetQuality(bool primary, VideoQuality quality)
{
	if (quality == VQ_SelfDef)
	{
		return;
	}

	if (primary)
	{
		m_index = g_PrimaryDefConf[quality];
	}
	else
	{
		m_index = g_StudentDefConf[quality];
	}

	InitByIndex();
}