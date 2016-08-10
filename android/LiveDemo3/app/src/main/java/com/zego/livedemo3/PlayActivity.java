package com.zego.livedemo3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

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
public class PlayActivity extends BaseLiveActivity {

    protected long mRoomKey;

    protected long mServerKey;

    protected ArrayList<BizStream> mListStream = new ArrayList<>();

    protected RelativeLayout mRlytPlayBackground;

    protected AlertDialog mDialogHandleRequestPublish = null;

    /**
     * 启动入口.
     *
     * @param activity 源activity
     */
    public static void actionStart(Activity activity, long roomKey, long serverKey, BizStream[] listStream) {
        Intent intent = new Intent(activity, PlayActivity.class);
        intent.putExtra(IntentExtra.ROOM_KEY, roomKey);
        intent.putExtra(IntentExtra.SERVER_KEY, serverKey);
        ArrayList<BizStream> arrayList = new ArrayList<>();
        for (BizStream stream : listStream) {
            arrayList.add(stream);
        }
        intent.putParcelableArrayListExtra(IntentExtra.LIST_STREAM, arrayList);
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
            mListStream = intent.getParcelableArrayListExtra(IntentExtra.LIST_STREAM);
            if (mListStream == null) {
                mListStream = new ArrayList<>();
            }
        }
        super.initExtraData(savedInstanceState);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);

        if (savedInstanceState == null) {
            mRlytPlayBackground = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_play_bg, null);
            mRlytPlayBackground.setLayoutParams(mListViewLive.get(0).getLayoutParams());
            ((RelativeLayout) mListViewLive.get(0).getParent()).addView(mRlytPlayBackground);
        }
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // 登录业务房间
            mBizLiveRoom.getInExistedRoom(mRoomKey, mServerKey, PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());

            // 登录频道
            mChannel = BizLiveRoomUitl.getChannel(mRoomKey, mServerKey);
            ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
            mZegoAVKit.loginChannel(zegoUser, mChannel);
        } else {
            // 恢复发布 播放
            replayAndRepublish();
        }

        mBizLiveRoom.setBizCallback(new BizCallback() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey) {
                if (errCode == 0) {
                    recordLog(MY_SELF + ": login room(" + roomKey + ") success");
                    recordLog(MY_SELF + ": start login channel(" + mChannel + ")");
                    // 获取流信息
                    mBizLiveRoom.getStreamList();
                } else {
                    recordLog(MY_SELF + ": login room(" + roomKey + ") fail --errCode:" + errCode);
                }
            }

            @Override
            public void onLeaveRoom(int i) {

            }

            @Override
            public void onStreamCreate(String streamID, String url) {
                if (!TextUtils.isEmpty(streamID)) {
                    recordLog(MY_SELF + ": create stream(" + streamID + ") success");
                    mPublishStreamID = streamID;
                    startPublish();
                } else {
                    recordLog(MY_SELF + ": create stream(" + streamID + ") fail");
                }
            }

            @Override
            public void onStreamAdd(BizStream[] bizStreams) {
                if (bizStreams != null && bizStreams.length > 0) {
                    for (BizStream bizStream : bizStreams) {
                        if (mHaveLoginedChannel) {
                            recordLog(bizStream.userName + ": create stream(" + bizStream.streamID + ") success");
                            startPlay(bizStream.streamID, getFreeZegoRemoteViewIndex());
                        } else {
                            mListStream.add(bizStream);
                        }
                    }
                }
            }

            @Override
            public void onStreamDelete(BizStream[] bizStreams) {
                if (bizStreams != null && bizStreams.length > 0) {
                    for (BizStream bizStream : bizStreams) {
                        recordLog(bizStream.userName + ": delete stream(" + bizStream.streamID + ")");
                        stopPlay(bizStream.streamID);
                    }
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
        if (mHostHasBeenCalled) {
            mHostHasBeenCalled = false;
            // 挂断电话重新恢复
            replayAndRepublishAfterRingOff();
        } else {
            for (BizStream bizStream : mListStream) {
                recordLog(bizStream.userName + ": create stream(" + bizStream.streamID + ") success");
                startPlay(bizStream.streamID, getFreeZegoRemoteViewIndex());
            }
        }
    }

    @Override
    protected void setPublishControlText() {
        if (mIsPublishing) {
            tvPublisnControl.setText(R.string.stop_publishing);
            tvPublishSetting.setEnabled(true);
        } else {
            tvPublisnControl.setText(R.string.request_to_join);
            tvPublishSetting.setEnabled(false);
        }
    }

    @Override
    protected void hidePlayBackground() {
        if (mRlytPlayBackground != null) {
            mRlytPlayBackground.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.tv_publish_control)
    public void doPublish() {
        if (mIsPublishing) {
            stopPublish();
        } else {
            BizLiveRoomUitl.sendMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH, null, "requst to broadcast", PreferenceUtil.getInstance().getUserID());
        }
    }

    private void handleMsg(int msgType, String data) {
        if (msgType != 2 || TextUtils.isEmpty(data)) {
            return;
        }

        Gson gson = new Gson();
        final HashMap<String, Object> mapData = gson.fromJson(data, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        if (mapData != null) {
            String command = (String) mapData.get(BizLiveRoomUitl.KEY_COMMAND);
            String fromUserID = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USER_ID);
            String fromUserName = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USRE_NAME);
            String magic = (String) mapData.get(BizLiveRoomUitl.KEY_MAGIC);

            if (BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND.equals(command)) {
                if (PreferenceUtil.getInstance().getUserID().equals(magic)) {
                    if (BizLiveRoomUitl.AGREE_PUBLISH.equals(mapData.get(BizLiveRoomUitl.KEY_CONTENT))) {
                        recordLog(getString(R.string.request_of_broadcast_has_been_allowed, MY_SELF));
                        mPublishTitle = PreferenceUtil.getInstance().getUserName() + " is coming";
                        mBizLiveRoom.createSreamInRoom(mPublishTitle, mPublishStreamID);
                    } else {
                        recordLog(getString(R.string.request_of_broadcast_has_been_denied, MY_SELF));
                        AlertDialog dialogNotify = new AlertDialog.Builder(this).setTitle(getString(R.string.hint)).setMessage(getString(R.string.your_request_has_been_denied)).setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                        dialogNotify.show();
                    }
                } else {
                    List<Object> listToUsers = (List<Object>) mapData.get(BizLiveRoomUitl.KEY_TO_USER);
                    LinkedTreeMap<String, String> mapToUser = (LinkedTreeMap<String, String>) listToUsers.get(0);
                    if (listToUsers != null && listToUsers.size() > 0) {
                        String toUserName = mapToUser.get(BizLiveRoomUitl.KEY_TO_USER_NAME);
                        if (BizLiveRoomUitl.AGREE_PUBLISH.equals(mapData.get(BizLiveRoomUitl.KEY_CONTENT))) {
                            recordLog(getString(R.string.request_of_broadcast_has_been_allowed, toUserName));
                        } else {
                            recordLog(getString(R.string.request_of_broadcast_has_been_denied, toUserName));
                        }
                    }

                    if (mDialogHandleRequestPublish != null && mDialogHandleRequestPublish.isShowing()) {
                        mDialogHandleRequestPublish.dismiss();
                    }
                }
            } else if (BizLiveRoomUitl.KEY_REQUEST_PUBLISH.equals(command)) {

                recordLog(getString(R.string.someone_is_requesting_to_broadcast, fromUserName));

                if (mIsPublishing) {

                    BizUser requestUser = new BizUser();
                    requestUser.userID = fromUserID;
                    requestUser.userName = fromUserName;
                    final List<BizUser> listToUsers = new ArrayList<>();
                    listToUsers.add(requestUser);

                    mDialogHandleRequestPublish = new AlertDialog.Builder(this).setTitle(getString(R.string.hint)).setMessage(getString(R.string.someone_is_requesting_to_broadcast_allow, fromUserName)).setPositiveButton(getString(R.string.Allow),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BizLiveRoomUitl.sendMsg(BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND, listToUsers, BizLiveRoomUitl.AGREE_PUBLISH, (String) mapData.get(BizLiveRoomUitl.KEY_MAGIC));
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(getString(R.string.Deny), new DialogInterface.OnClickListener() {
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
