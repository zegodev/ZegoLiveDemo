#include "stdafx.h"
#include <algorithm>
#include "ZegoRoomModel.h"
#include "ZegoUtility.h"

CZegoRoomModel::CZegoRoomModel(unsigned int uRoomKey, unsigned int uServerKey) : m_uRoomKey(uRoomKey), m_uServerKey(uServerKey)
{ 
	char szChannel[MAX_PATH] = { 0 };
	sprintf_s(szChannel, MAX_PATH, "0x%x-0x%x", uRoomKey, uServerKey);
	m_strChannel = szChannel;
}

CZegoRoomModel::~CZegoRoomModel(void) 
{
	m_pCurUser = nullptr;  
	m_otherVisitor.clear(); 
	m_pendingPrimaryStreams.clear();
	m_pendingStudentStreams.clear();
	m_playingPrimaryStreams.clear();
	m_playingStudentStreams.clear();
}


unsigned int CZegoRoomModel::GetRoomKey(void)
{
	return m_uRoomKey;
}

unsigned int CZegoRoomModel::GetServerKey(void)
{
	return m_uServerKey;
}

std::string CZegoRoomModel::GetChannel(void)
{
	return m_strChannel;
}

void CZegoRoomModel::SetRoomTitle(const std::wstring& title)
{
	m_strRoomTitle = title;
}

std::wstring CZegoRoomModel::GetRoomTitle(void)
{
	if ( m_strRoomTitle.empty() )
	{
		TCHAR szTmp[MAX_PATH] = { 0 };
		_stprintf_s(szTmp, MAX_PATH, _T("%u"), m_uRoomKey);
		std::wstring strCurUserName(szTmp);
		if (m_pCurUser && !m_pCurUser->GetUserName().empty())
		{
			strCurUserName = m_pCurUser->GetUserName();
		}
		return _T("Hello-")+strCurUserName;
	}
	return m_strRoomTitle;
}

UserPtr CZegoRoomModel::GetCurUser(void)
{
	return m_pCurUser;
}

bool CZegoRoomModel::AddVisitor(UserPtr user)
{
	if (user == nullptr) { return false; }

	std::wstring strUserId = user->GetUserId();
	auto iter = std::find_if(m_otherVisitor.begin(), m_otherVisitor.end(),
		[&strUserId](const UserPtr& elem) { return elem->GetUserId() == strUserId; });
	
	if (iter != m_otherVisitor.end())
	{
		return false;
	}

	if (m_pCurUser && m_pCurUser->GetUserId() == strUserId)
	{
		return false;
	}

	if (user->IsCurrentUser())
	{
		m_pCurUser = user;
	}
	else
	{
		m_otherVisitor.push_back(user);
	}

	return true;
}

bool CZegoRoomModel::RemoveVisitor(const std::wstring& userId)
{
	if (m_pCurUser != nullptr && m_pCurUser->GetUserId() == userId)
	{
		m_pCurUser = nullptr;
		return false;
	}
	auto iter = std::find_if(m_otherVisitor.begin(), m_otherVisitor.end(),
		[&userId](const UserPtr& elem) { return elem->GetUserId() == userId; });
	
	if (iter != m_otherVisitor.end())
	{
		m_otherVisitor.erase(iter);
		return true;
	}

	return false;
}

UINT CZegoRoomModel::GetVisitorCount(void)
{
	return m_otherVisitor.size();
}

UserPtr CZegoRoomModel::GetVisitor(UINT index)
{
	if (index < m_otherVisitor.size())
	{
		return m_otherVisitor[index];
	}

	return nullptr;
}

UserPtr CZegoRoomModel::GetVisitorById(const std::wstring& userId)
{
	if (m_pCurUser != nullptr && m_pCurUser->GetUserId() == userId)
	{
		return m_pCurUser;
	}

	auto iter = std::find_if(m_otherVisitor.begin(), m_otherVisitor.end(),
		[&userId](const UserPtr& elem) { return elem->GetUserId() == userId; });
	if (iter != m_otherVisitor.end())
	{
		return *iter;
	}

	return nullptr;
}

UserPtr CZegoRoomModel::GetVisitorByName(const std::wstring& userName)
{
	if (m_pCurUser != nullptr && m_pCurUser->GetUserName() == userName)
	{
		return m_pCurUser;
	}

	auto iter = std::find_if(m_otherVisitor.begin(), m_otherVisitor.end(),
		[&userName](const UserPtr& elem) { return elem->GetUserName() == userName; });
	if (iter != m_otherVisitor.end())
	{
		return *iter;
	}

	return nullptr;
}

void CZegoRoomModel::AddStream(StreamPtr stream)
{
	if (stream == nullptr) { return; }

	std::string strStreamId = stream->GetId();

	if (stream->IsPrimary())
	{
		if (stream->IsPlaying())
		{
			auto iter = std::find_if(m_playingPrimaryStreams.begin(), m_playingPrimaryStreams.end(),
				[&strStreamId](const StreamPtr& elem) { return elem->GetId() == strStreamId; });
			if (iter == m_playingPrimaryStreams.end())
				m_playingPrimaryStreams.push_back(stream);
		}
		else
		{
			auto iter = std::find_if(m_pendingPrimaryStreams.begin(), m_pendingPrimaryStreams.end(),
				[&strStreamId](const StreamPtr& elem) { return elem->GetId() == strStreamId; });
			if (iter == m_pendingPrimaryStreams.end())
				m_pendingPrimaryStreams.push_back(stream);
		}
	}
	else
	{
		if (stream->IsPlaying())
		{
			auto iter = std::find_if(m_playingStudentStreams.begin(), m_playingStudentStreams.end(),
				[&strStreamId](const StreamPtr& elem) { return elem->GetId() == strStreamId; });
			if (iter == m_playingStudentStreams.end())
				m_playingStudentStreams.push_back(stream);
		}
		else
		{
			auto iter = std::find_if(m_pendingStudentStreams.begin(), m_pendingStudentStreams.end(),
				[&strStreamId](const StreamPtr& elem) { return elem->GetId() == strStreamId; });
			if (iter == m_pendingStudentStreams.end())
				m_pendingStudentStreams.push_back(stream);
		}
	}
}

StreamPtr CZegoRoomModel::RemoveStream(const std::string& streamId)
{
	StreamPtr pStream(nullptr);

	do
	{
		auto iter = std::find_if(m_playingPrimaryStreams.begin(), m_playingPrimaryStreams.end(),
			[streamId](const StreamPtr& elem) { return elem->GetId() == streamId; });

		if (iter != m_playingPrimaryStreams.end())
		{
			pStream = *iter;
			m_playingPrimaryStreams.erase(iter);
			break;
		}

		iter = std::find_if(m_playingStudentStreams.begin(), m_playingStudentStreams.end(),
			[streamId](const StreamPtr& elem) { return elem->GetId() == streamId; });

		if (iter != m_playingStudentStreams.end())
		{
			pStream = *iter;
			m_playingStudentStreams.erase(iter);
			break;
		}

		auto qIter = std::find_if(m_pendingPrimaryStreams.begin(), m_pendingPrimaryStreams.end(),
			[streamId](const StreamPtr& elem) { return elem->GetId() == streamId;  });

		if (qIter != m_pendingPrimaryStreams.end())
		{
			pStream = *qIter;
			m_pendingPrimaryStreams.erase(qIter);
			break;
		}

		qIter = std::find_if(m_pendingStudentStreams.begin(), m_pendingStudentStreams.end(),
			[streamId](const StreamPtr& elem) { return elem->GetId() == streamId;  });

		if (qIter != m_pendingStudentStreams.end())
		{
			pStream = *qIter;
			m_pendingStudentStreams.erase(qIter);
			break;
		}
	} while (false);

	return pStream;
}

StreamPtr CZegoRoomModel::PopPendingStream(bool primary)
{
	StreamPtr pStream(nullptr);

	if (primary)
	{
		if (m_pendingPrimaryStreams.size() > 0)
		{
			pStream = m_pendingPrimaryStreams.front();
			m_pendingPrimaryStreams.pop_front();
		}
	}
	else
	{
		if (m_pendingStudentStreams.size() > 0)
		{
			pStream = m_pendingStudentStreams.front();
			m_pendingStudentStreams.pop_front();
		}
	}

	return pStream;
}

UINT CZegoRoomModel::GetPlayingStreamCount(bool primary)
{
	return primary ? m_playingPrimaryStreams.size() : m_playingStudentStreams.size();
}

StreamPtr CZegoRoomModel::GetPlayingStreamById(bool primary, const std::string streamId)
{
	auto iter = std::find_if(m_playingPrimaryStreams.begin(), m_playingPrimaryStreams.end(),
		[streamId](const StreamPtr& elem) { return elem->GetId() == streamId; });

	if (iter != m_playingPrimaryStreams.end())
	{
		return *iter;
	}

	iter = std::find_if(m_playingStudentStreams.begin(), m_playingStudentStreams.end(),
		[streamId](const StreamPtr& elem) { return elem->GetId() == streamId; });

	if (iter != m_playingStudentStreams.end())
	{
		return *iter;
	}

	return nullptr;
}

void CZegoRoomModel::SetCreatedTime(unsigned int time)
{
	m_uCreatedTime = time;
}

unsigned int CZegoRoomModel::GetCreatedTime(void)
{
	return m_uCreatedTime;
}

void CZegoRoomModel::SetLivesCount(unsigned int count)
{
	m_uLivesCount = count;
}

unsigned int CZegoRoomModel::GetLivesCount(void)
{
	return m_uLivesCount;
}