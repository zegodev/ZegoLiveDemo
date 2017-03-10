package com.zego.livedemo3.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.zego.biz.BizUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class BizLiveRoomUitl {

    public static final String KEY_REQUEST_PUBLISH = "requestPublish";

    public static final String KEY_REQUEST_PUBLISH_RESPOND = "requestPublishRespond";

    public static final String KEY_COMMAND = "command";

    public static final String KEY_FROM_USER_ID = "fromUserId";

    public static final String KEY_FROM_USRE_NAME = "fromUserName";

    public static final String KEY_TO_USER = "toUser";

    public static final String KEY_TO_USER_ID = "toUserId";

    public static final String KEY_TO_USER_NAME = "toUserName";

    public static final String KEY_CONTENT = "content";

    public static final String KEY_MAGIC = "magic";

    public static final String AGREE_PUBLISH = "YES";

    public static final String DISAGREE_PUBLISH = "NO";

    public static final String USER_NAME_PREFIX_SINGLE_ANCHOR = "#d-";

    public static final String USER_NAME_PREFIX_MORE_ANCHORS = "#m-";

    public static final String USER_NAME_PREFIX_MIX_STREAM = "#s-";

    public static final String USER_NAME_PREFIX_GAME_LIVING = "#g-";

    public static String getChannel(long roomKey, long serverKey){
       return "0x" + Long.toHexString(roomKey) + "-0x" + Long.toHexString(serverKey);
    }

    public static String formateMsg(String command, List<BizUser> listToUsers, String content, String magicNumber){

        Map<String, Object> requestInfo = new HashMap<>();

        requestInfo.put(KEY_COMMAND, command);
        requestInfo.put(KEY_FROM_USER_ID, PreferenceUtil.getInstance().getUserID());
        requestInfo.put(KEY_FROM_USRE_NAME, PreferenceUtil.getInstance().getUserName());

        if(listToUsers != null && listToUsers.size() > 0){
            List<HashMap<String, String>> listMapToUsers = new ArrayList<>();
            for(BizUser user : listToUsers){
                HashMap<String, String> mapUser = new HashMap<>();
                mapUser.put(KEY_TO_USER_ID, user.userID);
                mapUser.put(KEY_TO_USER_NAME, user.userName);
                listMapToUsers.add(mapUser);
            }

            requestInfo.put(KEY_TO_USER, listMapToUsers);
        }

        if(!TextUtils.isEmpty(content)){
            requestInfo.put(KEY_CONTENT, content);
        }

        requestInfo.put(KEY_MAGIC, magicNumber);

        Gson gson = new Gson();
        String data = gson.toJson(requestInfo);

        return data;
    }
}
