#pragma once

#include <vector>
#include <deque>
#include <memory>
#include "ZegoUserModel.h"
#include "ZegoStreamModel.h"

class CZegoRoomModel
{
public:
	CZegoRoomModel() { m_pCurUser = nullptr; m_uRoomKey = 0; m_uServerKey = 0; }
	CZegoRoomModel(unsigned int uRoomKey, unsigned int uServerKey);
	~CZegoRoomModel();

public:
	unsigned int GetRoomKey(void);
	unsigned int GetServerKey(void);
	std::string GetChannel(void);
	void SetRoomTitle(const std::wstring& title);
	std::wstring GetRoomTitle(void);

	UserPtr GetCurUser(void);
	bool AddVisitor(UserPtr user);
	bool RemoveVisitor(const std::wstring& userId);
	UINT GetVisitorCount(void);
	UserPtr GetVisitor(UINT index);
	UserPtr GetVisitorById(const std::wstring& userId);
	UserPtr GetVisitorByName(const std::wstring& userName);
	
	void AddStream(StreamPtr stream);
	StreamPtr RemoveStream(const std::string& streamId);
	StreamPtr PopPendingStream(bool primary);
	StreamPtr GetPlayingStreamById(bool primary, const std::string streamId);
	UINT GetPlayingStreamCount(bool primary);

	void SetCreatedTime(unsigned int time);
	unsigned int GetCreatedTime(void);
	void SetLivesCount(unsigned int count);
	unsigned int GetLivesCount(void);

private:
	unsigned int m_uRoomKey;
	unsigned int m_uServerKey;
	std::wstring m_strRoomTitle; // 实际上就是第一条stream的title
	
	UserPtr m_pCurUser;
	std::vector<UserPtr> m_otherVisitor;

	std::vector<StreamPtr> m_playingPrimaryStreams;
	std::vector<StreamPtr> m_playingStudentStreams;
	std::deque<StreamPtr> m_pendingPrimaryStreams;
	std::deque<StreamPtr> m_pendingStudentStreams;

	std::string m_strChannel;	// 一个房间只用一个channel,默认就roomkey+serverkey的组合字符串

	unsigned int m_uCreatedTime;
	unsigned int m_uLivesCount;
};

using RoomPtr = std::shared_ptr<CZegoRoomModel>;