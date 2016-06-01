package com.zego.livedemo2;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.livedemo2.base.AbsBaseActivity;
import com.zego.livedemo2.utils.PreferenceUtils;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * des: 设置频道ID
 */
public class MainActivity extends AbsBaseActivity {


    @Bind(R.id.toolbar)
    public Toolbar toolBar;

    @Bind(R.id.drawerlayout)
    public DrawerLayout drawerLayout;

    @Bind(R.id.tv_publish_title)
    public TextView tvPublishTitle;

    private OnSetConfigsCallback mSetConfigsCallback;

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initExtraData(Bundle savedInstanceState) {
    }

    @Override
    protected void initVariables(Bundle savedInstanceState) {

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mSetConfigsCallback = (SettingFragment) getFragmentManager().findFragmentById(R.id.setting_fragment);

        tvPublishTitle.setText("Title-" + PreferenceUtils.getInstance().getUserName());
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                toolBar.setTitle("设置");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                toolBar.setTitle(getString(R.string.app_name));
                // 当侧边栏关闭时, set配置
                if(mSetConfigsCallback != null){
                    mSetConfigsCallback.onSetConfig();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });

    }

    @Override
    protected void loadData(Bundle savedInstanceState) {

    }


    @OnClick(R.id.btn_publish)
    public void startPublish(){
        String liveChannel = PreferenceUtils.getInstance().getChannel();
        if(TextUtils.isEmpty(liveChannel)){
            liveChannel = getString(R.string.defult_channel);
        }
        PublishActivity.actionStart(MainActivity.this, tvPublishTitle.getText().toString(), liveChannel);
    }

    @OnClick(R.id.btn_play)
    public void startPlay(){
        String liveChannel = PreferenceUtils.getInstance().getChannel();
        if(TextUtils.isEmpty(liveChannel)){
            liveChannel = getString(R.string.defult_channel);
        }

        PlayActivity.actionStart(MainActivity.this, liveChannel);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 用户连续点击两次返回键可以退出应用的时间间隔.
     */
    public static final long EXIT_INTERVAL = 1000;

    private long mBackPressedTime;

    /**
     * 退出.
     */
    private void exit() {
        /* 连按两次退出 */
        long currentTime = System.currentTimeMillis();
        if (currentTime - mBackPressedTime > EXIT_INTERVAL) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mBackPressedTime = currentTime;
        } else {
            // 释放Zego sdk
            ZegoApiManager.getInstance().releaseSDK();
            System.exit(0);
        }
    }

    public interface OnSetConfigsCallback{
        void onSetConfig();
    }
}
