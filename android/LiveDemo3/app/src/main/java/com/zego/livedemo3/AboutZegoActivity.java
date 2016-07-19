package com.zego.livedemo3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.zego.livedemo3.base.AbsBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class AboutZegoActivity extends AbsBaseActivity {

    @Bind(R.id.webView)
    public WebView webView;

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
