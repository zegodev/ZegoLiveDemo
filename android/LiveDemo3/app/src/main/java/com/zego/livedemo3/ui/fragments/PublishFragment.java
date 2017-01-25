package com.zego.livedemo3.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
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
import com.zego.livedemo3.utils.SystemUtil;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.ZegoAvConfig;
import com.zego.zegoavkit2.callback.ZegoDeviceEventCallback;

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
        mZegoAVKit.setZegoDeviceEventCallback(new ZegoDeviceEventCallback() {
            @Override
            public void onDeviceError(String s, int i) {
                Log.e("PublishFragment", "device name: " + s + ", error: " + i);
            }
        });
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
    public void onStop() {
        super.onPause();
        if(SystemUtil.isAppBackground()){
            Log.i("Foreground", "Foreground");
        }else {
            Log.i("Foreground", "Background");
            // app进入后台, 停止预览
            stopPreview();
        }
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
                SingleAnchorPublishActivity.actionStart(mParentActivity, publishTitleTemp, tbEnableFrontCam.isChecked(), tbEnableTorch.isChecked(), mSelectedBeauty, mSelectedFilter, mParentActivity.getWindowManager().getDefaultDisplay().getRotation());

            }

            @Override
            public void onMoreAnchorsSelect() {
                MorAnchorsPublishActivity.actionStart(mParentActivity, publishTitleTemp, tbEnableFrontCam.isChecked(), tbEnableTorch.isChecked(), mSelectedBeauty, mSelectedFilter, mParentActivity.getWindowManager().getDefaultDisplay().getRotation());
            }

            @Override
            public void onMixStreamSelect() {
                MixStreamPublishActivity.actionStart(mParentActivity, publishTitleTemp, tbEnableFrontCam.isChecked(), tbEnableTorch.isChecked(), mSelectedBeauty, mSelectedFilter, mParentActivity.getWindowManager().getDefaultDisplay().getRotation());
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

        // 设置app朝向
        int currentOrientation = mParentActivity.getWindowManager().getDefaultDisplay().getRotation();
        mZegoAVKit.setAppOrientation(currentOrientation);

        // 设置推流配置
        ZegoAvConfig currentConfig = ZegoApiManager.getInstance().getZegoAvConfig();
        int videoWidth = currentConfig.getVideoEncodeResolutionWidth();
        int videoHeight = currentConfig.getVideoEncodeResolutionHeight();
        if(((currentOrientation == Surface.ROTATION_0 || currentOrientation == Surface.ROTATION_180) && videoWidth  > videoHeight) ||
                ((currentOrientation == Surface.ROTATION_90 || currentOrientation == Surface.ROTATION_270) && videoHeight  > videoWidth)){
            currentConfig.setVideoEncodeResolution(videoHeight, videoWidth);
            currentConfig.setVideoCaptureResolution(videoHeight, videoWidth);
        }
        ZegoApiManager.getInstance().setZegoConfig(currentConfig);

        // 设置水印
        ZegoAVKit.setWaterMarkImagePath("asset:watermark.png");
        Rect rect = new Rect();
        rect.left = 30;
        rect.top = 10;
        rect.right = 180;
        rect.bottom = 160;
        ZegoAVKit.setPreviewWaterMarkRect(rect);

        mZegoAVKit.setLocalView(svPreview);
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();
        mZegoAVKit.setFrontCam(tbEnableFrontCam.isChecked());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        stopPreview();
        startPreview();
    }
}
