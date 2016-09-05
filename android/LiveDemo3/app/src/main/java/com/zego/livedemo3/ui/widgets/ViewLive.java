package com.zego.livedemo3.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zego.livedemo3.R;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
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


    private View mRootView;

    private TextView mTvQualityColor;

    private TextView mTvQuality;

    private TextView mTvToFullScreen;

    private TextureView mTextureView;

    private Resources mResources;

    private int mLiveQuality = 0;

    private ZegoAVKitCommon.ZegoVideoViewMode mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;

    private boolean mIsShowFullScreen = false;

    private int[] mArrColor;

    private String[] mArrLiveQuality;

    private String mLiveTag;

    private ZegoAVKit mZegoAVKit;

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

    public ViewLive(Context context) {
        super(context);
    }

    public ViewLive(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewLive(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewLive, defStyleAttr, 0);
        boolean isFullScreen = a.getBoolean(R.styleable.ViewLive_isFullScreen, false);
        a.recycle();

        initViews(context, isFullScreen);
    }

    private void initViews(Context context, boolean isFullScreen){

        mResources = context.getResources();

        mArrColor = new int[4];
        mArrColor[0]  = R.drawable.circle_green;
        mArrColor[1]  = R.drawable.circle_yellow;
        mArrColor[2]  = R.drawable.circle_red;
        mArrColor[3]  = R.drawable.circle_gray;

        mArrLiveQuality = mResources.getStringArray(R.array.live_quality);

        if(isFullScreen){
            mRootView = LayoutInflater.from(context).inflate(R.layout.view_live_full_screen, this);
        }else {
            mRootView = LayoutInflater.from(context).inflate(R.layout.view_live, this);
        }

        mTextureView = (TextureView) mRootView.findViewById(R.id.textureView);
        mTvQualityColor = (TextView) mRootView.findViewById(R.id.tv_quality_color);
        mTvQuality = (TextView) mRootView.findViewById(R.id.tv_live_quality);

        // view默认为空闲
        mLiveTag = TAG_VIEW_IS_FREE;

        mTvToFullScreen = (TextView) mRootView.findViewById(R.id.tv_to_full_screen);
        mTvToFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换模式
                if(mZegoVideoViewMode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill){
                    mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit;
                    mTvToFullScreen.setText(R.string.full_screen);
                }else if(mZegoVideoViewMode == ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit){
                    mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;
                    mTvToFullScreen.setText(R.string.exit_full_screen);
                }

                int streamOrdinal = getStreamOrdinalFromLiveTag(mLiveTag);
                switch (streamOrdinal) {
                    case 0:
                    case 1:
                    case 2:
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal), mZegoVideoViewMode);
                        break;
                }
            }
        });
    }

    public void setZegoAVKit(ZegoAVKit zegoAVKit){
        mZegoAVKit = zegoAVKit;
    }

    public boolean isFree(){
        return  TAG_VIEW_IS_FREE.equals(mLiveTag);
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

    public int getStreamOrdinal(){
        return getStreamOrdinalFromLiveTag(mLiveTag);
    }

    public String getStreamID(){
        return getStreamIDFromLiveTag(mLiveTag);
    }

    public void toFullScreen(ViewLive vlBigView){

        String liveTagOfBigView = vlBigView.getLiveTag();
        String liveTagOfSmallView = mLiveTag;

        // 交换view
        int streamOrdinalOfBigView = getStreamOrdinalFromLiveTag(liveTagOfBigView);
        switch (streamOrdinalOfBigView) {
            case 0:
            case 1:
            case 2:
                mZegoAVKit.setRemoteView(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinalOfBigView), mTextureView);
                break;
            case PUBLISH_STREAM_ORDINAL:
                mZegoAVKit.setLocalView(mTextureView);
                break;
        }

        int streamOrdinalOfSmallView = getStreamOrdinalFromLiveTag(liveTagOfSmallView);
        switch (streamOrdinalOfSmallView) {
            case 0:
            case 1:
            case 2:
                mZegoAVKit.setRemoteView(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinalOfSmallView), vlBigView.getTextureView());
                break;
            case PUBLISH_STREAM_ORDINAL:
                mZegoAVKit.setLocalView(vlBigView.getTextureView());
                break;
        }
        // 交换tag
        vlBigView.setLiveTag(liveTagOfSmallView);
        mLiveTag = liveTagOfBigView;

        // 交换quality
        int liveQualityOfBigView = vlBigView.getLiveQuality();
        int liveQualityOfSmallView = mLiveQuality;
        vlBigView.setLiveQuality(liveQualityOfSmallView);
        mLiveQuality = liveQualityOfBigView;


        // 交换view mode
        ZegoAVKitCommon.ZegoVideoViewMode modeOfBigView = vlBigView.getZegoVideoViewMode();
        ZegoAVKitCommon.ZegoVideoViewMode modeOfSmallView = mZegoVideoViewMode;
        vlBigView.setZegoVideoViewMode(modeOfSmallView);
        mZegoVideoViewMode = modeOfBigView;


        // 交换"全屏播放"的标记
        boolean isShowFullScreenOfBigView = vlBigView.isShowFullScreen();
        boolean isShowFullScreenOfSmallView = mIsShowFullScreen;
        vlBigView.setShowFullScreen(isShowFullScreenOfSmallView);
        mIsShowFullScreen = isShowFullScreenOfBigView;

        if(vlBigView.isShowFullScreen()){
            vlBigView.getTvToFullScreen().setVisibility(View.VISIBLE);
            vlBigView.getTvToFullScreen().setText(mTvToFullScreen.getText());
        }else {
            vlBigView.getTvToFullScreen().setVisibility(View.GONE);
        }

        if(mIsShowFullScreen){
            mTvToFullScreen.setVisibility(View.VISIBLE);
            mTvToFullScreen.setText(vlBigView.getTvToFullScreen().getText());
        }else {
            mTvToFullScreen.setVisibility(View.GONE);
        }

    }

    public void setLiveQuality(int quality){
        if(quality >= 0 && quality <= 3){
            mLiveQuality = quality;
            mTvQualityColor.setBackgroundResource(mArrColor[quality]);
            mTvQuality.setText(mResources.getString(R.string.live_quality, mArrLiveQuality[quality]));
        }
    }

    public void setLiveTag(int streamOrdinal, String streamID) {
        mLiveTag = streamOrdinal + SEPARATOR + streamID;
    }

    public void setLiveTag(String liveTag) {
        mLiveTag = liveTag;
    }

    public void setFree(){
        mLiveTag = TAG_VIEW_IS_FREE;
        mLiveQuality = 0;
        setVisibility(View.INVISIBLE);
    }

    public void setRemoteViewMode(int width, int height){
        if(width > height){
            mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit;
            mIsShowFullScreen = true;
            mTvToFullScreen.setVisibility(View.VISIBLE);
            mTvToFullScreen.setText(R.string.full_screen);
        }else {
            mZegoVideoViewMode = ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill;
        }

        int streamOrdinal = getStreamOrdinalFromLiveTag(mLiveTag);
        switch (streamOrdinal) {
            case 0:
            case 1:
            case 2:
                mZegoAVKit.setRemoteViewMode(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal), mZegoVideoViewMode);
                break;
        }
    }

    public void setZegoVideoViewMode(ZegoAVKitCommon.ZegoVideoViewMode mode){
        mZegoVideoViewMode = mode;
    }

    public ZegoAVKitCommon.ZegoVideoViewMode getZegoVideoViewMode(){
        return mZegoVideoViewMode;
    }

    public boolean isShowFullScreen(){
        return mIsShowFullScreen;
    }

    public void setShowFullScreen(boolean showFullScreen){
        mIsShowFullScreen = showFullScreen;
    }

    public TextView getTvToFullScreen(){
        return mTvToFullScreen;
    }
}
