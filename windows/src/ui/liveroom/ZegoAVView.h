#pragma once

#include "TransparentStatic.h"

// CZegoAVView

class CZegoAVView : public CWnd
{
	DECLARE_DYNAMIC(CZegoAVView)

public:
	CZegoAVView();
	virtual ~CZegoAVView();

	HWND GetDisplaySafeWnd(void);
	void ShowQuailtyTips(int quality);

protected:
	DECLARE_MESSAGE_MAP()

	void OnSize(UINT nType, int cx, int cy);
	void PreSubclassWindow(void);
	BOOL RegisterWndClass(void);

private:
	CTransparentStatic m_internalAVView;
	CTransparentStatic m_labelQualityTips;
	int m_nAVQuality;
};


