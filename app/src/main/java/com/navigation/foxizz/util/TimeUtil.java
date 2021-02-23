package com.navigation.foxizz.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 */
public class TimeUtil {

    public final static String FORMATION_yMdHms = "yy-MM-dd HH:mm:ss";
    public final static String FORMATION_yMd = "yy-MM-dd";
    public final static String FORMATION_yM = "yy-MM";
    public final static String FORMATION_Hms = "HH:mm:ss";
    public final static String FORMATION_Hm = "HH:mm";

    /**
     * 格式化日期
     *
     * @param date      日期
     * @param formation 格式
     * @return 格式化的日期
     */
    @SuppressLint("SimpleDateFormat")
    public static String format(Date date, String formation) {
        DateFormat sdf = new SimpleDateFormat(formation);
        return sdf.format(date);
    }

    /**
     * 反格式化日期
     *
     * @param formatString 格式化的日期
     * @param formation    格式
     * @return 日期
     */
    @SuppressLint("SimpleDateFormat")
    public static Date parse(String formatString, String formation) {
        DateFormat sdf = new SimpleDateFormat(formation);
        try {
            return sdf.parse(formatString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    /**
     * 判断是否在时间内
     *
     * @param nowTime   现在的时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return boolean
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime != null && startTime != null && endTime != null) {
            if (nowTime.getTime() == startTime.getTime()
                    || nowTime.getTime() == endTime.getTime()) {
                return true;
            }
            if (startTime.getTime() > endTime.getTime()) {//结束时间超过24点时进入下一天
                endTime.setTime(endTime.getTime() + 24 * 60 * 60 * 1000);
            }
            return nowTime.after(startTime) && nowTime.before(endTime);
        }
        return false;
    }

}
