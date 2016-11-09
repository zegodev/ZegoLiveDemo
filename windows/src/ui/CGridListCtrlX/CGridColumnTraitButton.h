#pragma once

#include "CGridColumnTraitText.h"
#include <windows.h>

class CGridColumnTraitButton : public CGridColumnTraitText
{
public:
	CGridColumnTraitButton();
	virtual ~CGridColumnTraitButton();

	void SetStyle(DWORD dwStyle);
	DWORD GetStyle() const;

	virtual void OnHotTrack(CGridListCtrlEx& owner, int nRow, int nCol, CPoint pt, bool bLeave);

protected:
	virtual void Accept(CGridColumnTraitVisitor& visitor);
	virtual CButton* CreateButton(CGridListCtrlEx& owner, int nRow, int nCol);

	CButton* m_pButton;
	DWORD m_ButtonStyle;			

private:
	CGridColumnTraitButton(const CGridColumnTraitButton&);
	CGridColumnTraitButton& operator=(const CGridColumnTraitButton& other);
};