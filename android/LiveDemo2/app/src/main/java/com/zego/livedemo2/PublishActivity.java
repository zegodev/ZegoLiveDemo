package com.zego.livedemo2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.zego.livedemo2.constants.IntentExtra;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class PublishActivity extends BaseShowActivity {

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
    protected void initViews(Bundle savedInstanceState) {
        // 隐藏发布按钮
        btnPublish.setVisibility(View.INVISIBLE);

        super.initViews(savedInstanceState);
    }

    @Override
    protected void prePublishAndPlay() {
        if(mListOrdinalAndStreamID.size() == 0){
            startPublish(mListVideoView.get(0));
        }else {
            restorePublishAndPlay();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
