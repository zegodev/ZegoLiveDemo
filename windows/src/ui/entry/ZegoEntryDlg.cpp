// StartingDlg.cpp : 实现文件
//

#include "stdafx.h"
#include "ZegoEntryDlg.h"
#include "ZegoRoomDlg.h"
#include "ZegoSettingsDlg.h"
#include "CGridListCtrlX/CGridColumnTraitButton.h"
#include "ZegoLiveDemo.h"

#include "ZegoUtility.h"

#include "ZegoUserModel.h"
#include "ZegoChatRoom.h"
using namespace ZEGO;

#define AUTO_GETROOMLIST_TIMMERID	10086
#define ROOMLIST_MAX_NUM			30

// CZegoEntryDlg 对话框

IMPLEMENT_DYNAMIC(CZegoEntryDlg, CDialogEx)

CZegoEntryDlg::CZegoEntryDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_DIALOG_ENTRY, pParent)
	, m_bCKAutoRefresh(FALSE)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	
	GetChatRoomSignal().OnRoomList().connect(this, &CZegoEntryDlg::OnRoomList);
	GetChatRoomSignal().OnLoginResult().connect(this, &CZegoEntryDlg::OnLoginRoom);

	m_pRoomDlg = nullptr;
}

CZegoEntryDlg::~CZegoEntryDlg()
{
	m_roomList.clear();

	theApp.GetBase().UninitChatRoomSdk();

	delete m_pRoomDlg;
	m_pRoomDlg = nullptr;
}

void CZegoEntryDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);

	DDX_Text(pDX, IDC_EDIT_USERID, m_strEdUserId);
	DDX_Text(pDX, IDC_EDIT_USERNAME, m_strEdUserName);
	DDX_Control(pDX, IDC_COMBO_ROLE, m_cbRole);
	DDX_Control(pDX, IDC_COMBO_QUALITY, m_cbQuality);
	DDX_Control(pDX, IDC_LIST_ROOM, m_lsRooms);
	DDX_Control(pDX, IDC_EDIT_ROOMTITLE, m_edNewRoomTitle);
	DDX_Check(pDX, IDC_CHECK_AUTOREFRESH, m_bCKAutoRefresh);
}

BOOL CZegoEntryDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	SetIcon(m_hIcon, TRUE);	// 设置大图标
	SetIcon(m_hIcon, FALSE);	// 设置小图标

	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	EnableToolTips();

	m_userConfig.LoadConfig();

	m_strEdUserId = m_userConfig.GetUserId().c_str();
	m_strEdUserName = m_userConfig.GetUserName().c_str();

	m_cbRole.AddString(_T("主播"));
	m_cbRole.AddString(_T("观众"));
	m_cbRole.SetCurSel(m_userConfig.IsPrimary() ? 0 : 1);

	VideoQuality quality = m_userConfig.GetVideoQuality();
	m_cbQuality.AddString(_T("超低质量"));
	m_cbQuality.AddString(_T("低质量"));
	m_cbQuality.AddString(_T("标准质量"));
	m_cbQuality.AddString(_T("高质量"));
	m_cbQuality.AddString(_T("超高质量"));
	m_cbQuality.AddString(_T("自定义"));
	m_cbQuality.SetCurSel(quality);

	m_lsRooms.SetCellMargin(1.2);
	m_lsRooms.EnableVisualStyles(true);
	m_lsRooms.InsertHiddenLabelColumn();
	m_lsRooms.SetEmptyMarkupText(_T("当前没有房间正在直播..."));
	m_lsRooms.OnTraitSignal().connect(this, &CZegoEntryDlg::OnBnRoomListEntry);

	m_lsRooms.InsertColumnTrait(1, L"No.", LVCFMT_CENTER, 50, 1, NULL);
	CRect rtList;
	m_lsRooms.GetWindowRect(&rtList);
	m_lsRooms.InsertColumnTrait(2, L"标题", LVCFMT_LEFT, rtList.Width() - 290 - 4, 2, NULL);
	m_lsRooms.InsertColumnTrait(3, L"创建时间", LVCFMT_LEFT, 120, 3, NULL);

	CGridColumnTraitButton* pTraitButton = new CGridColumnTraitButton;
	m_lsRooms.InsertColumnTrait(4, L"详情", LVCFMT_LEFT, 120, 4, pTraitButton);

	m_edNewRoomTitle.SetCueBanner(_T("给直播起个标题吧[可选]"));

	UpdateData(FALSE);

	theApp.GetBase().InitChatRoomSdk();

	CHATROOM::DropRoomInfoCache();
	CHATROOM::GetRoomInfoList(0, ROOMLIST_MAX_NUM);

	return TRUE;
}

BEGIN_MESSAGE_MAP(CZegoEntryDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_CLOSE()
	ON_WM_NCLBUTTONDOWN()
	ON_BN_CLICKED(IDOK, &CZegoEntryDlg::OnBnClickedOk)
	ON_BN_CLICKED(IDC_BUTTON_SETTING, &CZegoEntryDlg::OnBnClickedButtonSetting)
	ON_BN_CLICKED(IDC_BUTTON_REFRESH, &CZegoEntryDlg::OnBnClickedButtonRefresh)
	ON_CBN_SELCHANGE(IDC_COMBO_ROLE, &CZegoEntryDlg::OnCbnSelchangeComboRole)
	ON_CBN_SELCHANGE(IDC_COMBO_QUALITY, &CZegoEntryDlg::OnCbnSelchangeComboQuality)
	ON_BN_CLICKED(IDC_CHECK_AUTOREFRESH, &CZegoEntryDlg::OnBnClickedCheckAutorefresh)
	ON_NOTIFY_EX(TTN_NEEDTEXT, 0, SetTipText)
	ON_NOTIFY(NM_DBLCLK, IDC_LIST_ROOM, &CZegoEntryDlg::OnNMDblclkListRoom)
END_MESSAGE_MAP()

// CStartingDlg 消息处理程序

void CZegoEntryDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

void CZegoEntryDlg::OnClose()
{
	UpdateData();

	m_userConfig.SetUserId(m_strEdUserId.GetBuffer());
	m_userConfig.SetUserName(m_strEdUserName.GetBuffer());
	m_userConfig.SaveConfig();

	KillTimer(AUTO_GETROOMLIST_TIMMERID);

	m_lsRooms.OnTraitSignal().disconnect(this);

	CDialogEx::OnClose();
}

void CZegoEntryDlg::OnNcLButtonDown(UINT nHitTest, CPoint point)
{
	if (nHitTest == HTHELP)
	{
		ShellExecute(GetSafeHwnd(), _T("open"), _T("www.zego.im"), NULL, NULL, SW_SHOWNORMAL);
		return;
	}

	CDialogEx::OnNcLButtonDown(nHitTest, point);
}

BOOL CZegoEntryDlg::SetTipText(UINT id, NMHDR *pTTTStruct, LRESULT *pResult)
{
	TOOLTIPTEXT *pTTT = (TOOLTIPTEXT *)pTTTStruct;
	UINT nID = pTTTStruct->idFrom;   

	if (pTTT->uFlags & TTF_IDISHWND)
	{
		nID = ::GetDlgCtrlID((HWND)nID);
		if (nID == 0)
		{
			return FALSE;
		}

		// strcpy(pTTT->lpszText, your_string1);

		return TRUE;
	}
	return FALSE;
}

void CZegoEntryDlg::OnBnClickedButtonRefresh()
{
	CHATROOM::DropRoomInfoCache();
	CHATROOM::GetRoomInfoList(0, ROOMLIST_MAX_NUM);
}

void CZegoEntryDlg::OnBnClickedCheckAutorefresh()
{
	UpdateData();

	if (m_bCKAutoRefresh)
	{
		SetTimer(AUTO_GETROOMLIST_TIMMERID, 5000, &CZegoEntryDlg::AutoGetRoomInfoList);
	}
	else
	{
		KillTimer(AUTO_GETROOMLIST_TIMMERID);
	}
}

void CZegoEntryDlg::AutoGetRoomInfoList(HWND hWnd, UINT nMsg, UINT_PTR nIDEvent, DWORD dwTime)
{
	if (nIDEvent == AUTO_GETROOMLIST_TIMMERID)
	{
		CHATROOM::DropRoomInfoCache();
		CHATROOM::GetRoomInfoList(0, ROOMLIST_MAX_NUM);
	}
}

void CZegoEntryDlg::OnBnClickedButtonSetting()
{
	UpdateData();

	CZegoSettingsDlg settingsDlg(m_userConfig.GetVideoSettings());
	if (IDOK == settingsDlg.DoModal())
	{
		m_userConfig.SaveConfig();
	}

	VideoQuality quality = m_userConfig.GetVideoQuality();
	m_cbQuality.SetCurSel(quality);
}

void CZegoEntryDlg::OnCbnSelchangeComboRole()
{
	m_userConfig.SetUserRole(m_cbRole.GetCurSel() == 0);
}

void CZegoEntryDlg::OnCbnSelchangeComboQuality()
{
	VideoQuality quality = (VideoQuality)m_cbQuality.GetCurSel();
	if (quality == VQ_SelfDef)
	{
		OnBnClickedButtonSetting();
	}
	else
	{
		m_userConfig.SetVideoQuality(quality);
	}
}

void CZegoEntryDlg::OnNMDblclkListRoom(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	
	if (pNMItemActivate->iItem >= 0)
	{
		OnBnRoomListEntry(pNMItemActivate->iItem);
	}

	*pResult = 0;
}

void CZegoEntryDlg::OnBnRoomListEntry(int nRow)
{
	if (m_pRoomDlg != nullptr) { return; }

	do
	{
		DWORD dwIndex = m_lsRooms.GetItemData(nRow);
		if (dwIndex >= m_roomList.size())
		{
			break;
		}

		unsigned int roomKey = m_roomList[dwIndex]->GetRoomKey();
		unsigned int serverKey = m_roomList[dwIndex]->GetServerKey();

		UpdateData();

		if (m_strEdUserId.IsEmpty() || m_strEdUserName.IsEmpty())
		{
			break;
		}

		m_userConfig.SetUserId(m_strEdUserId.GetString());
		std::string strUserId = WStringToUTF8(m_userConfig.GetUserIdWithRole().c_str()).c_str();
		m_userConfig.SetUserName(m_strEdUserName.GetString());
		std::string strUserName = WStringToUTF8(m_userConfig.GetUserName().c_str()).c_str();

		SettingsPtr pCurSettings = m_userConfig.GetVideoSettings();
		if (pCurSettings == nullptr)
		{
			break;
		}

		m_userConfig.SaveConfig();

		if (CHATROOM::GetInExistedRoom(roomKey, serverKey, strUserId.c_str(), strUserName.c_str()))
		{
			m_pRoomDlg = new CZegoRoomDlg(pCurSettings);
			EnableWindow(FALSE);
			return;
		}
	} while (false);

	MessageBox(L"进入房间失败", L"提示");
}

void CZegoEntryDlg::OnBnClickedOk()
{
	if (m_pRoomDlg != nullptr) { return; }

	do
	{
		UpdateData();

		if (m_strEdUserId.IsEmpty() || m_strEdUserName.IsEmpty())
		{
			break;
		}

		m_userConfig.SetUserId(m_strEdUserId.GetString());
		std::string strUserId = WStringToUTF8(m_userConfig.GetUserIdWithRole().c_str()).c_str();
		m_userConfig.SetUserName(m_strEdUserName.GetString());
		std::string strUserName = WStringToUTF8(m_userConfig.GetUserName().c_str()).c_str();

		SettingsPtr pCurSettings = m_userConfig.GetVideoSettings();
		if (pCurSettings == nullptr)
		{
			break;
		}

		if (CHATROOM::CreateRoomAndGetIn(strUserId.c_str(), strUserName.c_str()))
		{
			m_pRoomDlg = new CZegoRoomDlg(pCurSettings);
			EnableWindow(FALSE);
			return;
		}
	} while (false);

	MessageBox(L"创建房间失败", L"提示");
}

void CZegoEntryDlg::OnRoomList(int nErrorCode, std::vector<RoomPtr> roomList)
{
	UpdateData();

	if (nErrorCode != 0 && !m_bCKAutoRefresh)
	{
		MessageBox(L"房间信息刷新失败", L"提示");
		return;
	}

	m_roomList.clear();
	m_roomList = roomList;

	m_lsRooms.DeleteAllItems();

	for (size_t index = 0; index < m_roomList.size(); index++)
	{
		CString strIndex;
		strIndex.Format(_T("%u"), index + 1);
		m_lsRooms.InsertItem(index, strIndex);
		m_lsRooms.SetItemText(index, 1, strIndex);
		m_lsRooms.SetItemText(index, 2, m_roomList[index]->GetRoomTitle().c_str());
		
		CTime tmCreated(m_roomList[index]->GetCreatedTime());
		CString strCreatedTime;
		strCreatedTime.Format(_T("%d/%d %d:%d"), tmCreated.GetMonth(), tmCreated.GetDay(), tmCreated.GetHour(), tmCreated.GetMinute());
		m_lsRooms.SetItemText(index, 3, strCreatedTime);

		CString strDetail(_T("-"));
		unsigned int uLivesCount = m_roomList[index]->GetLivesCount();
		if (uLivesCount != 0)
		{
			strDetail.Format(_T("%u人正在直播"), m_roomList[index]->GetLivesCount());
		}
		m_lsRooms.SetItemText(index, 4, strDetail);

		m_lsRooms.SetItemData(index, index);
	}
}

void CZegoEntryDlg::OnLoginRoom(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool isPublic)
{
	EnableWindow(TRUE);

	if (nErrorCode != 0)
	{
		if (nErrorCode == 1)
		{
			MessageBox(L"网络异常,登录房间失败", L"提示");
		}
		else if (nErrorCode < 0)
		{
			MessageBox(L"房间可能已不存在,请尝试重新刷新", L"提示");
		}
		delete m_pRoomDlg;
		m_pRoomDlg = nullptr;
		return;
	}

	ShowWindow(SW_HIDE);

	auto iter = std::find_if(m_roomList.begin(), m_roomList.end(),
		[dwRoomKey, dwServerKey](const RoomPtr& elem) { 
		return elem->GetRoomKey() == dwRoomKey && elem->GetServerKey() == dwServerKey; 
	});

	RoomPtr pRoom(new CZegoRoomModel(dwRoomKey, dwServerKey));
	if (iter != m_roomList.end())
	{
		pRoom->SetRoomTitle((*iter)->GetRoomTitle());
	}
	else
	{
		CString strTitle;
		m_edNewRoomTitle.GetWindowText(strTitle);
		if (!strTitle.IsEmpty())
		{
			pRoom->SetRoomTitle(strTitle.GetString());
		}
	}

	UserPtr pCurUser(new CZegoUserModel(m_userConfig.GetUserIdWithRole(), m_userConfig.GetUserName(), true));
	pRoom->AddVisitor(pCurUser);

	m_pRoomDlg->SetRoom(pRoom);

	// 房间用户列表和Stream创建成功的回调通知都将在chatRoomDlg被关注和处理
	if (m_pRoomDlg->DoModal() != 100001)
	{
		theApp.m_pMainWnd->PostMessage(WM_CLOSE);
	}
	else
	{
		ShowWindow(SW_SHOW);
	}

	delete m_pRoomDlg;
	m_pRoomDlg = nullptr;
}