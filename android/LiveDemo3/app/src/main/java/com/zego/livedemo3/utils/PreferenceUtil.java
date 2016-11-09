package com.zego.livedemo3.utils;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import com.zego.livedemo3.ZegoApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: Preference管理工具类.
 */
public class PreferenceUtil {

    /**
     * 单例.
     */
    public static PreferenceUtil sInstance;


    public static final String SHARE_PREFERENCE_NAME = "ZEGO_LIVE_DEMO3";

    public static final String PREFERENCE_KEY_USER_ID = "PREFERENCE_KEY_USER_ID";

    public static final String PREFERENCE_KEY_USER_NAME = "PREFERENCE_KEY_USER_NAME";


    private SharedPreferences mSharedPreferences;

    private PreferenceUtil(){
        mSharedPreferences = ZegoApplication.sApplicationContext.getSharedPreferences(SHARE_PREFERENCE_NAME, AppCompatActivity.MODE_PRIVATE);
    }

    public static PreferenceUtil getInstance(){
        if(sInstance == null){
            synchronized (PreferenceUtil.class){
                if(sInstance == null){
                    sInstance = new PreferenceUtil();
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

    public void setLongValue(String key, long value){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLongValue(String key, long defaultValue){
        return mSharedPreferences.getLong(key, defaultValue);
    }


    public void setUserID(String userID){
        setStringValue(PREFERENCE_KEY_USER_ID, userID);
    }

    public String getUserID(){
        return getStringValue(PREFERENCE_KEY_USER_ID, null);
    }

    public void setUserName(String userName){
        setStringValue(PREFERENCE_KEY_USER_NAME, userName);
    }

    public String getUserName(){
        return getStringValue(PREFERENCE_KEY_USER_NAME, null);
    }

    public Object getObjectFromString(String key){
        Object value = null;
        try{
            byte[] bytes = Base64.decode(PreferenceUtil.getInstance().getStringValue(key, ""), Base64.DEFAULT);
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
            PreferenceUtil.getInstance().setStringValue(key, data);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
