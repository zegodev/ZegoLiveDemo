package com.zego.livedemo3;


import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zego.biz.BizLiveRoom;
import com.zego.livedemo3.base.AbsShowActivity;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.livedemo3.widgets.PublishSettingsPannel;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;

/**
 * des: 主页面
 */
public abstract class BaseDisplayActivity extends AbsShowActivity {

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

    public static final String KEY_LIST_TEXTUREVIEW_TAG = "KEY_LIST_TEXTUREVIEW_TAG";

    public static final String KEY_LIST_LOG = "KEY_LIST_LOG";

    public static final String MY_SELF = "MySelf";

    public static final int BIG_VIDEO_ORDINAL = 100;

    public static final int FREE_VIEW_ORDINAL = -1;

    public static final String NONE_STREAM_ID = "NONE";

    public static final String SEPARATOR = "&";

    public static final String TAG_OF_FREE_VIEW = FREE_VIEW_ORDINAL + SEPARATOR + NONE_STREAM_ID;

    protected ZegoAVKit mZegoAVKit;

    protected BizLiveRoom mBizLiveRoom;

    protected LinkedList<TextureView> mListTextureView = new LinkedList<>();
    protected List<String> mListTextureViewTag = new ArrayList<>();
    protected LinkedHashMap<ZegoAVKitCommon.ZegoRemoteViewIndex, String> mMapFreeViewIndex = new LinkedHashMap<>();
    protected LinkedList<String> mListLog = new LinkedList<>();

    public TextView tvPublisnControl;

    public TextView tvPublishSetting;

    public TextView tvSpeaker;

    public RelativeLayout mRlytControlPanel;

    public FrameLayout mFlytMainContent;

    public CoordinatorLayout coordinatorLayout;

    public PublishSettingsPannel mPublishSettingsPannel;

    private BottomSheetBehavior mBehavior;

    protected String mPublishTitle;

    protected String mPublishStreamID;

    protected String mChannel;

    protected boolean mIsPublishing = false;

    protected boolean mEnableCamera = true;

    protected boolean mEnableFrontCam = true;

    protected boolean mEnableTorch = false;

    protected boolean mEnableSpeaker = true;

    protected boolean mEnableMic = true;

    protected int mSelectedBeauty = 0;

    protected int mSelectedFilter = 0;

    protected int mPublishNumber = 0;

    protected boolean mHaveLoginedChannel = false;

    protected boolean mActivityHasBeenPaused = false;

    protected boolean mHostHasBeenCalled = false;

    protected boolean mHostHasBeenCalled2 = false;

    protected ZegoAVKitCommon.ZegoCameraCaptureRotation mZegoCameraCaptureRotation;

    protected DisplayManager.DisplayListener mDisplayListener;

    protected PhoneStateListener mPhoneStateListener;

    protected abstract void doLiveBusinessAfterLoginChannel();

    protected abstract void setPublishControlText();

    protected abstract void afterPlaySuccess();


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_display;
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

            mListTextureViewTag = (List<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_TEXTUREVIEW_TAG);
            if (mListTextureViewTag == null) {
                mListTextureViewTag = new ArrayList<>();
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

        mBizLiveRoom = BizApiManager.getInstance().getBizLiveRoom();

        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.First, NONE_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, NONE_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, NONE_STREAM_ID);

        // 初始化sdk回调
        initCallback();
        // 初始化电话监听器
        initPhoneCallingListener();
        // 初始化屏幕旋转
        initRotationListener();
    }


    @Override
    protected void initViews(Bundle savedInstanceState) {

        tvPublishSetting = (TextView) findViewById(R.id.tv_publish_settings);
        tvPublisnControl = (TextView) findViewById(R.id.tv_publish_control);
        setPublishControlText();
        tvSpeaker = (TextView) findViewById(R.id.tv_speaker);


        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mPublishSettingsPannel = (PublishSettingsPannel) findViewById(R.id.publishSettingsPannel);
        mBehavior = BottomSheetBehavior.from(mPublishSettingsPannel);
        mPublishSettingsPannel.initPublishSettings(mEnableCamera, mEnableFrontCam, mEnableMic, mEnableTorch, mSelectedBeauty, mSelectedFilter);
        initPublishSettingsCallback();

        mFlytMainContent = (FrameLayout) findViewById(R.id.main_content);
        mFlytMainContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        mRlytControlPanel = (RelativeLayout) findViewById(R.id.rlyt_control_panel);


        TextureView tvBigVideo = (TextureView) findViewById(R.id.tv_big_video);
        if (tvBigVideo != null) {
            tvBigVideo.setTag(TAG_OF_FREE_VIEW);
            mListTextureView.add(tvBigVideo);
        }

        TextureView tvSmallVideo1 = (TextureView) findViewById(R.id.tv_small_video1);
        if (tvSmallVideo1 != null) {
            tvSmallVideo1.setTag(TAG_OF_FREE_VIEW);
            tvSmallVideo1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exchangeTextureViewSize((TextureView) v);
                }
            });
            mListTextureView.add(tvSmallVideo1);
        }

        TextureView tvSmallVideo2 = (TextureView) findViewById(R.id.tv_small_video2);
        if (tvSmallVideo2 != null) {
            tvSmallVideo2.setTag(TAG_OF_FREE_VIEW);
            tvSmallVideo2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exchangeTextureViewSize((TextureView) v);
                }
            });
            mListTextureView.add(tvSmallVideo2);
        }

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
        tvSpeaker.setSelected(!mEnableSpeaker);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHaveLoginedChannel) {
            if (mActivityHasBeenPaused) {
                mActivityHasBeenPaused = false;
                replayAndRepublish();
            }
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
        PreferenceUtil.getInstance().setIntValue(KEY_PUBLISH_NUMBER, mPublishNumber);

        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LOG, mListLog);

        // 获取每个播放View上的流信息, 用于恢复播放
        if (!mHostHasBeenCalled2) {
            mActivityHasBeenPaused = true;

            mListTextureViewTag = new ArrayList<>();
            for (int i = 0, size = mListTextureView.size(); i < size; i++) {
                mListTextureViewTag.add((String) mListTextureView.get(i).getTag());
            }
            PreferenceUtil.getInstance().setObjectToString(KEY_LIST_TEXTUREVIEW_TAG, mListTextureViewTag);

        } else {
            mHostHasBeenCalled2 = false;
        }

        for (int i = 0, size = mListTextureView.size(); i < size; i++) {
            mListTextureView.get(i).setTag(TAG_OF_FREE_VIEW);
        }
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
        mPhoneStateListener = null;

        super.onDestroy();
    }

    /**
     * 恢复发布与播放.
     */
    protected void replayAndRepublish() {
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
                    startPublish();
                    break;
            }
        }
    }

    /**
     * 切换全屏.
     *
     * @param textureViewBeClicked
     */
    protected void exchangeTextureViewSize(TextureView textureViewBeClicked) {

        TextureView tvBigVideo = mListTextureView.get(0);
        TextureView tvSmallViedo = textureViewBeClicked;

        String bigVideoTag = (String) tvBigVideo.getTag();
        String smallVideoTag = (String) tvSmallViedo.getTag();

        switch (getPlayStreamOrdinalFromTag(bigVideoTag)) {
            case 0:
            case 1:
            case 2:
                mZegoAVKit.setRemoteView(getZegoRemoteViewIndexByOrdinal(getPlayStreamOrdinalFromTag(bigVideoTag)), tvSmallViedo);
                break;
            case BIG_VIDEO_ORDINAL:
                mZegoAVKit.setLocalView(tvSmallViedo);
                break;
        }

        switch (getPlayStreamOrdinalFromTag(smallVideoTag)) {
            case 0:
            case 1:
            case 2:
                mZegoAVKit.setRemoteView(getZegoRemoteViewIndexByOrdinal(getPlayStreamOrdinalFromTag(smallVideoTag)), tvBigVideo);
                break;
            case BIG_VIDEO_ORDINAL:
                mZegoAVKit.setLocalView(tvBigVideo);
                break;
        }

        tvBigVideo.setTag(smallVideoTag);
        tvSmallViedo.setTag(bigVideoTag);
    }


    protected ZegoAVKitCommon.ZegoRemoteViewIndex getZegoRemoteViewIndexByOrdinal(int playStreamOrdinal) {
        ZegoAVKitCommon.ZegoRemoteViewIndex index = null;

        switch (playStreamOrdinal) {
            case 0:
                index = ZegoAVKitCommon.ZegoRemoteViewIndex.First;
                break;
            case 1:
                index = ZegoAVKitCommon.ZegoRemoteViewIndex.Second;
                break;
            case 2:
                index = ZegoAVKitCommon.ZegoRemoteViewIndex.Third;
                break;
        }

        return index;
    }

    protected ZegoAVKitCommon.ZegoRemoteViewIndex getFreeZegoRemoteViewIndex() {
        ZegoAVKitCommon.ZegoRemoteViewIndex freeIndex = null;
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (NONE_STREAM_ID.equals(mMapFreeViewIndex.get(index))) {
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
    protected TextureView getFreeTextureView() {
        TextureView tvFreeView = null;
        for (int i = 0, size = mListTextureView.size(); i < size; i++) {
            TextureView textureView = mListTextureView.get(i);
            if (getPlayStreamOrdinalFromTag((String) textureView.getTag()) == FREE_VIEW_ORDINAL) {
                tvFreeView = textureView;
                tvFreeView.setVisibility(View.VISIBLE);
                break;
            }
        }
        return tvFreeView;
    }

    /**
     * 释放View用于再次播放, 释放remoteViewIndex.
     *
     * @param streamID
     */
    protected void releaseTextureViewAndRemoteViewIndex(String streamID) {
        for (int i = 0, size = mListTextureView.size(); i < size; i++) {
            TextureView textureView = mListTextureView.get(i);
            String tag = (String) textureView.getTag();
            if (getPlayStreamIDFromTag(tag).equals(streamID)) {
                int j = i;
                for (; j < size - 1; j++) {
                    mListTextureView.get(j).setTag(mListTextureView.get(j + 1).getTag());
                }
                // 标记最后一个View可用
                mListTextureView.get(j).setTag(TAG_OF_FREE_VIEW);
                mListTextureView.get(j).setVisibility(View.INVISIBLE);
                break;
            }
        }

        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (mMapFreeViewIndex.get(index).equals(streamID)) {
                // 标记remoteViewIndex可用
                mMapFreeViewIndex.put(index, NONE_STREAM_ID);
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
                mRlytControlPanel.bringToFront();
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
                mRlytControlPanel.bringToFront();
            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                recordLog(MY_SELF + ": play stream(" + streamID + ") success");
                mRlytControlPanel.bringToFront();
                afterPlaySuccess();
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                recordLog(MY_SELF + ": play stream(" + streamID + ") stop --errCode:" + retCode);
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);
                mRlytControlPanel.bringToFront();
            }

            @Override
            public void onVideoSizeChanged(String streamID, int width, int height) {
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
        });

    }

    private void initPublishSettingsCallback() {
        mPublishSettingsPannel.setPublishSettingsCallback(new PublishSettingsPannel.PublishSettingsCallback() {
            @Override
            public void onEnableCamera(boolean isEnable) {
                mEnableCamera = isEnable;
                mZegoAVKit.enableCamera(isEnable);
            }

            @Override
            public void onEnableFrontCamera(boolean isEnable) {
                mEnableFrontCam = isEnable;
                mZegoAVKit.setFrontCam(isEnable);
                if(mZegoCameraCaptureRotation != null){
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
                            mHostHasBeenCalled = false;
                            // 登陆频道
                            ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
                            mZegoAVKit.loginChannel(zegoUser, mChannel);
                        }

                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        mHostHasBeenCalled2 = true;
                        mHostHasBeenCalled = true;

                        mListTextureViewTag = new ArrayList<>();
                        for (int i = 0, size = mListTextureView.size(); i < size; i++) {
                            mListTextureViewTag.add((String) mListTextureView.get(i).getTag());
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

        final TextureView freeTextureView = getFreeTextureView();
        if (freeTextureView == null) {
            return;
        }

        // 标记view已经被占用
        freeTextureView.setTag(BIG_VIDEO_ORDINAL + SEPARATOR + mPublishStreamID);

        // 输出发布状态
        recordLog(MY_SELF + ": start publishing " + mPublishStreamID);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));

        // 开始播放
        mZegoAVKit.setLocalView(freeTextureView);
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

    /**
     * 开始播放流.
     */
    protected void startPlay(String streamID, ZegoAVKitCommon.ZegoRemoteViewIndex remoteViewIndex) {

        if (remoteViewIndex == null) {
            return;
        }

        final TextureView freeTextureView = getFreeTextureView();
        if (freeTextureView == null) {
            return;
        }

        // 标记remoteViewIndex已经被占用
        mMapFreeViewIndex.put(remoteViewIndex, streamID);

        // 标记view已经被占用
        freeTextureView.setTag(remoteViewIndex.code + SEPARATOR + streamID);

        // 输出播放状态
        recordLog(MY_SELF + ": start playing " + streamID);


        // 播放
        mZegoAVKit.setRemoteViewMode(remoteViewIndex, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setRemoteView(remoteViewIndex, freeTextureView);
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

    protected int getPlayStreamOrdinalFromTag(String tag) {
        int ordinal = FREE_VIEW_ORDINAL;

        if (tag != null) {
            String[] arr = tag.split(SEPARATOR);
            if (arr != null) {
                ordinal = Integer.valueOf(arr[0]);
            }
        }

        return ordinal;
    }

    protected String getPlayStreamIDFromTag(String tag) {
        String playStreamID = null;

        if (tag != null) {
            String[] arr = tag.split(SEPARATOR);
            if (arr != null) {
                playStreamID = arr[1];
            }
        }

        return playStreamID;
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

        for (int i = 0, size = mListTextureView.size(); i < size; i++) {
            int playStreamOrdinal = getPlayStreamOrdinalFromTag((String) mListTextureView.get(i).getTag());
            String playStreamID = getPlayStreamIDFromTag((String) mListTextureView.get(i).getTag());
            switch (playStreamOrdinal) {
                case 0:
                case 1:
                case 2:
                    stopPlay(playStreamID);
                    break;
                case BIG_VIDEO_ORDINAL:
                    stopPublish();
                    break;
            }


        }

        mZegoAVKit.logoutChannel();

    }

    protected void setPublishControlState() {
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
    public void doSpeaker() {
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
