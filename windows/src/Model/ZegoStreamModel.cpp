#include "stdafx.h"
#include "ZegoStreamModel.h"

CZegoStreamModel::CZegoStreamModel(const std::string& title, const std::string& id, const std::string& userName, bool curUser) :
	m_strTitle(title), m_strId(id), m_strUserName(userName), m_bCurUserCreated(curUser)
{
	m_nPlayViewId = -1;
	m_bPrimary = m_strId.find("@primary") != std::string::npos;
}

CZegoStreamModel::CZegoStreamModel(const CZegoStreamModel& otherStream)
{
	m_strTitle = otherStream.m_strTitle;
	m_strId = otherStream.m_strId;
	m_strUserName = otherStream.m_strUserName;
	m_nPlayViewId = otherStream.m_nPlayViewId;
	m_bCurUserCreated = otherStream.m_bCurUserCreated;
	m_bPrimary = otherStream.m_bPrimary;
}

std::string CZegoStreamModel::GetTitle(void)
{
	return m_strTitle;
}

std::string CZegoStreamModel::GetId(void)
{
	return m_strId;
}

std::string CZegoStreamModel::GetUserName(void)
{
	return m_strUserName;
}

void CZegoStreamModel::SetPlayView(int viewId)
{
	m_nPlayViewId = viewId;
}

int CZegoStreamModel::GetPlayView(void)
{
	return m_nPlayViewId;
}

bool CZegoStreamModel::IsPlaying(void)
{
	return m_nPlayViewId >= 0;
}

bool CZegoStreamModel::IsCurUserCreated(void)
{
	return m_bCurUserCreated;
}

bool CZegoStreamModel::IsPrimary(void)
{
	return m_bPrimary;
}