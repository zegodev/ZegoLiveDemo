package com.zego.livedemo2;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
public class PlayActivity extends AbsShowActivity {

    public static final String KEY_LIVE_CHANNEL = "KEY_LIVE_CHANNEL";

    public static final String KEY_PUBLISH_TITLE = "KEY_PUBLISH_TITLE";

    public static final String KEY_PUBLISH_STREAM_ID = "KEY_PUBLISH_STREAM_ID";

    public static final String KEY_IS_FRONT_CAM_SELECTED = "KEY_IS_FRONT_CAM_SELECTED";

    public static final String KEY_IS_SPEAKER_SELECTED = "KEY_IS_SPEAKER_SELECTED";

    public static final String KEY_IS_MIC_SELECTED = "KEY_IS_MIC_SELECTED";

    public static final String KEY_VIDEO_VIEW_TAKEN_FLAGS = "KEY_VIDEO_VIEW_TAKEN_FLAGS";

    public static final String KEY_PLAY_STREAM_ORDINAL = "KEY_PLAY_STREAM_ORDINAL";

    public static final String KEY_PLAY_STREAM_MAP = "KEY_PLAY_STREAM_MAP";

    public static final int VIDEO_NUM = 3;

    public static final int BIG_VIDEO_TAG = 100;

    private ZegoAVKit mZegoAVKit;

    private RelativeLayout.LayoutParams mParamsSmall;
    private RelativeLayout.LayoutParams mParamsBig;

    private int mVideoViewTakenFlags[][] = {{-1, -1}, {-1, -1}, {-1, -1}};

    private List<RelativeLayout> mVideoViewList = new ArrayList<>();

    private Map<Integer, String> mPlayStreamMap = new HashMap<>();

    private int mPlayStreamOrdinal = 0;

    private RelativeLayout mRlytBigVideoParent;

    private RadioGroup.OnCheckedChangeListener mCheckedChangeListener;

    private Map<String, String> mPlayInfoMap = new HashMap<>();

    @Bind(R.id.tv_channel)
    public TextView tvChannel;

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


    private String mPublishTitle;

    private String mPublishStreamID;

    private String mLiveChannel;

    /**
     * 默认开启前置摄像头.
     */
    private boolean mIsFrontCamSelected = true;

    /**
     * 默认开启听筒.
     */
    private boolean mIsSpeakerSelected = true;

    /**
     * 默认开启麦克.
     */
    private boolean mIsMicSelected = true;

    /**
     * 启动入口.
     *
     * @param activity 源activity
     */
    public static void actionStart(Activity activity, String liveChannel) {
        Intent intent = new Intent(activity, PlayActivity.class);
        intent.putExtra(IntentExtra.LIVE_CHANNEL, liveChannel);
        activity.startActivity(intent);
    }


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_play;
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

            try {
                byte[] bytesMap = Base64.decode(PreferenceUtils.getInstance().getStringValue(KEY_PLAY_STREAM_MAP, ""), Base64.DEFAULT);
                ByteArrayInputStream baisMap = new ByteArrayInputStream(bytesMap);
                ObjectInputStream oisMap = new ObjectInputStream(baisMap);
                mPlayStreamMap = (HashMap) oisMap.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                byte[] bytesArray = Base64.decode(PreferenceUtils.getInstance().getStringValue(KEY_VIDEO_VIEW_TAKEN_FLAGS, ""), Base64.DEFAULT);
                ByteArrayInputStream baisArray = new ByteArrayInputStream(bytesArray);
                ObjectInputStream oisArray = new ObjectInputStream(baisArray);
                mVideoViewTakenFlags = (int[][]) oisArray.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    @Override
    protected void initVariables(final Bundle savedInstanceState) {
        mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Integer playStreamOrdinal = (Integer) group.getTag();

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

        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();
        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onLoginChannel(String liveChannel, int retCode) {
                if (retCode == 0) {
                    tvChannel.setText("Ch:" + liveChannel);
                    // 开始播放
                    for (int i = 0; i < VIDEO_NUM; i++) {
                        if (mVideoViewTakenFlags[i][0] == -1) {
                            newDialog(mVideoViewList.get(i), i);
                            break;
                        }
                    }
                } else {
                    tvChannel.setText("Ch:error-" + retCode);
                }
            }

            @Override
            public void onPublishSucc(String streamID, String mixStreamID, String liveChannel, String playUrl) {
                mPlayInfoMap.put(mPublishStreamID, "Publish success: " + mPublishStreamID);
                showMessage();
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                mZegoAVKit.stopPreview();
                mPlayInfoMap.put(mPublishStreamID, "Publish stop: " + mPublishStreamID + "  --errCode:" + retCode);
                showMessage();
            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                mPlayInfoMap.put(streamID, "Play success: " + streamID);
                showMessage();
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                mPlayInfoMap.put(streamID, "Play stop: " + streamID + "  --errCode:" + retCode);
                showMessage();
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


    @Override
    protected void initViews(Bundle savedInstanceState) {

        mRlytBigVideoParent = (RelativeLayout) findViewById(R.id.rlyt_big_video_parent);
        RelativeLayout rlytBigVideo = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_small, null);
        mRlytBigVideoParent.addView(rlytBigVideo);
        mVideoViewList.add(rlytBigVideo);
        mParamsBig = (RelativeLayout.LayoutParams) rlytBigVideo.getLayoutParams();
        getRadioGroup(rlytBigVideo).setOnCheckedChangeListener(mCheckedChangeListener);


        //两个SurfaceView重叠时, 其中一个无法显示视频
        final RelativeLayout rlytSmallVideoParent1;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rlytSmallVideoParent1 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent1);
            findViewById(R.id.rlyt_small_video_parent2).setVisibility(View.GONE);
        } else {
            rlytSmallVideoParent1 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent2);
            findViewById(R.id.rlyt_small_video_parent1).setVisibility(View.GONE);
        }
        RelativeLayout rlytSmallVideo1 = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_small, null);
        mVideoViewList.add(rlytSmallVideo1);
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


        //两个SurfaceView重叠时, 其中一个无法显示视频
        final RelativeLayout rlytSmallVideoParent2;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rlytSmallVideoParent2 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent3);
            findViewById(R.id.rlyt_small_video_parent4).setVisibility(View.GONE);
        } else {
            rlytSmallVideoParent2 = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent4);
            findViewById(R.id.rlyt_small_video_parent3).setVisibility(View.GONE);
        }
        RelativeLayout rlytSmallVideo2 = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_small, null);
        mVideoViewList.add(rlytSmallVideo2);
        rlytSmallVideoParent2.addView(rlytSmallVideo2);
        getRadioGroup(rlytSmallVideo2).setVisibility(View.INVISIBLE);
        getRadioGroup(rlytSmallVideo2).setOnCheckedChangeListener(mCheckedChangeListener);
        rlytSmallVideoParent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeViewSize((RelativeLayout) v);
            }
        });


        // 设置听筒状态
        ibtnSpeaker.setSelected(mIsSpeakerSelected);
        mZegoAVKit.enableSpeaker(mIsSpeakerSelected);

        if (savedInstanceState != null) {
            tvChannel.setText("Ch:" + mLiveChannel);
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean remainFreeVideo = false;
                for (int i = 0; i < VIDEO_NUM; i++) {
                    if (mVideoViewTakenFlags[i][0] == -1) {
                        remainFreeVideo = true;
                        newDialog(mVideoViewList.get(i), i);
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
                for (int i = 0; i < VIDEO_NUM; i++) {
                    if (mVideoViewTakenFlags[i][0] == -1) {
                        // 记录流标识以及用于播放的View
                        mVideoViewTakenFlags[i][0] = BIG_VIDEO_TAG;
                        mVideoViewTakenFlags[i][1] = i;
                        startPublish(mVideoViewList.get(i));
                        getRadioGroup(mVideoViewList.get(i)).setTag(BIG_VIDEO_TAG);
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void loadData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // 登陆频道
            ZegoUser zegoUser = new ZegoUser(PreferenceUtils.getInstance().getUserID(), PreferenceUtils.getInstance().getUserName());
            mZegoAVKit.loginChannel(zegoUser, mLiveChannel);
        } else {

            for (int i = 0; i < VIDEO_NUM; i++) {
                getRadioGroup(mVideoViewList.get(i)).setTag(mVideoViewTakenFlags[i][0]);
                switch (mVideoViewTakenFlags[i][0]) {
                    case 0:
                        startPlay(mVideoViewList.get(mVideoViewTakenFlags[i][1]), ZegoAVKitCommon.ZegoRemoteViewIndex.First, mPlayStreamMap.get(mVideoViewTakenFlags[i][0]));
                        break;
                    case 1:
                        startPlay(mVideoViewList.get(mVideoViewTakenFlags[i][1]), ZegoAVKitCommon.ZegoRemoteViewIndex.Second, mPlayStreamMap.get(mVideoViewTakenFlags[i][0]));
                        break;
                    case 2:
                        startPlay(mVideoViewList.get(mVideoViewTakenFlags[i][1]), ZegoAVKitCommon.ZegoRemoteViewIndex.Third, mPlayStreamMap.get(mVideoViewTakenFlags[i][0]));
                        break;
                    case BIG_VIDEO_TAG:
                        startPublish(mVideoViewList.get(mVideoViewTakenFlags[i][1]));
                        break;
                }


            }
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

        //将map转换为byte[]
        ByteArrayOutputStream toByteMap = new ByteArrayOutputStream();
        ObjectOutputStream oosMap;
        try {
            oosMap = new ObjectOutputStream(toByteMap);
            oosMap.writeObject(mPlayStreamMap);
            String playStreamMap = new String(Base64.encodeToString(toByteMap.toByteArray(), Base64.DEFAULT));
            PreferenceUtils.getInstance().setStringValue(KEY_PLAY_STREAM_MAP, playStreamMap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //将array转换为byte[]
        ByteArrayOutputStream toByteArray = new ByteArrayOutputStream();
        ObjectOutputStream oosArray;
        try {
            oosArray = new ObjectOutputStream(toByteArray);
            oosArray.writeObject(mVideoViewTakenFlags);
            String videoViewTakenFlags = new String(Base64.encodeToString(toByteArray.toByteArray(), Base64.DEFAULT));
            PreferenceUtils.getInstance().setStringValue(KEY_VIDEO_VIEW_TAKEN_FLAGS, videoViewTakenFlags);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 获取SurfaceView.
     *
     * @param viewGroup
     * @return
     */
    private SurfaceView getSurfaceView(RelativeLayout viewGroup) {
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
    private RadioGroup getRadioGroup(RelativeLayout viewGroup) {
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
    private void exchangeViewSize(RelativeLayout rlytSmallVideoParent) {

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
    private void changeViewMode(int playStreamOrdinal, ZegoAVKitCommon.ZegoVideoViewMode mode) {
        switch (playStreamOrdinal) {
            case 0:
                mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, mode);
                break;
            case 1:
                mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, mode);
                break;
            case 2:
                mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, mode);
                break;
            case BIG_VIDEO_TAG:
                mZegoAVKit.setLocalViewMode(mode);
                break;
        }
    }

    private void newDialog(final RelativeLayout rlytVideoView, final int viewTakenFlagIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入streamID:");
        final EditText etStreamID = new EditText(this);
        builder.setView(etStreamID);
        builder.setPositiveButton("播放", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String streamID = etStreamID.getText().toString().trim();
                if (TextUtils.isEmpty(streamID)) {
                    Toast.makeText(PlayActivity.this, "streamID不能为空!", Toast.LENGTH_SHORT).show();
                } else {

                    if (mPlayStreamOrdinal < VIDEO_NUM) {

                        mVideoViewTakenFlags[viewTakenFlagIndex][0] = mPlayStreamOrdinal;
                        mVideoViewTakenFlags[viewTakenFlagIndex][1] = viewTakenFlagIndex;
                        getRadioGroup(rlytVideoView).setTag(mPlayStreamOrdinal);
                        mPlayStreamMap.put(mPlayStreamOrdinal, streamID);

                        switch (mPlayStreamOrdinal) {
                            case 0:
                                startPlay(rlytVideoView, ZegoAVKitCommon.ZegoRemoteViewIndex.First, streamID);
                                break;
                            case 1:
                                startPlay(rlytVideoView, ZegoAVKitCommon.ZegoRemoteViewIndex.Second, streamID);
                                break;
                            case 2:
                                startPlay(rlytVideoView, ZegoAVKitCommon.ZegoRemoteViewIndex.Third, streamID);
                                break;
                        }
                        mPlayStreamOrdinal++;
                    }
                }


            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showMessage(){
        StringBuilder sb = new StringBuilder();
        for(String info : mPlayInfoMap.values()){
            sb.append(info);
            sb.append("\n");
        }

        tvPlayInfo.setText(sb.toString());
    }


    /**
     * 开始发布.
     */
    private void startPublish(final RelativeLayout rlytVideo) {

        mPlayInfoMap.put(mPublishStreamID, "Publish starting: " + mPublishStreamID);
        showMessage();

        ibtnFrontCam.setSelected(mIsFrontCamSelected);
        mZegoAVKit.setFrontCam(mIsFrontCamSelected);

        ibtnMic.setSelected(mIsMicSelected);
        mZegoAVKit.enableMic(mIsMicSelected);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout) rlytVideo.getParent()).setVisibility(View.VISIBLE);
                ((RelativeLayout) rlytVideo.getParent()).bringToFront();
            }
        }, 500);
        mZegoAVKit.setLocalView(getSurfaceView(rlytVideo));
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
        mZegoAVKit.startPreview();
        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);

        changeRotation();
    }

    /**
     * 开始播放流.
     */
    private void startPlay(final RelativeLayout rlytVideo, ZegoAVKitCommon.ZegoRemoteViewIndex zegoRemoteViewIndex, String streamID) {

        mPlayInfoMap.put(streamID, "Play starting: " + streamID);
        showMessage();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((RelativeLayout) rlytVideo.getParent()).setVisibility(View.VISIBLE);
                ((RelativeLayout) rlytVideo.getParent()).bringToFront();
            }
        }, 1000);
        mZegoAVKit.setRemoteViewMode(zegoRemoteViewIndex, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
        mZegoAVKit.setRemoteView(zegoRemoteViewIndex, getSurfaceView(rlytVideo));
        mZegoAVKit.startPlayStream(streamID, zegoRemoteViewIndex);

        changeRotation();
    }

    private void changeRotation() {
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

    /**
     * 退出.
     */
    private void logout() {

        for (int i = 0; i < VIDEO_NUM; i++) {
            switch (mVideoViewTakenFlags[i][0]) {
                case 0:
                    mZegoAVKit.stopPlayStream(mPlayStreamMap.get(i));
                    mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, null);
                    break;
                case 1:
                    mZegoAVKit.stopPlayStream(mPlayStreamMap.get(i));
                    mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, null);
                    break;
                case 2:
                    mZegoAVKit.stopPlayStream(mPlayStreamMap.get(i));
                    mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, null);
                    break;
                case BIG_VIDEO_TAG:
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
