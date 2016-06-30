package com.zego.livedemo2;

import android.app.Activity;
import android.content.Intent;

import com.zego.livedemo2.constants.IntentExtra;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class PlayActivity extends BaseShowActivity {

    /**
     * 启动入口.
     *
     * @param activity 源activity
     */
    public static void actionStart(Activity activity, String liveChannel) {
        Intent intent = new Intent(activity, PlayActivity.class);
        intent.putExtra(IntentExtra.LIVE_CHANNEL, liveChannel);
        activity.startActivity(intent);
    }



    @Override
    protected void prePublishAndPlay() {
        if(mListOrdinalAndStreamID.size() == 0){
            newDialog(mListVideoView.get(0));
        }else {
            restorePublishAndPlay();
        }
    }
}
