#pragma once
#include "ZegoRoomDlg.h"
#include "ZegoSettingsModel.h"
#include "ZegoUserConfig.h"
#include "CGridListCtrlX/CGridListCtrlEx.h"
#include "afxwin.h"

// CZegoEntryDlg 对话框

class CZegoEntryDlg : public CDialogEx, public sigslot::has_slots<>
{
	DECLARE_DYNAMIC(CZegoEntryDlg)

public:
	CZegoEntryDlg(CWnd* pParent = NULL);   // 标准构造函数
	virtual ~CZegoEntryDlg();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_DIALOG_ENTRY };
#endif

protected:
	virtual BOOL OnInitDialog();
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持
	void OnSysCommand(UINT nID, LPARAM lParam);
	void OnClose();
	void OnNcLButtonDown(UINT nHitTest, CPoint point);
	BOOL SetTipText(UINT id, NMHDR *pTTTStruct, LRESULT *pResult);
	DECLARE_MESSAGE_MAP()

	afx_msg void OnCbnSelchangeComboRole();
	afx_msg void OnCbnSelchangeComboQuality();
	afx_msg void OnBnClickedButtonSetting();
	afx_msg void OnBnClickedButtonRefresh();
	afx_msg void OnBnClickedCheckAutorefresh();
	afx_msg void OnBnClickedOk();
	afx_msg void OnNMDblclkListRoom(NMHDR *pNMHDR, LRESULT *pResult);

	void OnBnRoomListEntry(int nRow);

	// Auto refresh timer proc
	static void CALLBACK AutoGetRoomInfoList(HWND hWnd, UINT nMsg, UINT_PTR nIDEvent, DWORD dwTime);

	void OnRoomList(int nErrorCode, std::vector<RoomPtr> roomList);
	void OnLoginRoom(int nErrorCode, unsigned int dwRoomKey, unsigned int dwServerKey, bool isPublic);

private:
	HICON m_hIcon;

	CZegoRoomDlg* m_pRoomDlg;

	CString m_strEdUserId;
	CString m_strEdUserName;
	CComboBox m_cbRole;
	CComboBox m_cbQuality;
	CGridListCtrlEx m_lsRooms;
	BOOL m_bCKAutoRefresh;
	CEdit m_edNewRoomTitle;

	CZegoUserConfig m_userConfig;
	std::vector<RoomPtr> m_roomList;
};
