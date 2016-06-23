package com.zego.livedemo2;


import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.livedemo2.base.AbsShowActivity;
import com.zego.livedemo2.constants.IntentExtra;
import com.zego.livedemo2.utils.PreferenceUtils;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * des: 主页面
 */
public abstract class BaseShowActivity extends AbsShowActivity {

    public static final String KEY_LIVE_CHANNEL = "KEY_LIVE_CHANNEL";

    public static final String KEY_PUBLISH_TITLE = "KEY_PUBLISH_TITLE";

    public static final String KEY_PUBLISH_STREAM_ID = "KEY_PUBLISH_STREAM_ID";

    public static final String KEY_IS_FRONT_CAM_SELECTED = "KEY_IS_FRONT_CAM_SELECTED";

    public static final String KEY_IS_SPEAKER_SELECTED = "KEY_IS_SPEAKER_SELECTED";

    public static final String KEY_IS_MIC_SELECTED = "KEY_IS_MIC_SELECTED";

    public static final String KEY_PLAY_STREAM_ORDINAL = "KEY_PLAY_STREAM_ORDINAL";

    public static final String KEY_LIST_STREAM_AND_ORDINAL = "KEY_LIST_STREAM_AND_ORDINAL";

    public static final String KEY_MAP_PLAY_INFO = "KEY_MAP_PLAY_INFO";

    public static final String KEY_SELECTED_BEAUTY = "KEY_SELECTED_BEAUTY";

    public static final String KEY_SELECTED_FILTER = "KEY_SELECTED_FILTER";

    public static final int BIG_VIDEO_ORDINAL = 100;

    public static final int TAG_VIEW_IS_FREE = -1;

    public static final String NONE_STREAM = "NONE";

    public static final String SEPARATOR = "_";

    protected ZegoAVKit mZegoAVKit;

    protected RelativeLayout.LayoutParams mParamsSmall;
    protected RelativeLayout.LayoutParams mParamsBig;

    protected List<RelativeLayout> mListVideoView = new ArrayList<>();
    protected List<RelativeLayout> mListViewParent = new ArrayList<>();
    protected Map<String, String> mMapPlayInfo = new HashMap<>();
    protected List<String>  mListSreamAndOrdinal = new ArrayList<>();

    protected int mPlayStreamOrdinal = 0;

    protected RelativeLayout mRlytBigVideoParent;

    protected RadioGroup.OnCheckedChangeListener mCheckedChangeListener;


    @Bind(R.id.tv_play_info)
    public TextView tvPlayInfo;

    @Bind(R.id.btn_publish)
    public Button btnPublish;

    @Bind(R.id.btn_play)
    public Button btnPlay;

    @Bind(R.id.ibtn_front_cam)
    public ImageButton ibtnFrontCam;

    @Bind(R.id.ibtn_speaker)
    public ImageButton ibtnSpeaker;

    @Bind(R.id.ibtn_mic)
    public ImageButton ibtnMic;

    @Bind(R.id.sp_filters)
    public Spinner spFilters;

    @Bind(R.id.sp_beauties)
    public Spinner spBeauties;


    protected String mPublishTitle;

    protected String mPublishStreamID;

    protected String mLiveChannel;

    /**
     * 默认未开启前置摄像头.
     */
    protected boolean mIsFrontCamSelected = false;

    /**
     * 默认开启听筒.
     */
    protected boolean mIsSpeakerSelected = true;

    /**
     * 默认未开启麦克.
     */
    protected boolean mIsMicSelected = false;

    protected int mSelectedBeauty = 0;

    protected int mSelectedFilter = 0;

    protected boolean mHaveBeenCalled = false;

    /**
     * 发布与播放前的操作.
     */
    protected abstract void prePublishAndPlay();


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_show;
    }


    @Override
    protected void initExtraData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mPublishTitle = getIntent().getStringExtra(IntentExtra.PUBLISH_TITLE);
            mLiveChannel = getIntent().getStringExtra(IntentExtra.LIVE_CHANNEL);

            // 用userID的后四位做publish流ID
            String userID = PreferenceUtils.getInstance().getUserID();
            mPublishStreamID = userID.substring(userID.length() - 4);
        } else {

            // Activity 后台被回收后重新启动, 恢复数据
            mLiveChannel = PreferenceUtils.getInstance().getStringValue(KEY_LIVE_CHANNEL, null);
            mPublishTitle = PreferenceUtils.getInstance().getStringValue(KEY_PUBLISH_TITLE, null);
            mPublishStreamID = PreferenceUtils.getInstance().getStringValue(KEY_PUBLISH_STREAM_ID, null);
            mIsFrontCamSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_FRONT_CAM_SELECTED, false);
            mIsSpeakerSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_SPEAKER_SELECTED, false);
            mIsMicSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_MIC_SELECTED, false);
            mPlayStreamOrdinal = PreferenceUtils.getInstance().getIntValue(KEY_PLAY_STREAM_ORDINAL, 0);
            mSelectedBeauty = PreferenceUtils.getInstance().getIntValue(KEY_SELECTED_BEAUTY, 0);
            mSelectedFilter = PreferenceUtils.getInstance().getIntValue(KEY_SELECTED_FILTER, 0);

            try {
                byte[] bytes = Base64.decode(PreferenceUtils.getInstance().getStringValue(KEY_LIST_STREAM_AND_ORDINAL, ""), Base64.DEFAULT);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream oisArray = new ObjectInputStream(bais);
                mListSreamAndOrdinal = (List<String>) oisArray.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                byte[] bytes = Base64.decode(PreferenceUtils.getInstance().getStringValue(KEY_MAP_PLAY_INFO, ""), Base64.DEFAULT);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream oisArray = new ObjectInputStream(bais);
                mMapPlayInfo = (Map<String, String>) oisArray.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void initVariables(final Bundle savedInstanceState) {

        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();

        mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RelativeLayout rlytParent = (RelativeLayout) group.getParent();
                Integer playStreamOrdinal = getPlayStreamOrdinalFromTag((String) rlytParent.getTag());

                switch (checkedId) {
                    case R.id.rb_asfit_small:
                        changeViewMode(playStreamOrdinal, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                        break;
                    case R.id.rb_asfill_small:
                        changeViewMode(playStreamOrdinal, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                        break;
                    case R.id.rb_fill_small:
                        changeViewMode(playStreamOrdinal, ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill);
                        break;
                }


            }
        };

        initZegoLiveCallback();
        initPhoneCallingListener();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mRlytBigVideoParent = (RelativeLayout) findViewById(R.id.rlyt_big_video_parent);
        RelativeLayout rlytBigVideo = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_show, null);
        mRlytBigVideoParent.addView(rlytBigVideo);
        mParamsBig = (RelativeLayout.LayoutParams) rlytBigVideo.getLayoutParams();
        getRadioGroup(rlytBigVideo).setOnCheckedChangeListener(mCheckedChangeListener);
        mListViewParent.add(mRlytBigVideoParent);
        mListVideoView.add(rlytBigVideo);
        // 标记View是空闲的
        rlytBigVideo.setTag(TAG_VIEW_IS_FREE + SEPARATOR + NONE_STREAM);


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
        getRadioGroup(rlytSmallVideo1).setVisibility(View.INVISIBLE);
        getRadioGroup(rlytSmallVideo1).setOnCheckedChangeListener(mCheckedChangeListener);
        rlytSmallVideoParent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeViewSize((RelativeLayout) v);
            }
        });
        mListViewParent.add(rlytSmallVideoParent1);
        mListVideoView.add(rlytSmallVideo1);
        // 标记View是空闲的
        rlytSmallVideo1.setTag(TAG_VIEW_IS_FREE + SEPARATOR + NONE_STREAM);


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
        getRadioGroup(rlytSmallVideo2).setVisibility(View.INVISIBLE);
        getRadioGroup(rlytSmallVideo2).setOnCheckedChangeListener(mCheckedChangeListener);
        rlytSmallVideoParent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeViewSize((RelativeLayout) v);
            }
        });
        mListViewParent.add(rlytSmallVideoParent2);
        mListVideoView.add(rlytSmallVideo2);
        // 标记View是空闲的
        rlytSmallVideo2.setTag(TAG_VIEW_IS_FREE + SEPARATOR + NONE_STREAM);


        // 设置听筒状态
        ibtnSpeaker.setSelected(mIsSpeakerSelected);
        mZegoAVKit.enableSpeaker(mIsSpeakerSelected);

        // 设置前置摄像头状态
        ibtnFrontCam.setSelected(mIsFrontCamSelected);
        mZegoAVKit.setFrontCam(mIsFrontCamSelected);

        // 设置麦克风状态
        ibtnMic.setSelected(mIsMicSelected);
        mZegoAVKit.enableMic(mIsMicSelected);


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean remainFreeVideo = false;
                for(int i = 0, size = mListVideoView.size(); i < size; i++){
                    RelativeLayout rlytView = mListVideoView.get(i);
                    if(getPlayStreamOrdinalFromTag((String)rlytView.getTag()) == TAG_VIEW_IS_FREE){
                        remainFreeVideo = true;
                        newDialog(rlytView);
                        break;
                    }
                }

                if (!remainFreeVideo) {
                    btnPlay.setEnabled(false);
                    btnPublish.setEnabled(false);
                }
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPublish.setEnabled(false);
                for(int i = 0, size = mListVideoView.size(); i < size; i++){
                    RelativeLayout rlytView = mListVideoView.get(i);
                    if(getPlayStreamOrdinalFromTag((String)rlytView.getTag()) == TAG_VIEW_IS_FREE){
                        startPublish(rlytView);
                        break;
                    }
                }
            }
        });

        // 美颜跟滤镜
        initBeautiesAndFilters();

        initRotationListener();
    }



    @Override
    protected void loadData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            showPlayInfo(mLiveChannel, "Login starting:" + mLiveChannel);
            // 登陆频道
            ZegoUser zegoUser = new ZegoUser(PreferenceUtils.getInstance().getUserID(), PreferenceUtils.getInstance().getUserName());
            mZegoAVKit.loginChannel(zegoUser, mLiveChannel);
        } else {
            // 恢复发布 播放
            restorePublishAndPlay();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // 保存数据, 用于Activity在后台被回收后重新恢复
        PreferenceUtils.getInstance().setStringValue(KEY_LIVE_CHANNEL, mLiveChannel);
        PreferenceUtils.getInstance().setStringValue(KEY_PUBLISH_TITLE, mPublishTitle);
        PreferenceUtils.getInstance().setStringValue(KEY_PUBLISH_STREAM_ID, mPublishStreamID);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_FRONT_CAM_SELECTED, mIsFrontCamSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_SPEAKER_SELECTED, mIsSpeakerSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_MIC_SELECTED, mIsMicSelected);
        PreferenceUtils.getInstance().setIntValue(KEY_PLAY_STREAM_ORDINAL, mPlayStreamOrdinal);
        PreferenceUtils.getInstance().setIntValue(KEY_SELECTED_BEAUTY, mSelectedBeauty);
        PreferenceUtils.getInstance().setIntValue(KEY_SELECTED_FILTER, mSelectedFilter);

        try {
            // 获取每个播放View上的流信息,用于恢复播放
            mListSreamAndOrdinal = new ArrayList<>();
            for(int i = 0, size = mListViewParent.size(); i < size; i++){
                mListSreamAndOrdinal.add((String) mListViewParent.get(i).getChildAt(0).getTag());
            }
            //将list转换为byte[]存储到SharePreference
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(mListSreamAndOrdinal);
            String videoViewTakenFlags = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
            PreferenceUtils.getInstance().setStringValue(KEY_LIST_STREAM_AND_ORDINAL, videoViewTakenFlags);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            //将map转换为byte[]存储到SharePreference
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(mMapPlayInfo);
            String videoViewTakenFlags = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
            PreferenceUtils.getInstance().setStringValue(KEY_MAP_PLAY_INFO, videoViewTakenFlags);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化美颜与滤镜.
     */
    private void initBeautiesAndFilters(){
        ArrayAdapter<String> beautyAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, mResources.getStringArray(R.array.beauties));
        spBeauties.setAdapter(beautyAdapter);
        spBeauties.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int beauty = 0;
                switch (position) {
                    case 0:
                        beauty = ZegoAVKitCommon.ZegoBeauty.NONE;
                        break;
                    case 1:
                        beauty = ZegoAVKitCommon.ZegoBeauty.POLISH;
                        break;
                    case 2:
                        beauty = ZegoAVKitCommon.ZegoBeauty.WHITEN;
                        break;
                    case 3:
                        beauty = ZegoAVKitCommon.ZegoBeauty.POLISH | ZegoAVKitCommon.ZegoBeauty.WHITEN;
                        break;
                    case 4:
                        beauty = ZegoAVKitCommon.ZegoBeauty.POLISH | ZegoAVKitCommon.ZegoBeauty.SKIN_WHITEN;
                        break;
                }
                mSelectedBeauty = beauty;
                mZegoAVKit.enableBeautifying(beauty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, mResources.getStringArray(R.array.filters));
        spFilters.setAdapter(filterAdapter);
        spFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedFilter = position;
                mZegoAVKit.setFilter(getZegoFilter(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    protected ZegoAVKitCommon.ZegoFilter getZegoFilter(int index){
        ZegoAVKitCommon.ZegoFilter filter = ZegoAVKitCommon.ZegoFilter.None;
        switch (index) {
            case 0:
                filter = ZegoAVKitCommon.ZegoFilter.None;
                break;
            case 1:
                filter = ZegoAVKitCommon.ZegoFilter.Lomo;
                break;
            case 2:
                filter = ZegoAVKitCommon.ZegoFilter.BlackWhite;
                break;
            case 3:
                filter = ZegoAVKitCommon.ZegoFilter.OldStyle;
                break;
            case 4:
                filter = ZegoAVKitCommon.ZegoFilter.Gothic;
                break;
            case 5:
                filter = ZegoAVKitCommon.ZegoFilter.SharpColor;
                break;
            case 6:
                filter = ZegoAVKitCommon.ZegoFilter.Fade;
                break;
            case 7:
                filter = ZegoAVKitCommon.ZegoFilter.Wine;
                break;
            case 8:
                filter = ZegoAVKitCommon.ZegoFilter.Lime;
                break;
            case 9:
                filter = ZegoAVKitCommon.ZegoFilter.Romantic;
                break;
            case 10:
                filter = ZegoAVKitCommon.ZegoFilter.Halo;
                break;
            case 11:
                filter = ZegoAVKitCommon.ZegoFilter.Blue;
                break;
            case 12:
                filter = ZegoAVKitCommon.ZegoFilter.Illusion;
                break;
            case 13:
                filter = ZegoAVKitCommon.ZegoFilter.Dark;
                break;
        }

        return filter;
    }

    /**
     * 初始化屏幕旋转监听器.
     */
    protected void initRotationListener(){
        DisplayManager.DisplayListener mDisplayListener;
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
        }else {
            changeRotation();
        }
    }

    /**
     * 恢复发布与播放.
     */
    protected void restorePublishAndPlay(){
        for(int i = 0, size = mListSreamAndOrdinal.size(); i < size; i++){
            int playStreamOrdinal = getPlayStreamOrdinalFromTag(mListSreamAndOrdinal.get(i));
            String playStreamID = getPlayStreamIDFromTag(mListSreamAndOrdinal.get(i));
            switch (playStreamOrdinal){
                case 0:
                case 1:
                case 2:
                    startPlay(mListVideoView.get(i), getZegoRemoteViewIndex(playStreamOrdinal), playStreamID);
                    break;
                case BIG_VIDEO_ORDINAL:
                    startPublish(mListVideoView.get(i));
                    break;
            }
        }
    }

    /**
     * 初始化zego sdk回调.
     */
    protected void initZegoLiveCallback(){
        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onLoginChannel(String liveChannel, int retCode) {
                if (retCode == 0) {
                    showPlayInfo(mLiveChannel, "Login success:" + liveChannel);
                    prePublishAndPlay();
                } else {
                    showPlayInfo(mLiveChannel, "Login fail:" + liveChannel + " -errCode:" + retCode);
                }
            }

            @Override
            public void onPublishSucc(String streamID, String liveChannel, HashMap<String, Object> info) {
                showPlayInfo(mPublishStreamID, "Publish success:" + mPublishStreamID);
                Log.i("TestInfo", info.toString());
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                mZegoAVKit.stopPreview();
                showPlayInfo(mPublishStreamID, "Publish stop:" + mPublishStreamID + " -errCode:" + retCode);
            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                showPlayInfo(streamID, "Play success:" + streamID);
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                showPlayInfo(streamID, "Play stop:" + streamID + " -errCode:" + retCode);
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
    protected void initPhoneCallingListener(){

        final TelephonyManager tm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch(state){
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.i("TestPhoneState", "挂断");
                        if(mHaveBeenCalled){
                            mHaveBeenCalled = false;
                            // 登陆频道
                            ZegoUser zegoUser = new ZegoUser(PreferenceUtils.getInstance().getUserID(), PreferenceUtils.getInstance().getUserName());
                            mZegoAVKit.loginChannel(zegoUser, mLiveChannel);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.i("TestPhoneState", "响铃:来电号码"+incomingNumber);
                        if(!mHaveBeenCalled){
                            mHaveBeenCalled = true;
                        }
                        // 来电停止发布与播放
                        logout();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.i("TestPhoneState", "接听");
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
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
     * 获取viewGroup.
     *
     * @param viewGroup
     * @return
     */
    protected RadioGroup getRadioGroup(RelativeLayout viewGroup) {
        RadioGroup radioGroup = null;
        if (viewGroup != null) {
            for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
                if (viewGroup.getChildAt(i) instanceof RadioGroup) {
                    radioGroup = (RadioGroup) viewGroup.getChildAt(i);
                    break;
                }
            }
        }
        return radioGroup;
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

        getRadioGroup(rlytBigVideo).setVisibility(View.INVISIBLE);
        getRadioGroup(rlytSmallVideo).setVisibility(View.VISIBLE);
    }


    /**
     * 改变view的模式.
     *
     * @param playStreamOrdinal
     * @param mode
     */
    protected void changeViewMode(int playStreamOrdinal, ZegoAVKitCommon.ZegoVideoViewMode mode) {
        switch (playStreamOrdinal) {
            case 0:
            case 1:
            case 2:
                mZegoAVKit.setRemoteViewMode(getZegoRemoteViewIndex(playStreamOrdinal), mode);
                break;
            case BIG_VIDEO_ORDINAL:
                mZegoAVKit.setLocalViewMode(mode);
                break;
        }
    }

    /**
     * 获取用于播放视频的Viuew的序号.
     *
     * @param playStreamOrdinal
     * @return
     */
    protected ZegoAVKitCommon.ZegoRemoteViewIndex getZegoRemoteViewIndex(int playStreamOrdinal){
        ZegoAVKitCommon.ZegoRemoteViewIndex index = null;

        switch (playStreamOrdinal){
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

    protected void newDialog(final RelativeLayout rlytVideoView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入streamID:");
        final EditText etStreamID = new EditText(this);
        builder.setView(etStreamID);
        builder.setPositiveButton("播放", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String streamID = etStreamID.getText().toString().trim();
                if (TextUtils.isEmpty(streamID)) {
                    Toast.makeText(BaseShowActivity.this, "streamID不能为空!", Toast.LENGTH_SHORT).show();
                } else {
                    ZegoAVKitCommon.ZegoRemoteViewIndex zegoRemoteViewIndex = getZegoRemoteViewIndex(mPlayStreamOrdinal);
                    if(zegoRemoteViewIndex != null){
                        mPlayStreamOrdinal++;
                        startPlay(rlytVideoView, zegoRemoteViewIndex, streamID);
                    }
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 打印状态信息.
     * @param key
     * @param value
     */
    protected void showPlayInfo(String key, String value){

        mMapPlayInfo.put(key, value);

        StringBuilder sb = new StringBuilder();
        for(String info : mMapPlayInfo.values()){
            sb.append(info);
            sb.append("\n");
        }
        tvPlayInfo.setText(sb.toString());
    }


    /**
     * 开始发布.
     */
    protected void startPublish(final RelativeLayout rlytVideo) {
        // 标记view已经被占用
        rlytVideo.setTag(BIG_VIDEO_ORDINAL + SEPARATOR + mPublishStreamID);

        // 输出发布状态
        showPlayInfo(mPublishStreamID, "Publish starting:" + mPublishStreamID);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(mSelectedBeauty);
        mZegoAVKit.setFilter(getZegoFilter(mSelectedFilter));

        // 开启前置摄像头
        mIsFrontCamSelected = true;
        ibtnFrontCam.setSelected(true);
        mZegoAVKit.setFrontCam(true);

        // 打开麦克风
        mIsFrontCamSelected = true;
        ibtnMic.setSelected(true);
        mZegoAVKit.enableMic(true);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout) rlytVideo.getParent()).setVisibility(View.VISIBLE);
                ((RelativeLayout) rlytVideo.getParent()).bringToFront();

            }
        }, 200);

        // 开始播放
        mZegoAVKit.setLocalView(getSurfaceView(rlytVideo));
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
        mZegoAVKit.startPreview();
        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);

    }

    /**
     * 开始播放流.
     */
    protected void startPlay(final RelativeLayout rlytVideo, ZegoAVKitCommon.ZegoRemoteViewIndex zegoRemoteViewIndex, String streamID) {
        // 标记view已经被占用
        rlytVideo.setTag(zegoRemoteViewIndex.code + SEPARATOR + streamID);

        // 输出播放状态
        showPlayInfo(streamID, "Play starting:" + streamID);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout) rlytVideo.getParent()).setVisibility(View.VISIBLE);
                ((RelativeLayout) rlytVideo.getParent()).bringToFront();
            }
        }, 500);

        // 播放
        mZegoAVKit.setRemoteViewMode(zegoRemoteViewIndex, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
        mZegoAVKit.setRemoteView(zegoRemoteViewIndex, getSurfaceView(rlytVideo));
        mZegoAVKit.startPlayStream(streamID, zegoRemoteViewIndex);
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

    protected int getPlayStreamOrdinalFromTag(String tag){
        int ordinal = TAG_VIEW_IS_FREE;

        if(tag != null){
            String []arr = tag.split(SEPARATOR);
            if(arr != null){
                ordinal = Integer.valueOf(arr[0]);
            }
        }

        return ordinal;
    }

    protected String getPlayStreamIDFromTag(String tag){
        String playStreamID = null;

        if(tag != null){
            String []arr = tag.split(SEPARATOR);
            if(arr != null){
                playStreamID = arr[1];
            }
        }

        return playStreamID;
    }

    /**
     * 退出.
     */
    protected void logout() {
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
                    mZegoAVKit.stopPreview();
                    mZegoAVKit.stopPublish();
                    mZegoAVKit.setLocalView(null);
                    break;
            }
        }

        mZegoAVKit.logoutChannel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 退出
            logout();
        }

        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.ibtn_front_cam, R.id.ibtn_speaker, R.id.ibtn_mic})
    public void onClick(View v) {

        ImageButton ibtn = (ImageButton) v;
        if (ibtn.isSelected()) {
            ibtn.setSelected(false);
        } else {
            ibtn.setSelected(true);
        }
        switch (v.getId()) {
            case R.id.ibtn_front_cam:
                if (ibtn.isSelected()) {
                    //开启前置摄像头
                    mZegoAVKit.setFrontCam(true);
                    mIsFrontCamSelected = true;
                } else {
                    //关闭前置摄像头
                    mZegoAVKit.setFrontCam(false);
                    mIsFrontCamSelected = false;
                }
                break;
            case R.id.ibtn_speaker:
                if (ibtn.isSelected()) {
                    //开启听筒
                    mZegoAVKit.enableSpeaker(true);
                    mIsSpeakerSelected = true;
                } else {
                    //关闭听筒
                    mZegoAVKit.enableSpeaker(false);
                    mIsSpeakerSelected = false;
                }
                break;
            case R.id.ibtn_mic:
                if (ibtn.isSelected()) {
                    //开启麦克
                    mZegoAVKit.enableMic(true);
                    mIsMicSelected = true;
                } else {
                    //关闭麦克
                    mZegoAVKit.enableMic(false);
                    mIsMicSelected = false;
                }
                break;
        }
    }
}
