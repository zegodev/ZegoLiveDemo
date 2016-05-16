package com.zego.livedemo2;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.livedemo2.base.AbsShowActivity;
import com.zego.livedemo2.constants.IntentExtra;
import com.zego.livedemo2.listener.OnWiredHeadsetChangeListener;
import com.zego.livedemo2.receiver.WiredHeadsetChangeReceiver;
import com.zego.livedemo2.utils.PreferenceUtils;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.ZegoConstants;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;

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

    public static final String KEY_PLAY_STREAM_ID1 = "KEY_PLAY_STREAM_ID1";

    public static final String KEY_PLAY_STREAM_ID2 = "KEY_PLAY_STREAM_ID2";

    public static final String KEY_IS_PUBLISHING = "KEY_IS_PUBLISHING";

    public static final String KEY_IS_FRONT_CAM_SELECTED = "KEY_IS_FRONT_CAM_SELECTED";

    public static final String KEY_IS_SPEAKER_SELECTED = "KEY_IS_SPEAKER_SELECTED";

    public static final String KEY_IS_MIC_SELECTED = "KEY_IS_MIC_SELECTED";

    public static final String KEY_IS_LOCAL_VIEW_TAKEN = "KEY_IS_LOCAL_VIEW_TAKEN";

    public static final String KEY_IS_REMOTE_VIEW_TAKEN = "KEY_IS_REMOTE_VIEW_TAKEN";


    private ZegoAVKit mZegoAVKit;

    private RelativeLayout.LayoutParams mParamsSmall;
    private RelativeLayout.LayoutParams mParamsBig;

    public RelativeLayout rlytSmallVideoParent;

    public ViewGroup viewSmall;

    private SurfaceView svSmall;

    public RadioGroup rgSmallViewMode;

    @Bind(R.id.rlyt_big_video_parent)
    public RelativeLayout rlytBigVideoParent;

    public ViewGroup viewBig;

    public SurfaceView svBig;

    public RadioGroup rgBigViewMode;

    @Bind(R.id.tv_channel)
    public TextView tvChannel;

    @Bind(R.id.btn_publish)
    public Button btnPublish;

    @Bind(R.id.btn_play)
    public Button btnPlay;

    @Bind(R.id.llyt_publish_state)
    public LinearLayout llytPublishState;

    @Bind(R.id.llyt_play_sate1)
    public LinearLayout llytPlayState1;

    @Bind(R.id.llyt_play_sate2)
    public LinearLayout llytPlayState2;


    @Bind(R.id.ibtn_front_cam)
    public ImageButton ibtnFrontCam;

    @Bind(R.id.ibtn_speaker)
    public ImageButton ibtnSpeaker;

    @Bind(R.id.ibtn_mic)
    public ImageButton ibtnMic;

    public EditText etStreamID;

    private boolean mSmallVideoIsSmall = true;

    private boolean mIsPublishing = false;

    private String mPublishTitle;

    private String mPublishStreamID;

    private String mPlayStreamID1;

    private String mPlayStreamID2;

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

    private boolean mIsSmallViewTaken = false;

    private boolean mIsBigViewTaken = false;

    /**
     * 耳机插拔事件的广播接收器.
     */
    private WiredHeadsetChangeReceiver mWiredHeadsetChangeReceiver;

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
            String userID = ZegoApiManager.getInstance().getZegoUser().getUserId();
            mPublishStreamID = userID.substring(userID.length() - 4);
        } else {

            // Activity 后台被回收后重新启动, 恢复数据
            mLiveChannel = PreferenceUtils.getInstance().getStringValue(KEY_LIVE_CHANNEL);
            mPublishTitle = PreferenceUtils.getInstance().getStringValue(KEY_PUBLISH_TITLE);
            mPublishStreamID = PreferenceUtils.getInstance().getStringValue(KEY_PUBLISH_STREAM_ID);
            mPlayStreamID1 = PreferenceUtils.getInstance().getStringValue(KEY_PLAY_STREAM_ID1);
            mPlayStreamID2 = PreferenceUtils.getInstance().getStringValue(KEY_PLAY_STREAM_ID2);
            mIsPublishing = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_PUBLISHING);
            mIsFrontCamSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_FRONT_CAM_SELECTED);
            mIsSpeakerSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_SPEAKER_SELECTED);
            mIsMicSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_MIC_SELECTED);
            mIsSmallViewTaken = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_LOCAL_VIEW_TAKEN);
            mIsBigViewTaken = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_REMOTE_VIEW_TAKEN);

        }

    }

    @Override
    protected void initVariables(final Bundle savedInstanceState) {

        mWiredHeadsetChangeReceiver = new WiredHeadsetChangeReceiver(new OnWiredHeadsetChangeListener() {
            @Override
            public void onWiredHeadsetOn() {
                mZegoAVKit.setBuiltInSpeakerOn(false);
            }

            @Override
            public void onWiredHeadsetOff() {
                mZegoAVKit.setBuiltInSpeakerOn(true);
            }
        });


        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();
        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onPublishSucc(String streamID) {
                mIsPublishing = true;
                ((TextView) llytPublishState.getChildAt(0)).setText("PublishState:onPublishSucc");
                ((TextView) llytPublishState.getChildAt(1)).setText("PublishStream:" + streamID);
            }

            @Override
            public void onPublishStop(ZegoAVKitCommon.ZegoStreamStopFlag stopFlag, String streamID) {
                ((TextView) llytPublishState.getChildAt(0)).setText("PublishState:onPublishStop-" + stopFlag);
                ((TextView) llytPublishState.getChildAt(1)).setText("PublishStream:null");
            }

            @Override
            public void onLoginChannel(boolean isLoginSuccess) {
                if (isLoginSuccess) {
                    tvChannel.setText("Ch: " + mLiveChannel);
                    // 开始播放
                    newDialog();
                } else {
                    tvChannel.setText("Ch: Error");
                }
            }

            @Override
            public void onDisconnected(int i, String s) {
                if(mIsPublishing){
                    mZegoAVKit.stopPreview();
                    mZegoAVKit.stopPublish();
                }
            }

            @Override
            public void onReconnected(String s) {
                if(mIsPublishing){
                    startPublish();
                }
            }

            @Override
            public void onPlaySucc(String streamID) {
                if (TextUtils.equals(mPlayStreamID1, streamID)) {
                    ((TextView) llytPlayState1.getChildAt(0)).setText("PlayState1:onPlaySucc");
                    ((TextView) llytPlayState1.getChildAt(1)).setText("PlayStream1:" + streamID);
                } else if (TextUtils.equals(mPlayStreamID2, streamID)) {
                    ((TextView) llytPlayState2.getChildAt(0)).setText("PlayState2:onPlaySucc");
                    ((TextView) llytPlayState2.getChildAt(1)).setText("PlayStream2:" + streamID);
                }
            }

            @Override
            public void onPlayStop(ZegoAVKitCommon.ZegoStreamStopFlag stopFlag, String streamID) {
                if (TextUtils.equals(mPlayStreamID1, streamID)) {
                    ((TextView) llytPlayState1.getChildAt(0)).setText("PlayState1:onPlayStop-" + stopFlag);
                    ((TextView) llytPlayState1.getChildAt(1)).setText("PlayStream1:null");
                } else if (TextUtils.equals(mPlayStreamID2, streamID)) {
                    ((TextView) llytPlayState2.getChildAt(0)).setText("PlayState2:onPlayStop-" + stopFlag);
                    ((TextView) llytPlayState2.getChildAt(1)).setText("PlayStream2:null");
                }
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
        });

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {


        //两个SurfaceView重叠时, 其中一个无法显示视频
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rlytSmallVideoParent = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent1);
            findViewById(R.id.rlyt_small_video_parent2).setVisibility(View.GONE);
        } else {
            rlytSmallVideoParent = (RelativeLayout) findViewById(R.id.rlyt_small_video_parent2);
            findViewById(R.id.rlyt_small_video_parent1).setVisibility(View.GONE);
        }


        viewSmall = (ViewGroup) getLayoutInflater().inflate(R.layout.view_small, null);
        svSmall = (SurfaceView) viewSmall.findViewById(R.id.sv_small_video);
        rgSmallViewMode = (RadioGroup) viewSmall.findViewById(R.id.rg_small_video_mode);
        rlytSmallVideoParent.addView(viewSmall);
        mParamsSmall = (RelativeLayout.LayoutParams) viewSmall.getLayoutParams();


        viewBig = (ViewGroup) getLayoutInflater().inflate(R.layout.view_big, null);
        rlytBigVideoParent.addView(viewBig);
        svBig = (SurfaceView) viewBig.findViewById(R.id.sv_big_video);
        rgBigViewMode = (RadioGroup) viewBig.findViewById(R.id.rg_big_video_mode);
        mParamsBig = (RelativeLayout.LayoutParams) viewBig.getLayoutParams();


        rlytSmallVideoParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSmallVideoIsSmall) {
                    // 小的变大
                    rlytSmallVideoParent.removeView(viewSmall);
                    viewSmall.setLayoutParams(mParamsBig);

                    rlytBigVideoParent.removeView(viewBig);
                    viewBig.setLayoutParams(mParamsSmall);


                    rlytSmallVideoParent.addView(viewBig);
                    rlytBigVideoParent.addView(viewSmall);
                    mSmallVideoIsSmall = false;
                } else {
                    //  复原
                    rlytSmallVideoParent.removeView(viewBig);
                    viewBig.setLayoutParams(mParamsBig);

                    rlytBigVideoParent.removeView(viewSmall);
                    viewSmall.setLayoutParams(mParamsSmall);


                    rlytSmallVideoParent.addView(viewSmall);
                    rlytBigVideoParent.addView(viewBig);
                    mSmallVideoIsSmall = true;
                }
            }
        });

        // 设置听筒状态
        ibtnSpeaker.setSelected(mIsSpeakerSelected);
        mZegoAVKit.enableSpeaker(mIsSpeakerSelected);

        tvChannel.setText("Ch:" + mLiveChannel);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsBigViewTaken || !mIsSmallViewTaken) {
                    newDialog();
                }
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPublish.setEnabled(false);
                if (!mIsSmallViewTaken) {
                    mIsSmallViewTaken = true;
                    startPublish();
                }
            }
        });

        rgSmallViewMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mIsPublishing){
                    switch (checkedId){
                        case R.id.rb_asfit_small:
                            mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                            break;
                        case R.id.rb_asfill_small:
                            mZegoAVKit.setLocalViewMode( ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                            break;
                        case R.id.rb_fill_small:
                            mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill);
                            break;
                    }
                }else {
                    switch (checkedId){
                        case R.id.rb_asfit_small:
                            mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                            break;
                        case R.id.rb_asfill_small:
                            mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                            break;
                        case R.id.rb_fill_small:
                            mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill);
                            break;
                    }
                }

            }
        });

        rgBigViewMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_asfit_big:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                        break;
                    case R.id.rb_asfill_big:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                        break;
                    case R.id.rb_fill_big:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill);
                        break;
                }
            }
        });


    }

    @Override
    protected void loadData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // 登陆频道
            mZegoAVKit.loginChannel(ZegoApiManager.getInstance().getZegoUser(), mLiveChannel);
        } else {

            // 恢复发布
            if (mIsPublishing) {
                btnPublish.setEnabled(false);
                Map<String, String> publishInfo = mZegoAVKit.getCurrentPublishInfo();
                // 当引擎依旧处于发布状态时, 只需要重新设置view即可
                if (publishInfo != null && !TextUtils.isEmpty(publishInfo.get(ZegoConstants.KEY_STREAM_ID))) {
                    ibtnFrontCam.setSelected(mIsFrontCamSelected);
                    mZegoAVKit.setFrontCam(mIsFrontCamSelected);

                    ibtnMic.setSelected(mIsMicSelected);
                    mZegoAVKit.enableMic(mIsMicSelected);

                    rlytSmallVideoParent.setVisibility(View.VISIBLE);
                    rlytSmallVideoParent.bringToFront();
                    mZegoAVKit.setLocalView(svSmall);
                    mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                    mZegoAVKit.startPreview();
                } else {
                    // 当引擎依停止发布了, 重新发布
                    startPublish();
                }
            }

            // 恢复播放流1
            if (!TextUtils.isEmpty(mPlayStreamID1)) {
                mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, svBig);
            }

            // 恢复播放流2
            if (!mIsPublishing && !TextUtils.isEmpty(mPlayStreamID2)) {
                rlytSmallVideoParent.setVisibility(View.VISIBLE);
                rlytSmallVideoParent.bringToFront();
                mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, svSmall);
            }

            if (mIsSmallViewTaken && mIsBigViewTaken) {
                btnPlay.setEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(mWiredHeadsetChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 保存数据, 用于Activity在后台被回收后重新恢复
        PreferenceUtils.getInstance().setStringValue(KEY_LIVE_CHANNEL, mLiveChannel);
        PreferenceUtils.getInstance().setStringValue(KEY_PUBLISH_TITLE, mPublishTitle);
        PreferenceUtils.getInstance().setStringValue(KEY_PUBLISH_STREAM_ID, mPublishStreamID);
        PreferenceUtils.getInstance().setStringValue(KEY_PLAY_STREAM_ID1, mPlayStreamID1);
        PreferenceUtils.getInstance().setStringValue(KEY_PLAY_STREAM_ID2, mPlayStreamID2);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_PUBLISHING, mIsPublishing);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_FRONT_CAM_SELECTED, mIsFrontCamSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_SPEAKER_SELECTED, mIsSpeakerSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_MIC_SELECTED, mIsMicSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_LOCAL_VIEW_TAKEN, mIsSmallViewTaken);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_REMOTE_VIEW_TAKEN, mIsBigViewTaken);
    }

    @Override
    protected void onDestroy() {
        // 注销广播
        unregisterReceiver(mWiredHeadsetChangeReceiver);
        super.onDestroy();
    }


    private void newDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入streamID:");
        etStreamID = new EditText(this);
        builder.setView(etStreamID);
        builder.setPositiveButton("播放", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!mIsBigViewTaken) {
                    mPlayStreamID1 = etStreamID.getText().toString().trim();
                    if (TextUtils.isEmpty(mPlayStreamID1)) {
                        Toast.makeText(PlayActivity.this, "streamID不能为空!", Toast.LENGTH_SHORT).show();
                    } else {
                        // 开始播放
                        mIsBigViewTaken = true;
                        startPlay(1);
                    }
                } else if (!mIsSmallViewTaken) {
                    mPlayStreamID2 = etStreamID.getText().toString().trim();
                    if (TextUtils.isEmpty(mPlayStreamID2)) {
                        Toast.makeText(PlayActivity.this, "streamID不能为空!", Toast.LENGTH_SHORT).show();
                    } else {
                        // 开始播放
                        mIsSmallViewTaken = true;
                        startPlay(2);
                    }
                }

                if (mIsSmallViewTaken && mIsBigViewTaken) {
                    btnPlay.setEnabled(false);
                }

            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }


    /**
     * 开始发布.
     */
    private void startPublish() {

        ibtnFrontCam.setSelected(mIsFrontCamSelected);
        mZegoAVKit.setFrontCam(mIsFrontCamSelected);

        ibtnMic.setSelected(mIsMicSelected);
        mZegoAVKit.enableMic(mIsMicSelected);

        rlytSmallVideoParent.setVisibility(View.VISIBLE);
        rlytSmallVideoParent.bringToFront();
        mZegoAVKit.setLocalView(svSmall);
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();
        boolean ret = mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);
        Log.i("TestData", ret + "");

    }

    /**
     * 开始播放流.
     */
    private void startPlay(int playStreamIndex) {
        if (playStreamIndex == 1) {
            mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
            mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, svBig);
            mZegoAVKit.startPlayStream(mPlayStreamID1, ZegoAVKitCommon.ZegoRemoteViewIndex.First);
        } else if (playStreamIndex == 2) {
            rlytSmallVideoParent.setVisibility(View.VISIBLE);
            rlytSmallVideoParent.bringToFront();
            mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
            mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, svSmall);
            mZegoAVKit.startPlayStream(mPlayStreamID2, ZegoAVKitCommon.ZegoRemoteViewIndex.Second);
        }

        // 检测是否插入耳机
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(audioManager.isWiredHeadsetOn()){
            mZegoAVKit.setBuiltInSpeakerOn(false);
        }
    }

    /**
     * 退出.
     */
    private void logout() {
        if (mIsPublishing) {
            mZegoAVKit.stopPreview();
            mZegoAVKit.stopPublish();
        }

        if (!TextUtils.isEmpty(mPlayStreamID1)) {
            mZegoAVKit.stopPlayStream(mPlayStreamID1);
        }

        if (!TextUtils.isEmpty(mPlayStreamID2)) {
            mZegoAVKit.stopPlayStream(mPlayStreamID2);
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
