package com.zego.livedemo3.ui.activities.singleanchor;


import android.Manifest;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.activities.LogListActivity;
import com.zego.livedemo3.ui.base.AbsShowActivity;
import com.zego.livedemo3.ui.widgets.PublishSettingsPannel;
import com.zego.livedemo3.ui.widgets.ViewLive;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ShareUtils;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.AuxData;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

/**
 * des: 主页面
 */
public abstract class SingleAnchorBaseLiveActivity extends AbsShowActivity {

    public static final String KEY_CHANNEL = "KEY_CHANNEL";

    public static final String KEY_PUBLISH_TITLE = "KEY_PUBLISH_TITLE";

    public static final String KEY_PUBLISH_STREAM_ID = "KEY_PUBLISH_STREAM_ID";

    public static final String KEY_IS_PUBLISHING = "KEY_IS_PUBLISHING";

    public static final String KEY_ENABLE_CAMERA = "KEY_ENABLE_CAMERA";

    public static final String KEY_ENABLE_FRONT_CAM = "KEY_ENABLE_FRONT_CAM";

    public static final String KEY_ENABLE_TORCH = "KEY_ENABLE_TORCH";

    public static final String KEY_ENABLE_SPEAKER = "KEY_ENABLE_SPEAKER";

    public static final String KEY_ENABLE_MIC = "KEY_ENABLE_MIC";

    public static final String KEY_HAVE_LOGINNED_CHANNEL = "KEY_HAVE_LOGINNED_CHANNEL";

    public static final String KEY_SELECTED_BEAUTY = "KEY_SELECTED_BEAUTY";

    public static final String KEY_SELECTED_FILTER = "KEY_SELECTED_FILTER";

    public static final String KEY_LIST_LIVEVIEW_TAG = "KEY_LIST_LIVEVIEW_TAG";

    public static final String KEY_LIST_LOG = "KEY_LIST_LOG";

    public static final String KEY_CAMERA_CAPTURE_ROTATION = "KEY_CAMERA_CAPTURE_ROTATION";

    public static final String KEY_ROOM_KEY = "KEY_ROOM_KEY";

    public static final String KEY_SETVER_KEY = "KEY_SERVER_KEY";

    public static final String MY_SELF = "MySelf";

    public static final String EMPTY_STREAM_ID = "EMPTY_STREAM_ID";

    protected ZegoAVKit mZegoAVKit;

    protected InputStream mIsBackgroundMusic;

    protected LinkedList<ViewLive> mListViewLive= new LinkedList<>();

    protected List<String> mListLiveViewTag = new ArrayList<>();

    protected List<String> mListLiveViewTagForCallComing = new ArrayList<>();

    protected LinkedHashMap<ZegoAVKitCommon.ZegoRemoteViewIndex, String> mMapFreeViewIndex;

    protected LinkedList<String> mListLog = new LinkedList<>();

    protected Map<String, Boolean> mMapReplayStreamID = new HashMap<>();

    public TextView tvPublisnControl;

    public TextView tvPublishSetting;

    public TextView tvSpeaker;

    public BottomSheetBehavior mBehavior;

    public RelativeLayout mRlytControlHeader;

    protected String mPublishTitle;

    protected String mPublishStreamID;

    protected String mChannel;

    protected boolean mIsPublishing = false;

    protected boolean mEnableSpeaker = true;

    protected boolean mEnableCamera = true;

    protected boolean mEnableFrontCam = true;

    protected boolean mEnableMic = true;

    protected boolean mEnableTorch = false;

    protected boolean mEnableBackgroundMusic = false;

    protected int mSelectedBeauty = 0;

    protected int mSelectedFilter = 0;

    protected boolean mHaveLoginedChannel = false;

    protected boolean mHostHasBeenCalled = false;

    protected PhoneStateListener mPhoneStateListener;

    protected PublishSettingsPannel mSettingsPannel;

    protected long mRoomKey;

    protected long mServerKey;

    protected abstract void doPublishOrPlay();

    protected abstract void initPublishControlText();

    protected abstract void hidePlayBackground();


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_live;
    }


    @Override
    protected void initExtraData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Activity 后台被回收后重新启动, 恢复数据
            mChannel = PreferenceUtil.getInstance().getStringValue(KEY_CHANNEL, null);
            mPublishTitle = PreferenceUtil.getInstance().getStringValue(KEY_PUBLISH_TITLE, null);
            mPublishStreamID = PreferenceUtil.getInstance().getStringValue(KEY_PUBLISH_STREAM_ID, null);
            mIsPublishing = PreferenceUtil.getInstance().getBooleanValue(KEY_IS_PUBLISHING, false);
            mEnableFrontCam = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_FRONT_CAM, false);
            mEnableTorch = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_TORCH, false);
            mEnableSpeaker = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_SPEAKER, false);
            mEnableMic = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_MIC, false);
            mEnableCamera = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_CAMERA, false);
            mHaveLoginedChannel = PreferenceUtil.getInstance().getBooleanValue(KEY_HAVE_LOGINNED_CHANNEL, false);
            mSelectedBeauty = PreferenceUtil.getInstance().getIntValue(KEY_SELECTED_BEAUTY, 0);
            mSelectedFilter = PreferenceUtil.getInstance().getIntValue(KEY_SELECTED_FILTER, 0);
            mRoomKey = PreferenceUtil.getInstance().getLongValue(KEY_ROOM_KEY, 0);
            mServerKey = PreferenceUtil.getInstance().getLongValue(KEY_SETVER_KEY, 0);

            mListLiveViewTag = (List<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LIVEVIEW_TAG);
            if (mListLiveViewTag == null) {
                mListLiveViewTag = new ArrayList<>();
            }

            mListLog = (LinkedList<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LOG);
            if (mListLog == null) {
                mListLog = new LinkedList<>();
            }
        }
    }


    @Override
    protected void initVariables(final Bundle savedInstanceState) {

        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();

        mMapFreeViewIndex = new LinkedHashMap<>();
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.First, EMPTY_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, EMPTY_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, EMPTY_STREAM_ID);

        // 初始化sdk回调
        initCallback();
        // 初始化电话监听器
        initPhoneCallingListener();
    }

    /**
     * 初始化设置面板.
     */
    private void initSettingPannel() {

        mSettingsPannel = (PublishSettingsPannel) findViewById(R.id.publishSettingsPannel);
        mSettingsPannel.initPublishSettings(mEnableCamera, mEnableFrontCam, mEnableMic, mEnableTorch, mEnableBackgroundMusic, mSelectedBeauty, mSelectedFilter);
        mSettingsPannel.setPublishSettingsCallback(new PublishSettingsPannel.PublishSettingsCallback() {
            @Override
            public void onEnableCamera(boolean isEnable) {
                mEnableCamera = isEnable;
                mZegoAVKit.enableCamera(isEnable);
            }

            @Override
            public void onEnableFrontCamera(boolean isEnable) {
                mEnableFrontCam = isEnable;
                mZegoAVKit.setFrontCam(isEnable);
            }

            @Override
            public void onEnableMic(boolean isEnable) {
                mEnableMic = isEnable;
                mZegoAVKit.enableMic(isEnable);
            }

            @Override
            public void onEnableTorch(boolean isEnable) {
                mEnableTorch = isEnable;
                mZegoAVKit.enableTorch(isEnable);
            }

            @Override
            public void onEnableBackgroundMusic(boolean isEnable) {
                mEnableBackgroundMusic = isEnable;
                mZegoAVKit.enableAux(isEnable);

                if(!isEnable){
                    if(mIsBackgroundMusic != null){
                        try {
                            mIsBackgroundMusic.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mIsBackgroundMusic = null;
                    }
                }
            }

            @Override
            public void onSetBeauty(int beauty) {
                mSelectedBeauty = beauty;
                mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(beauty));
            }

            @Override
            public void onSetFilter(int filter) {
                mSelectedFilter = filter;
                mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(filter));
            }
        });

        mBehavior = BottomSheetBehavior.from(mSettingsPannel);
        FrameLayout flytMainContent = (FrameLayout) findViewById(R.id.main_content);
        if (flytMainContent != null) {
            flytMainContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            });
        }
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        tvSpeaker = (TextView) findViewById(R.id.tv_speaker);
        tvPublishSetting = (TextView) findViewById(R.id.tv_publish_settings);
        tvPublisnControl = (TextView) findViewById(R.id.tv_publish_control);
        initPublishControlText();

        mRlytControlHeader = (RelativeLayout) findViewById(R.id.rlyt_control_header);

        initSettingPannel();

        final ViewLive vlBigView = (ViewLive) findViewById(R.id.vl_big_view);
        if (vlBigView != null) {
            mListViewLive.add(vlBigView);
        }
        vlBigView.setViewLiveCallback(new ViewLive.ViewLiveCallback() {
            @Override
            public void setLocalView(TextureView textureView) {
                mZegoAVKit.setLocalView(textureView);
            }

            @Override
            public void setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex index, TextureView textureView) {
                mZegoAVKit.setRemoteView(index, textureView);
            }

            @Override
            public void setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex index, ZegoAVKitCommon.ZegoVideoViewMode mode) {
                mZegoAVKit.setRemoteViewMode(index, mode);

                int currentOrientation = getWindowManager().getDefaultDisplay().getRotation();
                if(mode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit){
                    if(currentOrientation == Surface.ROTATION_90 || currentOrientation == Surface.ROTATION_270){
                        mZegoAVKit.setRemoteViewRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90, index);
                    }else {
                        mZegoAVKit.setRemoteViewRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0, index);
                    }
                }else if(mode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill){
                    if(currentOrientation == Surface.ROTATION_90 || currentOrientation == Surface.ROTATION_270){
                        mZegoAVKit.setRemoteViewRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0, index);
                    }else {
                        mZegoAVKit.setRemoteViewRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90, index);
                    }

                }
            }

            @Override
            public void shareToQQ(List<String> listShareUrls) {
                ShareUtils.getInstance().shareToQQ(SingleAnchorBaseLiveActivity.this, vlBigView.getListShareUrls(), mRoomKey, mServerKey, mPublishStreamID);
            }
        });

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
        tvSpeaker.setSelected(!mEnableSpeaker);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            replayAndRepublish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 保存数据, 用于Activity在后台被回收后重新恢复
        PreferenceUtil.getInstance().setStringValue(KEY_CHANNEL, mChannel);
        PreferenceUtil.getInstance().setStringValue(KEY_PUBLISH_TITLE, mPublishTitle);
        PreferenceUtil.getInstance().setStringValue(KEY_PUBLISH_STREAM_ID, mPublishStreamID);
        PreferenceUtil.getInstance().setBooleanValue(KEY_IS_PUBLISHING, mIsPublishing);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_CAMERA, mEnableCamera);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_FRONT_CAM, mEnableFrontCam);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_TORCH, mEnableTorch);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_SPEAKER, mEnableSpeaker);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_MIC, mEnableMic);
        PreferenceUtil.getInstance().setBooleanValue(KEY_HAVE_LOGINNED_CHANNEL, mHaveLoginedChannel);
        PreferenceUtil.getInstance().setIntValue(KEY_SELECTED_BEAUTY, mSelectedBeauty);
        PreferenceUtil.getInstance().setIntValue(KEY_SELECTED_FILTER, mSelectedFilter);
        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LOG, mListLog);
        PreferenceUtil.getInstance().setLongValue(KEY_ROOM_KEY, mRoomKey);
        PreferenceUtil.getInstance().setLongValue(KEY_SETVER_KEY, mServerKey);

        mListLiveViewTag = new ArrayList<>();
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            mListLiveViewTag.add(mListViewLive.get(i).getLiveTag());
        }
        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LIVEVIEW_TAG, mListLiveViewTag);
    }

    /**
     * activity重建后, 恢复发布与播放.
     */
    protected void replayAndRepublish() {

        for (int i = 0, size = mListLiveViewTag.size(); i < size; i++) {
            int streamOrdinal = ViewLive.getStreamOrdinalFromLiveTag(mListLiveViewTag.get(i));
            String streamID = ViewLive.getStreamIDFromLiveTag(mListLiveViewTag.get(i));

            if(ViewLive.isPublishView(streamOrdinal)){
                startPublish();
            }else {
                startPlay(streamID, ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
            }
        }
    }

    /**
     * 挂断电话后, 恢复发布与播放.
     */
    protected void replayAndRepublishAfterRingOff() {
        for (int i = 0, size = mListLiveViewTagForCallComing.size(); i < size; i++) {
            int streamOrdinal = ViewLive.getStreamOrdinalFromLiveTag(mListLiveViewTagForCallComing.get(i));
            String streamID = ViewLive.getStreamIDFromLiveTag(mListLiveViewTagForCallComing.get(i));

            if(ViewLive.isPublishView(streamOrdinal)){
                BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);
            }else {
                startPlay(streamID, ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
            }
        }
    }

    /**
     * 获取空闲的remoteViewIndex.
     * @return
     */
    protected ZegoAVKitCommon.ZegoRemoteViewIndex getFreeZegoRemoteViewIndex() {
        ZegoAVKitCommon.ZegoRemoteViewIndex freeIndex = null;
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (EMPTY_STREAM_ID.equals(mMapFreeViewIndex.get(index))) {
                freeIndex = index;
                break;
            }
        }
        return freeIndex;
    }

    /**
     * 获取空闲的View用于播放或者发布.
     *
     * @return
     */
    protected ViewLive getFreeViewLive() {
        ViewLive vlFreeView = null;
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            ViewLive viewLive = mListViewLive.get(i);
            if (viewLive.isFree()) {
                vlFreeView = viewLive;
                vlFreeView.setVisibility(View.VISIBLE);
                break;
            }
        }
        return vlFreeView;
    }

    /**
     * 释放View用于再次播放, 释放remoteViewIndex.
     *
     * @param streamID
     */
    protected void releaseTextureViewAndRemoteViewIndex(String streamID) {
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            mListViewLive.get(i).setFree();
        }

        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (mMapFreeViewIndex.get(index).equals(streamID)) {
                // 标记remoteViewIndex可用
                mMapFreeViewIndex.put(index, EMPTY_STREAM_ID);
                break;
            }
        }
    }

    /**
     * 通过streamID查找正在publish或者play的ViewLive.
     * @param streamID
     * @return
     */
    protected ViewLive getViewLiveByStreamID(String streamID){
        if(TextUtils.isEmpty(streamID)){
            return  null;
        }

        ViewLive viewLive = null;
        for(ViewLive vl : mListViewLive){
            if(streamID.equals(vl.getStreamID())){
                viewLive = vl;
                break;
            }
        }

        return viewLive;
    }


    /**
     * 初始化zego sdk回调.
     */
    protected void initCallback() {

        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onLoginChannel(String liveChannel, int retCode) {
                if (retCode == 0) {
                    if (!mHaveLoginedChannel) {
                        doPublishOrPlay();
                        mHaveLoginedChannel = true;
                        recordLog(MY_SELF + ": onLoginChannel success(" + liveChannel + ")");
                    }

                    if (mHostHasBeenCalled) {
                        mHostHasBeenCalled = false;
                        // 挂断电话重新恢复
                        replayAndRepublishAfterRingOff();
                    }
                } else {
                    recordLog(MY_SELF + ": onLoginChannel fail(" + liveChannel + ") --errCode:" + retCode);
                }
            }

            @Override
            public void onPublishSucc(String streamID, String liveChannel, HashMap<String, Object> info) {
                mIsPublishing = true;
                recordLog(MY_SELF + ": onPublishSucc(" + streamID + ")");

                initPublishControlText();
                BizLivePresenter.getInstance().reportStreamState(true, streamID, PreferenceUtil.getInstance().getUserID());

                ViewLive viewLivePublish = getViewLiveByStreamID(streamID);
                if(viewLivePublish != null){

                    // 显示声音大小
                    viewLivePublish.showSoundLevel(mZegoAVKit, mHandler);

                    List<String> listUrls = new ArrayList<>();
                    if(info != null){

                        String [] hlsList = (String [])info.get("hlsList");
                        if(hlsList != null && hlsList.length > 0){
                            listUrls.add(hlsList[0]);
                        }

                        String [] rtmpList = (String [])info.get("rtmpList");
                        if(rtmpList != null && rtmpList.length > 0){
                            listUrls.add(rtmpList[0]);
                        }
                    }
                    viewLivePublish.setListShareUrls(listUrls);
                }

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                mIsPublishing = false;
                recordLog(MY_SELF + ": onPublishStop(" + streamID + ") --errCode:" + retCode);
                // 停止预览
               // mZegoAVKit.stopPreview();
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);

                initPublishControlText();
                BizLivePresenter.getInstance().reportStreamState(false, streamID, PreferenceUtil.getInstance().getUserID());

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onMixStreamConfigUpdate(int retCode, String mixStreamID, HashMap<String, Object> info) {

            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                recordLog(MY_SELF + ": onPlaySucc(" + streamID + ")");

                // 显示声音大小
                ViewLive viewLivePlay = getViewLiveByStreamID(streamID);
                if(viewLivePlay != null){
                    viewLivePlay.showSoundLevel(mZegoAVKit, mHandler);
                }

                // 记录流ID用于play失败后重新play
                mMapReplayStreamID.put(streamID, false);

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                recordLog(MY_SELF + ": onPlayStop(" + streamID + ") --errCode:" + retCode);
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);

                // 当一条流play失败后重新play一次
                if(retCode == 2 && !TextUtils.isEmpty(streamID)){
                    if(!mMapReplayStreamID.get(streamID)){
                        mMapReplayStreamID.put(streamID, true);
                        startPlay(streamID, getFreeZegoRemoteViewIndex());
                    }
                }

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onVideoSizeChanged(String streamID, int width, int height) {
                hidePlayBackground();

                ViewLive viewLivePlayCurrentStream = getViewLiveByStreamID(streamID);
                if(viewLivePlayCurrentStream == null)
                    return;

                if(width > height) {
                    ViewLive viewLivePlay = getViewLiveByStreamID(streamID);
                    if (viewLivePlay != null) {
                        if(viewLivePlayCurrentStream.getWidth() < viewLivePlayCurrentStream.getHeight()){
                            viewLivePlay.setZegoVideoViewMode(true, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                            mZegoAVKit.setRemoteViewMode(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(viewLivePlay.getStreamOrdinal()), ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                        }else {
                            viewLivePlay.setZegoVideoViewMode(true, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                            mZegoAVKit.setRemoteViewMode(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(viewLivePlay.getStreamOrdinal()), ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                        }
                    }
                }

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onTakeRemoteViewSnapshot(final Bitmap bitmap, ZegoAVKitCommon.ZegoRemoteViewIndex zegoRemoteViewIndex) {
            }

            @Override
            public void onTakeLocalViewSnapshot(final Bitmap bitmap) {
            }

            @Override
            public void onCaptureVideoSize(int width, int height) {
            }

            @Override
            public void onPlayQualityUpdate(String streamID, int quality, double videoFPS, double videoBitrate) {
                ViewLive viewLive = getViewLiveByStreamID(streamID);
                if(viewLive != null){
                    viewLive.setLiveQuality(quality);
                }
            }

            @Override
            public void onPublishQulityUpdate(String streamID, int quality, double videoFPS, double videoBitrate) {
                ViewLive viewLive = getViewLiveByStreamID(streamID);
                if(viewLive != null){
                    viewLive.setLiveQuality(quality);
                }
            }

            @Override
            public AuxData onAuxCallback(int dataLen) {
                // 开启伴奏后, sdk每20毫秒一次取数据
                if(!mEnableBackgroundMusic || dataLen <= 0){
                    return null;
                }

                AuxData auxData = new AuxData();
                auxData.dataBuf = new byte[dataLen];

                try{
                    AssetManager am = getAssets();
                    if(mIsBackgroundMusic == null){
                        mIsBackgroundMusic = am.open("a.pcm");
                    }
                    int len = mIsBackgroundMusic.read(auxData.dataBuf);

                    if(len <= 0){
                        // 歌曲播放完毕
                        mIsBackgroundMusic.close();
                        mIsBackgroundMusic = null;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

                auxData.channelCount = 2;
                auxData.sampleRate = 44100;


                return auxData;
            }
        });

    }


    /**
     * 电话状态监听.
     */
    protected void initPhoneCallingListener() {
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mHostHasBeenCalled) {
                            recordLog(MY_SELF + ": call state idle");
                            // 登陆频道
                            loginChannel();
                        }

                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        recordLog(MY_SELF + ": call state ringing");
                        mHostHasBeenCalled = true;
                        mListLiveViewTagForCallComing = new ArrayList<>();
                        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
                            mListLiveViewTagForCallComing.add(mListViewLive.get(i).getLiveTag());
                        }
                        // 来电停止发布与播放
                        stopAllStreamAndLogout();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                }
            }
        };

        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    protected void recordLog(String msg) {
        mListLog.addFirst(msg);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                           publishStream();
                        }
                    });
                }else {


                    if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(this, R.string.allow_camera_permission, Toast.LENGTH_LONG).show();
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(this, R.string.open_recorder_permission, Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
                break;
        }
    }

    protected void publishStream(){

        ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive == null) {
            return;
        }

        // 标记view已经被占用
        freeViewLive.setLiveTag(ViewLive.PUBLISH_STREAM_ORDINAL, mPublishStreamID);

        // 输出发布状态
        recordLog(MY_SELF + ": start publish " + mPublishStreamID);

        // 设置水印
        ZegoAVKit.setWaterMarkImagePath("asset:watermark.png");
        Rect rect = new Rect();
        rect.left = 50;
        rect.top = 20;
        rect.right = 200;
        rect.bottom = 170;
        ZegoAVKit.setPreviewWaterMarkRect(rect);
        ZegoAVKit.setPublishWaterMarkRect(rect);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));

        // 开始播放
        mZegoAVKit.setLocalView(freeViewLive.getTextureView());
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();

        // 开启码率自动调整
        mZegoAVKit.enableRateControl(true);

        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);
        mZegoAVKit.setFrontCam(mEnableFrontCam);
        mZegoAVKit.enableTorch(mEnableTorch);
        mZegoAVKit.enableMic(mEnableMic);
    }

    /**
     * 开始发布.
     */
    protected void startPublish() {
        // 6.0及以上的系统需要在运行时申请CAMERA RECORD_AUDIO权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
            } else {
                publishStream();
            }
        } else {
            publishStream();
        }
    }

    protected void stopPublish() {
        mZegoAVKit.stopPreview();
        mZegoAVKit.stopPublish();
        mZegoAVKit.setLocalView(null);
    }

    protected boolean isStreamExisted(String streamID){
        boolean isExisted = false;
        for(String value : mMapFreeViewIndex.values()){
            if(value.equals(streamID)){
                isExisted = true;
                break;
            }
        }
        return isExisted;
    }

    /**
     * 开始播放流.
     */
    protected void startPlay(String streamID, ZegoAVKitCommon.ZegoRemoteViewIndex remoteViewIndex) {

        if(isStreamExisted(streamID)){
            return ;
        }

        if (remoteViewIndex == null) {
            return;
        }

         ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive == null) {
            return;
        }

        // 标记remoteViewIndex已经被占用
        mMapFreeViewIndex.put(remoteViewIndex, streamID);

        // 标记view已经被占用
        freeViewLive.setLiveTag(remoteViewIndex.code, streamID);

        // 输出播放状态
        recordLog(MY_SELF + ": start play " + streamID);

        // 播放
        mZegoAVKit.setRemoteViewMode(remoteViewIndex, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setRemoteView(remoteViewIndex, freeViewLive.getTextureView());
        mZegoAVKit.startPlayStream(streamID, remoteViewIndex);
    }

    protected void stopPlay(String streamID) {
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (mMapFreeViewIndex.get(index).equals(streamID)) {
                mZegoAVKit.stopPlayStream(streamID);
                mZegoAVKit.setRemoteView(index, null);
                break;
            }
        }
    }


    protected void logout() {

        if(mIsPublishing){
            AlertDialog dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.do_you_really_want_to_leave)).setTitle(getString(R.string.hint)).setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    stopAllStreamAndLogout();
                    BizLivePresenter.getInstance().leaveRoom();
                    dialog.dismiss();
                    finish();
                }
            }).setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();

            dialog.show();
        }else {

            stopAllStreamAndLogout();
            BizLivePresenter.getInstance().leaveRoom();
            finish();
        }

    }

    /**
     * 退出.
     */
    protected void stopAllStreamAndLogout() {

        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            if(ViewLive.isPublishView(mListViewLive.get(i).getStreamOrdinal())){
                stopPublish();
            }else {
                stopPlay(mListViewLive.get(i).getStreamID());
            }

            mListViewLive.get(i).setFree();
        }

        mZegoAVKit.logoutChannel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return false;
            } else {
                // 退出
                logout();
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.tv_log_list)
    public void openLogList() {
        LogListActivity.actionStart(this);
    }

    @OnClick(R.id.tv_publish_settings)
    public void publishSettings() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @OnClick(R.id.tv_speaker)
    public void doMute() {
        if (mEnableSpeaker) {
            mEnableSpeaker = false;
        } else {
            mEnableSpeaker = true;
        }

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
        tvSpeaker.setSelected(!mEnableSpeaker);
    }

    @OnClick(R.id.tv_close)
    public void close() {
        logout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 注销电话监听
        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        // 注销回调, 避免内存泄漏
        mZegoAVKit.setZegoLiveCallback(null);

    }

    protected void loginChannel(){
        ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
        mZegoAVKit.loginChannel(zegoUser, mChannel);
    }

}
