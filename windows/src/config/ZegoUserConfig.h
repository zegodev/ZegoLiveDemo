#pragma once

#include "SimpleIni.h"
#include "ZegoSettingsModel.h"

#define MAKE_CONST_VAL(name) static const TCHAR name[] = _T(#name);

class CZegoUserConfig
{
public:
	CZegoUserConfig();
	~CZegoUserConfig();

	void LoadConfig(void);
	void SaveConfig(void);

	std::wstring GetUserId(void);
	std::wstring GetUserIdWithRole(void);
	void SetUserId(const std::wstring strUserId);

	std::wstring GetUserName(void);
	void SetUserName(const std::wstring strUserName);

	bool IsPrimary(void);
	void SetUserRole(bool primary);

	VideoQuality GetVideoQuality(void);
	void SetVideoQuality(VideoQuality quality);

	SettingsPtr GetVideoSettings(void);

private:
	bool LoadConfigInternal(void);

private:
	CSimpleIni m_iniEngine;
	std::wstring m_strIniPath;

	std::wstring m_strUserId;
	std::wstring m_strUserName;
	bool m_bPrimary;
	SettingsPtr m_pVideoSettings;
};

