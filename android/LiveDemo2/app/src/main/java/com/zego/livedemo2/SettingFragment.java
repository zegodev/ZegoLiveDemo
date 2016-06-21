package com.zego.livedemo2;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zego.livedemo2.base.AbsBaseFragment;
import com.zego.livedemo2.utils.PreferenceUtils;
import com.zego.zegoavkit2.ZegoAvConfig;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * des: 设置页面.
 */
public class SettingFragment extends AbsBaseFragment implements MainActivity.OnSetConfigsCallback {

    public static final String TAG = "SettingFragment";

    @Bind(R.id.sp_resolutions)
    public Spinner spinnerResolutions;

    @Bind(R.id.et_user_account)
    public EditText etUserAccount;

    @Bind(R.id.et_user_name)
    public EditText etUserName;

    @Bind(R.id.et_channel)
    public EditText etChannel;

    @Bind(R.id.et_appid)
    public EditText etAppID;

    @Bind(R.id.et_appkey)
    public EditText etAppKey;

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

    @Bind(R.id.tb_modify_test_env)
    public ToggleButton tbTestEnv;

    @Bind(R.id.llyt_hide_operation)
    public LinearLayout llytHideOperation;

    // 分辨率text
    private String mResolutionTexts[];

    private int mCount = 0;

    @Override
    protected int getContentViewLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void initExtraData() {

    }

    @Override
    protected void initVariables() {
        mResolutionTexts = mResources.getStringArray(R.array.resolutions);
    }

    @Override
    protected void initViews() {

        // 用户信息
        etUserAccount.setText(PreferenceUtils.getInstance().getUserID());
        etUserName.setText(PreferenceUtils.getInstance().getUserName());

        // 频道
        etChannel.setText(PreferenceUtils.getInstance().getChannel());

        final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()) {
                    case R.id.sb_resolution:
                        tvResolution.setText(getString(R.string.resolution_prefix, mResolutionTexts[progress]));
                        break;
                    case R.id.sb_fps:
                        tvFps.setText(getString(R.string.fps_prefix, progress));
                        break;
                    case R.id.sb_bitrate:
                        tvBitrate.setText(getString(R.string.bitrate_prefix, progress));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                spinnerResolutions.setSelection(ZegoAvConfig.LEVEL_COUNT - 1);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        // 默认设置级别为"高"
        final ZegoAvConfig.Level defaultLevel = ZegoAvConfig.Level.High;
        // 初始化分辨率, 默认为640x480
        seekbarResolution.setMax(ZegoAvConfig.MAX_LEVEL);
        seekbarResolution.setProgress(defaultLevel.code);
        seekbarResolution.setOnSeekBarChangeListener(seekBarChangeListener);
        tvResolution.setText(getString(R.string.resolution_prefix, mResolutionTexts[defaultLevel.code]));

        // 初始化帧率, 默认为15
        seekBarFps.setMax(ZegoAvConfig.MAX_VIDEO_FPS);
        seekBarFps.setProgress(15);
        seekBarFps.setOnSeekBarChangeListener(seekBarChangeListener);
        tvFps.setText(getString(R.string.fps_prefix, 15));

        // 初始化码率, 默认为600 * 1000
        seekBarBitrate.setMax(ZegoAvConfig.MAX_VIDEO_BITRATE);
        seekBarBitrate.setProgress(ZegoAvConfig.VIDEO_BITRATES[defaultLevel.code]);
        seekBarBitrate.setOnSeekBarChangeListener(seekBarChangeListener);
        tvBitrate.setText(getString(R.string.bitrate_prefix, ZegoAvConfig.VIDEO_BITRATES[defaultLevel.code]));

        spinnerResolutions.setSelection(defaultLevel.code);
        spinnerResolutions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= ZegoAvConfig.MAX_LEVEL) {
                    int level = position;
                    seekbarResolution.setProgress(level);
                    // 预设级别中,帧率固定为"15"
                    seekBarFps.setProgress(15);
                    seekBarBitrate.setProgress(ZegoAvConfig.VIDEO_BITRATES[level]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tbTestEnv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(mParentActivity, "成功打开测试环境", Toast.LENGTH_SHORT).show();
                    ZegoApiManager.getInstance().getZegoAVKit().setTestEnv(true);
                } else {
                    Toast.makeText(mParentActivity, "已关闭测试环境", Toast.LENGTH_SHORT).show();
                    ZegoApiManager.getInstance().getZegoAVKit().setTestEnv(false);
                }
            }
        });

    }

    @Override
    protected void loadData() {

    }

    @Override
    protected String getPageTag() {
        return TAG;
    }

    @OnClick(R.id.tv_version)
    public void showHideOperation() {
        mCount++;
        if (mCount % 3 == 0) {
            if (llytHideOperation.getVisibility() == View.INVISIBLE) {
                llytHideOperation.setVisibility(View.VISIBLE);
            } else {
                llytHideOperation.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onSetConfig() {

        PreferenceUtils.getInstance().setUserID(etUserAccount.getText().toString().trim());
        PreferenceUtils.getInstance().setUserName(etUserName.getText().toString().trim());
        PreferenceUtils.getInstance().setChannel(etChannel.getText().toString().trim());

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
                zegoAvConfig = new ZegoAvConfig();
                zegoAvConfig.setResolution(ZegoAvConfig.VIDEO_RESOLUTIONS[seekbarResolution.getProgress()][0], ZegoAvConfig.VIDEO_RESOLUTIONS[seekbarResolution.getProgress()][1]);
                zegoAvConfig.setVideoFPS(seekBarFps.getProgress());
                zegoAvConfig.setVideoBitrate(seekBarBitrate.getProgress());
                break;
        }

        if (zegoAvConfig != null) {
            ZegoApiManager.getInstance().setZegoConfig(zegoAvConfig);
        }

        Toast.makeText(getActivity(), R.string.setting_successfully, Toast.LENGTH_SHORT).show();

        // 设置appID appKey
        final String appID = etAppID.getText().toString().trim();
        final String appKey = etAppKey.getText().toString().trim();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(appID) && !TextUtils.isEmpty(appKey)) {
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

                    ZegoApiManager.getInstance().getZegoAVKit().unInit();
                    ZegoApiManager.getInstance().getZegoAVKit().init(Integer.valueOf(appID), signKey, ZegoApplication.sApplicationContext);
                }
            }
        }).start();

    }
}
