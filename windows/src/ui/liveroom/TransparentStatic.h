#pragma once

// CTransparentStatic

class CTransparentStatic : public CStatic
{
	DECLARE_DYNAMIC(CTransparentStatic)

public:
	CTransparentStatic();
	virtual ~CTransparentStatic();

	void SetTextColor(COLORREF color);
	void SetBkColor(COLORREF color);

protected:
	afx_msg LRESULT OnSetText(WPARAM, LPARAM);
	afx_msg HBRUSH CtlColor(CDC* /*pDC*/, UINT /*nCtlColor*/);
	int OnCreate(LPCREATESTRUCT lpCreateStruct);
	DECLARE_MESSAGE_MAP()

private:
	COLORREF m_clrText;
	COLORREF m_clrBk;
	COLORREF m_clrTransparent;
	CBrush m_brush;
};