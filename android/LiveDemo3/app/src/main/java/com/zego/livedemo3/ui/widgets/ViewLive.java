package com.zego.livedemo3.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zego.livedemo3.R;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;

import java.util.ArrayList;
import java.util.List;

import static com.zego.livedemo3.R.id.textureView;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: 直播view.
 */
public class ViewLive extends RelativeLayout {

    /**
     * ZegoAVkit支持3条流同时play, 索引为ZegoRemoteViewIndex.First,
     * ZegoRemoteViewIndex.Second, ZegoRemoteViewIndex.Third
     * 索引值相应为0, 1, 2
     * 自定义publish的流的所引值为100
     */
    public static final int PUBLISH_STREAM_ORDINAL = 100;

    /**
     * 无用的所引值.
     */
    public static final int USELESS_STREAM_ORDINAL = -1;

    /**
     * 标识空流.
     */
    public static final String EMPTY_STREAM_ID = "EMPTY";

    /**
     * 分隔符.
     */
    public static final String SEPARATOR = "&";

    /**
     * 标记空闲的LiveView, tag由流的索引值与流ID拼接而成.
     */
    public static final String TAG_VIEW_IS_FREE = USELESS_STREAM_ORDINAL + SEPARATOR + EMPTY_STREAM_ID;

    private int[] mArrColor;

    private String[] mArrLiveQuality;

    private Resources mResources;

    private ViewLiveCallback mViewLiveCallback;

    private View mRootView;

    /**
     * 推拉流颜色.
     */
    private TextView mTvQualityColor;

    /**
     * 推拉流质量.
     */
    private TextView mTvQuality;

    /**
     * 全屏.
     */
    private TextView mTvSwitchToFullScreen;

    /**
     * 分享.
     */
    private TextView mTvShare;

    /**
     * 用于渲染视频.
     */
    private TextureView mTextureView;

    /**
     * 声音大小.
     */
    private ProgressBar mPbSoundLevel;

    /**
     * 推拉流质量.
     */
    private int mLiveQuality = 0;

    /**
     * 视频显示模式.
     */
    private ZegoAVKitCommon.ZegoVideoViewMode mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;

    /**
     * tag.
     */
    private String mLiveTag;

    /**
     * 分享地址.
     */
    private List<String> mListShareUrls = new ArrayList<>();

    /**
     * "切换全屏" 标记.
     */
    private boolean mNeedToSwitchFullScreen = false;

    /**
     * 显示声音大小标记.
     */
    private boolean mIsShowingSoundLevel = false;


    public ViewLive(Context context) {
        super(context);
    }

    public ViewLive(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewLive(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewLive, defStyleAttr, 0);
        boolean isBigView = a.getBoolean(R.styleable.ViewLive_isBigView, false);
        a.recycle();

        initViews(context, isBigView);
    }

    private void initViews(Context context, boolean isBigView) {

        mResources = context.getResources();

        mArrColor = new int[4];
        mArrColor[0] = R.drawable.circle_green;
        mArrColor[1] = R.drawable.circle_yellow;
        mArrColor[2] = R.drawable.circle_red;
        mArrColor[3] = R.drawable.circle_gray;

        mArrLiveQuality = mResources.getStringArray(R.array.live_quality);

        if (isBigView) {
            mRootView = LayoutInflater.from(context).inflate(R.layout.view_live_big, this);

            mTvSwitchToFullScreen = (TextView) mRootView.findViewById(R.id.tv_switch_full_screen);
            mTvSwitchToFullScreen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mZegoVideoViewMode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill){

                        setZegoVideoViewMode(mNeedToSwitchFullScreen, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                    }else if(mZegoVideoViewMode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit){

                        setZegoVideoViewMode(mNeedToSwitchFullScreen, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
                    }

                    if(mViewLiveCallback != null){
                        int streamOrdinal = getStreamOrdinalFromLiveTag(mLiveTag);
                        if (!isFree() && !isPublishView(streamOrdinal)) {
                            mViewLiveCallback.setRemoteViewMode(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal), mZegoVideoViewMode);
                        }
                    }
                }
            });

            mTvShare = (TextView) mRootView.findViewById(R.id.tv_share);
            mTvShare.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mViewLiveCallback != null){
                        mViewLiveCallback.shareToQQ(mListShareUrls);
                    }
                }
            });
        } else {
            mRootView = LayoutInflater.from(context).inflate(R.layout.view_live, this);
        }

        mTextureView = (TextureView) mRootView.findViewById(textureView);
        mTvQualityColor = (TextView) mRootView.findViewById(R.id.tv_quality_color);
        mTvQuality = (TextView) mRootView.findViewById(R.id.tv_live_quality);

        // view默认为空闲
        mLiveTag = TAG_VIEW_IS_FREE;


        mPbSoundLevel = (ProgressBar)mRootView.findViewById(R.id.pb_sound_level);
    }

    public void setViewLiveCallback(ViewLiveCallback viewLiveCallback) {
        mViewLiveCallback = viewLiveCallback;
    }

    public ViewLiveCallback getViewLiveCallback(){
        return mViewLiveCallback;
    }

    /**
     * 返回view是否为"空闲"状态.
     */
    public boolean isFree() {
        return TAG_VIEW_IS_FREE.equals(mLiveTag);
    }

    /**
     * 释放view.
     */
    public void setFree() {
        mLiveTag = TAG_VIEW_IS_FREE;
        mLiveQuality = 0;
        setVisibility(View.INVISIBLE);

        mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;
        mNeedToSwitchFullScreen = false;
        mListShareUrls = new ArrayList<>();

        if (mTvSwitchToFullScreen != null) {
            mTvSwitchToFullScreen.setVisibility(View.INVISIBLE);
        }

        if (mTvShare != null) {
            mTvShare.setVisibility(View.INVISIBLE);
        }

        mIsShowingSoundLevel = false;
    }


    /**
     * 交换view, 通常是跟大的View交换.
     */
    public void toExchangeView(ViewLive vlBigView) {

        String liveTagOfBigView = vlBigView.getLiveTag();
        String liveTagOfSmallView = mLiveTag;

        // 交换view
        int streamOrdinalOfBigView = getStreamOrdinalFromLiveTag(liveTagOfBigView);
        if (!vlBigView.isFree()) {
            if (isPublishView(streamOrdinalOfBigView)) {
                if (vlBigView.getViewLiveCallback() != null) {
                    vlBigView.getViewLiveCallback().setLocalView(mTextureView);
                }
            } else {
                if(vlBigView.getViewLiveCallback() != null){
                    vlBigView.getViewLiveCallback().setRemoteView(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinalOfBigView), mTextureView);
                }
            }
        }

        // 交换view
        int streamOrdinalOfSmallView = getStreamOrdinalFromLiveTag(liveTagOfSmallView);
        if(!isFree()){
            if (isPublishView(streamOrdinalOfSmallView)) {
                if(vlBigView.getViewLiveCallback() != null){
                    vlBigView.getViewLiveCallback().setLocalView(vlBigView.getTextureView());
                }
            } else {
                if(vlBigView.getViewLiveCallback() != null){
                    vlBigView.getViewLiveCallback().setRemoteView(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinalOfSmallView), vlBigView.getTextureView());
                }
            }
        }

        // 交换tag
        vlBigView.setLiveTag(liveTagOfSmallView);
        mLiveTag = liveTagOfBigView;

        // 交换quality
        int liveQualityOfBigView = vlBigView.getLiveQuality();
        int liveQualityOfSmallView = mLiveQuality;
        vlBigView.setLiveQuality(liveQualityOfSmallView);
        setLiveQuality(liveQualityOfBigView);

        // 交换"全屏播放"的标记, 交换view mode
        boolean needToSwitchFullScreenOfBigView = vlBigView.isNeedToSwitchFullScreen();
        boolean needToSwitchFullScreenOfSmallView = mNeedToSwitchFullScreen;
        ZegoAVKitCommon.ZegoVideoViewMode modeOfBigView = vlBigView.getZegoVideoViewMode();
        ZegoAVKitCommon.ZegoVideoViewMode modeOfSmallView = mZegoVideoViewMode;
        vlBigView.setZegoVideoViewMode(needToSwitchFullScreenOfSmallView, modeOfSmallView);
        setZegoVideoViewMode(needToSwitchFullScreenOfBigView, modeOfBigView);


        // 交换share urls
        List<String> listShareUrlsOfBifView = vlBigView.getListShareUrls();
        List<String> listShareUrlsOfSmallView = mListShareUrls;
        vlBigView.setListShareUrls(listShareUrlsOfSmallView);
        setListShareUrls(listShareUrlsOfBifView);


    }

    /**
     * 设置播放质量.
     */
    public void setLiveQuality(int quality) {
        if (quality >= 0 && quality <= 3) {
            mLiveQuality = quality;
            mTvQualityColor.setBackgroundResource(mArrColor[quality]);
            mTvQuality.setText(mResources.getString(R.string.live_quality, mArrLiveQuality[quality]));
        }
    }


    /**
     * 设置tag.
     */
    public void setLiveTag(int streamOrdinal, String streamID) {
        mLiveTag = streamOrdinal + SEPARATOR + streamID;
    }

    /**
     * 设置tag.
     */
    public void setLiveTag(String liveTag) {
        mLiveTag = liveTag;
    }

    /**
     * 设置mode.
     */
    public void setZegoVideoViewMode(boolean needToSwitchFullScreen, ZegoAVKitCommon.ZegoVideoViewMode mode) {
        mNeedToSwitchFullScreen = needToSwitchFullScreen;
        mZegoVideoViewMode = mode;

        if(mTvSwitchToFullScreen != null){
            if (mNeedToSwitchFullScreen) {
                mTvSwitchToFullScreen.setVisibility(View.VISIBLE);

                if (mode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill) {
                    // 退出全屏
                    mTvSwitchToFullScreen.setText(R.string.exit_full_screen);

                } else if (mode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit) {
                    // 全屏显示
                    mTvSwitchToFullScreen.setText(R.string.full_screen);
                }
            } else {
                mTvSwitchToFullScreen.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 设置分享url列表.
     */
    public void setListShareUrls(List<String> listShareUrls) {
        mListShareUrls = listShareUrls;

        if(mTvShare != null){
            if (listShareUrls != null && listShareUrls.size() > 0) {
                mTvShare.setVisibility(View.VISIBLE);
            } else {
                mTvShare.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 显示声音大小.
     */
    public void showSoundLevel(final ZegoAVKit zegoAVKit, final Handler handler){
        if(!mIsShowingSoundLevel){
            mIsShowingSoundLevel = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mIsShowingSoundLevel){
                        int soundLevel;
                        try {
                            if(isPublishView(getStreamOrdinal())){
                                soundLevel = (int) zegoAVKit.getCaptureSoundLevel();
                            }else {
                                soundLevel = (int) zegoAVKit.getRemoteSoundLevel(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(getStreamOrdinal()));
                            }

                            final int finalSoundLevel = soundLevel;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mPbSoundLevel.setProgress(finalSoundLevel);
                                }
                            });
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    public int getLiveQuality() {
        return mLiveQuality;
    }

    public TextureView getTextureView() {
        return mTextureView;
    }

    public String getLiveTag() {
        return mLiveTag;
    }

    public int getStreamOrdinal() {
        return getStreamOrdinalFromLiveTag(mLiveTag);
    }

    public String getStreamID() {
        return getStreamIDFromLiveTag(mLiveTag);
    }

    public boolean isNeedToSwitchFullScreen() {
        return mNeedToSwitchFullScreen;
    }


    public ZegoAVKitCommon.ZegoVideoViewMode getZegoVideoViewMode() {
        return mZegoVideoViewMode;
    }


    public List<String> getListShareUrls() {
        return mListShareUrls;
    }





    public static int getStreamOrdinalFromLiveTag(String liveTag) {
        int streamOrdinal = USELESS_STREAM_ORDINAL;

        if (liveTag != null) {
            String[] arr = liveTag.split(SEPARATOR);
            if (arr != null) {
                streamOrdinal = Integer.valueOf(arr[0]);
            }
        }

        return streamOrdinal;
    }

    public static String getStreamIDFromLiveTag(String liveTag) {
        String streamID = EMPTY_STREAM_ID;

        if (liveTag != null) {
            String[] arr = liveTag.split(SEPARATOR);
            if (arr != null) {
                streamID = arr[1];
            }
        }

        return streamID;
    }

    public static boolean isPublishView(int streamOrdinal) {
        boolean isPublishView = true;

        switch (streamOrdinal) {
            case 0:
            case 1:
            case 2:
                isPublishView = false;
                break;
        }
        return isPublishView;
    }


    public interface ViewLiveCallback {
        void setLocalView(TextureView textureView);

        void setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex index, TextureView textureView);

        void setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex index, ZegoAVKitCommon.ZegoVideoViewMode mode);

        void shareToQQ(List<String> listShareUrls);
    }
}
