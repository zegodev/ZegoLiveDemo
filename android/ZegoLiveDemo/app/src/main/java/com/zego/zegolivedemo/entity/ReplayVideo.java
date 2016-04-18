package com.zego.zegolivedemo.entity;

/**
 * Created by Mark on 2016/3/14
 *
 * Des: 回播视频.
 */
public class ReplayVideo {

    /**
     * 视频标题.
     */
    private String mVideoTitle;

    /**
     * 视频url.
     */
    private String mVideoUrl;

    /**
     * 视频截图url.
     */
    private String mScreenShotUrl;

    /**
     * 发布时间.
     */
    private String mPublishTime;

    /**
     * 视频时长.
     */
    private String mVideoTime;

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



    public String getVideoTitle() {
        return mVideoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        mVideoTitle = videoTitle;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        mVideoUrl = videoUrl;
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

    public String getVideoTime() {
        return mVideoTime;
    }

    public void setVideoTime(String videoTime) {
        mVideoTime = videoTime;
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
