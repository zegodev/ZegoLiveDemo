package com.zego.livedemo3.ui.activities.externalrender;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.zego.biz.BizStream;
import com.zego.livedemo3.R;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.interfaces.OnLiveRoomListener;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;

import java.util.ArrayList;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class ExternalRenderPlayActivity extends ExternalRenderBaseLiveActivity {

    protected ArrayList<BizStream> mListStream = new ArrayList<>();

    protected RelativeLayout mRlytPlayBackground;

    /**
     * 启动入口.
     *
     * @param activity 源activity
     */
    public static void actionStart(Activity activity, long roomKey, long serverKey, BizStream[] listStream) {
        Intent intent = new Intent(activity, ExternalRenderPlayActivity.class);
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

//        if (savedInstanceState == null) {
//            mRlytPlayBackground = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_play_bg, null);
//            mRlytPlayBackground.setLayoutParams(mListViewLive.get(0).getLayoutParams());
//            ((RelativeLayout) mListViewLive.get(0).getParent()).addView(mRlytPlayBackground);
//        }
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        super.doBusiness(savedInstanceState);
        if (savedInstanceState == null) {

            // 登录业务房间
            BizLivePresenter.getInstance().loginExistedRoom(mRoomKey, mServerKey, PreferenceUtil.getInstance().getUserID(), BizLiveRoomUitl.USER_NAME_PREFIX_SINGLE_ANCHOR + PreferenceUtil.getInstance().getUserName());
            recordLog(MY_SELF + ": start login room(" + mRoomKey + ")");

            // 登录频道, zego sdk
            mChannel = BizLiveRoomUitl.getChannel(mRoomKey, mServerKey);

            loginChannel();

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
            }

            @Override
            public void onStreamCreate(String streamID, String url) {
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
            }

            @Override
            public void onReceiveRespondMsg(boolean isRespondToMyRequest, boolean isAgree, String userNameOfRequest) {
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
        tvPublisnControl.setText(R.string.request_to_join);
        tvPublisnControl.setEnabled(false);
        tvPublishSetting.setEnabled(false);
    }

    @Override
    protected void hidePlayBackground() {
        if (mRlytPlayBackground != null) {
            mRlytPlayBackground.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清空回调, 避免内存泄漏
        BizLivePresenter.getInstance().setLiveRoomListener(null, null);
    }
}
