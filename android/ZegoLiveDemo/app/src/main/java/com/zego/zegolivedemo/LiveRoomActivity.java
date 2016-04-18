package com.zego.zegolivedemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zego.zegoavkit.ZegoAVApi;
import com.zego.zegoavkit.ZegoAVChatRoomCallback;
import com.zego.zegoavkit.ZegoAVKitCommon;
import com.zego.zegoavkit.ZegoAVVideoCallback;
import com.zego.zegoavkit.ZegoStreamInfo;
import com.zego.zegoavkit.ZegoUser;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LiveRoomActivity extends Activity {

    final public static String INTENT_KEY_ZEGO_TOKEN = "token";
    final public static String INTENT_KEY_ZEGO_ID = "id";
    final public static String INTENT_KEY_IS_PLAY = "is_play";
    final public static String INTENT_KEY_PUBLISH_TITLE = "title";

    private RelativeLayout mRlytLocalFrame;

    private boolean mIsPlaying = false;
    private String mPublishTitle = null;
    private boolean mIsPublishing;
    private long mPlayingStream;
    private ZegoAVApi mZegoAVApi;
    private ZegoUser mZegoUser;

    private ZegoAVChatRoomCallback mChatRoomCallback;
    private ZegoAVVideoCallback mVideoCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveroom);

        mZegoAVApi = ZegoApiManager.getInstance().getZegoAVApi();
        mZegoUser = ZegoApiManager.getInstance().getZegoUser();

        // 初始化回调
        initCallback();
        mZegoAVApi.setChatRoomCallback(mChatRoomCallback);
        mZegoAVApi.setVideoCallback(mVideoCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA}, 101);
            }else {
                initViews();
            }
        }else {
            initViews();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initViews();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (!mIsPlaying) {
            stopPublish();
        }

        mZegoAVApi.leaveChatRoom();
        finish();
    }

    private void initViews() {

        Intent intent = getIntent();
        int zegoToken = intent.getIntExtra(INTENT_KEY_ZEGO_TOKEN, 0);
        int zegoId = intent.getIntExtra(INTENT_KEY_ZEGO_ID, 0);
        mIsPlaying = intent.getBooleanExtra(INTENT_KEY_IS_PLAY, false);

        int play = mIsPlaying ? 1 : 0;

        if (!mIsPlaying) {
            mPublishTitle = intent.getStringExtra(INTENT_KEY_PUBLISH_TITLE);
        }

        String s = String.format("zegoID: %d, zegoToken: %d, play: %d", zegoId, zegoToken, play);
        addLog(s);

        mZegoAVApi.getInChatRoom(mZegoUser, zegoToken, zegoId);

        // localViewFrame().setVisibility(View.INVISIBLE);
        View child = getLayoutInflater().inflate(R.layout.view_local_video_frame, null);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mRlytLocalFrame = (RelativeLayout) findViewById(R.id.layoutLocalView1);
            findViewById(R.id.layoutLocalView2).setVisibility(View.GONE);
        } else {
            mRlytLocalFrame = (RelativeLayout) findViewById(R.id.layoutLocalView2);
            findViewById(R.id.layoutLocalView1).setVisibility(View.GONE);
        }
        mRlytLocalFrame.addView(child);

        View view = findViewById(R.id.btnClose);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsPublishing) {
                    stopPublish();
                }
            }
        });

        Button joinButton = (Button) findViewById(R.id.btnJoinPublish);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsPublishing) {
                    startPublish();
                }
            }
        });

        CheckBox cbFlash = (CheckBox) findViewById(R.id.chkFlash);
        cbFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addLog("on flash change: " + isChecked);
                mZegoAVApi.enableTorch(isChecked);
            }
        });

        CheckBox cbFrontCam = (CheckBox) findViewById(R.id.chkFrontCam);
        cbFrontCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addLog("on front cam change: " + isChecked);
                mZegoAVApi.setFrontCam(isChecked);
            }
        });

        CheckBox cbEnableMic = (CheckBox) findViewById(R.id.chkEnableMic);
        cbEnableMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addLog("on enable mic change: " + isChecked);
                mZegoAVApi.enableMic(isChecked);
            }
        });

        View btnTakeRemoteViewShot = findViewById(R.id.btnRemoteViewShot);
        btnTakeRemoteViewShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mZegoAVApi.takeRemoteViewSnapshot(ZegoAVKitCommon.ZegoRemoteViewIndex.First);
            }
        });

        View btnTakeLocalViewShot = findViewById(R.id.btnLocalViewShot);
        btnTakeLocalViewShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mZegoAVApi.takeLocalViewSnapshot();
            }
        });

        RadioGroup rg = (RadioGroup) findViewById(R.id.rgVideoMode);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ZegoAVKitCommon.ZegoVideoViewMode mode = null;

                if (checkedId == R.id.rbFill) {
                    mode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill;
                } else if (checkedId == R.id.rbAspectFit) {
                    mode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit;
                } else if (checkedId == R.id.rbAspectFill) {
                    mode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;
                }

                mZegoAVApi.setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex.First, mode);
            }
        });

        RadioGroup rgLocal = (RadioGroup) findViewById(R.id.rgLocalViewMode);
        if (rgLocal != null) {
            rgLocal.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    ZegoAVKitCommon.ZegoVideoViewMode mode = null;

                    if (checkedId == R.id.rbF) {
                        mode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleToFill;
                    } else if (checkedId == R.id.rbAFit) {
                        mode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit;
                    } else if (checkedId == R.id.rbAFill) {
                        mode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;
                    }

                    mZegoAVApi.setLocalViewMode(mode);
                }
            });
        }

        View v = findViewById(R.id.ivSnapshot);
        if (v != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void initCallback() {
        mChatRoomCallback = new ZegoAVChatRoomCallback() {
            @Override
            public void onGetInChatRoom(int errorCode, int zegoToken, int zegoId) {
                if (mIsPlaying) {
                    mZegoAVApi.sendBroadcastTextMsgInChatRoom("Hello from Android SDK.");
                } else {
                    startPublish();
                }
            }

            @Override
            public void onPlayListUpdate(ZegoAVKitCommon.ZegoPlayListUpdateFlag flag, ZegoStreamInfo[] streamInfoList) {
                switch (flag) {
                    case Error:
                        break;
                    case Add:
                    case UpdateAll:
                        if (streamInfoList != null && streamInfoList.length > 0) {
                            for (ZegoStreamInfo info : streamInfoList) {
                                if (info.userID.equals(mZegoUser.id)) {
                                    continue;
                                }

                                addLog(String.format("Trying to play stream: %d, title: ", info.streamID) + info.streamTitle);
                                startPlay(info.streamID);
                            }
                        }
                        break;
                    case Remove:
                        break;
                }
            }

            @Override
            public void onChatRoomUserUpdate(ZegoUser[] newUserList, ZegoUser[] leftUserList) {
                Log.i("LiveRoomActivity", "onChatRoomUserUpdate");
            }

            @Override
            public void onChatRoomUserUpdateAll(ZegoUser[] allUserList) {
                Log.i("LiveRoomActivity", "onChatRoomUserUpdate");
            }

            @Override
            public void onReceiveBroadcastCustomMsg(ZegoUser sender, int sendTime, Map<String, Object> msgInfo) {
                addLog("onReceiveBroadcastCustomMsg" + msgInfo);
            }

        };

        mVideoCallback = new ZegoAVVideoCallback() {

            @Override
            public void onPlaySucc(long streamID, int zegoToken, int zegoId) {
                mPlayingStream = streamID;
                addLog("on play begin: " + Long.toString(streamID));
            }

            @Override
            public void onPlayStop(int errorCode, long streamID, int zegoToken, int zegoId) {
                addLog("onPlayStop: " + errorCode + " " + streamID);
                if (errorCode == ZegoAVVideoCallback.NormalStop) {
                    mPlayingStream = 0;
                } else if (errorCode == ZegoAVVideoCallback.TempErr) {
                    mPlayingStream = 0;
                }
            }

            @Override
            public void onPublishSucc(int zegoToken, int zegoId) {
                addLog("onPublishStateUpdate: " + "publish started");
                mIsPublishing = true;

                Map<String, Object> publishInfo = mZegoAVApi.getCurrentPublishInfo();
                Long streamID = (Long) publishInfo.get(ZegoAVApi.kZegoPublishStreamIDKey);
                String playURL = (String) publishInfo.get(ZegoAVApi.kZegoPublishStreamURLKey);
//            String streamAlias = (String)publishInfo.get(ZegoAVApi.kZegoPublishStreamAliasKey);
//            addLog("onPublishStateUpdate: " + streamID.toString() + "\n" + playURL + "\n" + streamAlias);
            }

            @Override
            public void onPublishStop(int errorCode, int zegoToken, int zegoId) {
                addLog("onPublishStateUpdate: " + "publish ended");
                mIsPublishing = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //localViewFrame().setVisibility(View.INVISIBLE);
                        mRlytLocalFrame.setVisibility(View.INVISIBLE);
                        localView().setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onPlayerCountUpdate(int count) {
                addLog("onPlayerCountUpdate: " + Integer.toString(count));

                final int c = count;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView v = (TextView) findViewById(R.id.TxtUserCount);
                        v.setText(String.format("%d", c));
                    }
                });
            }

            @Override
            public void onTakeLocalViewSnapshot(Bitmap img) {
                addLog("onTakeLocalViewSnapshot " + img);
                showImage(img);
            }

            @Override
            public void onTakeRemoteViewSnapshot(Bitmap img, ZegoAVKitCommon.ZegoRemoteViewIndex idx) {
                addLog("onTakeRemoteViewSnapshot: " + img + idx);
                showImage(img);
            }

            @Override
            public void onSetPublishExtraData(int errorCode, int zegoToken, int zegoId, String key) {
                addLog("onSetPublishExtraData: " + errorCode + ":" + key);
            }
        };
    }


    public void showImage(Bitmap img) {
        final Bitmap bm = img;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView iv = (ImageView) findViewById(R.id.ivSnapshot);
                if (iv != null) {
                    iv.setImageBitmap(bm);
                    iv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void startPlay(long streamID) {
        addLog("startPlay");
        final long sID = streamID;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View rv = remoteView();
                if (rv != null) {
                    mZegoAVApi.setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex.First, rv);
                    mZegoAVApi.startPlayInChatRoom(ZegoAVKitCommon.ZegoRemoteViewIndex.First, sID);
                }
            }
        });
    }


    public View remoteView() {
        View rv = findViewById(R.id.svVideoView);
        rv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLog("remote view clicked!");

                Map<String, Object> counter = new HashMap<String, Object>();
                counter.put(ZegoAVApi.SYNC_COUNT_0, 1);
                byte[] d = new byte[8];
                d[0] = 0;
                d[1] = 0;
                d[2] = 0;
                d[3] = 1;

                d[4] = 0;
                d[5] = 0;
                d[6] = 0;
                d[7] = 1;

                counter.put(ZegoAVApi.CUSTOM_DATA, d);
                mZegoAVApi.sendBroadcastCustomMsgInChatRoom(counter);
            }
        });
        return rv;
    }

    public View remoteViewFrame() {
        remoteView();
        return findViewById(R.id.layoutRemoteView);
    }

    public View localView() {
        View lv = findViewById(R.id.svSmallVideoView);
        lv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLog("local view clicked!");

                View rv = remoteViewFrame();
                boolean becameBig = flipBigSmall(rv);

                View v = null;
                if (becameBig) {
                    v = remoteView();
                }

                //View lv = localViewFrame();
                becameBig = flipBigSmall(mRlytLocalFrame);
                if (becameBig) {
                    v = localView();
                }

                RelativeLayout layout = (RelativeLayout) v.getParent();
                if (layout != null) {
                    layout.removeView(v);
                    layout.addView(v);
                }

                findViewById(R.id.layoutToolbar).bringToFront();
                findViewById(R.id.layoutRemoteViewTool).bringToFront();
                findViewById(R.id.layoutLocalViewTool).bringToFront();

            }
        });
        return lv;
    }

//    public View localViewFrame() {
//        localView();
//        return findViewById(R.id.layoutLocalView);
//    }

    public void startPublish() {
        addLog("startPublish");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mRlytLocalFrame.setVisibility(View.VISIBLE);
                mRlytLocalFrame.bringToFront();
                View lv = localView();
                if (lv != null) {

                    mZegoAVApi.setLocalView(lv);
                    mZegoAVApi.startPreview();
                    mZegoAVApi.setFrontCam(false);

                    CheckBox cbEnableMic = (CheckBox) findViewById(R.id.chkEnableMic);
                    mZegoAVApi.enableMic(cbEnableMic.isChecked());

                    CheckBox cbEnableFlash = (CheckBox) findViewById(R.id.chkFlash);
                    mZegoAVApi.enableTorch(cbEnableFlash.isChecked());
                    mZegoAVApi.startPublishInChatRoom(mPublishTitle);

                }
            }
        });
    }


    private void stopPublish() {
        mZegoAVApi.stopPublishInChatRoom();
        mZegoAVApi.stopPreview();
    }


    private void addLog(String msg) {
        Log.i("LiveRoomActivity", msg);
    }


    public boolean flipBigSmall(View v) {
        if (v == null) {
            addLog("flipBigSmall, null view");
            return false;
        }

        ViewGroup.LayoutParams lp = v.getLayoutParams();

        addLog("flipBigSmall" + lp.height);

        boolean becameBig = false;
        if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            lp.height = getResources().getDimensionPixelOffset(R.dimen.small_window_height);
            lp.width = getResources().getDimensionPixelOffset(R.dimen.small_window_width);
            ;
        } else {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;

            becameBig = true;
        }

        v.setLayoutParams(lp);
        return becameBig;
    }

}
