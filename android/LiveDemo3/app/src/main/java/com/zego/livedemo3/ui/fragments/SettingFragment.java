package com.zego.livedemo3.ui.fragments;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zego.livedemo3.MainActivity;
import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.ui.activities.AboutZegoActivity;
import com.zego.livedemo3.ui.base.AbsBaseFragment;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.SystemUtil;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAvConfig;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


/**
 * des: 设置页面.
 */
public class SettingFragment extends AbsBaseFragment implements MainActivity.OnSetConfigsCallback {


    @Bind(R.id.tv_sdk_version)
    public TextView tvsdkVersion;

    @Bind(R.id.et_user_account)
    public EditText etUserAccount;

    @Bind(R.id.et_user_name)
    public EditText etUserName;

    @Bind(R.id.sp_resolutions)
    public Spinner spinnerResolutions;

    @Bind(R.id.tv_resolution)
    public TextView tvResolution;

    @Bind(R.id.sb_resolution)
    public SeekBar seekbarResolution;

    @Bind(R.id.tv_fps)
    public TextView tvFps;

    @Bind(R.id.sb_fps)
    public SeekBar seekBarFps;

    @Bind(R.id.tv_bitrate)
    public TextView tvBitrate;

    @Bind(R.id.sb_bitrate)
    public SeekBar seekBarBitrate;

    @Bind(R.id.tv_demo_version)
    public TextView tvDemoVersion;

    @Bind(R.id.llyt_hide_operation)
    public LinearLayout llytHideOperation;

    @Bind(R.id.tb_modify_test_env)
    public ToggleButton tbTestEnv;

    @Bind(R.id.et_appid)
    public EditText etAppID;

    @Bind(R.id.et_appkey)
    public EditText etAppKey;

    @Bind(R.id.sv)
    public ScrollView scrollView;

    @Bind(R.id.tb_video_capture)
    public ToggleButton tbVideoCapture;

    @Bind(R.id.tb_video_filter)
    public ToggleButton tbVideoFilter;

    @Bind(R.id.tb_external_render)
    public ToggleButton tbExternalRender;

    @Bind(R.id.tb_hardware_encode)
    public ToggleButton tbHardwareEncode;

    @Bind(R.id.tb_hardware_decode)
    public ToggleButton tbHardwareDecode;

    @Bind(R.id.tb_rate_control)
    public ToggleButton tbRateControl;

    // 分辨率text
    private String mResolutionTexts[];

    private int mCount = 0;

    private final int[][] VIDEO_RESOLUTIONS = new int[][]{{320, 240}, {352, 288}, {640, 360},
            {640, 360}, {1280, 720}, {1920, 1080}};

    private boolean mNeedToReInitSDK = false;

    private ZegoAVKit mZegoAVKit = null;

    @Override
    protected int getContentViewLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void initExtraData() {

    }

    @Override
    protected void initVariables() {
        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();
        mResolutionTexts = mResources.getStringArray(R.array.resolutions);

    }

    @Override
    protected void initViews() {

        // 用户信息
        tvsdkVersion.setText(mZegoAVKit.version());
        etUserAccount.setText(PreferenceUtil.getInstance().getUserID());
        etUserAccount.setSelection(etUserAccount.getText().toString().length());
        etUserName.setText(PreferenceUtil.getInstance().getUserName());
        etUserName.setSelection(etUserName.getText().toString().length());
        tvDemoVersion.setText(SystemUtil.getAppVersionName(mParentActivity));

        final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()) {
                    case R.id.sb_resolution:
                        tvResolution.setText(getString(R.string.resolution_prefix, mResolutionTexts[progress]));
                        break;
                    case R.id.sb_fps:
                        tvFps.setText(getString(R.string.fps_prefix, progress + ""));
                        break;
                    case R.id.sb_bitrate:
                        tvBitrate.setText(getString(R.string.bitrate_prefix, progress + ""));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                spinnerResolutions.setSelection(5);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        // 默认设置级别为"高"
        ZegoAvConfig.Level defaultLevel = ZegoAvConfig.Level.High;

        // 初始化分辨率, 默认为640x480
        seekbarResolution.setMax(6);
        seekbarResolution.setProgress(defaultLevel.code);
        seekbarResolution.setOnSeekBarChangeListener(seekBarChangeListener);
        tvResolution.setText(getString(R.string.resolution_prefix, mResolutionTexts[defaultLevel.code]));

        // 初始化帧率, 默认为15
        seekBarFps.setMax(30);
        seekBarFps.setProgress(15);
        seekBarFps.setOnSeekBarChangeListener(seekBarChangeListener);
        tvFps.setText(getString(R.string.fps_prefix, "15"));

        // 初始化码率, 默认为600 * 1000
        seekBarBitrate.setMax(1000000);
        seekBarBitrate.setProgress(ZegoAvConfig.VIDEO_BITRATES[defaultLevel.code]);
        seekBarBitrate.setOnSeekBarChangeListener(seekBarChangeListener);
        tvBitrate.setText(getString(R.string.bitrate_prefix, "" + ZegoAvConfig.VIDEO_BITRATES[defaultLevel.code]));

        spinnerResolutions.setSelection(defaultLevel.code);
        spinnerResolutions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= ZegoAvConfig.Level.VeryHigh.code) {
                    int level = position;
                    seekbarResolution.setProgress(level);
                    // 预设级别中,帧率固定为"15"
                    seekBarFps.setProgress(15);
                    seekBarBitrate.setProgress(ZegoAvConfig.VIDEO_BITRATES[level]);

                    seekbarResolution.setEnabled(false);
                    seekBarFps.setEnabled(false);
                    seekBarBitrate.setEnabled(false);
                } else {
                    seekbarResolution.setEnabled(true);
                    seekBarFps.setEnabled(true);
                    seekBarBitrate.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        seekbarResolution.setEnabled(false);
        seekBarFps.setEnabled(false);
        seekBarBitrate.setEnabled(false);
    }

    @Override
    protected void loadData() {

    }


    @OnClick(R.id.tv_demo_version)
    public void showHideOperation() {
        mCount++;
        if (mCount % 3 == 0) {
            if (llytHideOperation.getVisibility() == View.GONE) {
                llytHideOperation.setVisibility(View.VISIBLE);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            } else {
                llytHideOperation.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.tv_advanced)
    public void showAdvaaced() {
        llytHideOperation.setVisibility(View.VISIBLE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    @OnClick(R.id.tv_upload_log)
    public void uploadLog() {
        mZegoAVKit.uploadLog();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mParentActivity, R.string.upload_log_successfully, Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    @OnClick(R.id.tv_about)
    public void openAboutPage() {
        AboutZegoActivity.actionStart(mParentActivity);
    }

    @Override
    public void onSetConfig() {

        InputMethodManager imm = (InputMethodManager) mParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mParentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        ZegoAvConfig zegoAvConfig = null;
        switch (spinnerResolutions.getSelectedItemPosition()) {
            case 0:
                zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.VeryLow);
                break;
            case 1:
                zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.Low);
                break;
            case 2:
                zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.Generic);
                break;
            case 3:
                zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.High);
                break;
            case 4:
                zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.VeryHigh);
                break;
            case 5:
                // 自定义设置
                zegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.High);
                int progress = seekbarResolution.getProgress();
                zegoAvConfig.setVideoEncodeResolution(VIDEO_RESOLUTIONS[progress][0], VIDEO_RESOLUTIONS[progress][1]);
                zegoAvConfig.setVideoCaptureResolution(VIDEO_RESOLUTIONS[progress][0], VIDEO_RESOLUTIONS[progress][1]);
                zegoAvConfig.setVideoFPS(seekBarFps.getProgress());
                zegoAvConfig.setVideoBitrate(seekBarBitrate.getProgress());
                break;
        }

        if (zegoAvConfig != null) {
            ZegoApiManager.getInstance().setZegoConfig(zegoAvConfig);
        }

        // 设置appID appKey
        final String appID = etAppID.getText().toString().trim();
        final String appKey = etAppKey.getText().toString().trim();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if ((!TextUtils.isEmpty(appID) && !TextUtils.isEmpty(appKey))) {
                    // appID必须是数字
                    if (!TextUtils.isDigitsOnly(appID)) {
                        return;
                    }
                    // appKey长度必须等于32位
                    String[] keys = appKey.split(",");
                    if (keys.length != 32) {
                        return;
                    }

                    byte[] signKey = new byte[32];
                    for (int i = 0; i < 32; i++) {
                        int data = Integer.valueOf(keys[i].trim().replace("0x", ""), 16);
                        signKey[i] = (byte) data;
                    }

                    // 重新初始化sdk
                    ZegoApiManager.getInstance().releaseSDK();
                    ZegoApiManager.getInstance().reInitSDK(Long.valueOf(appID), signKey);

                } else if (mNeedToReInitSDK) {

                    // 重新初始化sdk
                    mNeedToReInitSDK = false;
                    ZegoApiManager.getInstance().releaseSDK();
                    ZegoApiManager.getInstance().initSDK();
                }
            }
        }).start();
    }

    @OnCheckedChanged({R.id.tb_modify_test_env, R.id.tb_hardware_encode, R.id.tb_hardware_decode, R.id.tb_rate_control})
    public void onCheckedChanged1(CompoundButton compoundButton, boolean checked) {
        // "非点击按钮"时不触发回调
        if (!compoundButton.isPressed()) return;

        switch (compoundButton.getId()) {
            case R.id.tb_modify_test_env:

                // 标记需要"重新初始化sdk"
                mNeedToReInitSDK = true;
                ZegoApiManager.getInstance().setUseTestEvn(checked);

                break;
            case R.id.tb_hardware_encode:
                ZegoApiManager.getInstance().setUseHardwareEncode(checked);
                // 开硬编时, 关闭码率控制
                if (tbRateControl.isChecked()) {
                    tbRateControl.setChecked(false);
                }
                break;
            case R.id.tb_hardware_decode:
                ZegoApiManager.getInstance().setUseHardwareDecode(checked);
                break;
            case R.id.tb_rate_control:
                // 开码率控制时, 关硬编
                ZegoApiManager.getInstance().setUseRateControl(checked);
                if (tbHardwareEncode.isChecked()) {
                    tbHardwareEncode.setChecked(false);
                }
                break;
        }
    }

    @OnCheckedChanged({R.id.tb_video_capture, R.id.tb_video_filter, R.id.tb_external_render})
    public void onCheckedChanged2(CompoundButton compoundButton, boolean checked) {

        // "非点击按钮"时不触发回调
        if (!compoundButton.isPressed()) return;

        switch (compoundButton.getId()) {
            case R.id.tb_video_capture:
                if(checked){
                    // 开启外部采集时, 关闭外部滤镜
                    tbVideoFilter.setChecked(false);
                    ZegoApiManager.getInstance().setUseVideoFilter(false);
                }

                ZegoApiManager.getInstance().setUseVideoCapture(checked);
                break;
            case R.id.tb_video_filter:
                if(checked){
                    // 开启外部滤镜时, 关闭外部采集
                    tbVideoCapture.setChecked(false);
                    ZegoApiManager.getInstance().setUseVideoCapture(false);
                }

                ZegoApiManager.getInstance().setUseVideoFilter(checked);
                break;
            case R.id.tb_external_render:
                ZegoApiManager.getInstance().setUseExternalRender(checked);
                break;
        }

        // 标记需要"重新初始化sdk"
        mNeedToReInitSDK = true;
    }
}
