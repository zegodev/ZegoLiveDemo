package com.zego.livedemo3;


import android.text.TextUtils;
import android.widget.Toast;

import com.zego.livedemo3.advanced.VideoCaptureFactoryDemo;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.videofilter.VideoFilterFactoryDemo;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAvConfig;

/**
 * des: zego api管理器.
 */
public class ZegoApiManager {

    private static ZegoApiManager sInstance = null;

    private ZegoAVKit mZegoAVKit = null;

    private ZegoAvConfig mZegoAvConfig;

    /**
     * 外部渲染开关.
     */
    private boolean mUseExternalRender = false;

    /**
     *  测试环境开关.
     */
    private boolean mUseTestEvn = false;

    /**
     * 外部采集开关.
     */
    private boolean mUseVideoCapture = false;

    /**
     * 外部滤镜开关.
     */
    private boolean mUseVideoFilter = false;

    private boolean mUseHardwareEncode = false;

    private boolean mUseHardwareDecode = false;

    private boolean mUseRateControl = false;

    private ZegoApiManager() {
        mZegoAVKit = new ZegoAVKit();
    }

    public static ZegoApiManager getInstance() {
        if (sInstance == null) {
            synchronized (ZegoApiManager.class) {
                if (sInstance == null) {
                    sInstance = new ZegoApiManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 高级功能.
     */
    private void openAndvancedFunctions(){

        // 开启测试环境
        if(mUseTestEvn){
            mZegoAVKit.setTestEnv(true);
        }else {
            mZegoAVKit.setTestEnv(false);
        }

        // 外部渲染
        if(mUseExternalRender){
            // 开启外部渲染
            mZegoAVKit.setExternalRender(true);
        }else {
            mZegoAVKit.setExternalRender(false);
        }

        // 外部采集
        if(mUseVideoCapture){
            // 外部采集
            VideoCaptureFactoryDemo factoryDemo = new VideoCaptureFactoryDemo();
            factoryDemo.setContext(ZegoApplication.sApplicationContext);
            mZegoAVKit.setVideoCaptureFactory(factoryDemo);
        }else {
            mZegoAVKit.setVideoCaptureFactory(null);
        }

        // 外部滤镜
        if(mUseVideoFilter){
            // 外部滤镜
            VideoFilterFactoryDemo videoFilterFactoryDemo = new VideoFilterFactoryDemo();
            mZegoAVKit.setVideoFilterFactory(videoFilterFactoryDemo);
        }else {
            mZegoAVKit.setVideoFilterFactory(null);
        }
    }

    private void initUserInfo(){
        // 初始化用户信息
        String userID = PreferenceUtil.getInstance().getUserID();
        String userName = PreferenceUtil.getInstance().getUserName();

        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
            long ms = System.currentTimeMillis();
            userID = ms/1000 + "";
            userName = "Android-" +  ms/1000 ;

            // 保存用户信息
            PreferenceUtil.getInstance().setUserID(userID);
            PreferenceUtil.getInstance().setUserName(userName);
        }
    }


    private void init(long appID, byte[] signKey){

        initUserInfo();

        // 开发者根据需求定制
        openAndvancedFunctions();

        // 初始化sdk
        boolean ret = mZegoAVKit.init(appID, signKey, ZegoApplication.sApplicationContext);
        if(!ret){
            // sdk初始化失败
            Toast.makeText(ZegoApplication.sApplicationContext, "Zego SDK初始化失败!", Toast.LENGTH_LONG).show();
        }
        // 初始化设置级别为"High"
        mZegoAvConfig = new ZegoAvConfig(ZegoAvConfig.Level.High);
        mZegoAVKit.setAVConfig(mZegoAvConfig);


        // 开发者根据需求定制
        // 硬件编码
        setUseHardwareEncode(mUseHardwareEncode);
        // 硬件解码
        setUseHardwareDecode(mUseHardwareDecode);
        // 码率控制
        setUseRateControl(mUseRateControl);
    }

    /**
     * 初始化sdk.
     */
    public void initSDK(){
        // 即构分配的key与id
        byte[] signKey = {
                (byte)0x91, (byte)0x93, (byte)0xcc, (byte)0x66, (byte)0x2a, (byte)0x1c, (byte)0x0e, (byte)0xc1,
                (byte)0x35, (byte)0xec, (byte)0x71, (byte)0xfb, (byte)0x07, (byte)0x19, (byte)0x4b, (byte)0x38,
                (byte)0x41, (byte)0xd4, (byte)0xad, (byte)0x83, (byte)0x78, (byte)0xf2, (byte)0x59, (byte)0x90,
                (byte)0xe0, (byte)0xa4, (byte)0x0c, (byte)0x7f, (byte)0xf4, (byte)0x28, (byte)0x41, (byte)0xf7
        };
        long appID = 1;

        init(appID, signKey);
    }

    public void reInitSDK(long appID, byte[] signKey) {
        init(appID, signKey);
    }

    public void releaseSDK() {
        mZegoAVKit.unInit();
    }

    public ZegoAVKit getZegoAVKit() {
        return mZegoAVKit;
    }

    public void setZegoConfig(ZegoAvConfig config) {
        mZegoAvConfig = config;
        mZegoAVKit.setAVConfig(config);
    }


    public ZegoAvConfig getZegoAvConfig(){
        return  mZegoAvConfig;
    }


    public void setUseTestEvn(boolean useTestEvn) {
        mUseTestEvn = useTestEvn;
    }

    public boolean getUseExternalRender(){
        return mUseExternalRender;
    }

    public void setUseExternalRender(boolean useExternalRender){
        mUseExternalRender = useExternalRender;
    }

    public void setUseVideoCapture(boolean useVideoCapture) {
        mUseVideoCapture = useVideoCapture;
    }

    public void setUseVideoFilter(boolean useVideoFilter) {
        mUseVideoFilter = useVideoFilter;
    }

    public void setUseHardwareEncode(boolean useHardwareEncode) {
        if(useHardwareEncode){
            // 开硬编时, 关闭码率控制
            if(mUseRateControl){
                mUseRateControl = false;
                mZegoAVKit.enableRateControl(false);
            }
        }
        mUseHardwareEncode = useHardwareEncode;
        ZegoAVKit.requireHardwareEncoder(useHardwareEncode);
    }

    public void setUseHardwareDecode(boolean useHardwareDecode) {
        mUseHardwareDecode = useHardwareDecode;
        ZegoAVKit.requireHardwareDecoder(useHardwareDecode);
    }

    public void setUseRateControl(boolean useRateControl) {
        if(useRateControl){
            // 开码率控制时, 关硬编
            if(mUseHardwareEncode){
                mUseHardwareEncode = false;
                ZegoAVKit.requireHardwareEncoder(false);
            }
        }
        mUseRateControl = useRateControl;
        mZegoAVKit.enableRateControl(useRateControl);
    }
}
