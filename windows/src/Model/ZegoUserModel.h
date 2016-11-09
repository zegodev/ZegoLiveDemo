#pragma once

#include <string>
#include <memory>

class CZegoUserModel
{
public:
	CZegoUserModel(const std::wstring& userId, const std::wstring& userName, bool isCurUser=false);
	~CZegoUserModel();

public:
	bool IsCurrentUser(void);
	std::wstring GetUserId(void);
	std::wstring GetUserName(void);

	bool IsPrimary(void);
	
private:
	bool m_bCurrentUser;
	std::wstring m_strUserId;
	std::wstring m_strUserName;

	bool m_bPrimary;
};

using UserPtr = std::shared_ptr<CZegoUserModel>;