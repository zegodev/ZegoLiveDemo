package com.zego.livedemo3;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.utils.PreferenceUtil;


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

        CrashReport.initCrashReport(getApplicationContext(), "e40f06d75c", false);
        // bugly初始化用户id
        CrashReport.setUserId(PreferenceUtil.getInstance().getUserID());
    }

    public Context getApplicationContext(){
        return sApplicationContext;
    }

}
