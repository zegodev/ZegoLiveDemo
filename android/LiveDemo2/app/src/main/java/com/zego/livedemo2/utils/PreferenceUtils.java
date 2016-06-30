package com.zego.livedemo2.utils;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import com.zego.livedemo2.ZegoApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    public static final String KEY_PUBLISH_LEVEL = "KEY_PUBLISH_LEVEL";


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

    public void setIntValue(String key, int value){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getIntValue(String key, int defaultValue){
        return mSharedPreferences.getInt(key, defaultValue);
    }


    public void setUserID(String userID){
        setStringValue(KEY_USER_ID, userID);
    }

    public String getUserID(){
        return getStringValue(KEY_USER_ID, null);
    }

    public void setUserName(String userName){
        setStringValue(KEY_USER_NAME, userName);
    }

    public String getUserName(){
        return getStringValue(KEY_USER_NAME, null);
    }

    public void setChannel(String channel){
        setStringValue(KEY_CHANNEL, channel);
    }


    public String getChannel(){
        return getStringValue(KEY_CHANNEL, null);
    }

    public void setPublishLevel(int level){
        setIntValue(KEY_PUBLISH_LEVEL, level);
    }


    public int getPublishLevel(){
        return getIntValue(KEY_PUBLISH_LEVEL, -1);
    }

    public Object getObjectFromString(String key){
        Object value = null;
        try{
            byte[] bytes = Base64.decode(PreferenceUtils.getInstance().getStringValue(key, ""), Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream oisArray = new ObjectInputStream(bais);
            value = oisArray.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }

        return value;
    }

    public void setObjectToString(String key, Object value){

        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            String data = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
            PreferenceUtils.getInstance().setStringValue(key, data);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
