package com.zego.zegolivedemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Mark on 2016/3/11
 *
 * Des: 自定义Application..
 * -*+--------
 *
 */
public class ZegoApplication extends Application{

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = this;

        // 初始化zego sdk
        initZegoSDK(this);

        // 初始化ImageLoader
        initImageLoader();
    }

    /**
     * 初始化Zego SDK.
     *
     * @param context
     */
    private void initZegoSDK(Context context){
        // 初始化sdk
        ZegoApiManager.getInstance().initSdk(context);
    }

    /**
     * 初始化ImageLoader.
     */
    private void initImageLoader() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(sContext)
                .memoryCache(new WeakMemoryCache())
                .defaultDisplayImageOptions(options)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(configuration);
    }

    public static Context getContext() {
        return sContext;
    }
}
