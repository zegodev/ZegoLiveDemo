package com.zego.livedemo3.ui.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.zego.biz.BizStream;
import com.zego.biz.BizUser;
import com.zego.livedemo3.R;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.interfaces.OnLiveRoomListener;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.base.BaseLiveActivity;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.util.ArrayList;
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
        super.doBusiness(savedInstanceState);
        if (savedInstanceState == null) {

            // 登录业务房间
            BizLivePresenter.getInstance().loginExistedRoom(mRoomKey, mServerKey);
            recordLog(MY_SELF + ": start login room(" + mRoomKey + ")");

            // 登录频道, zego sdk
            mChannel = BizLiveRoomUitl.getChannel(mRoomKey, mServerKey);
            ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
            mZegoAVKit.loginChannel(zegoUser, mChannel);
            recordLog(MY_SELF + ": start login channel(" + mChannel + ")");
        }

        BizLivePresenter.getInstance().setLiveRoomListener(new OnLiveRoomListener() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey) {
                if (errCode == 0) {
                    recordLog(MY_SELF + ": onLoginRoom success(" + roomKey + ")");

                    // 获取房间流信息
                    BizLivePresenter.getInstance().getStreamList();
                } else {
                    recordLog(MY_SELF + ": onLoginRoom fail(" + roomKey + ") --errCode:" + errCode);
                }
            }

            @Override
            public void onDisconnected(int errCode, long roomKey, long serverKey) {
                recordLog(MY_SELF + ": onDisconnected");
            }

            @Override
            public void onStreamCreate(String streamID, String url) {
                recordLog(MY_SELF + ": onStreamCreate(" + streamID + ")");
                if (!TextUtils.isEmpty(streamID)) {
                    mPublishStreamID = streamID;
                    // 业务服务器创建流成功, 开始使用zego sdk推流
                    startPublish();
                }
            }

            @Override
            public void onStreamAdd(BizStream[] listStream) {
                if (listStream != null && listStream.length > 0) {
                    for (BizStream bizStream : listStream) {
                        recordLog(bizStream.userName + ": onStreamAdd(" + bizStream.streamID + ")");
                        if (mHaveLoginedChannel) {
                            startPlay(bizStream.streamID, getFreeZegoRemoteViewIndex());
                        } else {
                            mListStream.add(bizStream);
                        }
                    }
                }
            }

            @Override
            public void onStreamDelete(BizStream[] listStream) {
                if (listStream != null && listStream.length > 0) {
                    for (BizStream bizStream : listStream) {
                        recordLog(bizStream.userName + ": onStreamDelete(" + bizStream.streamID + ")");
                        stopPlay(bizStream.streamID);
                    }
                }
            }

            @Override
            public void onReceiveRequestMsg(String fromUserID, String fromUserName, final String magic) {
                // 打印log
                recordLog(getString(R.string.someone_is_requesting_to_broadcast, fromUserName));

                // 当前用户处于连麦状态时,才能响应其他用户的连麦请求
                if (mIsPublishing) {

                    BizUser toUser = new BizUser();
                    toUser.userID = fromUserID;
                    toUser.userName = fromUserName;
                    final List<BizUser> listToUsers = new ArrayList<>();
                    listToUsers.add(toUser);

                    // 弹出对话框
                    mDialogHandleRequestPublish = new AlertDialog.Builder(PlayActivity.this).setTitle(getString(R.string.hint))
                            .setMessage(getString(R.string.someone_is_requesting_to_broadcast_allow, fromUserName))
                            .setPositiveButton(getString(R.string.Allow), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 同意连麦请求
                                    BizLivePresenter.getInstance().respondLiveTogether(listToUsers, magic, true);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(getString(R.string.Deny), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 拒绝连麦请求
                            BizLivePresenter.getInstance().respondLiveTogether(listToUsers, magic, false);
                            dialog.dismiss();
                        }
                    }).create();

                    mDialogHandleRequestPublish.show();
                }
            }

            @Override
            public void onReceiveRespondMsg(boolean isRespondToMyRequest, boolean isAgree, String userNameOfRequest) {

                if (isRespondToMyRequest) {
                    if (isAgree) {
                        // 打印日志
                        recordLog(getString(R.string.request_of_broadcast_has_been_allowed, MY_SELF));
                        mPublishTitle = PreferenceUtil.getInstance().getUserName() + " is coming";
                        // 连麦请求被通过, 在业务房间创建流
                        BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);
                    } else {
                        // 打印日志
                        recordLog(getString(R.string.request_of_broadcast_has_been_denied, MY_SELF));
                        AlertDialog dialogNotify = new AlertDialog.Builder(PlayActivity.this).setTitle(getString(R.string.hint))
                                .setMessage(getString(R.string.your_request_has_been_denied)).setPositiveButton(getString(R.string.got_it),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create();
                        dialogNotify.show();
                    }
                } else {
                    if (isAgree) {
                        // 打印日志
                        recordLog(getString(R.string.request_of_broadcast_has_been_allowed, userNameOfRequest));
                        if (mDialogHandleRequestPublish != null && mDialogHandleRequestPublish.isShowing()) {
                            mDialogHandleRequestPublish.dismiss();
                        }
                    } else {
                        recordLog(getString(R.string.request_of_broadcast_has_been_denied, userNameOfRequest));
                    }
                }
            }
        }, mHandler);
    }


    @Override
    protected void doPublishOrPlay() {
        for (BizStream bizStream : mListStream) {
            startPlay(bizStream.streamID, getFreeZegoRemoteViewIndex());
        }
    }

    @Override
    protected void initPublishControlText() {
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
            BizLivePresenter.getInstance().requestLivingTogether();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清空回调, 避免内存泄漏
        BizLivePresenter.getInstance().setLiveRoomListener(null, null);
    }
}
