package com.zego.livedemo3.performance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Copyright © 2016 Zego. All rights reserved.
 */

public class Worker {

    private static Worker sInstance;

    private boolean mIsTaskRuning = false;

    private Runnable mTask;

    private float mLastTotalCpuTime;

    private float mLastAppCpuTime;

    private long mSamplingCount;

    private long mTotalCpuRate;

    private long mNativeHeapAllocatedTotal;

    private long mDalvikHeapAllocatedTotal;

    private double mPublishFPS;

    private double mPublishBitrate;

    private double mPlayFPS;

    private double mPlayBitrate;

    private double mPublishFPSTotal;

    private double mPublishBitrateTotal;

    private double mPlayFPSTotal;

    private double mPlayBitrateTotal;

    private FileWriter mFileWriter;

    private StringBuilder mSbResultLine;

    private BroadcastReceiver mPowerBroadcastReceiver;

    private int mInitPowerLevel;

    private int mCurrentPowerLevel;

    private Context mContext;

    private boolean mIsPublishing = false;

    private boolean mIsPlaying = false;

    private Worker() {

        mPowerBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    mCurrentPowerLevel = intent.getIntExtra("level", 0);
                }
            }
        };

        mSbResultLine = new StringBuilder();
        mTask = new Runnable() {
            @Override
            public void run() {
                while (mIsTaskRuning) {
                    try {
                        Thread.sleep(1000);
                        mSbResultLine.setLength(0);
                        samplingData();
                        if (mSbResultLine.length() > 0) {
                            try {
                                mFileWriter.write(mSbResultLine.toString());
                                mFileWriter.write("\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    mFileWriter.flush();
                    mFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static Worker getInstance() {
        if (sInstance == null) {
            synchronized (Worker.class) {
                if (sInstance == null) {
                    sInstance = new Worker();
                }
            }
        }

        return sInstance;
    }

    public void start(File file, Context context, boolean isPublishing, boolean isPlaying) {

        if (file == null || !file.exists() || context == null) {
            return;
        }

        synchronized (sInstance) {
            if (!mIsTaskRuning) {
                mIsTaskRuning = true;
                mContext = context;
                mSamplingCount = 0;
                mIsPublishing = isPublishing;
                mIsPlaying = isPlaying;

                mLastTotalCpuTime = 0;
                mLastAppCpuTime = 0;
                mTotalCpuRate = 0;

                mNativeHeapAllocatedTotal = 0;
                mDalvikHeapAllocatedTotal = 0;

                mPublishFPS = 0;
                mPublishBitrate = 0;
                mPlayFPS = 0;
                mPlayBitrate = 0;

                mPublishFPSTotal = 0;
                mPublishBitrateTotal = 0;
                mPlayFPSTotal = 0;
                mPlayBitrateTotal = 0;

                try {
                    mFileWriter = new FileWriter(file, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent intent = context.registerReceiver(mPowerBroadcastReceiver, intentFilter);
                mCurrentPowerLevel = intent.getIntExtra("level", 0);
                mInitPowerLevel = mCurrentPowerLevel;

                new Thread(mTask).start();
            }
        }
    }

    public void stop() {
        synchronized (sInstance) {
            mIsTaskRuning = false;
            mContext.unregisterReceiver(mPowerBroadcastReceiver);
            mContext = null;
        }
    }

    public int getAverCpuRate() {
        return (int) (mTotalCpuRate / mSamplingCount);
    }

    public long getAverDalvikHeapAllocated() {
        return mDalvikHeapAllocatedTotal / mSamplingCount;
    }

    public long getAverNativeHeapAllocated() {
        return mNativeHeapAllocatedTotal / mSamplingCount;
    }

    public int getPowerConsumption() {
        return mInitPowerLevel - mCurrentPowerLevel;
    }

    public double getAverPublishFPS() {
        return Math.round((mPublishFPSTotal / mSamplingCount) * 100) / 100.0;
    }

    public double getAverPublishBitrate() {
        return Math.round((mPublishBitrateTotal / mSamplingCount) * 100) / 100.0;
    }

    public double getAverPlayFPS() {
        return Math.round((mPlayFPSTotal / mSamplingCount) * 100) / 100.0;
    }

    public double getAverPlayBitrate() {
       return Math.round((mPlayBitrateTotal / mSamplingCount) * 100) / 100.0;
    }

    public void setPublishConfig(double publishFPS, double publishBitrate) {
        mPublishFPS = Math.round(publishFPS * 100) / 100.0;
        mPublishBitrate = Math.round(publishBitrate * 100) / 100.0;
    }

    public void setPlayConfig(double playFPS, double playBitrate) {
        mPlayFPS = Math.round(playFPS * 100) / 100.0;
        mPlayBitrate = Math.round(playBitrate * 100) / 100.0;
    }

    private void samplingData() {

        mSamplingCount += 1;

        samplingMemory();

        float appCpuTime = getAppCpuTime();
        float totalCpuTime = getTotalCpuTime();
        int cpuRate = (int) (100 * (appCpuTime - mLastAppCpuTime)
                / (totalCpuTime - mLastTotalCpuTime));
        mTotalCpuRate += cpuRate;

        mSbResultLine.append(cpuRate);
        mSbResultLine.append("\t\t");

        mSbResultLine.append(mCurrentPowerLevel);
        mSbResultLine.append("\t\t");

        mLastAppCpuTime = appCpuTime;
        mLastTotalCpuTime = totalCpuTime;

        if (mIsPublishing) {
            mSbResultLine.append(mPublishFPS);
            mSbResultLine.append("\t\t");
            mPublishFPSTotal += mPublishFPS;

            mSbResultLine.append(mPublishBitrate);
            mSbResultLine.append("\t\t");
            mPublishBitrateTotal += mPublishBitrate;
        }

        if (mIsPlaying) {
            mSbResultLine.append(mPlayFPS);
            mSbResultLine.append("\t\t");
            mPlayFPSTotal += mPlayFPS;

            mSbResultLine.append(mPlayBitrate);
            mSbResultLine.append("\t\t");
            mPlayBitrateTotal += mPlayBitrate;
        }

        mSbResultLine.append("\n");

//        Log.i(PerformanceTest.TAG, "\n\n");
//        Log.i(PerformanceTest.TAG, "power consumption: " + mPowerConsumption + "%");
//        Log.i(PerformanceTest.TAG, "cpu rate: " + cpuRate + "%");
    }


    private long getTotalCpuTime() { // 获取系统总CPU使用时间
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    private long getAppCpuTime() {
        // 获取应用占用的CPU时间
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }

    private void samplingMemory() {
//        Log.i(PerformanceTest.TAG, "DalvikHeapTotal:" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");

        long dalvikAllocated = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
        mDalvikHeapAllocatedTotal += dalvikAllocated;
//        Log.i(PerformanceTest.TAG, "DalvikHeapAllocated:" + dalvikAllocated + "MB");
        mSbResultLine.append(dalvikAllocated);
        mSbResultLine.append("\t\t");

//        Log.i(PerformanceTest.TAG, "NativeHeapTotal:" + (Debug.getNativeHeapSize() / 1024 / 1024) + "MB");
        long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024 / 1024;
        mNativeHeapAllocatedTotal += nativeAllocated;
//        Log.i(PerformanceTest.TAG, "NativeHeapAllocated:" + nativeAllocated + "MB");
        mSbResultLine.append(nativeAllocated);
        mSbResultLine.append("\t\t");
    }
}
