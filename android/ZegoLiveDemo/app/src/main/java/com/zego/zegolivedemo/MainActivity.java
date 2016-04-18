package com.zego.zegolivedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.zego.zegolivedemo.ViewIndicator.OnIndicateListener;


/**
 * 主页面.
 */
public class MainActivity extends FragmentActivity {

    public static Fragment[] mFragments;

    /**
     * 用户连续点击两次返回键可以退出应用的时间间隔.
     */
    public static final long EXIT_INTERVAL = 1000;

    /**
     * 第一次按退出键的时间.
     */
    private long mBackPressedTime;

    private ViewIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragmentIndicator(0);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出.
     */
    private void exit() {
        /* 连按两次退出 */
        long currentTime = System.currentTimeMillis();
        if (currentTime - mBackPressedTime > EXIT_INTERVAL) {
            Toast.makeText(this, R.string.app_will_exit, Toast.LENGTH_SHORT).show();
            mBackPressedTime = currentTime;
        } else {
            // 释放Zego sdk
            ZegoApiManager.getInstance().releaseSDK();
            System.exit(0);
        }
    }


    public void selectFragment(final int idx) {
        ViewIndicator mIndicator = (ViewIndicator) findViewById(R.id.indicator);
        ViewIndicator.setIndicator(idx);

        getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragments[0])
                .hide(mFragments[1])
                .hide(mFragments[2])
                .hide(mFragments[3])
                .hide(mFragments[4])
                .show(mFragments[idx])
                .commit();
    }

    public void getInRoom(int zegoToken, int zegoId, boolean play) {
        Intent intent = new Intent(this, LiveRoomActivity.class);
        intent.putExtra(LiveRoomActivity.INTENT_KEY_ZEGO_TOKEN, zegoToken);
        intent.putExtra(LiveRoomActivity.INTENT_KEY_ZEGO_ID, zegoId);
        intent.putExtra(LiveRoomActivity.INTENT_KEY_IS_PLAY, true);
        startActivity(intent);
    }

    /**
     * 初始化fragment
     */
    private void setFragmentIndicator(final int whichIsDefault) {
        //实例化 Fragment 集合
        mFragments = new Fragment[5];
        mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_home);
        mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_replay);
        mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_publish);
        mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_hot);
        mFragments[4] = getSupportFragmentManager().findFragmentById(R.id.fragment_profile);
        //显示默认的Fragment
        selectFragment(whichIsDefault);


        //绑定自定义的菜单栏组件
        ViewIndicator mIndicator = (ViewIndicator) findViewById(R.id.indicator);
        mIndicator.setOnIndicateListener(new OnIndicateListener() {
            @Override
            public void onIndicate(View v, int which) {
                Log.i("onIndicate", String.format("select: %d", which));

                //显示指定的Fragment
                getSupportFragmentManager().beginTransaction()
                        .hide(mFragments[0])
                        .hide(mFragments[1])
                        .hide(mFragments[2])
                        .hide(mFragments[3])
                        .hide(mFragments[4])
                        .show(mFragments[which])
                        .commit();
            }
        });
    }
}
