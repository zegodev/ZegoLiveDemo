package com.zego.livedemo3.utils;

import android.text.format.DateUtils;

/**
 * 时间相关常量.
 */
public class Times {

    /**
     * 时间相关常量.
     */
    private Times() {
    }

    /**
     * 东八区时间偏移量(毫秒).
     */
    public static final long UTC_8_OFFSET = 28800000;

    /**
     * 图片渐显时间(单位:毫秒).
     */
    public static final int PICTURE_FADE_IN_TIME = 200;

    /**
     * 用户连续点击两次导航按键可以返回到内容顶部的时间间隔.
     */
    public static final long TOP_INTERVAL = 1000;

    /**
     * eg: 2013-05-25.
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * eg: 5月25日.
     */
    public static final String CN_M_DD = "M月dd日";

    /**
     * eg: 2013年5月25日.
     */
    public static final String CN_YYYY_M_DD = "yyyy年M月dd日";

    /**
     * 用于时间格式化, eg:2013-05-25 15:35:20.
     */
    public static final String YYYY_MM_DD_KK_MM_SS = "yyyy-MM-dd kk:mm:ss";

    /**
     * 用于时间解析, eg:2013-05-25 15:35:20.
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd hh:mm:ss";

    /**
     * 用户时间解析, eg:5月25日 15:35.
     */
    public static final String CN_M_DD_KK_MM = "M月dd日 kk:mm";

    /**
     * 用户时间解析, eg:2014年5月25日 15:35.
     */
    public static final String CN_YYYY_M_DD_KK_MM = "yyyy年M月dd日 kk:mm";

    /**
     * 一分钟对应的毫秒数.
     */
    public static final long ONE_MINUTE_IN_MILLIS = DateUtils.MINUTE_IN_MILLIS;

    /**
     * 一小时对应的毫秒数.
     */
    public static final long ONE_HOUR_IN_MILLIS = DateUtils.HOUR_IN_MILLIS;

    /**
     * 一天对应的毫秒数.
     */
    public static final long ONE_DAY_IN_MILLIS = DateUtils.DAY_IN_MILLIS;

    /**
     *
     */
    public static final long ONE_YEAR_IN_MILLIS = DateUtils.YEAR_IN_MILLIS;

}
