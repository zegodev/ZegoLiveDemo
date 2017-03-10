package com.zego.livedemo3.ui.activities.gamelive;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zego.biz.BizStream;
import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.constants.IntentExtra;
import com.zego.livedemo3.interfaces.OnLiveRoomListener;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.base.AbsBaseActivity;
import com.zego.livedemo3.utils.BizLiveRoomUitl;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.zegoavkit2.AuxData;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright © 2016 Zego. All rights reserved.
 */

public class GameLivingPlayActivity extends AppCompatActivity {

    protected ArrayList<BizStream> mListStream = new ArrayList<>();

    private TextureView mTextureView;

    protected long mRoomKey;

    protected long mServerKey;

    private String mChannel;

    private boolean mHaveLoginedChannel = false;

    private ZegoAVKit mZegoAVKit;


    /**
     * 启动入口.
     *
     * @param activity 源activity
     */
    public static void actionStart(Activity activity, long roomKey, long serverKey, BizStream[] listStream) {
        Intent intent = new Intent(activity, GameLivingPlayActivity.class);
        intent.putExtra(IntentExtra.ROOM_KEY, roomKey);
        intent.putExtra(IntentExtra.SERVER_KEY, serverKey);
        ArrayList<BizStream> arrayList = new ArrayList<>();
        for (BizStream stream : listStream) {
            arrayList.add(stream);
        }
        intent.putParcelableArrayListExtra(IntentExtra.LIST_STREAM, arrayList);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.scale_translate,
                R.anim.my_alpha_action);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_gameliving_play);

        if(savedInstanceState == null){
            Intent intent = getIntent();
            mRoomKey = intent.getLongExtra(IntentExtra.ROOM_KEY, 0);
            mServerKey = intent.getLongExtra(IntentExtra.SERVER_KEY, 0);
            mListStream = intent.getParcelableArrayListExtra(IntentExtra.LIST_STREAM);
            if (mListStream == null) {
                mListStream = new ArrayList<>();
            }
        }

        mTextureView = (TextureView) findViewById(R.id.textureView);

        initCallback();

        // 登录业务房间
        BizLivePresenter.getInstance().loginExistedRoom(mRoomKey, mServerKey, PreferenceUtil.getInstance().getUserID(), BizLiveRoomUitl.USER_NAME_PREFIX_GAME_LIVING + PreferenceUtil.getInstance().getUserName());
    }


    private void initCallback() {
        BizLivePresenter.getInstance().setLiveRoomListener(new OnLiveRoomListener() {
            @Override
            public void onLoginRoom(int errCode, long roomKey, long serverKey) {
                if(errCode == 0){
                    // 登录频道, zego sdk
                    mChannel = BizLiveRoomUitl.getChannel(mRoomKey, mServerKey);
                    loginChannel();
                }
            }

            @Override
            public void onDisconnected(int errCode, long roomKey, long serverKey) {
            }

            @Override
            public void onStreamCreate(String streamID, String url) {
            }

            @Override
            public void onStreamAdd(BizStream[] listStream) {
            }

            @Override
            public void onStreamDelete(BizStream[] listStream) {
            }

            @Override
            public void onReceiveRequestMsg(String fromUserID, String fromUserName, final String magic) {
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

                        if(mListStream != null && mListStream.size() > 0){
                            startPlay(mListStream.get(0).streamID);
                        }
                    }
                }
            }

            @Override
            public void onPublishSucc(String streamID, String liveChannel, HashMap<String, Object> info) {
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
            }

            @Override
            public void onMixStreamConfigUpdate(int retCode, String mixStreamID, HashMap<String, Object> info) {

            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                Toast.makeText(GameLivingPlayActivity.this, "播放成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                Toast.makeText(GameLivingPlayActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
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
            }

            @Override
            public AuxData onAuxCallback(int dataLen) {
                return null;
            }
        });
    }


    private void loginChannel() {
        ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
        mZegoAVKit.loginChannel(zegoUser, mChannel);
    }


    protected void startPlay(String streamID) {
        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, mTextureView);
        mZegoAVKit.startPlayStream(streamID, ZegoAVKitCommon.ZegoRemoteViewIndex.First);
    }

    private void logout(){
        mZegoAVKit.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, null);
        mZegoAVKit.stopPlayStream(mListStream.get(0).streamID);
        mZegoAVKit.logoutChannel();
        mZegoAVKit.setZegoLiveCallback(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logout();
        BizLivePresenter.getInstance().leaveRoom();
        BizLivePresenter.getInstance().setLiveRoomListener(null, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ZegoAVKitCommon.ZegoCameraCaptureRotation rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0;
                break;
            case Surface.ROTATION_90:
                rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90;
                break;
            case Surface.ROTATION_180:
                rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_180;
                break;
            case Surface.ROTATION_270:
                rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_270;
                break;
        }

        mZegoAVKit.setRemoteViewRotation(rotation, ZegoAVKitCommon.ZegoRemoteViewIndex.First);
    }
}
