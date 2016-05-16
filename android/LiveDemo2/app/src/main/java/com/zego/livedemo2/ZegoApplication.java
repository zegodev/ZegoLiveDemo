package com.zego.livedemo2;

import android.app.Application;
import android.content.Context;


/**
 * des: 自定义Application.
 */
public class ZegoApplication extends Application{

    public static Context sApplicationContext;


    @Override
    public void onCreate() {
        super.onCreate();

        sApplicationContext = this;

        // 初始化Zego sdk
        ZegoApiManager.getInstance().initSDK(this);
    }

    /**
     * 获取Application Context.
     *
     * @return Application Context
     */
    public Context getApplicationContext(){
        return sApplicationContext;
    }

}
