package com.zego.livedemo3.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.ui.activities.mixstream.MixStreamPublishActivity;
import com.zego.livedemo3.ui.activities.moreanchors.MorAnchorsPublishActivity;
import com.zego.livedemo3.ui.activities.singleanchor.SingleAnchorPublishActivity;
import com.zego.livedemo3.ui.base.AbsBaseFragment;
import com.zego.livedemo3.ui.widgets.DialogSelectPublishMode;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.ZegoAvConfig;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class PublishFragment extends AbsBaseFragment {

    @Bind(R.id.tb_enable_front_cam)
    public ToggleButton tbEnableFrontCam;

    @Bind(R.id.tb_enable_torch)
    public ToggleButton tbEnableTorch;


    @Bind(R.id.sp_filters)
    public Spinner spFilters;

    @Bind(R.id.sp_beauties)
    public Spinner spBeauties;

    @Bind(R.id.et_publish_title)
    public EditText etPublishTitle;

    @Bind(R.id.sv_preview)
    public SurfaceView svPreview;


    private int mSelectedBeauty = 0;

    private int mSelectedFilter = 0;

    private ZegoAVKit mZegoAVKit;

    private boolean mHasBeenCreated = false;

    private boolean mIsVisiableToUser = false;

    private boolean mSpinnerOfBeautyInitialed = false;

    private boolean mSpinnerOfFilterInitialed = false;

    public static PublishFragment newInstance() {
        return new PublishFragment();
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.fragment_publish;
    }

    @Override
    protected void initExtraData() {

    }

    @Override
    protected void initVariables() {
        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();
    }

    @Override
    protected void initViews() {
        ArrayAdapter<String> beautyAdapter = new ArrayAdapter<>(mParentActivity, R.layout.item_spinner, mResources.getStringArray(R.array.beauties));
        spBeauties.setAdapter(beautyAdapter);
        spBeauties.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedBeauty = position;
                if(mSpinnerOfBeautyInitialed){
                    mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
                }else {
                    mSpinnerOfBeautyInitialed = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(mParentActivity, R.layout.item_spinner, mResources.getStringArray(R.array.filters));
        spFilters.setAdapter(filterAdapter);
        spFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedFilter = position;

                if(mSpinnerOfFilterInitialed){
                    mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));
                }else {
                    mSpinnerOfFilterInitialed = true;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 默认"全屏+美白"
        spBeauties.setSelection(3);

        // 开启前置摄像头时, 手电筒不可用
        if (tbEnableFrontCam.isChecked()) {
            tbEnableTorch.setEnabled(false);
        }
        tbEnableFrontCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mZegoAVKit.setFrontCam(isChecked);

                // 开启前置摄像头时, 手电筒不可用
                if (isChecked) {
                    mZegoAVKit.enableTorch(false);
                    tbEnableTorch.setChecked(false);
                    tbEnableTorch.setEnabled(false);
                } else {
                    tbEnableTorch.setEnabled(true);
                }

                setRotateFromInterfaceOrientation(mParentActivity.getWindowManager().getDefaultDisplay().getRotation());
            }
        });

        tbEnableTorch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mZegoAVKit.enableTorch(isChecked);
            }
        });
    }

    @Override
    protected void loadData() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHasBeenCreated) {
            if (mIsVisiableToUser) {
                startPreview();
            }
        } else {
            mHasBeenCreated = true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @OnClick(R.id.btn_start_publish)
    public void startPublishing() {

        String publishTitle = etPublishTitle.getText().toString();
        if (TextUtils.isEmpty(publishTitle)) {
            publishTitle = PreferenceUtil.getInstance().getUserName();
        }

        hideInputWindow();

        final String publishTitleTemp = publishTitle;
        final DialogSelectPublishMode dialog = new DialogSelectPublishMode();
        dialog.setOnSelectPublishModeListener(new DialogSelectPublishMode.OnSelectPublishModeListener() {
            @Override
            public void onSingleAnchorSelect() {
                SingleAnchorPublishActivity.actionStart(mParentActivity, publishTitleTemp, tbEnableFrontCam.isChecked(), tbEnableTorch.isChecked(), mSelectedBeauty, mSelectedFilter);

            }

            @Override
            public void onMoreAnchorsSelect() {
                MorAnchorsPublishActivity.actionStart(mParentActivity, publishTitleTemp, tbEnableFrontCam.isChecked(), tbEnableTorch.isChecked(), mSelectedBeauty, mSelectedFilter);
            }

            @Override
            public void onMixStreamSelect() {
                MixStreamPublishActivity.actionStart(mParentActivity, publishTitleTemp, tbEnableFrontCam.isChecked(), tbEnableTorch.isChecked(), mSelectedBeauty, mSelectedFilter);
            }
        });

        dialog.show(mParentActivity.getFragmentManager(), "selectPublishModeDialog");
    }

    @OnClick(R.id.main_content)
    public void hideInputWindow() {
        InputMethodManager imm = (InputMethodManager) mParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mParentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisiableToUser = isVisibleToUser;
        if (mHasBeenCreated) {
            if (isVisibleToUser) {

                // 6.0及以上的系统需要在运行时申请CAMERA RECORD_AUDIO权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(mParentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(mParentActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(mParentActivity, new String[]{
                                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
                    } else {
                        startPreview();
                    }
                } else {
                    startPreview();
                }
            } else {
                stopPreview();
            }
        }
    }

    private void startPreview() {

        mZegoAVKit.setFrontCam(tbEnableFrontCam.isChecked());
        
        // 设置手机朝向
        setRotateFromInterfaceOrientation(mParentActivity.getWindowManager().getDefaultDisplay().getRotation());


        mZegoAVKit.setLocalView(svPreview);
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();
        svPreview.setVisibility(View.VISIBLE);
        mZegoAVKit.enableTorch(tbEnableTorch.isChecked());

        // 设置美颜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        // 设置滤镜
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));


    }

    private void stopPreview() {
        svPreview.setVisibility(View.INVISIBLE);
        mZegoAVKit.stopPreview();
        mZegoAVKit.setLocalView(null);
    }

    protected boolean isDeviceOrientationPortrait(){

        int orientation = mParentActivity.getWindowManager().getDefaultDisplay().getRotation();
        // 判断手机是否垂直摆放
        boolean isDeviceOrientationPortrait = true;
        switch (orientation){
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                isDeviceOrientationPortrait = false;
                break;
        }

        return isDeviceOrientationPortrait;
    }

    /**
     * 设置设备朝向.
     */
    protected void setupDeviceOrientation(){

        //  修正最终需要的输出分辨率, 保证：横屏姿势时，输出横屏视频，竖屏姿势时，输出竖屏视频
        ZegoAvConfig currentConfig = ZegoApiManager.getInstance().getZegoAvConfig();
        int width = currentConfig.getVideoEncodeResolutionWidth();
        int height = currentConfig.getVideoEncodeResolutionHeight();

        if((isDeviceOrientationPortrait() &&  width > height) // 手机竖屏, 但是 宽 > 高
                || (!isDeviceOrientationPortrait() && width < height)){ // 手机横屏, 但是 宽 < 高

            currentConfig.setVideoEncodeResolution(height, width);
            ZegoApiManager.getInstance().setZegoConfig(currentConfig);
        }


    }

    protected void setRotateFromInterfaceOrientation(int orientation){

        setupDeviceOrientation();

        // 设置手机朝向
        mZegoAVKit.setAppOrientation(orientation);
        mZegoAVKit.setLocalViewRotation(ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0);
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setRotateFromInterfaceOrientation(mParentActivity.getWindowManager().getDefaultDisplay().getRotation());
    }
}
