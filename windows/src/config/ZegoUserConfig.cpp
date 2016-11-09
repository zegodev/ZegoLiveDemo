#include "stdafx.h"
#include "ZegoUserConfig.h"
#include <memory>
#include <random>

MAKE_CONST_VAL(sUserRecords)
MAKE_CONST_VAL(kUserId)
MAKE_CONST_VAL(kUserName)
MAKE_CONST_VAL(kIsPrimary)
MAKE_CONST_VAL(kResolutionX)
MAKE_CONST_VAL(kResolutionY)
MAKE_CONST_VAL(kBitrate)
MAKE_CONST_VAL(kFps);

CZegoUserConfig::CZegoUserConfig()
{
	TCHAR szAppName[MAX_PATH] = { 0 };
	::GetModuleFileNameW(NULL, szAppName, MAX_PATH);
	CString strAppFullName = szAppName;
	CString strPath = strAppFullName.Left(strAppFullName.ReverseFind('\\') + 1);
	m_strIniPath = strPath + L"zegosix.ini";

	m_pVideoSettings = nullptr;
}

CZegoUserConfig::~CZegoUserConfig()
{
}

void CZegoUserConfig::LoadConfig(void)
{
	if (LoadConfigInternal())
	{
		return;
	}

	std::random_device rd;
	std::uniform_int_distribution<int> dist(10000000, 99999999);

	TCHAR buf[100] = { 0 };
	_stprintf_s(buf, _T("%u"), dist(rd));
	m_strUserId = buf;
	m_strUserName = _T("windows-") + m_strUserId;

	m_bPrimary = true;

	if (m_pVideoSettings == nullptr)
	{
		m_pVideoSettings = std::make_shared<CZegoSettingsModel>();
	}
	m_pVideoSettings->SetQuality(m_bPrimary, VQ_Middle);

	SaveConfig();
}

bool CZegoUserConfig::LoadConfigInternal(void)
{
	if (m_iniEngine.LoadFile(m_strIniPath.c_str()) != SI_OK)
	{
		return false;
	}

	std::wstring strUserId = m_iniEngine.GetValue(sUserRecords, kUserId, _T(""));
	std::wstring strUserName = m_iniEngine.GetValue(sUserRecords, kUserName, _T(""));
	int nRole = m_iniEngine.GetLongValue(sUserRecords, kIsPrimary, 0);
	if (strUserId.empty() || strUserName.empty() || nRole == 0)
	{
		return false;
	}

	SIZE sizeResolution;
	sizeResolution.cx = m_iniEngine.GetLongValue(sUserRecords, kResolutionX, 0);
	sizeResolution.cy = m_iniEngine.GetLongValue(sUserRecords, kResolutionY, 0);
	int nBitrate = m_iniEngine.GetLongValue(sUserRecords, kBitrate, 0);
	int nFps = m_iniEngine.GetLongValue(sUserRecords, kFps, 0);

	if (sizeResolution.cx == 0 || sizeResolution.cy == 0 || nBitrate == 0 || nFps == 0)
	{
		return false;
	}

	m_strUserId = strUserId;
	m_strUserName = strUserName;
	m_bPrimary = nRole == 1;

	if (m_pVideoSettings == nullptr)
	{
		m_pVideoSettings = std::make_shared<CZegoSettingsModel>();
	}
	m_pVideoSettings->SetResolution(sizeResolution);
	m_pVideoSettings->SetBitrate(nBitrate);
	m_pVideoSettings->SetFps(nFps);
	return true;
}

void CZegoUserConfig::SaveConfig(void)
{
	if (m_strUserId.empty() || m_strUserName.empty() || m_pVideoSettings == nullptr)
	{
		return;
	}

	m_iniEngine.SetValue(sUserRecords, kUserId, m_strUserId.c_str());
	m_iniEngine.SetValue(sUserRecords, kUserName, m_strUserName.c_str());
	m_iniEngine.SetLongValue(sUserRecords, kIsPrimary, m_bPrimary ? 1 : 2);

	m_iniEngine.SetLongValue(sUserRecords, kResolutionX, m_pVideoSettings->GetResolution().cx);
	m_iniEngine.SetLongValue(sUserRecords, kResolutionY, m_pVideoSettings->GetResolution().cy);
	m_iniEngine.SetLongValue(sUserRecords, kBitrate, m_pVideoSettings->GetBitrate());
	m_iniEngine.SetLongValue(sUserRecords, kFps, m_pVideoSettings->GetFps());

	m_iniEngine.SaveFile(m_strIniPath.c_str(), true);
}

std::wstring CZegoUserConfig::GetUserId(void)
{
	return m_strUserId;
}

std::wstring CZegoUserConfig::GetUserIdWithRole(void)
{
	std::wstring strUserIdWithRole = m_strUserId;
	if (m_bPrimary)
	{
		strUserIdWithRole += L"@primary";
	}
	return strUserIdWithRole;
}

void CZegoUserConfig::SetUserId(const std::wstring strUserId)
{
	if (!strUserId.empty())
	{
		m_strUserId = strUserId;
	}
}

std::wstring CZegoUserConfig::GetUserName(void)
{
	return m_strUserName;
}

void CZegoUserConfig::SetUserName(const std::wstring strUserName)
{
	if (!strUserName.empty())
	{
		m_strUserName = strUserName;
	}
}

bool CZegoUserConfig::IsPrimary(void)
{
	return m_bPrimary;
}

void CZegoUserConfig::SetUserRole(bool primary)
{
	if (m_bPrimary == primary)
	{
		return;
	}

	if (m_pVideoSettings != nullptr)
	{
		VideoQuality quality = m_pVideoSettings->GetQuality(m_bPrimary);
		if (quality != VQ_SelfDef)
		{
			m_pVideoSettings->SetQuality(primary, quality);
		}
	}

	m_bPrimary = primary;
}

VideoQuality CZegoUserConfig::GetVideoQuality(void)
{
	if (m_pVideoSettings != nullptr)
	{
		return m_pVideoSettings->GetQuality(m_bPrimary);
	}
	return VQ_SelfDef;
}

void CZegoUserConfig::SetVideoQuality(VideoQuality quality)
{
	if (m_pVideoSettings != nullptr)
	{
		m_pVideoSettings->SetQuality(m_bPrimary, quality);
	}
}

SettingsPtr CZegoUserConfig::GetVideoSettings(void)
{
	return m_pVideoSettings;
}