package com.zego.livedemo3.performance;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Copyright © 2016 Zego. All rights reserved.
 */
public class PerformanceTest {

    public static String FILE_NAME_REAL_TIME = "/实时数据.txt";

    public static String FILE_NAME_RESULT = "/汇总.txt";

    public static String TAG = "PerformanceTest";

    public static int TYPE_PUBLISH = 1;

    public static int TYPE_PLAY = 2;

    public static int TYPE_MORE_ANCHORS = 3;

    private static PerformanceTest sInstance;

    private boolean mIsTaskRunning = false;

    private Context mContext;

    private Worker mWorker;

    private int mTestType;

    private String mPublishResolution;

    private int mPublishFPS;

    private int mPublishBitrate;

    private String mPlayResolution;

    private long mTestTimeInit;

    private String mTestResultDir;


    private PerformanceTest(){
        mWorker = Worker.getInstance();
    }

    public static PerformanceTest getInstance(){
        if (sInstance == null) {
            synchronized (PerformanceTest.class){
                if(sInstance == null){
                    sInstance = new PerformanceTest();
                }
            }
        }

        return sInstance;
    }

    public void start(Context context, int testType){
        if(context == null || (testType < TYPE_PUBLISH || testType > TYPE_MORE_ANCHORS)){
            return;
        }

        synchronized (sInstance){
            if(!mIsTaskRunning){
                mContext = context;
                mTestType = testType;

                // 初始化数据
                mIsTaskRunning = true;
                mPublishResolution = null;
                mPublishFPS = 0;
                mPublishBitrate = 0;
                mPlayResolution = null;
                mTestTimeInit = System.currentTimeMillis();

                initDirs();
                startWorker();
            }
        }
    }



    private void initDirs(){

        String subDir = null;

        switch (mTestType){
            case 1:
                subDir = "推流";
                break;
            case 2:
                subDir = "拉流";
                break;
            case 3:
                subDir = "连麦";
                break;
        }

        mTestResultDir = mContext.getExternalFilesDir(null) + "/ZegoPerformaceTest/" + subDir;
        File dir = new File(mTestResultDir);
        if(!dir.exists()){
            dir.mkdirs();
        }

        File fileFinalResult = new File(mTestResultDir + FILE_NAME_RESULT);
        if(!fileFinalResult.exists()){
            FileWriter fileWriter = null;
            try {
                fileFinalResult.createNewFile();
                fileWriter = new FileWriter(fileFinalResult, true);
                fileWriter.write(getSystemInfo());
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fileWriter != null){
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void startWorker(){

        File fileRealTime = new File(mTestResultDir + FILE_NAME_REAL_TIME);
        if(!fileRealTime.exists()){
            try {
                fileRealTime.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileRealTime);
            switch (mTestType){
                case 1:
                    fileWriter.write("Dalvik(MB)\tNative(MB)\tCpu(%)\t\tPower(%)\tPublishFPS\tPublishBit(kb)\n");
                    mWorker.start(fileRealTime, mContext, true, false);
                    break;
                case 2:
                    fileWriter.write("Dalvik(MB)\tNative(MB)\tCpu(%)\t\tPower(%)\tPlayFPS\tPlayBit(kb)\n");
                    mWorker.start(fileRealTime, mContext, false, true);
                    break;
                case 3:
                    fileWriter.write("Dalvik(MB)\tNative(MB)\tCpu(%)\t\tPower(%)\tPublishFPS\tPublishBit(kb)\tPlayFPS\tPlayBit(kb)\n");
                    mWorker.start(fileRealTime, mContext, true, true);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void renameRealTimeFile(){

        File fileRealTime = new File(mTestResultDir + FILE_NAME_REAL_TIME);
        if(fileRealTime.exists()){
            String targetFilePath = null;
            switch (mTestType){
                case 1:
                    targetFilePath = mTestResultDir + "/" + mPublishResolution + "_" + mPublishFPS + "_" + mPublishBitrate + "_实时.txt";
                    break;
                case 2:
                    targetFilePath = mTestResultDir + "/" + mPlayResolution + "_" + (int)mWorker.getAverPlayFPS() + "_" + (int)mWorker.getAverPlayBitrate() +  "_实时.txt";
                    break;
                case 3:
                    targetFilePath = mTestResultDir + "/" + mPublishResolution + "_" + mPublishFPS + "_" + mPublishBitrate + "_" + mPlayResolution + "_" + (int)mWorker.getAverPlayFPS() + "_" + (int)mWorker.getAverPlayBitrate() + "_实时.txt";
                    break;
            }

            File targetFile = new File(targetFilePath);
            if(targetFile.exists()){
                targetFile.delete();
            }
            fileRealTime.renameTo(new File(targetFilePath));
        }
    }

    public void stop(){
        synchronized (sInstance){
            if(mIsTaskRunning){
                mIsTaskRunning = false;
                mWorker.stop();
                mContext = null;

                renameRealTimeFile();

                FileWriter fileWriter = null;
                try {

                    fileWriter = new FileWriter(new File(mTestResultDir + FILE_NAME_RESULT), true);

                    fileWriter.write("\n");
                    fileWriter.write("\n");

                    fileWriter.write("测试时长: " + getFormatTime((System.currentTimeMillis() - mTestTimeInit) / 1000));
                    fileWriter.write("\n");

                    if(!TextUtils.isEmpty(mPublishResolution)){
                        fileWriter.write("推流分辨率: " + mPublishResolution);
                        fileWriter.write("\n");
                        fileWriter.write("推流帧率(平均): " + mWorker.getAverPublishFPS());
                        fileWriter.write("\n");
                        fileWriter.write("推流码率(平均): " + mWorker.getAverPublishBitrate() + "kb");
                        fileWriter.write("\n");
                    }

                    if(!TextUtils.isEmpty(mPlayResolution)){
                        fileWriter.write("拉流分辨率: " + mPlayResolution);
                        fileWriter.write("\n");
                        fileWriter.write("拉流帧率(平均): " + mWorker.getAverPlayFPS());
                        fileWriter.write("\n");
                        fileWriter.write("拉流码率(平均): " + mWorker.getAverPlayBitrate() + "kb");
                        fileWriter.write("\n");
                    }

                    fileWriter.write("Dalvik堆: " + mWorker.getAverDalvikHeapAllocated() + "MB");
                    fileWriter.write("\n");
                    fileWriter.write("Native堆: " + mWorker.getAverNativeHeapAllocated() + "MB");
                    fileWriter.write("\n");
                    fileWriter.write("Cpu消耗: " + mWorker.getAverCpuRate() + "%");
                    fileWriter.write("\n");
                    fileWriter.write("电量消耗: " + mWorker.getPowerConsumption() + "%");
                    fileWriter.write("\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(fileWriter != null){
                        try {
                            fileWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    @TargetApi(16)
    private String getSystemInfo(){

        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        ((ActivityManager)mContext.getSystemService(mContext.ACTIVITY_SERVICE)).getMemoryInfo(info);

        String manufacture = Build.MANUFACTURER;
        String model = Build.MODEL;
        String release = Build.VERSION.RELEASE;

        return "手机型号:" + manufacture + "_" + model + "\tAndroid版本:" +release + "\t运行内存" + (info.totalMem / 1024 / 1024) + "MB";
    }

    private String getFormatTime(long time){
        if(time < 0)
            return null;

        int hour = (int) (time / 3600);

        int minutes = (int) ((time % 3600) / 60);

        int seconds = (int) ((time % 3600) % 60);

        return hour + "时" + minutes + "分" + seconds + "秒";
    }

    public void setPublishResolution(String publishResolution, int publishFPS, int publishBitrate) {
        mPublishResolution = publishResolution;
        mPublishFPS = publishFPS;
        mPublishBitrate = publishBitrate;
    }

    public void setPlayResolution(String playResolution) {
        mPlayResolution = playResolution;
    }

    public void setPublishConfig(double publishFPS, double publishBitrate){
        mWorker.setPublishConfig(publishFPS, publishBitrate);
    }

    public void setPlayConfig(double playFPS, double playBitrate){
        mWorker.setPlayConfig(playFPS, playBitrate);
    }
}
