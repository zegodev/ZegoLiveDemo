package com.zego.livedemo3;

import android.app.Application;
import android.content.Context;

import com.zego.livedemo3.presenters.BizLivePresenter;


/**
 * des: 自定义Application.
 */
public class ZegoApplication extends Application{

    public static Context sApplicationContext;


    @Override
    public void onCreate() {
        super.onCreate();

        sApplicationContext = this;

        BizLivePresenter.getInstance();
        ZegoApiManager.getInstance().initSDK(this);
    }

    public Context getApplicationContext(){
        return sApplicationContext;
    }

}
