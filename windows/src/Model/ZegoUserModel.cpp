#include "stdafx.h"
#include "ZegoUserModel.h"

CZegoUserModel::CZegoUserModel(const std::wstring& userId, const std::wstring& userName, bool isCurUser/*= false*/):
	m_strUserId(userId), 
	m_strUserName(userName), 
	m_bCurrentUser(isCurUser)
{
	m_bPrimary = m_strUserId.find(_T("@primary")) != std::string::npos;
}

CZegoUserModel::~CZegoUserModel() {}

bool CZegoUserModel::IsCurrentUser(void)
{
	return m_bCurrentUser;
}

std::wstring CZegoUserModel::GetUserId(void)
{
	return m_strUserId;
}

std::wstring CZegoUserModel::GetUserName(void)
{
	return m_strUserName;
}

bool CZegoUserModel::IsPrimary(void)
{
	return m_bPrimary;
}
