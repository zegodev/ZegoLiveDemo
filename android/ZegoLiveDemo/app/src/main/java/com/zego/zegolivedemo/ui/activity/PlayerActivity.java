package com.zego.zegolivedemo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.zego.zegoavkit.ZegoPlayer.ZegoVideoPlayer;
import com.zego.zegoavkit.ZegoPlayer.listener.OnCompletionListener;
import com.zego.zegoavkit.ZegoPlayer.listener.OnErrorListener;
import com.zego.zegoavkit.ZegoPlayer.player.VideoViewPlayer;
import com.zego.zegolivedemo.R;
import com.zego.zegolivedemo.constant.IntentExtra;

/**
 * Created by wubiao on 2016/3/8
 *
 * Des: 播放器.
 */
public class PlayerActivity extends AppCompatActivity {

    private final String TAG = "PlayerActivity";

    private Button mBtnPlay;

    private Button mBtnPause;

    private Button mBtnReplay;

    private Button mBtnStop;

    private SeekBar mSeekBar;

    private VideoView mVideoView;

    private ZegoVideoPlayer mZegoVideoPlayer;

    private boolean mHasBeenPaused = true;

    /**
     * 视频地址.
     */
    private String mVideoUrl;

    public static void actionStart(Activity activity, String videoUrl){
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(IntentExtra.KEY_VIDEO_URL, videoUrl);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // 视频地址为空，直接退出当前页面
        mVideoUrl = getIntent().getStringExtra(IntentExtra.KEY_VIDEO_URL);
        if (TextUtils.isEmpty(mVideoUrl)) {
            finish();
        }

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mVideoView = (VideoView) findViewById(R.id.vv_videoview);

        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnReplay = (Button) findViewById(R.id.btn_replay);
        mBtnStop = (Button) findViewById(R.id.btn_stop);

        mBtnPlay.setOnClickListener(click);
        mBtnPause.setOnClickListener(click);
        mBtnReplay.setOnClickListener(click);
        mBtnStop.setOnClickListener(click);

        // 为进度条添加进度更改事件
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 拖动进度条修改播放进度
                int progress = seekBar.getProgress();
                mZegoVideoPlayer.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

            }
        });

        // 初始化播放器
        initPlayer();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                play();
            }
        }, 1000);
    }

    private void initPlayer() {
        mZegoVideoPlayer = new ZegoVideoPlayer(new VideoViewPlayer(mVideoView), new OnCompletionListener() {
            @Override
            public void onCompletion() {
                // 在播放完毕被回调
//                mBtnPlay.setEnabled(true);
//                mSeekBar.setProgress(0);
            }
        }, new OnErrorListener() {
            @Override
            public void onError(int errCode, String errMsg) {
                mBtnPlay.setEnabled(true);
                mSeekBar.setProgress(0);
            }
        });
        mZegoVideoPlayer.setVideoUri(mVideoUrl);
    }

    private View.OnClickListener click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_play:
                    play();
                    break;
                case R.id.btn_pause:
                    pause();
                    break;
                case R.id.btn_replay:
                    replay();
                    break;
                case R.id.btn_stop:
                    stop();
                    break;
                default:
                    break;
            }
        }
    };

    protected void play() {

        mSeekBar.setMax(mZegoVideoPlayer.start());

        // 开始线程，更新进度条的刻度
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (mZegoVideoPlayer.isPlaying()) {
                            int current = mZegoVideoPlayer.getCurrentProcess();
                            mSeekBar.setProgress(current);
                        }
                        // 如果正在播放，没0.5.毫秒更新一次进度条
                        sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        // 播放之后设置播放按钮不可用
        mBtnPlay.setEnabled(false);
    }

    /**
     * 重新开始播放
     */
    protected void replay() {
        mSeekBar.setProgress(0);
        mZegoVideoPlayer.replay();

        mBtnPause.setText("继续");
        mHasBeenPaused = false;
    }

    /**
     * 暂停或继续
     */
    protected void pause() {
        if (mZegoVideoPlayer.isPlaying()) {
            mZegoVideoPlayer.pause();
            mBtnPause.setText("继续");
            mHasBeenPaused = false;
        } else {
            if (!mHasBeenPaused) {
                mZegoVideoPlayer.continueToPlay();
                mHasBeenPaused = true;
                mBtnPause.setText("暂停");
            }
        }
    }

    /*
     * 停止播放
     */
    protected void stop() {
        mSeekBar.setProgress(0);
        mZegoVideoPlayer.stop();

        mBtnPlay.setEnabled(true);
        mBtnPause.setText("继续");
        mHasBeenPaused = false;
    }
}
