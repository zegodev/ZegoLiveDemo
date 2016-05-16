package com.zego.livedemo2;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.livedemo2.base.AbsBaseFragment;
import com.zego.livedemo2.utils.PreferenceUtils;
import com.zego.zegoavkit2.ZegoAvConfig;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * des: 设置页面.
 */
public class SettingFragment extends AbsBaseFragment {

    public static final String  TAG = "SettingFragment";

    /**
     * 初始级别.
     */
    public static final int INIT_LEVEL = 2;


    @Bind(R.id.sp_resolutions)
    public Spinner spinnerResolutions;

    @Bind(R.id.et_user_account)
    public EditText etUserAccount;

    @Bind(R.id.et_user_name)
    public EditText etUserName;

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

    // 分辨率text
    private String mResolutionTexts[];

    // 分辨率列表
    private int mVideoResolutions[][];

    // 帧率列表
    private int mVideoFpss[];

    // 码率列表
    private int mVideoBitrates[];

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
        mVideoResolutions = ZegoAvConfig.VIDEO_RESOLUTIONS;
        mVideoFpss = ZegoAvConfig.VIDEO_FPSS;
        mVideoBitrates = ZegoAvConfig.VIDEO_BITRATES;
    }

    @Override
    protected void initViews() {

        // 用户信息
        etUserAccount.setText(ZegoApiManager.getInstance().getZegoUser().getUserId());
        etUserName.setText(ZegoApiManager.getInstance().getZegoUser().getUserName());

        final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (seekBar.getId()){
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

        // 初始化分辨率
        seekbarResolution.setMax(ZegoAvConfig.LEVEL_COUNT - 1);
        seekbarResolution.setProgress(INIT_LEVEL);
        seekbarResolution.setOnSeekBarChangeListener(seekBarChangeListener);
        tvResolution.setText(getString(R.string.resolution_prefix, mResolutionTexts[INIT_LEVEL]));

        // 初始化帧率
        seekBarFps.setMax(ZegoAvConfig.MAX_VIDEO_FPS);
        seekBarFps.setProgress(mVideoFpss[INIT_LEVEL]);
        seekBarFps.setOnSeekBarChangeListener(seekBarChangeListener);
        tvFps.setText(getString(R.string.fps_prefix, mVideoFpss[INIT_LEVEL]));

        // 初始化码率
        seekBarBitrate.setMax(ZegoAvConfig.MAX_VIDEO_BITRATE);
        seekBarBitrate.setProgress(mVideoBitrates[INIT_LEVEL]);
        seekBarBitrate.setOnSeekBarChangeListener(seekBarChangeListener);
        tvBitrate.setText(getString(R.string.bitrate_prefix, mVideoBitrates[INIT_LEVEL]));


        spinnerResolutions.setSelection(INIT_LEVEL);
        spinnerResolutions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < ZegoAvConfig.LEVEL_COUNT - 1) {
                    seekbarResolution.setProgress(position);
                    seekBarFps.setProgress(mVideoFpss[INIT_LEVEL]);
                    seekBarBitrate.setProgress(mVideoBitrates[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    @OnClick(R.id.tv_change_account)
    public void changeAccount(){
        // 获取新的用户信息
        long ms = System.currentTimeMillis();
        etUserName.setText("User" + ms);
        etUserAccount.setText(ms + "");
    }

    @OnClick(R.id.btn_complete_setting)
    public void completeSetting(){


        String userID = etUserAccount.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        ZegoApiManager.getInstance().getZegoUser().setUserId(userID);
        ZegoApiManager.getInstance().getZegoUser().setUserName(userName);
        PreferenceUtils.getInstance().setUserID(userID);
        PreferenceUtils.getInstance().setUserID(userName);

        // 设置配置信息
        ZegoAvConfig config = ZegoApiManager.getInstance().getZegoAVConfig();
        config.setResolution(mVideoResolutions[seekbarResolution.getProgress()][0], mVideoResolutions[seekbarResolution.getProgress()][1]);
        config.setVideoFPS(seekBarFps.getProgress());
        config.setVideoBitrate(seekBarBitrate.getProgress());
        ZegoApiManager.getInstance().setZegoConfig(config);

        Toast.makeText(getActivity(), "设置成功!", Toast.LENGTH_SHORT).show();
    }
}
