#pragma once
#include <functional>

#define WM_ZEGO_SWITCH_THREAD		WM_APP+10086
#define ZegoCommWndClassName		_T("SignalWndClass")
#define ZegoCommWndName			_T("SignalWnd")

#define ZEGO_SWITCH_THREAD_PRE std::function<void(void)>* pFunc = new  std::function<void(void)>;\
*pFunc = [=](void) {

#define ZEGO_SWITCH_THREAD_ING }; \
PostMessage(m_hCommWnd, WM_ZEGO_SWITCH_THREAD, (WPARAM)pFunc, 0);

#define ZEGO_MAKE_SIGNAL(name, count, ...) \
protected:\
	sigslot::signal##count<__VA_ARGS__> m_sig##name;\
public:\
	sigslot::signal##count<__VA_ARGS__>& On##name(void) { return m_sig##name; }