package com.zego.livedemo3.interfaces;

import com.zego.biz.BizStream;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */

public interface OnLiveRoomListener {

    void onLoginRoom(int errCode, long roomKey, long serverKey);

    void onDisconnected(int errCode, long roomKey, long serverKey);

    void onStreamCreate(String streamID, String url);

    void onStreamAdd(BizStream[] listStream);

    void onStreamDelete(BizStream[] listStream);

    void onReceiveRequestMsg(String fromUserID, String fromUserName, String magic);

    void onReceiveRespondMsg(boolean isRespondToMyRequest, boolean isAgree, String userNameOfRequest);
}
