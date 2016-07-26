package com.zego.livedemo3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.TextureView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.zego.biz.BizStream;
import com.zego.biz.BizUser;
import com.zego.biz.callback.BizCallback;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class PublishActivity extends BaseDisplayActivity {

    protected AlertDialog mDialogHandleRequestPublish = null;

    /**
     * 启动入口.
     *
     * @param activity     源activity
     * @param publishTitle 视频标题
     */
    public static void actionStart(Activity activity, String publishTitle, boolean enableFrontCam, boolean enableTorch, int selectedBeauty, int selectedFilter) {
        Intent intent = new Intent(activity, PublishActivity.class);
        intent.putExtra(IntentExtra.PUBLISH_TITLE, publishTitle);
        intent.putExtra(IntentExtra.ENABLE_FRONT_CAM, enableFrontCam);
        intent.putExtra(IntentExtra.ENABLE_TORCH, enableTorch);
        intent.putExtra(IntentExtra.SELECTED_BEAUTY, selectedBeauty);
        intent.putExtra(IntentExtra.SELECTED_FILTER, selectedFilter);
        activity.startActivity(intent);
    }

    @Override
    protected void initExtraData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {

            Intent intent = getIntent();
            mPublishTitle = intent.getStringExtra(IntentExtra.PUBLISH_TITLE);
            mEnableFrontCam = intent.getBooleanExtra(IntentExtra.ENABLE_FRONT_CAM, false);
            mEnableTorch = intent.getBooleanExtra(IntentExtra.ENABLE_TORCH, false);
            mSelectedBeauty = intent.getIntExtra(IntentExtra.SELECTED_BEAUTY, 0);
            mSelectedFilter = intent.getIntExtra(IntentExtra.SELECTED_FILTER, 0);
        }

        super.initExtraData(savedInstanceState);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);

        final TextureView freeTextureView = getFreeTextureView();
        if (freeTextureView == null) {
            return;
        }

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));
        mZegoAVKit.setLocalView(freeTextureView);
        mZegoAVKit.startPreview();
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setFrontCam(mEnableFrontCam);
        mZegoAVKit.enableTorch(mEnableTorch);
        mZegoAVKit.enableMic(mEnableMic);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // 登录业务房间
            mBizLiveRoom.createRoomAndGetIn(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
        } else {
            // 恢复发布 播放
            replayAndRepublish();
        }

        mBizLiveRoom.setBizCallback(new BizCallback() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey) {
                if (errCode == 0) {
                    mChannel = BizLiveRoomUitl.getChannel(roomKey, serverKey);
                    mBizLiveRoom.createSreamInRoom(mPublishTitle, "");
                    recordLog(MY_SELF + ": login room(" + roomKey + ") successfully");
                    recordLog(MY_SELF + ": start login channel(" + mChannel + ")");
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
                    if (!mHaveLoginedChannel) {
                        ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
                        mZegoAVKit.loginChannel(zegoUser, mChannel);
                    } else {
                        startPublish();
                    }

                } else {
                    recordLog(MY_SELF + ": create stream(" + streamID + ") fail");
                }
            }

            @Override
            public void onStreamAdd(BizStream[] bizStreams) {
                if (bizStreams != null && bizStreams.length > 0) {
                    for (BizStream bizStream : bizStreams) {
                        recordLog(bizStream.userName + ": create stream(" + bizStream.streamID + ") success");
                        startPlay(bizStream.streamID, getFreeZegoRemoteViewIndex());
                        mPublishNumber++;
                    }
                    setPublishControlState();
                }
            }

            @Override
            public void onStreamDelete(BizStream[] bizStreams) {
                if (bizStreams != null && bizStreams.length > 0) {
                    for (BizStream bizStream : bizStreams) {
                        recordLog(bizStream.userName + ": delete stream(" + bizStream.streamID + ")");
                        stopPlay(bizStream.streamID);
                        mPublishNumber--;
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
        if (!mHaveLoginedChannel) {
            startPublish();
        } else {
            // 挂断电话重新恢复
            for (int i = 0, size = mListTextureViewTag.size(); i < size; i++) {
                int playStreamOrdinal = getPlayStreamOrdinalFromTag(mListTextureViewTag.get(i));
                String playStreamID = getPlayStreamIDFromTag(mListTextureViewTag.get(i));
                switch (playStreamOrdinal) {
                    case 0:
                    case 1:
                    case 2:
                        startPlay(playStreamID, getZegoRemoteViewIndexByOrdinal(playStreamOrdinal));
                        break;
                    case BIG_VIDEO_ORDINAL:
                        mBizLiveRoom.createSreamInRoom(mPublishTitle, mPublishStreamID);
                        break;
                }
            }
        }
    }

    @Override
    protected void setPublishControlText() {
        if (mIsPublishing) {
            tvPublisnControl.setText(R.string.stop_publishing);
            tvPublishSetting.setEnabled(true);
        } else {
            tvPublisnControl.setText(R.string.start_publishing);
            tvPublishSetting.setEnabled(false);
        }
    }

    @Override
    protected void afterPlaySuccess() {

    }

    @OnClick(R.id.tv_publish_control)
    public void doPublish() {
        if (mIsPublishing) {
            stopPublish();
        } else {
            mBizLiveRoom.createSreamInRoom(mPublishTitle, mPublishStreamID);
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

            final String fromUserID = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USER_ID);
            final String fromUserName = (String) mapData.get(BizLiveRoomUitl.KEY_FROM_USRE_NAME);
            String command = (String) mapData.get(BizLiveRoomUitl.KEY_COMMAND);

            if (BizLiveRoomUitl.KEY_REQUEST_PUBLISH.equals(command)) {
                recordLog(getString(R.string.someone_is_requesting_to_broadcast, fromUserName));

                BizUser requestUser = new BizUser();
                requestUser.userID = fromUserID;
                requestUser.userName = fromUserName;
                final List<BizUser> listToUsers = new ArrayList<>();
                listToUsers.add(requestUser);

                mDialogHandleRequestPublish = new AlertDialog.Builder(this).setTitle(getString(R.string.hint)).setMessage(getString(R.string.someone_is_requesting_to_broadcast_allow, fromUserName)).setPositiveButton(getString(R.string.Allow), new DialogInterface.OnClickListener() {
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

            } else if (BizLiveRoomUitl.KEY_REQUEST_PUBLISH_RESPOND.equals(command)) {
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
        }
    }
}
