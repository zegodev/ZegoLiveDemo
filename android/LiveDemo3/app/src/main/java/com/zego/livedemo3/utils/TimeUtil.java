package com.zego.livedemo3.utils;

import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.zego.livedemo3.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具.
 */
public final class TimeUtil {

    /**
     * 时间工具.
     */
    private TimeUtil() {
    }

    /**
     * 获取Calendar对象.
     *
     * @param millis 时间戳
     * @return Calendar对象
     */
    public static Calendar getCalendar(final long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    /**
     * 把用户时区的时间戳转成UTC时间戳.
     *
     * @param millis 用户时区的时间戳
     * @return UTC时间戳
     */
    public static long getUtcMillis(final long millis) {
        Calendar calendar = getCalendar(millis);
        long offset = calendar.get(Calendar.ZONE_OFFSET);
        return millis - offset;
    }

    /**
     * 把用户时区的时间戳转成东八区时间戳
     *
     * @param millis 用户时区的时间戳
     * @return 东八区时间戳
     */
    public static long getMillis(final long millis) {
        return getUtcMillis(millis) + Times.UTC_8_OFFSET;
    }

    /**
     * 把客户端时间戳（13位）转换成PHP服务器的时间戳（11位）
     *
     * @param millis 客户端时间戳
     * @return PHP服务器时间戳
     */
    public static long getPhpSeconds(final long millis) {
        return millis / 1000;
    }

    /**
     * 把PHP服务器的时间戳（11位）转换成客户端时间戳（13位）.
     *
     * @param seconds 服务器时间戳
     * @return 客户端时间戳
     */
    public static long getAndroidMillis(final long seconds) {
        return seconds * 1000;
    }

    /**
     * 获取东八区当前时间戳.
     *
     * @return 东八区当前时间戳
     */
    public static long getCurMillis() {
        return getMillis(System.currentTimeMillis());
    }

    /**
     * 获取东八区当前时间戳.
     *
     * @return 东八区当前时间戳
     */
    public static int getCurSeconds() {
        return (int) (getCurMillis() / 1000);
    }


    /**
     * 获取东八区当前时间的描述(yyyy-MM-dd).
     *
     * @return 东八区当前时间的描述
     */
    public static String getCurDateText() {
        long curMillis = getCurMillis();
        return TimeUtil.format(curMillis, Times.YYYY_MM_DD);
    }

    /**
     * 获取当前时间的描述(yyyy-MM-dd HH:mm:ss).
     *
     * @return 当前时间的描述
     */
    public static String getCurDateTimeText() {
        long curMillis = getCurMillis();
        return TimeUtil.format(curMillis, Times.YYYY_MM_DD_KK_MM_SS);
    }

    /**
     * 把时间文本转换成时间戳.
     *
     * @param dateText 时间文本
     * @param fmt      时间格式
     * @return 时间戳
     * @throws ParseException 时间转换异常
     */
    public static long parseMillis(final String dateText, final String fmt) throws ParseException {
        if (TextUtils.isEmpty(dateText)) {
            throw new NullPointerException("dateText不能为空");
        } else if (TextUtils.isEmpty(fmt)) {
            throw new NullPointerException("fmt不能为空");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.getDefault());
        Date date = sdf.parse(dateText);
        return date.getTime();
    }

    /**
     * 格式化时间戳.
     *
     * @param millis 时间戳
     * @param fmt    时间格式
     * @return 格式化时间文本
     */
    public static String format(final long millis, final String fmt) {
        if (TextUtils.isEmpty(fmt)) {
            throw new NullPointerException("fmt不能为空");
        }
        return (String) DateFormat.format(fmt, millis);
    }

    /**
     * 将输入时间文本格式化为其他格式的文本.
     *
     * @param inDateText 输入时间文本
     * @param inFmt      输入时间格式
     * @param outFmt     输出时间格式
     * @return 格式化后的时间文本
     * @throws ParseException 时间转换异常
     */
    public static String format(final String inDateText, final String inFmt, final String outFmt)
            throws ParseException {
        if (TextUtils.isEmpty(inDateText)) {
            throw new NullPointerException("inDateText不能为空");
        } else if (TextUtils.isEmpty(inFmt)) {
            throw new NullPointerException("inFmt不能为空");
        } else if (TextUtils.isEmpty(outFmt)) {
            throw new NullPointerException("outFmt不能为空");
        }
        long millis = TimeUtil.parseMillis(inDateText, inFmt);
        return TimeUtil.format(millis, outFmt);
    }

    /**
     * 根据当前时间生成合适的时间描述.
     *
     * @param resources  Resources
     * @param millis     需要比较的时间
     * @param isShowTime 跨年时间是否显示时间, true - 显示, false - 不显示
     * @return 时间描述, 例如1分钟前
     */
    public static String getRelativeTime(final Resources resources, final long millis, final boolean isShowTime) {
        if (resources == null) {
            throw new NullPointerException("resources不能为空");
        }
        //long curMillis = getCurMillis();
        long curMillis = System.currentTimeMillis() /  1000;
        long offset = (curMillis - millis) * 1000;
        if (offset < Times.ONE_MINUTE_IN_MILLIS) {
            return resources.getString(R.string.recent); // 刚刚
        } else if (offset < Times.ONE_HOUR_IN_MILLIS) {
            long minutes = offset / Times.ONE_MINUTE_IN_MILLIS;
            return minutes + resources.getString(R.string.minutes_ago); // 几分钟
        } else if (offset < Times.ONE_DAY_IN_MILLIS) {
            long hours = offset / Times.ONE_HOUR_IN_MILLIS;
            return hours + resources.getString(R.string.hours_ago); // 几小时
        } else {
            Calendar calendar = getCalendar(millis);
            Calendar curCalendar = Calendar.getInstance();
            if (calendar.get(Calendar.YEAR) == curCalendar.get(Calendar.YEAR)) {
                return format(millis, Times.CN_M_DD_KK_MM); // 未跨年
            } else {
                if (isShowTime) {
                    return format(millis, Times.CN_YYYY_M_DD_KK_MM); // 显示时间
                } else {
                    return format(millis, Times.CN_YYYY_M_DD); // 不显示时间
                }
            }
        }
    }

    /**
     * 获取与当前的时间的距离描述，比如200天前,1年前
     */
    public static String getRelativeTimeWithSingleUnit(final Resources resources, final long millis,
            final boolean isShowTime) {
        if (resources == null) {
            throw new NullPointerException("resources不能为空");
        }
        //long curMillis = getCurMillis();
        long curMillis = System.currentTimeMillis();
        long offset = curMillis - millis;
        if (offset < Times.ONE_MINUTE_IN_MILLIS) {
            return resources.getString(R.string.recent); // 刚刚
        } else if (offset < Times.ONE_HOUR_IN_MILLIS) {
            long minutes = offset / Times.ONE_MINUTE_IN_MILLIS;
            return minutes + resources.getString(R.string.minutes_ago); // 几分钟
        } else if (offset < Times.ONE_DAY_IN_MILLIS) {
            long hours = offset / Times.ONE_HOUR_IN_MILLIS;
            return hours + resources.getString(R.string.hours_ago); // 几小时
        } else if (offset < Times.ONE_YEAR_IN_MILLIS) {
            long days = offset / Times.ONE_DAY_IN_MILLIS;
            return days + resources.getString(R.string.day_ago);//几天
        } else {
            long years = offset / Times.ONE_YEAR_IN_MILLIS;
            return years + resources.getString(R.string.year_ago);//几年
        }
    }


    /**
     * 按日调整时间.
     *
     * @param timeText 时间
     * @param format   时间的格式
     * @param days     调整的天数
     * @return 调整后的时间
     * @throws ParseException 时间转换异常
     */
    public static String modDays(final String timeText, final String format, final int days) throws ParseException {
        if (TextUtils.isEmpty(timeText)) {
            throw new NullPointerException("timeText不能为空");
        } else if (TextUtils.isEmpty(format)) {
            throw new NullPointerException("format不能为空");
        }
        long millis = TimeUtil.parseMillis(timeText, format);
        Calendar calendar = getCalendar(millis);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + days);
        millis = calendar.getTimeInMillis();
        return TimeUtil.format(millis, format);
    }

    /**
     * 时间是否在年内.
     *
     * @param timeText 时间
     * @param format   时间的格式
     * @return true - 时间在年内, false - 时间不在年内
     * @throws ParseException 时间转换异常
     */
    public static boolean isThisYear(final String timeText, final String format) throws ParseException {
        if (TextUtils.isEmpty(timeText)) {
            throw new NullPointerException("timeText不能为空");
        } else if (TextUtils.isEmpty(format)) {
            throw new NullPointerException("format不能为空");
        }
        long millis = TimeUtil.parseMillis(timeText, format);
        Calendar calendar = getCalendar(millis);
        Calendar curCalendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == curCalendar.get(Calendar.YEAR);
    }

    /**
     * 计算相对时间，天为单位.
     *
     * @param lastMillis 过去的时间
     * @return 相对的天数
     */
    public static long getRelativeDays(long lastMillis) {
        return (getCurMillis() - getAndroidMillis(lastMillis)) / (1000 * 60 * 60 * 24);
    }
}
