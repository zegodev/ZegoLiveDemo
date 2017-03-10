package com.zego.livedemo3.ui.activities.gamelive;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zego.biz.BizStream;
import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.interfaces.OnLiveRoomListener;
import com.zego.livedemo3.performance.PerformanceTest;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.widgets.ViewLive;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ShareUtils;
import com.zego.zegoavkit2.AuxData;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.ZegoAvConfig;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Copyright © 2016 Zego. All rights reserved.
 */
@TargetApi(21)
public  class GameLiveActivity extends AppCompatActivity {

    private static final String TAG = GameLiveActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1001;

    private Button mStartBtn;
    private Button mShare;
    private boolean mIsRunning = false;

    private String mChannel;
    private String mPublishTitle;
    private String mPublishStreamID;
    private long mRoomKey;
    private long mServerKey;
    private ZegoAVKit mZegoAVKit;
    private boolean mHaveLoginedChannel = false;
    private int mAppOrientation = 0;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private List<String> mListUrls;

    public static void actionStart(Activity activity, String publishTitle, int appOrientation){
        Intent intent = new Intent(activity, GameLiveActivity.class);
        intent.putExtra(IntentExtra.PUBLISH_TITLE, publishTitle);
        intent.putExtra(IntentExtra.APP_ORIENTATION, appOrientation);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_live);

        PerformanceTest.getInstance().start(this, PerformanceTest.TYPE_PUBLISH);
        ZegoAvConfig zegoAVConfig = ZegoApiManager.getInstance().getZegoAvConfig();
        PerformanceTest.getInstance().setPublishResolution(zegoAVConfig.getVideoEncodeResolutionWidth() + "X" + zegoAVConfig.getVideoEncodeResolutionHeight(), zegoAVConfig.getVideoFPS(), zegoAVConfig.getVideoBitrate() / 1000);


        // 检测系统版本
        if(Build.VERSION.SDK_INT < 21){
            Toast.makeText(this, "录屏功能只能在Android5.0及以上版本的系统中运行", Toast.LENGTH_LONG).show();
            finish();
        }else {

            if (savedInstanceState == null) {
                Intent intent = getIntent();
                mPublishTitle = intent.getStringExtra(IntentExtra.PUBLISH_TITLE);
                mAppOrientation = intent.getIntExtra(IntentExtra.APP_ORIENTATION, Surface.ROTATION_0);
            }

            // 手机横屏直播
            if((mAppOrientation == Surface.ROTATION_90 || mAppOrientation == Surface.ROTATION_270)){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            mStartBtn = (Button) findViewById(R.id.start_record);
            mStartBtn.setEnabled(false);
            mStartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mIsRunning){
                        mIsRunning = false;
                        mStartBtn.setText("开始录屏");

                        stopPublish();
                    }else {
                        mIsRunning = true;
                        mStartBtn.setText("停止录屏");

                        // 成功登陆业务房间后, 开始创建流
                        BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);

                    }
                }
            });

            mShare = (Button)findViewById(R.id.btn_share);
            mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListUrls != null){
                        ShareUtils.getInstance().shareToQQ(GameLiveActivity.this, mListUrls, mRoomKey, mServerKey, mPublishStreamID);
                    }
                }
            });

            initCallback();

            // 设置直播模式为"录屏模式"
            mZegoAVKit.setLiveMode(ZegoAVKit.LIVE_MODE_SCREEN_CAPTURE);

            // 请求录屏权限, 等待用户授权
            mMediaProjectionManager =  (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        }
    }


    private void initCallback(){
        BizLivePresenter.getInstance().setLiveRoomListener(new OnLiveRoomListener() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey) {
                if (errCode == 0) {
                    Log.i(TAG, "创建业务房间成功");

                    mRoomKey = roomKey;
                    mServerKey = serverKey;

                    // 获取channel
                    mChannel = BizLiveRoomUitl.getChannel(roomKey, serverKey);

                    // 创建流
                    BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);
                    mIsRunning = true;
                    mStartBtn.setText("停止录屏");
                    mStartBtn.setEnabled(true);
                }else {
                    Log.i(TAG, "创建业务房间失败");
                }
            }

            @Override
            public void onDisconnected(int errCode, long roomKey, long serverKey) {

            }

            @Override
            public void onStreamCreate(String streamID, String url) {
                if (!TextUtils.isEmpty(streamID)) {
                    Log.i(TAG, "业务侧创建流成功");
                    mPublishStreamID = streamID;
                    if (!mHaveLoginedChannel) {
                        loginChannel();
                    } else {
                        startPublish();
                    }
                }else {
                    Log.i(TAG, "业务侧创建流失败");
                }
            }

            @Override
            public void onStreamAdd(BizStream[] listStream) {

            }

            @Override
            public void onStreamDelete(BizStream[] listStream) {

            }

            @Override
            public void onReceiveRequestMsg(String fromUserID, String fromUserName, String magic) {

            }

            @Override
            public void onReceiveRespondMsg(boolean isRespondToMyRequest, boolean isAgree, String userNameOfRequest) {

            }
        }, new Handler());


        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();
        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onLoginChannel(String liveChannel, int retCode) {
                if (retCode == 0) {
                    if (!mHaveLoginedChannel) {
                        mHaveLoginedChannel = true;
                        startPublish();
                    }
                }
            }

            @Override
            public void onPublishSucc(String streamID, String liveChannel, HashMap<String, Object> info) {
                BizLivePresenter.getInstance().reportStreamState(true, streamID, PreferenceUtil.getInstance().getUserID());

                mListUrls = new ArrayList<>();
                if (info != null) {

                    mShare.setEnabled(true);

                    String[] hlsList = (String[]) info.get("hlsList");
                    if (hlsList != null && hlsList.length > 0) {
                        mListUrls.add(hlsList[0]);
                    }

                    String[] rtmpList = (String[]) info.get("rtmpList");
                    if (rtmpList != null && rtmpList.length > 0) {
                        mListUrls.add(rtmpList[0]);
                    }
                }
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                BizLivePresenter.getInstance().reportStreamState(false, streamID, PreferenceUtil.getInstance().getUserID());

                Log.i(TAG, "sdk停止推流");
            }

            @Override
            public void onMixStreamConfigUpdate(int retCode, String mixStreamID, HashMap<String, Object> info) {

            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
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

            @Override
            public void onPlayQualityUpdate(String streamID, int quality, double videoFPS, double videoBitrate) {
            }

            @Override
            public void onPublishQulityUpdate(String streamID, int quality, double videoFPS, double videoBitrate) {
                PerformanceTest.getInstance().setPublishConfig(videoFPS, videoBitrate);
            }

            @Override
            public AuxData onAuxCallback(int dataLen) {
                return null;
            }
        });
    }

    private void loginChannel(){
        ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
        mZegoAVKit.loginChannel(zegoUser, mChannel);
    }

    private void startPublish(){
        mZegoAVKit.startPreview();
        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);
    }

    private void stopPublish(){
        mZegoAVKit.stopPreview();
        mZegoAVKit.stopPublish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            Log.i(TAG, "获取MediaProjection成功");

            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            // 传递MediaProjection对象给sdk, 用于截屏
            mZegoAVKit.setMediaProjection(mMediaProjection);

            // 创建业务房间
            BizLivePresenter.getInstance().createAndLoginRoom(PreferenceUtil.getInstance().getUserID(), BizLiveRoomUitl.USER_NAME_PREFIX_GAME_LIVING + PreferenceUtil.getInstance().getUserName());
        }else {
            Log.i(TAG, "获取MediaProjection失败");
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.do_you_really_want_to_leave)).setTitle(getString(R.string.hint)).setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    stopPublish();
                    mZegoAVKit.logoutChannel();
                    mZegoAVKit.setZegoLiveCallback(null);

                    // 恢复直播模式为"视频直播模式"
                    mZegoAVKit.setLiveMode(ZegoAVKit.LIVE_MODE_CAMERA);

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
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PerformanceTest.getInstance().stop();

        // 离开业务房间
        BizLivePresenter.getInstance().leaveRoom();
        BizLivePresenter.getInstance().setLiveRoomListener(null, null);
    }

}
