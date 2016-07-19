package com.zego.livedemo3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.zego.biz.BizStream;
import com.zego.biz.BizUser;
import com.zego.biz.callback.BizCallback;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class PlayActivity extends BaseDisplayActivity {

    protected long mRoomKey;

    protected long mServerKey;

    protected AlertDialog mDialogHandleRequestPublish = null;
    /**
     * 启动入口.
     *
     * @param activity 源activity
     */
    public static void actionStart(Activity activity, long roomKey, long serverKey) {
        Intent intent = new Intent(activity, PlayActivity.class);
        intent.putExtra(IntentExtra.ROOM_KEY, roomKey);
        intent.putExtra(IntentExtra.SERVER_KEY, serverKey);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.scale_translate,
                R.anim.my_alpha_action);
    }

    @Override
    protected void initExtraData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {

            Intent intent = getIntent();
            mRoomKey = intent.getLongExtra(IntentExtra.ROOM_KEY, 0);
            mServerKey = intent.getLongExtra(IntentExtra.SERVER_KEY, 0);

        }
        super.initExtraData(savedInstanceState);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            // 登录业务房间
            mBizLiveRoom.getInExistedRoom(mRoomKey, mServerKey, PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
        } else {
            // 恢复发布 播放
            restorePublishAndPlay();
        }

        mBizLiveRoom.setBizCallback(new BizCallback() {
            @Override
            public void onLogin(int errCode, long roomKey, long serverKey) {
                if(errCode == 0){
                    mChannel = BizLiveRoomUitl.getChannel(roomKey, serverKey);
                    ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
                    mZegoAVKit.loginChannel(zegoUser, mChannel);
                    recordLog(MY_SELF + ": login room(" + roomKey +  ") success");
                    recordLog(MY_SELF + ": start login channel(" +  mChannel + ")");
                }else {
                    recordLog(MY_SELF + ": login room(" + roomKey +  ") fail --errCode:" + errCode);
                }
            }

            @Override
            public void onStreamCreate(String streamID, String url) {
                if(!TextUtils.isEmpty(streamID)){
                    recordLog(MY_SELF + ": create stream(" + streamID +  ") success");
                    mPublishStreamID = streamID;
                    startPublish();
                }else {
                    recordLog(MY_SELF + ": create stream(" + streamID +  ") fail");
                }
            }

            @Override
            public void onStreamAdd(BizStream[] bizStreams) {
                if(bizStreams != null && bizStreams.length > 0){

                    for(BizStream bizStream : bizStreams){
                        recordLog(bizStream.userName + ": create stream(" + bizStream.streamID +  ") success");
                        startPlay(bizStream.streamID);
                        mLiveCount++;
                    }
                    setPublishControlState();
                }
            }

            @Override
            public void onStreamDelete(BizStream[] bizStreams) {
                if(bizStreams != null && bizStreams.length > 0){
                    for(BizStream bizStream : bizStreams){
                        recordLog(bizStream.userName + ": delete stream(" + bizStream.streamID +  ")");
                        stopPlay(bizStream.streamID);
                        mLiveCount--;
                    }
                    setPublishControlState();
                }

            }

            @Override
            public void onReceiveMsg(int msgType, String data) {
                handleMsg(msgType, data);
            }
        });
    }

    @Override
    protected void doLiveBusinessAfterLoginChannel() {
        if(!mHadBeenLoginned){
            mBizLiveRoom.getStreamList();
        }else {
            restorePublishAndPlay();
        }
    }

    @Override
    protected void setPublishControlText() {
        if(mIsPublishing){
            tvPublisnControl.setText("停止连麦");
            tvPublishSetting.setEnabled(true);
        }else {
            tvPublisnControl.setText("请求连麦");
            tvPublishSetting.setEnabled(false);
        }
    }

    @OnClick(R.id.tv_publish_control)
    public void doPublish(){
        if(mIsPublishing){
            stopPublish();
        }else{
            BizLiveRoomUitl.sendMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH, null, "test", BizApiManager.getInstance().getBizUser().userID);
        }
    }

    private void handleMsg(int msgType, String data){
        if(msgType != 2 || TextUtils.isEmpty(data)){
            return;
        }

        Gson gson = new Gson();
        final HashMap<String, Object> mapData = gson.fromJson(data, new TypeToken<HashMap<String, Object>>(){}.getType());
        if(mapData != null){
            String command = (String) mapData.get(BizLiveRoomUitl.KEY_COMMAND);
            String fromUserID = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USER_ID);
            String fromUserName = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USRE_NAME);
            String magic = (String) mapData.get(BizLiveRoomUitl.KEY_MAGIC);

            if(BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND.equals(command)){
                if(BizApiManager.getInstance().getBizUser().userID.equals(magic)){
                    if(BizLiveRoomUitl.AGREE_PUBLISH.equals(mapData.get(BizLiveRoomUitl.KEY_CONTENT))){
                        recordLog(MY_SELF + ": 请求连麦被通过!");
                        mBizLiveRoom.createSreamInRoom(BizApiManager.getInstance().getBizUser().userName + "is coming!", null);
                    }else{
                        recordLog(MY_SELF + ": 请求连麦被拒绝!");
                        AlertDialog dialogNotify = new AlertDialog.Builder(this).setTitle("提示").setMessage("你的连麦请求被拒绝").setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                        dialogNotify.show();
                    }
                }else {
                    List<Object> listToUsers = (List<Object>) mapData.get(BizLiveRoomUitl.KEY_TO_USER);
                    LinkedTreeMap<String, String> mapToUser = (LinkedTreeMap<String, String>) listToUsers.get(0);
                    if(listToUsers != null && listToUsers.size() > 0){
                        String toUserName = mapToUser.get(BizLiveRoomUitl.KEY_TO_USER_NAME);
                        if(BizLiveRoomUitl.AGREE_PUBLISH.equals(mapData.get(BizLiveRoomUitl.KEY_CONTENT))){
                            recordLog(toUserName + ": 请求连麦被通过!");
                        }else{
                            recordLog(toUserName + ": 请求连麦被拒绝!");
                        }
                    }

                    if(mDialogHandleRequestPublish != null && mDialogHandleRequestPublish.isShowing()){
                        mDialogHandleRequestPublish.dismiss();
                    }
                }
            }else if(BizLiveRoomUitl.KEY_REQUEST_PUBLISH.equals(command)){

                recordLog(fromUserName + ": 请求连麦");

                if(mIsPublishing){

                    BizUser requestUser = new BizUser();
                    requestUser.userID = fromUserID;
                    requestUser.userName = fromUserName;
                    final List<BizUser> listToUsers =new ArrayList<>();
                    listToUsers.add(requestUser);

                    mDialogHandleRequestPublish = new AlertDialog.Builder(this).setTitle("提示").setMessage(fromUserName + " 正在请求连麦, 是否同意?").setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BizLiveRoomUitl.sendMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND, listToUsers, BizLiveRoomUitl.AGREE_PUBLISH, (String)mapData.get(BizLiveRoomUitl.KEY_MAGIC));
                            dialog.dismiss();
                        }
                    }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BizLiveRoomUitl.sendMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND, listToUsers, BizLiveRoomUitl.DISAGREE_PUBLISH, (String) mapData.get(BizLiveRoomUitl.KEY_MAGIC));
                            dialog.dismiss();
                        }
                    }).create();

                    mDialogHandleRequestPublish.show();
                }
            }
        }
    }
}
