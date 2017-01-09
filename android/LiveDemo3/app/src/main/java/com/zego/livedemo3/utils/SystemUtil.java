package com.zego.livedemo3.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.ZegoApplication;

import java.util.List;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class SystemUtil {
    public static String getAppVersionName(Context context) {

        String versionName = context.getString(R.string.demo_version);

        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = versionName + info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static boolean isAppBackground() {

        ActivityManager activityManager = (ActivityManager) ZegoApplication.sApplicationContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(ZegoApplication.sApplicationContext.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
