package com.zego.zegolivedemo.entity;

/**
 * Copyright © 2016 Zego. All rights reserved.
 *
 * des: 直播视频.
 */
public class LiveVideo {

    private int mZegoToken;

    private int mZegoId;

    /**
     * 视频标题.
     */
    private String mVideoTitle;


    /**
     * 视频截图url.
     */
    private String mScreenShotUrl;

    /**
     * 发布时间.
     */
    private String mPublishTime;


    /**
     * 用户名.
     */
    private String mUserName;

    /**
     * 用户id.
     */
    private String mUserId;

    /**
     * 用户头像.
     */
    private String mAvatar;


    public int getZegoId() {
        return mZegoId;
    }

    public void setZegoId(int zegoId) {
        mZegoId = zegoId;
    }

    public int getZegoToken() {
        return mZegoToken;
    }

    public void setZegoToken(int zegoToken) {
        mZegoToken = zegoToken;
    }

    public String getVideoTitle() {
        return mVideoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        mVideoTitle = videoTitle;
    }


    public String getScreenShotUrl() {
        return mScreenShotUrl;
    }

    public void setScreenShotUrl(String screenShotUrl) {
        mScreenShotUrl = screenShotUrl;
    }

    public String getPublishTime() {
        return mPublishTime;
    }

    public void setPublishTime(String publishTime) {
        mPublishTime = publishTime;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }
}
