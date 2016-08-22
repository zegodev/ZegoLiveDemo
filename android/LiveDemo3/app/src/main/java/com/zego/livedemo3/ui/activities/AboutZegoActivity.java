package com.zego.livedemo3.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.zego.livedemo3.R;
import com.zego.livedemo3.ui.base.AbsBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class AboutZegoActivity extends AbsBaseActivity {

    @Bind(R.id.webView)
    public WebView webView;

    @Bind(R.id.pb_progress)
    public ProgressBar pbProgress;

    public static void actionStart(Activity activity){
        Intent intent = new Intent(activity, AboutZegoActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_about_zego;
    }

    @Override
    protected void initExtraData(Bundle savedInstanceState) {

    }

    @Override
    protected void initVariables(Bundle savedInstanceState) {
        pbProgress.setMax(100);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 100){
                    pbProgress.setVisibility(View.GONE);
                }else {
                    pbProgress.setVisibility(View.VISIBLE);
                    pbProgress.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

    }

    @Override
    protected void loadData(Bundle savedInstanceState) {
        webView.loadUrl("https://www.zego.im");
    }

    @OnClick(R.id.tv_back)
    public void back(){
        finish();
    }
}
