#pragma once
class CSetting
{
public:
	CSetting();
	~CSetting();

	
	int m_nResolutionWidth;
	int m_nResolutionHeight;
	BOOL m_bMic;
	BOOL m_bAux;

	DWORD m_dwAppId;
	BYTE m_signkey[32];

	CString m_strCam;
	CString m_strMic;
};

