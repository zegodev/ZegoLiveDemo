package com.zego.livedemo3.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.zego.livedemo3.R;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class PublishSettingsPannel extends LinearLayout {

    private View mRootView;

    private ToggleButton mTbCamera;

    private ToggleButton mTbFrontCam;

    private ToggleButton mTbMic;

    private ToggleButton mTbTorch;

    private ToggleButton mTbBackgroundMusic;

    private Spinner mSpBeauty;

    private Spinner mSpFilter;



    private PublishSettingsCallback mPublishSettingsCallback;

    public PublishSettingsPannel(Context context) {
        super(context);
    }

    public PublishSettingsPannel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PublishSettingsPannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initViews(context);
    }

    private void initViews(Context context){
        mRootView =  LayoutInflater.from(context).inflate(R.layout.view_publish_settings, this);

        mTbCamera = (ToggleButton) mRootView.findViewById(R.id.tb_camera);
        mTbCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onEnableCamera(isChecked);
                }
            }
        });

        mTbFrontCam = (ToggleButton) mRootView.findViewById(R.id.tb_front_cam);
        mTbFrontCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onEnableFrontCamera(isChecked);
                }
                if(isChecked){
                    mTbTorch.setEnabled(false);
                }else {
                    mTbTorch.setEnabled(true);
                }
            }
        });


        mTbMic = (ToggleButton)mRootView.findViewById(R.id.tb_mic);
        mTbMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onEnableMic(isChecked);
                }
            }
        });

        mTbTorch = (ToggleButton) mRootView.findViewById(R.id.tb_torch);
        mTbTorch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onEnableTorch(isChecked);
                }
            }
        });

        mTbBackgroundMusic = (ToggleButton)mRootView.findViewById(R.id.tb_background_music);
        mTbBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onEnableBackgroundMusic(isChecked);
                }
            }
        });

        mSpBeauty = (Spinner) mRootView.findViewById(R.id.sp_beauties);
        ArrayAdapter<String> beautyAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, context.getResources().getStringArray(R.array.beauties));
        mSpBeauty.setAdapter(beautyAdapter);
        mSpBeauty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onSetBeauty(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpFilter = (Spinner) mRootView.findViewById(R.id.sp_filters);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, context.getResources().getStringArray(R.array.filters));
        mSpFilter.setAdapter(filterAdapter);
        mSpFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mPublishSettingsCallback != null){
                    mPublishSettingsCallback.onSetFilter(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setPublishSettingsCallback(PublishSettingsCallback callback){
        mPublishSettingsCallback = callback;
    }

    public void initPublishSettings(boolean isEnableCamera, boolean isEnableFrontCam, boolean isEnableMic,
                                    boolean isEnableTorch, boolean isEnableBackgroundMusic, int beauty, int filter){
        if(isEnableFrontCam){
            mTbTorch.setEnabled(false);
        }
        mTbCamera.setChecked(isEnableCamera);
        mTbFrontCam.setChecked(isEnableFrontCam);
        mTbMic.setChecked(isEnableMic);
        mTbTorch.setChecked(isEnableTorch);
        mTbBackgroundMusic.setChecked(isEnableBackgroundMusic);
        mSpBeauty.setSelection(beauty);
        mSpFilter.setSelection(filter);
    }

    public void setSelectedBeauty(int index){
        mSpBeauty.setSelection(index);
    }

    public interface PublishSettingsCallback{
        void onEnableCamera(boolean isEnable);
        void onEnableFrontCamera(boolean isEnable);
        void onEnableMic(boolean isEnable);
        void onEnableTorch(boolean isEnable);
        void onEnableBackgroundMusic(boolean isEnable);
        void onSetBeauty(int beauty);
        void onSetFilter(int filter);
    }
}
