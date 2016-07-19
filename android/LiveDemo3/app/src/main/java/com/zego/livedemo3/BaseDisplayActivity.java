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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

/**
 * des: 主页面
 */
public abstract class BaseDisplayActivity extends AbsShowActivity {

    public static final String KEY_LIVE_CHANNEL = "KEY_LIVE_CHANNEL";

    public static final String KEY_PUBLISH_TITLE = "KEY_PUBLISH_TITLE";

    public static final String KEY_PUBLISH_STREAM_ID = "KEY_PUBLISH_STREAM_ID";

    public static final String KEY_IS_PUBLISHING = "KEY_IS_PUBLISHING";

    public static final String KEY_ENABLE_FRONT_CAM = "KEY_ENABLE_FRONT_CAM";

    public static final String KEY_ENABLE_TORCH = "KEY_ENABLE_TORCH";

    public static final String KEY_ENABLE_SPEAKER = "KEY_ENABLE_SPEAKER";

    public static final String KEY_ENABLE_MIC = "KEY_ENABLE_MIC";

    public static final String KEY_ENABLE_CAM = "KEY_ENABLE_CAM";

    public static final String KEY_HAD_BEEN_LOGINNED = "KEY_HAD_BEEN_LOGINNED";

    public static final String KEY_LIST_ORDINAL_AND_STREAM_ID = "KEY_LIST_ORDINAL_AND_STREAM_ID";

    public static final String KEY_MAP_FREE_ZEGO_REMOTE_VIEW_INDEX = "KEY_MAP_FREE_ZEGO_REMOTE_VIEW_INDEX";

    public static final String KEY_LIST_LOG = "KEY_LIST_LOG";

    public static final String KEY_SELECTED_BEAUTY = "KEY_SELECTED_BEAUTY";

    public static final String KEY_SELECTED_FILTER = "KEY_SELECTED_FILTER";

    public static final String KEY_LIST_MAGIC_NUM = "KEY_LIST_MAGIC_NUM";

    public static final String KEY_LIVE_COUNT = "KEY_LIVE_COUNT";

    public static final String MY_SELF = "MySelf";

    public static final int BIG_VIDEO_ORDINAL = 100;

    public static final int FREE_VIEW_ORDINAL = -1;

    public static final String NONE_STREAM_ID = "NONE";

    public static final String SEPARATOR = "&";

    public static final String TAG_VIEW_IS_FREE = FREE_VIEW_ORDINAL + SEPARATOR + NONE_STREAM_ID;

    protected ZegoAVKit mZegoAVKit;

    protected BizLiveRoom mBizLiveRoom;

    protected RelativeLayout.LayoutParams mParamsSmall;
    protected RelativeLayout.LayoutParams mParamsBig;

    protected List<RelativeLayout> mListVideoView = new ArrayList<>();
    protected List<RelativeLayout> mListViewParent = new ArrayList<>();
    protected List<String> mListOrdinalAndStreamID = new ArrayList<>();
    protected Map<ZegoAVKitCommon.ZegoRemoteViewIndex, String> mMapFreeZegoRemoteViewIndex = new HashMap<>();
    protected List<String> mListMagicNum = new ArrayList<>();
    protected LinkedList<String> mListLog = new LinkedList<>();
    protected RelativeLayout mRlytBigVideoParent;

    public TextView tvPublisnControl;

    public TextView tvPublishSetting;

    public FrameLayout mFlytMainContent;

    /**
     * 发布流的Title.
     */
    protected String mPublishTitle;

    /**
     * 发布流的ID.
     */
    protected String mPublishStreamID;

    /**
     * 频道ID.
     */
    protected String mChannel;

    /**
     * 发布标记, false表示"未发布".
     */
    protected boolean mIsPublishing = false;

    protected boolean mEnableFrontCam = true;

    protected boolean mEnableTorch = false;

    protected boolean mEnableSpeaker = true;

    protected boolean mEnableMic = true;

    protected boolean mEnableCam = true;

    protected boolean mHadBeenLoginned = false;

    /**
     * 美颜.
     */
    protected int mSelectedBeauty = 0;

    /**
     * 滤镜.
     */
    protected int mSelectedFilter = 0;

    protected int mLiveCount = 0;

    /**
     * 标记是否有电话呼入, false表示没有.
     */
    protected boolean mHaveBeenCalled = false;

    /**
     * 监听屏幕变化.
     */
    protected DisplayManager.DisplayListener mDisplayListener;

    /**
     * 电话监听.
     */
    protected PhoneStateListener mPhoneStateListener;

    public CoordinatorLayout coordinatorLayout;

    public PublishSettingsPannel mPublishSettingsPannel;

    private BottomSheetBehavior mBehavior;


    /**
     * 登录channel后, publish或者play.
     */
    protected abstract void doLiveBusinessAfterLoginChannel();

    protected abstract void setPublishControlText();

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_display;
    }


    @Override
    protected void initExtraData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mMapFreeZegoRemoteViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.First, NONE_STREAM_ID);
            mMapFreeZegoRemoteViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, NONE_STREAM_ID);
            mMapFreeZegoRemoteViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, NONE_STREAM_ID);
        } else {

            // Activity 后台被回收后重新启动, 恢复数据
            mChannel = PreferenceUtil.getInstance().getStringValue(KEY_LIVE_CHANNEL, null);
            mPublishTitle = PreferenceUtil.getInstance().getStringValue(KEY_PUBLISH_TITLE, null);
            mPublishStreamID = PreferenceUtil.getInstance().getStringValue(KEY_PUBLISH_STREAM_ID, null);
            mIsPublishing = PreferenceUtil.getInstance().getBooleanValue(KEY_IS_PUBLISHING, false);
            mEnableFrontCam = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_FRONT_CAM, false);
            mEnableTorch = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_TORCH, false);
            mEnableSpeaker = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_SPEAKER, false);
            mEnableMic = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_MIC, false);
            mEnableCam = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_CAM, false);
            mHadBeenLoginned = PreferenceUtil.getInstance().getBooleanValue(KEY_HAD_BEEN_LOGINNED, false);
            mSelectedBeauty = PreferenceUtil.getInstance().getIntValue(KEY_SELECTED_BEAUTY, 0);
            mSelectedFilter = PreferenceUtil.getInstance().getIntValue(KEY_SELECTED_FILTER, 0);
            mLiveCount = PreferenceUtil.getInstance().getIntValue(KEY_LIVE_COUNT, 0);

            mMapFreeZegoRemoteViewIndex = (Map<ZegoAVKitCommon.ZegoRemoteViewIndex, String>) PreferenceUtil.getInstance().getObjectFromString(KEY_MAP_FREE_ZEGO_REMOTE_VIEW_INDEX);
            if (mMapFreeZegoRemoteViewIndex == null) {
                mMapFreeZegoRemoteViewIndex = new HashMap<>();
            }

            mListOrdinalAndStreamID = (List<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_ORDINAL_AND_STREAM_ID);
            if (mListOrdinalAndStreamID == null) {
                mListOrdinalAndStreamID = new ArrayList<>();
            }

            mListLog = (LinkedList<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LOG);
            if (mListLog == null) {
                mListLog = new LinkedList<>();
            }

            mListMagicNum = (List<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_MAGIC_NUM);
        }

    }


    @Override
    protected void initVariables(final Bundle savedInstanceState) {

        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();

        mBizLiveRoom = BizApiManager.getInstance().getBizLiveRoom();

        // 初始化sdk回调
        initCallback();
        // 初始化电话监听器
        initPhoneCallingListener();
        // 初始化屏幕旋转
        initRotationListener();
    }

    private void initPublishSettingsCallback() {
      mPublishSettingsPannel.setPublishSettingsCallback(new PublishSettingsPannel.PublishSettingsCallback() {
          @Override
          public void onEnableCamera(boolean isEnable) {
              mEnableCam = isEnable;
              mZegoAVKit.enableCamera(isEnable);
          }

          @Override
          public void onEnableFrontCamera(boolean isEnable) {
              mEnableFrontCam = isEnable;
              mZegoAVKit.setFrontCam(isEnable);
          }

          @Override
          public void onEnalbeSpeaker(boolean isEnable) {
              mEnableSpeaker =isEnable;
              mZegoAVKit.enableSpeaker(isEnable);
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

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mFlytMainContent = (FrameLayout) findViewById(R.id.main_content);

        tvPublishSetting = (TextView)findViewById(R.id.tv_publish_settings);
        tvPublisnControl = (TextView) findViewById(R.id.tv_publish_control);
        setPublishControlText();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mPublishSettingsPannel = (PublishSettingsPannel) findViewById(R.id.publishSettingsPannel);
        mBehavior = BottomSheetBehavior.from(mPublishSettingsPannel);
        mPublishSettingsPannel.initPublishSettings(mEnableCam, mEnableFrontCam, mEnableSpeaker, mEnableMic, mEnableTorch, mSelectedBeauty, mSelectedFilter);
        initPublishSettingsCallback();
        mFlytMainContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        mRlytBigVideoParent = (RelativeLayout) findViewById(R.id.rlyt_big_video_parent);
        RelativeLayout rlytBigVideo = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_show, null);
        mRlytBigVideoParent.addView(rlytBigVideo);
        mParamsBig = (RelativeLayout.LayoutParams) rlytBigVideo.getLayoutParams();
        mListViewParent.add(mRlytBigVideoParent);
        mListVideoView.add(rlytBigVideo);
        // 标记View是空闲的
        rlytBigVideo.setTag(TAG_VIEW_IS_FREE);


        //两个SurfaceView重叠时, 其中一个无法显示视频, 所以用这个办法绕过去
        final RelativeLayout rlytSmallVideoParent1;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rlytSmallVideoParent1 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent1);
            findViewById(R.id.rlyt_small_video_parent2).setVisibility(View.GONE);
        } else {
            rlytSmallVideoParent1 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent2);
            findViewById(R.id.rlyt_small_video_parent1).setVisibility(View.GONE);
        }
        RelativeLayout rlytSmallVideo1 = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_show, null);
        rlytSmallVideoParent1.addView(rlytSmallVideo1);
        mParamsSmall = (RelativeLayout.LayoutParams) rlytSmallVideo1.getLayoutParams();
        rlytSmallVideoParent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeViewSize((RelativeLayout) v);
            }
        });
        mListViewParent.add(rlytSmallVideoParent1);
        mListVideoView.add(rlytSmallVideo1);
        // 标记View是空闲的
        rlytSmallVideo1.setTag(TAG_VIEW_IS_FREE);


        //两个SurfaceView重叠时, 其中一个无法显示视频
        final RelativeLayout rlytSmallVideoParent2;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rlytSmallVideoParent2 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent3);
            findViewById(R.id.rlyt_small_video_parent4).setVisibility(View.GONE);
        } else {
            rlytSmallVideoParent2 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent4);
            findViewById(R.id.rlyt_small_video_parent3).setVisibility(View.GONE);
        }
        RelativeLayout rlytSmallVideo2 = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_show, null);
        rlytSmallVideoParent2.addView(rlytSmallVideo2);
        rlytSmallVideoParent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeViewSize((RelativeLayout) v);
            }
        });
        mListViewParent.add(rlytSmallVideoParent2);
        mListVideoView.add(rlytSmallVideo2);
        // 标记View是空闲的
        rlytSmallVideo2.setTag(TAG_VIEW_IS_FREE);

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // 保存数据, 用于Activity在后台被回收后重新恢复
        PreferenceUtil.getInstance().setStringValue(KEY_LIVE_CHANNEL, mChannel);
        PreferenceUtil.getInstance().setStringValue(KEY_PUBLISH_TITLE, mPublishTitle);
        PreferenceUtil.getInstance().setStringValue(KEY_PUBLISH_STREAM_ID, mPublishStreamID);
        PreferenceUtil.getInstance().setBooleanValue(KEY_IS_PUBLISHING, mIsPublishing);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_FRONT_CAM, mEnableFrontCam);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_TORCH, mEnableTorch);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_SPEAKER, mEnableSpeaker);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_MIC, mEnableMic);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_CAM, mEnableCam);
        PreferenceUtil.getInstance().setBooleanValue(KEY_HAD_BEEN_LOGINNED, mHadBeenLoginned);
        PreferenceUtil.getInstance().setIntValue(KEY_SELECTED_BEAUTY, mSelectedBeauty);
        PreferenceUtil.getInstance().setIntValue(KEY_SELECTED_FILTER, mSelectedFilter);
        PreferenceUtil.getInstance().setIntValue(KEY_LIVE_COUNT, mLiveCount);

        PreferenceUtil.getInstance().setObjectToString(KEY_MAP_FREE_ZEGO_REMOTE_VIEW_INDEX, mMapFreeZegoRemoteViewIndex);

        // 获取每个播放View上的流信息,用于恢复播放
        mListOrdinalAndStreamID = new ArrayList<>();
        for (int i = 0, size = mListViewParent.size(); i < size; i++) {
            mListOrdinalAndStreamID.add((String) mListViewParent.get(i).getChildAt(0).getTag());
        }
        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_ORDINAL_AND_STREAM_ID, mListOrdinalAndStreamID);

        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LOG, mListLog);

        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_MAGIC_NUM, mListMagicNum);
    }

    /**
     * 初始化屏幕旋转监听器.
     */
    protected void initRotationListener() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDisplayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                    Log.i("Display", "Display #" + displayId + " added.");
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    // Log.i("Display", "Display #" + displayId + " changed.");
                    changeRotation();
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    Log.i("Display", "Display #" + displayId + " removed.");
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
    protected void restorePublishAndPlay() {
        for (int i = 0, size = mListOrdinalAndStreamID.size(); i < size; i++) {
            int playStreamOrdinal = getPlayStreamOrdinalFromTag(mListOrdinalAndStreamID.get(i));
            String playStreamID = getPlayStreamIDFromTag(mListOrdinalAndStreamID.get(i));
            switch (playStreamOrdinal) {
                case 0:
                case 1:
                case 2:
                    startPlay(playStreamID);
                    break;
                case BIG_VIDEO_ORDINAL:
                    startPublish();
                    break;
            }
        }
    }

    /**
     * 获取空闲的View用于播放或者发布.
     *
     * @return
     */
    protected RelativeLayout getFreeView() {
        RelativeLayout rlytFreeView = null;
        for (int i = 0, size = mListVideoView.size(); i < size; i++) {
            RelativeLayout rlytView = mListVideoView.get(i);
            if (getPlayStreamOrdinalFromTag((String) rlytView.getTag()) == FREE_VIEW_ORDINAL) {
                rlytFreeView = rlytView;
                getSurfaceView(rlytView).setVisibility(View.VISIBLE);
                break;
            }
        }
        return rlytFreeView;
    }

    /**
     * 释放View用于再次播放.
     *
     * @param streamID
     */
    protected void releaseViewAndRemoteViewIndex(String streamID) {
        for (int i = 0, size = mListVideoView.size(); i < size; i++) {
            RelativeLayout rlytView = mListVideoView.get(i);
            String tag = (String) rlytView.getTag();
            if (getPlayStreamIDFromTag(tag).equals(streamID)) {
                // 设置View可用
                rlytView.setTag(TAG_VIEW_IS_FREE);
                getSurfaceView(rlytView).setVisibility(View.GONE);
                break;
            }
        }

        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeZegoRemoteViewIndex.keySet()) {
            if (mMapFreeZegoRemoteViewIndex.get(index).equals(streamID)) {
                // 设置remoteViewIndex可用
                mMapFreeZegoRemoteViewIndex.put(index, NONE_STREAM_ID);
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

                    if(!mHadBeenLoginned){
                        mHadBeenLoginned = true;
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
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                mIsPublishing = false;
                // 停止预览
                mZegoAVKit.stopPreview();
                // 释放View
                releaseViewAndRemoteViewIndex(streamID);
                recordLog(MY_SELF + ": publish stream(" + streamID + ") stop --errCode:" + retCode);
                setPublishControlText();
                mBizLiveRoom.reportStreamState(false, streamID, PreferenceUtil.getInstance().getUserID());
            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                recordLog(MY_SELF + ": play stream(" + streamID + ") success");
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                recordLog(MY_SELF + ": play stream(" + streamID + ") stop --errCode:" + retCode);
                // 释放View
                releaseViewAndRemoteViewIndex(streamID);
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
                        Log.i("TestPhoneState", "挂断");
                        if (mHaveBeenCalled) {
                            mHaveBeenCalled = false;
                            // 登陆频道
                            ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
                            mZegoAVKit.loginChannel(zegoUser, mChannel);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.i("TestPhoneState", "响铃:来电号码" + incomingNumber);
                        if (!mHaveBeenCalled) {
                            mHaveBeenCalled = true;
                        }
                        // 来电停止发布与播放
                        stopAllStream();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.i("TestPhoneState", "接听");
                        break;
                }
            }
        };

        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 获取SurfaceView.
     *
     * @param viewGroup
     * @return
     */
    protected SurfaceView getSurfaceView(RelativeLayout viewGroup) {
        SurfaceView surfaceView = null;
        if (viewGroup != null) {
            for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
                if (viewGroup.getChildAt(i) instanceof SurfaceView) {
                    surfaceView = (SurfaceView) viewGroup.getChildAt(i);
                    break;
                }
            }
        }
        return surfaceView;
    }

    /**
     * 切换全屏.
     *
     * @param rlytSmallVideoParent
     */
    protected void exchangeViewSize(RelativeLayout rlytSmallVideoParent) {

        RelativeLayout rlytSmallVideo = (RelativeLayout) rlytSmallVideoParent.getChildAt(0);
        RelativeLayout rlytBigVideo = (RelativeLayout) mRlytBigVideoParent.getChildAt(0);

        rlytSmallVideoParent.removeViewAt(0);
        mRlytBigVideoParent.removeViewAt(0);

        rlytSmallVideo.setLayoutParams(mParamsBig);
        rlytBigVideo.setLayoutParams(mParamsSmall);

        rlytSmallVideoParent.addView(rlytBigVideo);
        mRlytBigVideoParent.addView(rlytSmallVideo);
    }


    /**
     * 获取用于播放视频的Viuew的序号.
     *
     * @param playStreamOrdinal
     * @return
     */
    protected ZegoAVKitCommon.ZegoRemoteViewIndex getZegoRemoteViewIndex(int playStreamOrdinal) {
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

    protected ZegoAVKitCommon.ZegoRemoteViewIndex getFreeZegoReoteViewIndex() {
        ZegoAVKitCommon.ZegoRemoteViewIndex freeIndex = null;
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeZegoRemoteViewIndex.keySet()) {
            if (NONE_STREAM_ID.equals(mMapFreeZegoRemoteViewIndex.get(index))) {
                freeIndex = index;
                break;
            }
        }
        return freeIndex;
    }


    protected void recordLog(String msg) {
        mListLog.addFirst(msg);
    }


    /**
     * 开始发布.
     */
    protected void startPublish() {
        if (mIsPublishing) {
            return;
        }

        final RelativeLayout rlytFreeView = getFreeView();
        if (rlytFreeView == null) {
            return;
        }

        // 标记view已经被占用
        rlytFreeView.setTag(BIG_VIDEO_ORDINAL + SEPARATOR + mPublishStreamID);

        // 输出发布状态
        recordLog(MY_SELF + ": start publishing " + mPublishStreamID);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout) rlytFreeView.getParent()).setVisibility(View.VISIBLE);
                ((RelativeLayout) rlytFreeView.getParent()).bringToFront();

            }
        }, 200);

        // 开始播放
        mZegoAVKit.setLocalView(getSurfaceView(rlytFreeView));
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
    protected void startPlay(String streamID) {

        final RelativeLayout rlytFreeView = getFreeView();
        ZegoAVKitCommon.ZegoRemoteViewIndex freeRemoteViewIndex = getFreeZegoReoteViewIndex();

        if (rlytFreeView == null || freeRemoteViewIndex == null) {
            return;
        }

        // 标记remoteViewIndex已经被占用
        mMapFreeZegoRemoteViewIndex.put(freeRemoteViewIndex, streamID);

        // 标记view已经被占用
        rlytFreeView.setTag(freeRemoteViewIndex.code + SEPARATOR + streamID);

        // 输出播放状态
        recordLog(MY_SELF + ": start playing " + streamID);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout) rlytFreeView.getParent()).setVisibility(View.VISIBLE);
                ((RelativeLayout) rlytFreeView.getParent()).bringToFront();
            }
        }, 500);

        // 播放
        mZegoAVKit.setRemoteViewMode(freeRemoteViewIndex, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setRemoteView(freeRemoteViewIndex, getSurfaceView(rlytFreeView));
        mZegoAVKit.startPlayStream(streamID, freeRemoteViewIndex);
    }

    protected void stopPlay(String streamID) {
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeZegoRemoteViewIndex.keySet()) {
            if (mMapFreeZegoRemoteViewIndex.get(index).equals(streamID)) {
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
                break;
            case Surface.ROTATION_90:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90);
                break;
            case Surface.ROTATION_180:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_180);
                break;
            case Surface.ROTATION_270:
                mZegoAVKit.setDisplayRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_270);
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

    protected void logout(){
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("确定要退出直播页面吗").setTitle("提示").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                stopAllStream();
                mBizLiveRoom.leaveRoom();

                dialog.dismiss();

                finish();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
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

        for (int i = 0, size = mListVideoView.size(); i < size; i++) {
            int playStreamOrdinal = getPlayStreamOrdinalFromTag((String) mListVideoView.get(i).getTag());
            String playStreamID = getPlayStreamIDFromTag((String) mListVideoView.get(i).getTag());
            switch (playStreamOrdinal) {
                case 0:
                case 1:
                case 2:
                    mZegoAVKit.stopPlayStream(playStreamID);
                    mZegoAVKit.setRemoteView(getZegoRemoteViewIndex(playStreamOrdinal), null);
                    break;
                case BIG_VIDEO_ORDINAL:
                    stopPublish();
                    break;
            }
        }

        mZegoAVKit.logoutChannel();

    }

    protected void setPublishControlState(){
        if(!mIsPublishing){
            if(mLiveCount >= 3){
                tvPublisnControl.setEnabled(false);
            }else {
                tvPublisnControl.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return false;
            }else {
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

    @OnClick(R.id.tv_close)
    public void close(){
        logout();
    }

}
