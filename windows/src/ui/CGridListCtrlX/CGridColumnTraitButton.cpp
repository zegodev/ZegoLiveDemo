#include "stdafx.h"
#pragma warning(disable:4100)	// unreferenced formal parameter

#include "CGridColumnTraitButton.h"

#include "CGridColumnTraitVisitor.h"
#include "CGridListCtrlEx.h"

CGridColumnTraitButton::CGridColumnTraitButton() : m_pButton(NULL)
{
	m_ButtonStyle = WS_CHILD | WS_VISIBLE | BS_PUSHBUTTON | BS_TEXT;
}

CGridColumnTraitButton::~CGridColumnTraitButton()
{
	delete m_pButton;
	m_pButton = nullptr;
}

void CGridColumnTraitButton::Accept(CGridColumnTraitVisitor& visitor)
{
	visitor.Visit(*this);
}

void CGridColumnTraitButton::SetStyle(DWORD dwStyle)
{
	m_ButtonStyle = dwStyle;
}

DWORD CGridColumnTraitButton::GetStyle() const
{
	return m_ButtonStyle;
}

CButton* CGridColumnTraitButton::CreateButton(CGridListCtrlEx& owner, int nRow, int nCol)
{
	CRect rect = GetCellEditRect(owner, nRow, nCol);
	CRect btnRect = rect;
	//btnRect.left = rect.right - 100;
	//btnRect.right = rect.right;

	if (m_pButton == NULL)
	{
		m_pButton = new CButton;
		m_pButton->Create(L"½øÈë", WS_CHILD | m_ButtonStyle, btnRect, &owner, 0);
		m_pButton->SetFont(owner.GetCellFont());
		m_pButton->SetDlgCtrlID(100000);
	}
	else
	{
		m_pButton->SetWindowPos(NULL, btnRect.left, btnRect.top, btnRect.Width(), btnRect.Height(), 0);
	}

	return m_pButton;
}

void CGridColumnTraitButton::OnHotTrack(CGridListCtrlEx& owner, int nRow, int nCol, CPoint pt, bool bLeave)
{
	CButton* pButton = CreateButton(owner, nRow, nCol);
	if (!IsWindow(pButton->GetSafeHwnd()))
	{
		return;
	}

	if (bLeave)
	{
		pButton->ShowWindow(SW_HIDE);
		TCHAR buf[MAX_PATH] = { 0 };
		_stprintf(buf, _T("--- hide row(%d)'s button\n"), nRow);
		OutputDebugString(buf);
	}
	else
	{
		CRect rect = GetCellEditRect(owner, nRow, nCol);
		pButton->ShowWindow(SW_SHOW);

		TCHAR buf[MAX_PATH] = { 0 };
		_stprintf(buf, _T("+++ show row(%d)'s button\n"), nRow);
		OutputDebugString(buf);
	}
}