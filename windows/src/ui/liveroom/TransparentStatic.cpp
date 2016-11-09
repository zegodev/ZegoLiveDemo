// TransparentStatic.cpp : implementation file
//

#include "stdafx.h"
#include "TransparentStatic.h"

// CTransparentStatic

IMPLEMENT_DYNAMIC(CTransparentStatic, CStatic)
CTransparentStatic::CTransparentStatic()
{
	m_clrText = RGB(0, 0, 0);
	m_clrBk = RGB(0, 0, 0);
	m_clrTransparent = RGB(255, 255, 255);
}

CTransparentStatic::~CTransparentStatic()
{
}

void CTransparentStatic::SetTextColor(COLORREF color)
{
	m_clrText = color;
}

void CTransparentStatic::SetBkColor(COLORREF color)
{
	m_clrBk = color;
}

BEGIN_MESSAGE_MAP(CTransparentStatic, CStatic)
	ON_WM_CREATE()
	ON_MESSAGE(WM_SETTEXT, OnSetText)
	ON_WM_CTLCOLOR_REFLECT()
END_MESSAGE_MAP()

// CTransparentStatic message handlers
int CTransparentStatic::OnCreate(LPCREATESTRUCT lpCreateStruct)
{
	m_brush.CreateSolidBrush(m_clrTransparent);

	DWORD dwExStyle = ::GetWindowLong(m_hWnd, GWL_EXSTYLE);
	::SetWindowLong(m_hWnd, GWL_EXSTYLE, dwExStyle | 0x80000);

	HMODULE hInst = LoadLibrary(_T("User32.DLL"));
	typedef BOOL(WINAPI *MYFUNC)(HWND, COLORREF, BYTE, DWORD);
	MYFUNC SetLayeredWindowAttributes = NULL;
	SetLayeredWindowAttributes = (MYFUNC)::GetProcAddress(hInst, "SetLayeredWindowAttributes");
	//SetLayeredWindowAttributes(GetSafeHwnd(), m_clrTransparent, 0, 1);
	::FreeLibrary(hInst);

	return 0;
}

LRESULT CTransparentStatic::OnSetText(WPARAM wParam, LPARAM lParam)
{
	LRESULT Result = Default();
	/*CRect Rect;
	GetWindowRect(&Rect);
	GetParent()->ScreenToClient(&Rect);
	GetParent()->InvalidateRect(&Rect);
	GetParent()->UpdateWindow();*/
	return Result;
}

HBRUSH CTransparentStatic::CtlColor(CDC* pDC, UINT nCtlColor)
{
	//pDC->SetTextColor(m_clrText);
	pDC->SetTextColor(m_clrBk);
	pDC->SetBkColor(m_clrBk);
	return CreateSolidBrush(m_clrBk);

	//pDC->SetBkMode(TRANSPARENT);
	//return (HBRUSH)GetStockObject(HOLLOW_BRUSH);

	//pDC->SetBkColor(m_clrTransparent);
	//return m_brush;
}