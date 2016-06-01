package com.zego.livedemo2.utils;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.zego.livedemo2.ZegoApplication;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: Preference管理工具类.
 */
public class PreferenceUtils {

    /**
     * 单例.
     */
    public static PreferenceUtils sInstance;


    public static final String SHARE_PREFERENCE_NAME = "Zego_live_demo2";

    public static final String KEY_USER_ID = "user_id";

    public static final String KEY_USER_NAME = "user_name";

    public static final String KEY_CHANNEL = "channel";


    private SharedPreferences mSharedPreferences;

    private PreferenceUtils(){
        mSharedPreferences = ZegoApplication.sApplicationContext.getSharedPreferences(SHARE_PREFERENCE_NAME, AppCompatActivity.MODE_PRIVATE);
    }

    public static PreferenceUtils getInstance(){
        if(sInstance == null){
            synchronized (PreferenceUtils.class){
                if(sInstance == null){
                    sInstance = new PreferenceUtils();
                }
            }
        }
        return sInstance;
    }

    public void setStringValue(String key, String value){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getStringValue(String key, String defaultValue){
        return mSharedPreferences.getString(key, defaultValue);
    }

    public void setBooleanValue(String key, boolean value){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public boolean getBooleanValue(String key, boolean defaultValue){
        return mSharedPreferences.getBoolean(key, defaultValue);
    }


    public void setUserID(String userID){
        setStringValue(KEY_USER_ID, userID);
    }

    public void setUserName(String userName){
        setStringValue(KEY_USER_NAME, userName);
    }

    public String getUserID(){
        return getStringValue(KEY_USER_ID, null);
    }

    public String getUserName(){
        return mSharedPreferences.getString(KEY_USER_NAME, null);
    }

    public void setChannel(String channel){
        setStringValue(KEY_CHANNEL, channel);
    }

    public String getChannel(){
        return getStringValue(KEY_CHANNEL, null);
    }
}
