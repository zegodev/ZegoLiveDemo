// ZegoAVView.cpp : 实现文件
//

#include "stdafx.h"
#include "ZegoAVView.h"


// CZegoAVView

IMPLEMENT_DYNAMIC(CZegoAVView, CWnd)

CZegoAVView::CZegoAVView()
{
	RegisterWndClass();
	m_nAVQuality = -1;
}

CZegoAVView::~CZegoAVView()
{
	m_internalAVView.Detach();
}

void CZegoAVView::ShowQuailtyTips(int quality)
{
	if (!IsWindow(m_internalAVView.GetSafeHwnd()))
	{
		return;
	}

	if (m_nAVQuality == quality) 
	{ 
		return; 
	}

	m_nAVQuality = quality;

	if (m_nAVQuality < 0)
	{
		m_labelQualityTips.ShowWindow(SW_HIDE);
		return;
	}

	switch (m_nAVQuality)
	{
	case 0:
		m_labelQualityTips.SetWindowText(L"优");
		m_labelQualityTips.SetBkColor(RGB(0,255,0));
		break;
	case 1:
		m_labelQualityTips.SetWindowText(L"良");
		m_labelQualityTips.SetBkColor(RGB(255,255,0));
		break;
	case 2:
		m_labelQualityTips.SetWindowText(L"中");
		m_labelQualityTips.SetBkColor(RGB(255, 0, 0));
		break;
	case 3:
		m_labelQualityTips.SetWindowText(L"差");
		m_labelQualityTips.SetBkColor(RGB(211, 211, 211));
		break;
	default:
		break;
	}

	m_labelQualityTips.ShowWindow(SW_SHOW);
	m_labelQualityTips.Invalidate();
}

HWND CZegoAVView::GetDisplaySafeWnd(void)
{
	return m_internalAVView.GetSafeHwnd();
}

BEGIN_MESSAGE_MAP(CZegoAVView, CWnd)
	ON_WM_SIZE()
END_MESSAGE_MAP()

void CZegoAVView::PreSubclassWindow(void)
{
	m_labelQualityTips.CreateEx(0 , L"STATIC", L"",  WS_CHILD | WS_VISIBLE, {5,5,20,20}, this, 0);
	m_labelQualityTips.ShowWindow(SW_HIDE);

	CRect rect;
	GetClientRect(&rect);
	m_internalAVView.Create(L"", WS_CHILD | WS_VISIBLE | WS_BORDER | WS_CLIPSIBLINGS, rect, this);
	m_internalAVView.SetBkColor(RGB(193,205,205));
}

BOOL CZegoAVView::RegisterWndClass(void)
{
	WNDCLASS windowclass;
	HINSTANCE hInst = AfxGetInstanceHandle();

	if (!(::GetClassInfo(hInst, L"ZegoAVView", &windowclass)))
	{
		windowclass.style = CS_DBLCLKS;
		windowclass.lpfnWndProc = ::DefWindowProc;
		windowclass.cbClsExtra = windowclass.cbWndExtra = 0;
		windowclass.hInstance = hInst;
		windowclass.hIcon = NULL;
		windowclass.hCursor = AfxGetApp()->LoadStandardCursor(IDC_ARROW);
		windowclass.hbrBackground = (HBRUSH)GetStockObject(NULL_BRUSH);
		windowclass.lpszMenuName = NULL;
		windowclass.lpszClassName = L"ZegoAVView";

		if (!AfxRegisterClass(&windowclass))
		{
			AfxThrowResourceException();
			return FALSE;
		}
	}

	return TRUE;
}

void CZegoAVView::OnSize(UINT nType, int cx, int cy)
{
	__super::OnSize(nType, cx, cy);

	CRect rect;
	GetClientRect(&rect);
	m_internalAVView.SetWindowPos(&m_labelQualityTips, 0, 0, rect.Width(), rect.Height(), SWP_NOMOVE);
}