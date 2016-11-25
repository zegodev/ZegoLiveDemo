package com.zego.livedemo3.ui.activities.mixstream;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Surface;

import com.zego.biz.BizStream;
import com.zego.biz.BizUser;
import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.interfaces.OnLiveRoomListener;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.widgets.ViewLive;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.ZegoAvConfig;
import com.zego.zegoavkit2.entity.MixStreamInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class MixStreamPublishActivity extends MixstreamBaseLiveActivity {

    protected AlertDialog mDialogHandleRequestPublish = null;

    protected List<MixStreamInfo> mixStreamInfos = new ArrayList<>();

    /**
     *   app朝向, Surface.ROTATION_0或者Surface.ROTATION_180表示竖屏推流,
     *   Surface.ROTATION_90或者Surface.ROTATION_270表示横屏推流.
     */
    protected int mAppOrientation;

    /**
     * 启动入口.
     *
     * @param activity     源activity
     * @param publishTitle 视频标题
     */
    public static void actionStart(Activity activity, String publishTitle, boolean enableFrontCam, boolean enableTorch, int selectedBeauty, int selectedFilter,  int appOrientation) {
        Intent intent = new Intent(activity, MixStreamPublishActivity.class);
        intent.putExtra(IntentExtra.PUBLISH_TITLE, publishTitle);
        intent.putExtra(IntentExtra.ENABLE_FRONT_CAM, enableFrontCam);
        intent.putExtra(IntentExtra.ENABLE_TORCH, enableTorch);
        intent.putExtra(IntentExtra.SELECTED_BEAUTY, selectedBeauty);
        intent.putExtra(IntentExtra.SELECTED_FILTER, selectedFilter);
        intent.putExtra(IntentExtra.APP_ORIENTATION, appOrientation);
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
            mAppOrientation = intent.getIntExtra(IntentExtra.APP_ORIENTATION, Surface.ROTATION_0);
        }

        super.initExtraData(savedInstanceState);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);

        // 提前预览, 提升用户体验
        ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive != null) {
            // 根据推流方向, 设置publish界面的横、竖朝向
            if(mAppOrientation == Surface.ROTATION_90 || mAppOrientation == Surface.ROTATION_270){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            mZegoAVKit.setLocalView(freeViewLive.getTextureView());
            mZegoAVKit.startPreview();
            mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        }

        mZegoAVKit.setFrontCam(mEnableFrontCam);
        mZegoAVKit.enableTorch(mEnableTorch);
        mZegoAVKit.enableMic(mEnableMic);

        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        super.doBusiness(savedInstanceState);

        if (savedInstanceState == null) {
             // 创建业务房间
            BizLivePresenter.getInstance().createAndLoginRoom(PreferenceUtil.getInstance().getUserID(), BizLiveRoomUitl.USER_NAME_PREFIX_MIX_STREAM + PreferenceUtil.getInstance().getUserName());
        }

        BizLivePresenter.getInstance().setLiveRoomListener(new OnLiveRoomListener() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey) {
                if (errCode == 0) {

                    mRoomKey = roomKey;
                    mServerKey = serverKey;

                    // 获取channel
                    mChannel = BizLiveRoomUitl.getChannel(roomKey, serverKey);

                    // 成功登陆业务房间后, 开始创建流
                    BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);
                    // 打印log
                    recordLog(MY_SELF + ": onLoginRoom success(" + roomKey + ")");
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
                    if(mIsMixStreamMode){
                        mMixStreamID = mPublishStreamID;
                    }

                    mPublishStreamID = streamID;

                    if (!mHaveLoginedChannel) {
                        loginChannel();
                    } else {
                        if(mIsMixStreamMode){
                            publishMixStream();
                        }else {
                            startPublish();
                        }
                    }
                }
            }

            @Override
            public void onStreamAdd(BizStream[] listStream) {
                ZegoAvConfig zegoAvConfig  = ZegoApiManager.getInstance().getZegoAvConfig();

                int width = zegoAvConfig.getVideoEncodeResolutionWidth();
                int height = zegoAvConfig.getVideoEncodeResolutionHeight();
                if (listStream != null && listStream.length > 0) {
                    for (BizStream bizStream : listStream) {
                        recordLog(bizStream.userName + ": onStreamAdd(" + bizStream.streamID + ")");
                        startPlay(bizStream.streamID, getFreeZegoRemoteViewIndex());

                        if(mixStreamInfos.size() == 0){
                            MixStreamInfo mixStreamInfo = new MixStreamInfo();
                            mixStreamInfo.streamID = mPublishStreamID;
                            mixStreamInfo.top = 0;
                            mixStreamInfo.bottom = height;
                            mixStreamInfo.left = 0;
                            mixStreamInfo.right = width;
                            mixStreamInfos.add(mixStreamInfo);
                        }

                        if(mixStreamInfos.size() == 1){
                            MixStreamInfo mixStreamInfo = new MixStreamInfo();
                            mixStreamInfo.streamID = bizStream.streamID;
                            mixStreamInfo.top = (int) (height * 2.0 / 3);
                            mixStreamInfo.bottom = height;
                            mixStreamInfo.left = (int) (width * 2.0 / 3);
                            mixStreamInfo.right = width;
                            mixStreamInfos.add(mixStreamInfo);

                        }else if(mixStreamInfos.size() == 2){

                            MixStreamInfo mixStreamInfo = new MixStreamInfo();
                            mixStreamInfo.streamID = bizStream.streamID;
                            mixStreamInfo.top = (int) (height * 2.0 / 3);
                            mixStreamInfo.bottom = height;
                            mixStreamInfo.left = (int) (width * 1.0 / 3) - 10;
                            mixStreamInfo.right = (int) (width * 2.0 / 3) - 10;
                            mixStreamInfos.add(mixStreamInfo);
                        }
                    }

                    int size = mixStreamInfos.size();
                    MixStreamInfo []infos = new MixStreamInfo[size];

                    for(int i = 0; i < size; i++){
                        infos[i] = mixStreamInfos.get(i);
                    }
                    mZegoAVKit.updateMixStreamConfig(infos);
                }
            }

            @Override
            public void onStreamDelete(BizStream[] listStream) {
                if (listStream != null && listStream.length > 0) {
                    for (BizStream bizStream : listStream) {
                        recordLog(bizStream.userName + ": onStreamDelete(" + bizStream.streamID + ")");
                        stopPlay(bizStream.streamID);

                        for(MixStreamInfo info : mixStreamInfos){
                            if(bizStream.streamID.equals(info.streamID)){
                                mixStreamInfos.remove(info);
                                break;
                            }
                        }
                    }


                    // 更新混流信息
                    int size = mixStreamInfos.size();
                    MixStreamInfo []infos = new MixStreamInfo[size];

                    for(int i = 0; i < size; i++){
                        infos[i] = mixStreamInfos.get(i);
                    }
                    mZegoAVKit.updateMixStreamConfig(infos);
                }
            }

            @Override
            public void onReceiveRequestMsg(final String fromUserID, final String fromUserName, final String magic) {
                // 打印log
                recordLog(getString(R.string.someone_is_requesting_to_broadcast, fromUserName));

                BizUser toUser = new BizUser();
                toUser.userID = fromUserID;
                toUser.userName = fromUserName;
                final List<BizUser> listToUsers = new ArrayList<>();
                listToUsers.add(toUser);

                // 弹出对话框
                mDialogHandleRequestPublish = new AlertDialog.Builder(MixStreamPublishActivity.this).setTitle(getString(R.string.hint))
                        .setMessage(getString(R.string.someone_is_requesting_to_broadcast_allow, fromUserName))
                        .setPositiveButton(getString(R.string.Allow), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                if(mIsMixStreamMode){
                                    // 同意连麦请求
                                     BizLivePresenter.getInstance().respondLiveTogether(listToUsers, magic, true);
                                }else {
                                    // 混流相关逻辑
                                    mMixStreamMagic = magic;
                                    mMixStreamRequestUser = new BizUser();
                                    mMixStreamRequestUser.userID = fromUserID;
                                    mMixStreamRequestUser.userName = fromUserName;
                                    setMixStreamMode(true);
                                    stopPublish();
                                }
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

            @Override
            public void onReceiveRespondMsg(boolean isRespondToMyRequest, boolean isAgree, String userNameOfRequest) {
                if (!isRespondToMyRequest) {
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
        startPublish();
    }

    @Override
    protected void initPublishControlText() {
        if (mIsPublishing) {
            tvPublisnControl.setText(R.string.stop_publishing);
            tvPublishSetting.setEnabled(true);
        } else {
            tvPublisnControl.setText(R.string.start_publishing);
            tvPublishSetting.setEnabled(false);
        }
    }

    @Override
    protected void hidePlayBackground() {

    }

    @OnClick(R.id.tv_publish_control)
    public void doPublish() {
        if (mIsPublishing) {
            setMixStreamMode(false);
            stopPublish();
        } else {
            BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清空回调, 避免内存泄漏
        BizLivePresenter.getInstance().setLiveRoomListener(null, null);
    }
}
