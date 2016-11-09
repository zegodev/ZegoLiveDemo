#pragma once

#include <string>
#include <vector>
#include <memory>

class CZegoStreamModel
{
public:
	CZegoStreamModel(const std::string& title, const std::string& id, const std::string& userName, bool curUser = false);
	CZegoStreamModel(const CZegoStreamModel& otherStream);

public:
	std::string GetTitle(void);
	std::string GetId(void);
	std::string GetUserName(void);

	void SetPlayView(int viewId);
	int GetPlayView(void);
	bool IsPlaying(void);

	bool IsCurUserCreated(void);

	bool IsPrimary(void);

	std::vector<std::string> m_vecRtmpUrls;
	std::vector<std::string> m_vecFlvUrls;
	std::vector<std::string> m_vecHlsUrls;

private:
	std::string m_strTitle;
	std::string m_strId;
	std::string m_strUserName;

	int m_nPlayViewId;
	bool m_bCurUserCreated;
	bool m_bPrimary;
};

using StreamPtr = std::shared_ptr<CZegoStreamModel>;