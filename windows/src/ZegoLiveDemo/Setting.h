#pragma once
class CSetting
{
public:
	CSetting();
	~CSetting();

	
	int m_nResolutionWidth;
	int m_nResolutionHeight;
	int m_nBitrate;
	int m_nFPS;
	BOOL m_bMic;
	BOOL m_bAux;

	DWORD m_dwAppId;
	BYTE m_signkey[32];

	CString m_strCam;
	CString m_strMic;
};

