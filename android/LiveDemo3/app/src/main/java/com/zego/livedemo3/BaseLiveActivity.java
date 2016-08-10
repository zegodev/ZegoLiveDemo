package com.zego.livedemo3;


import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zego.biz.BizLiveRoom;
import com.zego.livedemo3.base.AbsShowActivity;
import com.zego.livedemo3.utils.LiveUtils;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.livedemo3.widgets.PublishSettingsPannel;
import com.zego.livedemo3.widgets.ViewLive;
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
public abstract class BaseLiveActivity extends AbsShowActivity {

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

    public static final String KEY_PUBLISH_NUMBER = "KEY_PUBLISH_NUMBER";

    public static final String KEY_LIST_LIVEVIEW_TAG = "KEY_LIST_LIVEVIEW_TAG";

    public static final String KEY_LIST_LOG = "KEY_LIST_LOG";

    public static final String KEY_CAMERA_CAPTURE_ROTATION = "KEY_CAMERA_CAPTURE_ROTATION";

    public static final String MY_SELF = "MySelf";

    public static final String EMPTY_STREAM_ID = "EMPTY_STREAM_ID";

    protected ZegoAVKit mZegoAVKit;

    protected BizLiveRoom mBizLiveRoom;

    protected InputStream mIsBackgroundMusic;

    protected LinkedList<ViewLive> mListViewLive= new LinkedList<>();
    protected List<String> mListLiveViewTag = new ArrayList<>();
    protected List<String> mListLiveViewTagForCallComing = new ArrayList<>();
    protected LinkedHashMap<ZegoAVKitCommon.ZegoRemoteViewIndex, String> mMapFreeViewIndex = new LinkedHashMap<>();
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

    protected int mPublishNumber = 0;

    protected boolean mHaveLoginedChannel = false;

    protected boolean mHostHasBeenCalled = false;

    protected ZegoAVKitCommon.ZegoCameraCaptureRotation mZegoCameraCaptureRotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0;

    protected DisplayManager.DisplayListener mDisplayListener;

    protected PhoneStateListener mPhoneStateListener;

    protected abstract void doLiveBusinessAfterLoginChannel();

    protected abstract void setPublishControlText();

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
            mPublishNumber = PreferenceUtil.getInstance().getIntValue(KEY_PUBLISH_NUMBER, 0);

            mListLiveViewTag = (List<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LIVEVIEW_TAG);
            if (mListLiveViewTag == null) {
                mListLiveViewTag = new ArrayList<>();
            }

            mListLog = (LinkedList<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LOG);
            if (mListLog == null) {
                mListLog = new LinkedList<>();
            }

            mZegoCameraCaptureRotation = (ZegoAVKitCommon.ZegoCameraCaptureRotation) PreferenceUtil.getInstance().getObjectFromString(KEY_CAMERA_CAPTURE_ROTATION);
        }
    }


    @Override
    protected void initVariables(final Bundle savedInstanceState) {

        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();
        mBizLiveRoom = BizApiManager.getInstance().getBizLiveRoom();

        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.First, EMPTY_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, EMPTY_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, EMPTY_STREAM_ID);

        // 初始化sdk回调
        initCallback();
        // 初始化电话监听器
        initPhoneCallingListener();
        // 初始化屏幕旋转
        initRotationListener();
    }

    /**
     * 初始化设置面板.
     */
    private void initSettingPannel() {

        PublishSettingsPannel settingsPannel = (PublishSettingsPannel) findViewById(R.id.publishSettingsPannel);
        settingsPannel.initPublishSettings(mEnableCamera, mEnableFrontCam, mEnableMic, mEnableTorch, mEnableBackgroundMusic, mSelectedBeauty, mSelectedFilter);
        settingsPannel.setPublishSettingsCallback(new PublishSettingsPannel.PublishSettingsCallback() {
            @Override
            public void onEnableCamera(boolean isEnable) {
                mEnableCamera = isEnable;
                mZegoAVKit.enableCamera(isEnable);
            }

            @Override
            public void onEnableFrontCamera(boolean isEnable) {
                mEnableFrontCam = isEnable;
                mZegoAVKit.setFrontCam(isEnable);
                if (mZegoCameraCaptureRotation != null) {
                    mZegoAVKit.setDisplayRotation(mZegoCameraCaptureRotation);
                }

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

        mBehavior = BottomSheetBehavior.from(settingsPannel);
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
        setPublishControlText();

        mRlytControlHeader = (RelativeLayout) findViewById(R.id.rlyt_control_header);

        initSettingPannel();

        final ViewLive vlBigView = (ViewLive) findViewById(R.id.vl_big_view);
        if (vlBigView != null) {
            mListViewLive.add(vlBigView);
        }

        final ViewLive vlSmallView1 = (ViewLive) findViewById(R.id.vl_small_view1);
        if (vlSmallView1 != null) {
            vlSmallView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vlSmallView1.toFullScreen(vlBigView, mZegoAVKit);
                }
            });
            mListViewLive.add(vlSmallView1);
        }

        final ViewLive vlSmallView2 = (ViewLive) findViewById(R.id.vl_small_view2);
        if (vlSmallView2 != null) {
            vlSmallView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vlSmallView2.toFullScreen(vlBigView, mZegoAVKit);
                }
            });
            mListViewLive.add(vlSmallView2);
        }

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
        tvSpeaker.setSelected(!mEnableSpeaker);

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
        PreferenceUtil.getInstance().setIntValue(KEY_PUBLISH_NUMBER, mPublishNumber);

        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LOG, mListLog);

        mListLiveViewTag = new ArrayList<>();
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            mListLiveViewTag.add((String) mListViewLive.get(i).getTag());
        }
        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LIVEVIEW_TAG, mListLiveViewTag);

        PreferenceUtil.getInstance().setObjectToString(KEY_CAMERA_CAPTURE_ROTATION, mZegoCameraCaptureRotation);
    }

    /**
     * 初始化屏幕旋转监听器.
     */
    protected void initRotationListener() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDisplayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    changeRotation();
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            };

            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            displayManager.registerDisplayListener(mDisplayListener, mHandler);
        } else {
            changeRotation();
        }
    }

    @Override
    protected void onDestroy() {
        // 注销屏幕监听
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            displayManager.unregisterDisplayListener(mDisplayListener);
        }

        // 注销电话监听
        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        super.onDestroy();
    }

    /**
     * activity重建后, 恢复发布与播放.
     */
    protected void replayAndRepublish() {

        for (int i = 0, size = mListLiveViewTag.size(); i < size; i++) {
            int streamOrdinal = ViewLive.getStreamOrdinalFromLiveTag(mListLiveViewTag.get(i));
            String streamID = ViewLive.getStreamIDFromLiveTag(mListLiveViewTag.get(i));
            switch (streamOrdinal) {
                case 0:
                case 1:
                case 2:
                    startPlay(streamID, LiveUtils.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
                    break;
                case ViewLive.PUBLISH_STREAM_ORDINAL:
                    startPublish();
                    break;
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
            switch (streamOrdinal) {
                case 0:
                case 1:
                case 2:
                    startPlay(streamID, LiveUtils.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
                    break;
                case ViewLive.PUBLISH_STREAM_ORDINAL:
                    mBizLiveRoom.createSreamInRoom(mPublishTitle, mPublishStreamID);
                    break;
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
            ViewLive currentViewLive = mListViewLive.get(i);
            if (currentViewLive.getStreamID().equals(streamID)) {
                int j = i;
                for (; j < size - 1; j++) {
                    ViewLive nextViewLive = mListViewLive.get(j + 1);
                    if (nextViewLive.isFree()) {
                        break;
                    }

                    int nextStreamOrdinal = nextViewLive.getStreamOrdinal();
                    switch (nextStreamOrdinal){
                        case 0:
                        case 1:
                        case 2:
                            mZegoAVKit.setRemoteView(LiveUtils.getZegoRemoteViewIndexByOrdinal(nextStreamOrdinal), currentViewLive.getTextureView());
                            break;
                        case ViewLive.PUBLISH_STREAM_ORDINAL:
                            mZegoAVKit.setLocalView(currentViewLive.getTextureView());
                            break;
                    }
                    currentViewLive.setLiveTag(nextViewLive.getLiveTag());
                    currentViewLive.setLiveQuality(nextViewLive.getLiveQuality());
                    currentViewLive = nextViewLive;
                }
                // 标记最后一个View可用
                mListViewLive.get(j).setFree();
                break;
            }
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
     * 初始化zego sdk回调.
     */
    protected void initCallback() {

        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onLoginChannel(String liveChannel, int retCode) {
                if (retCode == 0) {
                    recordLog(MY_SELF + ": login channel(" + liveChannel + ") success");
                    doLiveBusinessAfterLoginChannel();

                    if (!mHaveLoginedChannel) {
                        mHaveLoginedChannel = true;
                    }
                } else {
                    recordLog(MY_SELF + ": login channel(" + liveChannel + ") fail --errCode:" + retCode);
                }
            }

            @Override
            public void onPublishSucc(String streamID, String liveChannel, HashMap<String, Object> info) {
                mIsPublishing = true;
                recordLog(MY_SELF + ": publish stream(" + streamID + ") success");
                setPublishControlText();
                mBizLiveRoom.reportStreamState(true, streamID, PreferenceUtil.getInstance().getUserID());
                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                mIsPublishing = false;
                // 停止预览
                mZegoAVKit.stopPreview();
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);
                recordLog(MY_SELF + ": publish stream(" + streamID + ") stop --errCode:" + retCode);
                setPublishControlText();
                mBizLiveRoom.reportStreamState(false, streamID, PreferenceUtil.getInstance().getUserID());
                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                recordLog(MY_SELF + ": play stream(" + streamID + ") success");
                mRlytControlHeader.bringToFront();

                mPublishNumber++;
                setPublishEnabled();

                // 记录流ID用于play失败后重新play
                mMapReplayStreamID.put(streamID, false);

            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                recordLog(MY_SELF + ": play stream(" + streamID + ") stop --errCode:" + retCode);
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);
                mRlytControlHeader.bringToFront();

                mPublishNumber--;
                setPublishEnabled();

                // 当一条流play失败后重新play一次
                if(retCode == 2 && !TextUtils.isEmpty(streamID)){
                    if(!mMapReplayStreamID.get(streamID)){
                        mMapReplayStreamID.put(streamID, true);
                        startPlay(streamID, getFreeZegoRemoteViewIndex());
                    }
                }
            }

            @Override
            public void onVideoSizeChanged(String streamID, int width, int height) {
                hidePlayBackground();
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
            public void onPlayQualityUpdate(String streamID, int quality) {
                setLiveQuality(streamID, quality);

            }

            @Override
            public void onPublishQulityUpdate(String streamID, int quality) {
                setLiveQuality(streamID, quality);
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

    private void setLiveQuality(String streamID, int quality){
        if(TextUtils.isEmpty(streamID)){
            return;
        }
        for(ViewLive vl : mListViewLive){
            if(streamID.equals(vl.getStreamID())){
                vl.setLiveQuality(quality);
                break;
            }
        }
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
                            // 登陆频道
                            ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
                            mZegoAVKit.loginChannel(zegoUser, mChannel);
                        }

                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        mHostHasBeenCalled = true;
                        mListLiveViewTagForCallComing = new ArrayList<>();
                        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
                            mListLiveViewTagForCallComing.add((String) mListViewLive.get(i).getTag());
                        }
                        // 来电停止发布与播放
                        stopAllStream();
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


    /**
     * 开始发布.
     */
    protected void startPublish() {

        ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive == null) {
            return;
        }

        // 标记view已经被占用
        freeViewLive.setLiveTag(ViewLive.PUBLISH_STREAM_ORDINAL, mPublishStreamID);

        // 输出发布状态
        recordLog(MY_SELF + ": start publishing " + mPublishStreamID);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));

        // 开始播放
        mZegoAVKit.setLocalView(freeViewLive.getTextureView());
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();
        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);

        mZegoAVKit.setFrontCam(mEnableFrontCam);
        mZegoAVKit.enableTorch(mEnableTorch);
        mZegoAVKit.enableMic(mEnableMic);
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
        recordLog(MY_SELF + ": start playing " + streamID);


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

    /**
     * 根据屏幕的旋转角度旋转用于play或者publish的TextureView.
     */
    protected void changeRotation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0);
                mZegoCameraCaptureRotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0;
                break;
            case Surface.ROTATION_90:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90);
                mZegoCameraCaptureRotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90;
                break;
            case Surface.ROTATION_180:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_180);
                mZegoCameraCaptureRotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_180;
                break;
            case Surface.ROTATION_270:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_270);
                mZegoCameraCaptureRotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_270;
                break;
        }
    }


    protected void logout() {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.do_you_really_want_to_leave)).setTitle(getString(R.string.hint)).setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                stopAllStream();
                mBizLiveRoom.leaveRoom();

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
    }

    /**
     * 退出.
     */
    protected void stopAllStream() {

        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            switch (mListViewLive.get(i).getStreamOrdinal()) {
                case 0:
                case 1:
                case 2:
                    stopPlay(mListViewLive.get(i).getStreamID());
                    break;
                case ViewLive.PUBLISH_STREAM_ORDINAL:
                    stopPublish();
                    break;
            }


        }

        mZegoAVKit.logoutChannel();

    }

    protected void setPublishEnabled() {
        if (!mIsPublishing) {
            if (mPublishNumber >= 3) {
                tvPublisnControl.setEnabled(false);
            } else {
                tvPublisnControl.setEnabled(true);
            }
        }
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

}
