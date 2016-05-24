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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
 * des: 发布页面.
 */
public class PublishActivity extends AbsShowActivity {

    public static final String KEY_LIVE_CHANNEL = "KEY_LIVE_CHANNEL";

    public static final String KEY_PUBLISH_TITLE = "KEY_PUBLISH_TITLE";

    public static final String KEY_PUBLISH_STREAM_ID = "KEY_PUBLISH_STREAM_ID";

    public static final String KEY_PLAY_STREAM_ID = "KEY_PLAY_STREAM_ID";

    public static final String KEY_IS_FRONT_CAM_SELECTED = "KEY_IS_FRONT_CAM_SELECTED";

    public static final String KEY_IS_SPEAKER_SELECTED = "KEY_IS_SPEAKER_SELECTED";

    public static final String KEY_IS_MIC_SELECTED = "KEY_IS_MIC_SELECTED";


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

    @Bind(R.id.llyt_publish_state)
    public LinearLayout llytPublSate;

    @Bind(R.id.llyt_play_sate)
    public LinearLayout llytPlayState;

    @Bind(R.id.ibtn_front_cam)
    public ImageButton ibtnFrontCam;

    @Bind(R.id.ibtn_speaker)
    public ImageButton ibtnSpeaker;

    @Bind(R.id.ibtn_mic)
    public ImageButton ibtnMic;

    @Bind(R.id.btn_play)
    public Button btnPlay;

    @Bind(R.id.sp_filters)
    public Spinner spFilters;

    @Bind(R.id.sp_beauties)
    public Spinner spBeauties;


    public EditText etStreamID;

    private boolean mSmallVideoIsSmall = true;

    private String mPublishTitle;

    private String mPublishStreamID;

    private String mPlayStreamID;

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
     * 耳机插拔事件的广播接收器.
     */
    private WiredHeadsetChangeReceiver mWiredHeadsetChangeReceiver;

    /**
     * 启动入口.
     *
     * @param activity     源activity
     * @param publishTitle 视频标题
     */
    public static void actionStart(Activity activity, String publishTitle, String liveChannel) {
        Intent intent = new Intent(activity, PublishActivity.class);
        intent.putExtra(IntentExtra.PUBLISH_TITLE, publishTitle);
        intent.putExtra(IntentExtra.LIVE_CHANNEL, liveChannel);
        activity.startActivity(intent);
    }


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_publish;
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
            mPlayStreamID = PreferenceUtils.getInstance().getStringValue(KEY_PLAY_STREAM_ID);
            mIsFrontCamSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_FRONT_CAM_SELECTED);
            mIsSpeakerSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_SPEAKER_SELECTED);
            mIsMicSelected = PreferenceUtils.getInstance().getBooleanValue(KEY_IS_MIC_SELECTED);
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
                ((TextView) llytPublSate.getChildAt(0)).setText("PublishState:onPublishSucc");
                ((TextView) llytPublSate.getChildAt(1)).setText("PublishStream:" + streamID);

            }

            @Override
            public void onPublishStop(ZegoAVKitCommon.ZegoStreamStopFlag stopFlag, String streamID) {
                ((TextView) llytPublSate.getChildAt(0)).setText("PublishState:onPublishStop-" + stopFlag);
                ((TextView) llytPublSate.getChildAt(1)).setText("PublishStream:null");
            }

            @Override
            public void onLoginChannel(boolean isLoginSuccess) {
                if (isLoginSuccess) {
                    tvChannel.setText("Channel:" + mLiveChannel);
                    // 登陆成功, 开始发布
                    startPublish();
                } else {
                    tvChannel.setText("Channel: Error");
                }
            }

            @Override
            public void onDisconnected(int i, String s) {
                // 停止发布前, 必须调用stopPreview
                mZegoAVKit.stopPreview();
                mZegoAVKit.stopPublish();
            }

            @Override
            public void onReconnected(String s) {
                startPublish();
            }


            @Override
            public void onPlaySucc(String streamID) {

                // 播放成功以后,禁止播放按钮
                btnPlay.setEnabled(false);
                ((TextView) llytPlayState.getChildAt(0)).setText("PlayState:onPlaySucc");
                ((TextView) llytPlayState.getChildAt(1)).setText("PlayStream:" + streamID);

            }

            @Override
            public void onPlayStop(ZegoAVKitCommon.ZegoStreamStopFlag stopFlag, String streamID) {
                ((TextView) llytPlayState.getChildAt(0)).setText("PlayState:onPlayStop-" + stopFlag);
                ((TextView) llytPlayState.getChildAt(1)).setText("PlayStream:null");
            }


            @Override
            public void onVideoSizeChanged(String streamID, int width, int height) {
                Log.i("TestData", "");
            }

            @Override
            public void onTakeRemoteViewSnapshot(final Bitmap bitmap, ZegoAVKitCommon.ZegoRemoteViewIndex zegoRemoteViewIndex) {
                Log.i("TestData", "");
            }

            @Override
            public void onTakeLocalViewSnapshot(final Bitmap bitmap) {
                Log.i("TestData", "");
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

        ibtnFrontCam.setSelected(mIsFrontCamSelected);
        ibtnSpeaker.setSelected(mIsSpeakerSelected);
        ibtnMic.setSelected(mIsMicSelected);

        if(savedInstanceState != null){
            tvChannel.setText("Channel:" + mLiveChannel);
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDialog();
            }
        });

        rgSmallViewMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_asfit_small:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                        break;
                    case R.id.rb_asfill_small:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                        break;
                    case R.id.rb_fill_small:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill);
                        break;
                }
            }
        });

        rgBigViewMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_asfit_big:
                        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                        break;
                    case R.id.rb_asfill_big:
                        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                        break;
                    case R.id.rb_fill_big:
                        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill);
                        break;
                }
            }
        });


        spBeauties.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int beauty = 0;
                switch (position){
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
                mZegoAVKit.enableBeautifying(beauty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ZegoAVKitCommon.ZegoFilter filter = null;
                switch (position){
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

                mZegoAVKit.setFilter(filter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void loadData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // 登陆频道
            mZegoAVKit.loginChannel(ZegoApiManager.getInstance().getZegoUser(), mLiveChannel);
        } else {

            Map<String, String> publishInfo = mZegoAVKit.getCurrentPublishInfo();
            // 当引擎依旧处于发布状态时, 只需要重新设置view即可
            if (publishInfo != null && !TextUtils.isEmpty(publishInfo.get(ZegoConstants.KEY_STREAM_ID))) {
                mZegoAVKit.setFrontCam(mIsFrontCamSelected);
                mZegoAVKit.enableSpeaker(mIsSpeakerSelected);
                mZegoAVKit.enableMic(mIsMicSelected);
                mZegoAVKit.setLocalView(svBig);
                mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                mZegoAVKit.startPreview();
            } else {
                /// 当引擎依停止发布了, 重新发布
                startPublish();
            }

            // 恢复播放
            if (!TextUtils.isEmpty(mPlayStreamID)) {
                rlytSmallVideoParent.setVisibility(View.VISIBLE);
                rlytSmallVideoParent.bringToFront();
                btnPlay.setEnabled(false);
                mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, svSmall);
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
        PreferenceUtils.getInstance().setStringValue(KEY_PLAY_STREAM_ID, mPlayStreamID);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_FRONT_CAM_SELECTED, mIsFrontCamSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_SPEAKER_SELECTED, mIsSpeakerSelected);
        PreferenceUtils.getInstance().setBooleanValue(KEY_IS_MIC_SELECTED, mIsMicSelected);
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
                mPlayStreamID = etStreamID.getText().toString().trim();
                if (TextUtils.isEmpty(mPlayStreamID)) {
                    Toast.makeText(PublishActivity.this, "streamID不能为空!", Toast.LENGTH_SHORT).show();
                } else {
                    // 开始播放
                    startPlay();
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

        mZegoAVKit.setFrontCam(mIsFrontCamSelected);
        mZegoAVKit.enableSpeaker(mIsSpeakerSelected);
        mZegoAVKit.enableMic(mIsMicSelected);
        mZegoAVKit.setLocalView(svBig);
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();
        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);


    }

    /**
     * 开始播放流.
     */
    private void startPlay() {

        rlytSmallVideoParent.setVisibility(View.VISIBLE);
        rlytSmallVideoParent.bringToFront();
        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, svSmall);
        mZegoAVKit.startPlayStream(mPlayStreamID, ZegoAVKitCommon.ZegoRemoteViewIndex.First);


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
        // 停止发布前, 必须调用stopPreview
        mZegoAVKit.stopPreview();
        mZegoAVKit.stopPublish();

        // 停止播放
        if (!TextUtils.isEmpty(mPlayStreamID)) {
            mZegoAVKit.stopPlayStream(mPlayStreamID);
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
                    //关闭前置摄像头
                    mZegoAVKit.setFrontCam(true);
                    mIsFrontCamSelected = true;
                } else {
                    //开启前置摄像头
                    mZegoAVKit.setFrontCam(false);
                    mIsFrontCamSelected = false;
                }
                break;
            case R.id.ibtn_speaker:
                if (ibtn.isSelected()) {
                    //关闭声音
                    mZegoAVKit.enableSpeaker(true);
                    mIsSpeakerSelected = true;
                } else {
                    //开启声音
                    mZegoAVKit.enableSpeaker(false);
                    mIsSpeakerSelected = false;
                }
                break;
            case R.id.ibtn_mic:
                if (ibtn.isSelected()) {
                    //关闭话筒
                    mZegoAVKit.enableMic(true);
                    mIsMicSelected = true;
                } else {
                    //开启话筒
                    mZegoAVKit.enableMic(false);
                    mIsMicSelected = false;
                }
                break;
        }
    }

}
