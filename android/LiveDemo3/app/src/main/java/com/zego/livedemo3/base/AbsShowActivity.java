package com.zego.livedemo3.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import butterknife.ButterKnife;


/**
 * des: activity 基类
 */
public abstract class AbsShowActivity extends AppCompatActivity {

    protected Resources mResources;

    protected Handler mHandler;

    protected ProgressDialog mProgressDialog;

    /**
     * 获取内容页面的布局.
     *
     * @return 返回内容页面的布局
     */
    protected abstract int getContentViewLayout();

    /**
     * 初始化从外部传递过来的数据.
     */
    protected abstract void initExtraData(Bundle savedInstanceState);

    /**
     * 初始化子类中的变量.
     */
    protected abstract void initVariables(Bundle savedInstanceState);

    /**
     * 初始化子类中的控件.
     */
    protected abstract void initViews(Bundle savedInstanceState);

    /**
     * 加载数据.
     */
    protected abstract void doBusiness(Bundle savedInstanceState);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 禁止手机休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(getContentViewLayout());

        // 初始化butternife
        ButterKnife.bind(this);

        initExtraData(savedInstanceState);
        initBaseVariables();
        initVariables(savedInstanceState);

        // 6.0及以上的系统需要在运行时申请CAMERA RECORD_AUDIO权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
            } else {
                initViews(savedInstanceState);
                doBusiness(savedInstanceState);
            }
        } else {
            initViews(savedInstanceState);
            doBusiness(savedInstanceState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            initViews(null);
                             doBusiness(null);

                        }
                    });

                }
                break;
        }
    }

    /**
     * 初始化基类中的变量.
     */
    private void initBaseVariables() {
        mResources = getResources();
        mHandler = new Handler();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


}
