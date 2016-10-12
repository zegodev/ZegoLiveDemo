package com.zego.livedemo3.presenters;


import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.zego.biz.BizLiveRoom;
import com.zego.biz.BizRoom;
import com.zego.biz.BizStream;
import com.zego.biz.BizUser;
import com.zego.biz.callback.BizLiveCallback;
import com.zego.biz.callback.BizRoomListCallback;
import com.zego.livedemo3.ZegoApplication;
import com.zego.livedemo3.interfaces.OnLiveRoomListener;
import com.zego.livedemo3.interfaces.OnUpdateRoomListListener;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.zegoavkit2.ZegoAVKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * des: 为了更好的展示zego的直播技术, 我们开发了简单的业务测功能, 主要用于管理用户信息、 流信息,
 * 类似于zego客户的业务逻辑.
 */
public class BizLivePresenter {

    private static BizLivePresenter sInstance;

    private BizLiveRoom mBizLiveRoom;

    /**
     * 房间列表更新监听.
     */
    private OnUpdateRoomListListener mUpdateRoomListListener;

    private Handler mHandlerUpdateRoomList;

    /**
     * 直播间监听.
     */
    private OnLiveRoomListener mLiveRoomListener;

    private Handler mHandlerLiveRoom;

    /**
     * 线程池.
     */
    private ExecutorService mExecutorService;

    private String mMyMagic = "";

    private BizLivePresenter() {
        mBizLiveRoom = new BizLiveRoom();
        mExecutorService = Executors.newFixedThreadPool(4);
        init();
        initCallback();
    }

    public static BizLivePresenter getInstance() {
        if (sInstance == null) {
            synchronized (BizLivePresenter.class) {
                if (sInstance == null) {
                    sInstance = new BizLivePresenter();
                }
            }
        }
        return sInstance;
    }


    private void init() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                // 初始化用户信息
                String userID = PreferenceUtil.getInstance().getUserID();
                String userName = PreferenceUtil.getInstance().getUserName();

                if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
                    long ms = System.currentTimeMillis();
                    userID = ms + "";
                    userName = "Android-" + ms;

                    // 保存用户信息
                    PreferenceUtil.getInstance().setUserID(userID);
                    PreferenceUtil.getInstance().setUserName(userName);
                }

                // 设置日志level
                mBizLiveRoom.setLogLevel(ZegoApplication.sApplicationContext, ZegoAVKit.LOG_LEVEL_DEBUG, null);

                byte[] signKey = {
                        (byte) 0x91, (byte) 0x93, (byte) 0xcc, (byte) 0x66, (byte) 0x2a, (byte) 0x1c, (byte) 0xe, (byte) 0xc1,
                        (byte) 0x35, (byte) 0xec, (byte) 0x71, (byte) 0xfb, (byte) 0x7, (byte) 0x19, (byte) 0x4b, (byte) 0x38,
                        (byte) 0x15, (byte) 0xf1, (byte) 0x43, (byte) 0xf5, (byte) 0x7c, (byte) 0xd2, (byte) 0xb5, (byte) 0x9a,
                        (byte) 0xe3, (byte) 0xdd, (byte) 0xdb, (byte) 0xe0, (byte) 0xf1, (byte) 0x74, (byte) 0x36, (byte) 0xd
                };
                long appID = 1;

                mBizLiveRoom.init(appID, signKey, signKey.length, ZegoApplication.sApplicationContext);
            }
        });
    }

    /**
     * 初始化回调.
     */
    private void initCallback() {
        mBizLiveRoom.setBizRoomListCallback(new BizRoomListCallback() {
            @Override
            public void onGetRoomList(int errCode, int totalCount, int beginIndex, final BizRoom[] listRoom, int roomCount) {
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        // 获取房间信息
                        final List<BizRoom> list = new ArrayList<>();
                        if(listRoom != null){
                            for(BizRoom roomInfo : listRoom){
                                list.add(roomInfo);
                            }
                        }

                        // 将房间信息更新到界面
                        if(mUpdateRoomListListener != null && mHandlerUpdateRoomList != null){
                            mHandlerUpdateRoomList.post(new Runnable() {
                                @Override
                                public void run() {
                                    mUpdateRoomListListener.onUpdateRoomList(list);
                                }
                            });
                        }
                    }
                });
            }
        });

        mBizLiveRoom.setBizLiveCallback(new BizLiveCallback() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey, boolean isPublicRoom) {
                if(mLiveRoomListener != null){
                    mLiveRoomListener.onLoginRoom(errCode, roomKey, serverKey);
                }
            }

            @Override
            public void onLeaveRoom(int errCode, boolean isPublicRoom) {
            }

            @Override
            public void onDisconnected(int errCode, long roomKey, long serverKey, boolean isPublicRoom) {
                if(mLiveRoomListener != null){
                    mLiveRoomListener.onDisconnected(errCode, roomKey, serverKey);
                }
            }

            @Override
            public void onKickOut(int errCode, String msg, boolean isPublicRoom) {

            }

            @Override
            public void onStreamCreate(String streamID, String url, boolean isPublicRoom) {
                if(mLiveRoomListener != null){
                    mLiveRoomListener.onStreamCreate(streamID, url);
                }
            }

            @Override
            public void onStreamAdd(BizStream[] listStream, boolean isPublicRoom) {
                if(mLiveRoomListener != null){
                    mLiveRoomListener.onStreamAdd(listStream);
                }
            }

            @Override
            public void onStreamDelete(BizStream[] listStream, boolean isPublicRoom) {
                if(mLiveRoomListener != null){
                    mLiveRoomListener.onStreamDelete(listStream);
                }
            }

            @Override
            public void onReceiveMsg(int msgType, String data, boolean isPublicRoom) {
                handleMsg(msgType, data);
            }

            @Override
            public void onRoomUserUpdate(BizUser[] listUser, int flag, boolean isPublicRoom) {

            }

            @Override
            public void onRoomUserCountUpdate(int newUserCount, boolean isPublicRoom) {

            }
        });
    }

    /**
     * 设置房间列表监听.
     * @param updateRoomListListener
     * @param handlerUpdateRoomList
     */
    public void setUpdateRoomListListener(OnUpdateRoomListListener updateRoomListListener, Handler handlerUpdateRoomList) {
        mUpdateRoomListListener = updateRoomListListener;
        mHandlerUpdateRoomList = handlerUpdateRoomList;
    }

    /**
     * 设置直播间监听.
     * @param liveRoomListener
     * @param handlerLiveRoom
     */
    public void setLiveRoomListener(OnLiveRoomListener liveRoomListener, Handler handlerLiveRoom){
        mLiveRoomListener = liveRoomListener;
        mHandlerLiveRoom = handlerLiveRoom;
    }


    /**
     * 获取房间列表.
     * @param startIndex
     * @param pageSize
     */
    public void getRoomList(int startIndex, int pageSize){
        mBizLiveRoom.dropRoomInfoCache();
        mBizLiveRoom.getRoomList(startIndex, pageSize);
    }

    /**
     * 请求连麦.
     */
    public void requestLivingTogether(){
        mMyMagic = PreferenceUtil.getInstance().getUserID();
        String data = BizLiveRoomUitl.formateMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH, null,
                "requst to broadcast", mMyMagic);

        if(!TextUtils.isEmpty(data)){
            mBizLiveRoom.sendRelayBroadcastCustomMsg(data, data.length(), true);
        }
    }

    /**
     * 响应连麦.
     * @param listToUsers
     * @param magicNumber
     * @param isAgree
     */
    public void respondLiveTogether(List<BizUser> listToUsers, String magicNumber, boolean isAgree){
        String content = isAgree ? BizLiveRoomUitl.AGREE_PUBLISH : BizLiveRoomUitl.DISAGREE_PUBLISH;
        String data = BizLiveRoomUitl.formateMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND, listToUsers, content, magicNumber);

        if(!TextUtils.isEmpty(data)){
            mBizLiveRoom.sendRelayBroadcastCustomMsg(data, data.length(), true);
        }
    }

    /**
     * 处理消息.
     * @param msgType
     * @param data
     */
    public void handleMsg(final int msgType, final String data){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (msgType != 2 || TextUtils.isEmpty(data)) {
                    return;
                }

                Gson gson = new Gson();
                final HashMap<String, Object> mapData = gson.fromJson(data, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                if (mapData != null) {

                    String command = (String) mapData.get(BizLiveRoomUitl.KEY_COMMAND);
                    final String fromUserID = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USER_ID);
                    final String fromUserName = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USRE_NAME);
                    final String magic = (String) mapData.get(BizLiveRoomUitl.KEY_MAGIC);

                    if (BizLiveRoomUitl.KEY_REQUEST_PUBLISH.equals(command)) {
                        if (mLiveRoomListener != null && mHandlerLiveRoom != null) {
                            mHandlerLiveRoom.post(new Runnable() {
                                @Override
                                public void run() {
                                    mLiveRoomListener.onReceiveRequestMsg(fromUserID, fromUserName, magic);
                                }
                            });
                        }
                    } else if (BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND.equals(command)) {

                        // 获取被响应的用户名
                        String userNameOfRequest = null;
                        List<Object> listToUsers = (List<Object>) mapData.get(BizLiveRoomUitl.KEY_TO_USER);
                        if(listToUsers != null && listToUsers.size() > 0){
                            LinkedTreeMap<String, String> mapToUser = (LinkedTreeMap<String, String>) listToUsers.get(0);
                            userNameOfRequest = mapToUser.get(BizLiveRoomUitl.KEY_TO_USER_NAME);
                        }

                        // 是否响应我发出的请求
                        final boolean isRespondToMyRequest = mMyMagic.equals(magic) ? true : false;
                        // 是否同意
                        final boolean isAgree = BizLiveRoomUitl.AGREE_PUBLISH.equals(mapData.get(BizLiveRoomUitl.KEY_CONTENT)) ? true : false;

                        final String userNameTemp = userNameOfRequest;
                        if (mLiveRoomListener != null && mHandlerLiveRoom != null) {
                            mHandlerLiveRoom.post(new Runnable() {
                                @Override
                                public void run() {
                                    mLiveRoomListener.onReceiveRespondMsg(isRespondToMyRequest, isAgree, userNameTemp);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * 向业务服务器请求streamID.
     *
     * @param streamTitle
     */
    public void createStream(String streamTitle, String streamID){
        mBizLiveRoom.createSreamInRoom(streamTitle, streamID, true);
    }

    /**
     * 登陆已经存在的业务房间.
     * @param roomKey
     * @param serverKey
     */
    public void loginExistedRoom(long roomKey, long serverKey, String userID, String userName){
        mBizLiveRoom.getInExistedRoom(roomKey, serverKey, userID, userName, true);
    }

    /**
     * 创建新的房间并登陆.
     */
    public void createAndLoginRoom(String userID, String userName){
        mBizLiveRoom.createRoomAndGetIn(userID, userName, true);
    }

    /**
     * 获取房间的流信息.
     */
    public void getStreamList(){
        mBizLiveRoom.getStreamList(true);
    }

    /**
     * 通知业务服务器流的状态.
     * @param isActive
     * @param streamID
     * @param userID
     */
    public void reportStreamState(boolean isActive, String streamID, String userID){
        mBizLiveRoom.reportStreamState(isActive, streamID, userID, true);
    }

    public void leaveRoom(){
        mBizLiveRoom.leaveRoom(true);
    }

    public void init(long appID, byte[] appSign, int signLen, Context context){
        mBizLiveRoom.init(appID, appSign, signLen, context);
    }

    public void unInit() {
        mBizLiveRoom.uninit();
    }

}