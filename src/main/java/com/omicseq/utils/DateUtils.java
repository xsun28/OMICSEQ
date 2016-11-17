package com.omicseq.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class DateUtils {
    private static final String EMPTY = "";
    public static final String FT_LONG_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FT_DATE = "yyyy-MM-dd";
    public static final String FT_DATE_TIME = "yyyy-MM-dd HH:mm";
    public static final String FT_SORT_TIME = "HH:mm";
    public static final String FT_TIME = "HH:mm:ss";
    /**
     * units 时间间隔单位
     */
    public static final String[] units = { "y", "m", "d", "w" };
    public static final String[] weeks = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

    private DateUtils() {
    }

    public static java.sql.Date toDate(String sdate) {
        return toDate(sdate, FT_DATE);
    }

    public static java.sql.Date toDate(String sdate, String format) {
        Date date = parseToDate(sdate, format);
        return date == null ? null : new java.sql.Date(date.getTime());
    }

    public static Time toTime(String stime) {
        return toTime(stime, FT_SORT_TIME);
    }

    public static Time toTime(String stime, String format) {
        Date date = parseToDate(stime, format);
        return StringUtils.isBlank(stime) ? null : new Time(date.getTime());
    }

    public static Timestamp toDateTime(String dateTime) {
        return toDateTime(dateTime, FT_DATE_TIME);
    }

    public static Timestamp toDateTime(String dateTime, String format) {
        Date _date = parseToDate(dateTime, format);
        return _date == null ? null : new Timestamp(_date.getTime());
    }

    public static Date parseToTime(String date) {
        return parseToDate(date, FT_SORT_TIME);
    }

    public static Date parseToDate(String date) {
        return parseToDate(date, FT_DATE);
    }

    public static Timestamp parseToTimestamp(String date) {
        Date _date = parseToDate(date, FT_DATE_TIME);
        return _date == null ? null : new Timestamp(_date.getTime());
    }

    public static Date parseToDate(String date, String format) {
        try {
            if (StringUtils.isNotBlank(date)) {
                java.text.DateFormat df = new java.text.SimpleDateFormat(format);
                return df.parse(date);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 日期相减
     * 
     * @param date
     *            日期
     * @param date1
     *            日期
     * @return 返回相减后的日期
     */
    public static long diffDate(java.util.Date date, java.util.Date date1) {
        return ((date.getTime() - date1.getTime()) / (24 * 3600 * 1000));
    }

    /**
     * 格式化输出日期
     * @param date 日期
     * @param format  格式
     * @return 返回字符型日期
     */
    public static String format(java.util.Date date, String format) {
        return format(date, format, Locale.getDefault());
    }

    /** 
     * 格式化输出日期 支持Locale
    * @param date 日期
    * @param format 格式
    * @param locale 返回字符型日期
    * @return
    */
    public static String format(java.util.Date date, String format, Locale locale) {
        try {
            if (null != date) {
                java.text.DateFormat df = new java.text.SimpleDateFormat(format, locale);
                return df.format(date);
            }
        } catch (Exception e) {
        }
        return EMPTY;
    }

    /**
     * 获取比当前日期早多少天或者晚多少天的日期 例如 前五天 －5 后五天 5
     * 
     * @param days
     * @param format
     *            返回日期的格式
     * @return 格式化好的字符串
     */
    public static String DateBefAft(int days, String format) {
        //
        format = StringUtils.isBlank(format) ? FT_DATE : format;
        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        now.add(Calendar.DAY_OF_YEAR, days);
        return formatter.format(now.getTime());
    }

    public static Date DateBefAft(int days) {
        //
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, days);
        return now.getTime();
    }

    /**
     * 获取比指定日期早多少天或者晚多少天的日期 例如 前五天 －5 后五天 5
     * 
     * @param days
     * @param format 返回日期的格式
     * @param date  指定日期
     * @return 格式化好的字符串
     */
    public static String DateBefAft(int days, String format, Date date) {
        //
        format = StringUtils.isBlank(format) ? FT_DATE : format;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        gc.add(Calendar.DAY_OF_YEAR, days);
        return formatter.format(gc.getTime());
    }

    /**
     * <p>
     * 返回一个当前的时间，并按格式转换为字符串
     * </p>
     * 例：17:27:03
     * 
     * @return String
     */
    public static String getNowTime() {
        GregorianCalendar gcNow = new GregorianCalendar();
        java.util.Date dNow = gcNow.getTime();
        DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.SIMPLIFIED_CHINESE);
        return df.format(dNow);
    }

    /**
     * <p>
     * 返回一个当前日期，并按格式转换为字符串
     * </p>
     * 例：2004-4-30
     * 
     * @return String
     */
    public static String getNowDate() {
        GregorianCalendar gcNow = new GregorianCalendar();
        java.util.Date dNow = gcNow.getTime();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.SIMPLIFIED_CHINESE);
        return df.format(dNow);
    }

    /**
     * <p>
     * 返回一个当前日期和时间，并按格式转换为字符串
     * </p>
     * 例：2004-4-30 17:27:03
     * 
     * @return String
     */
    public static String getNowDateTime() {
        GregorianCalendar gcNow = new GregorianCalendar();
        java.util.Date dNow = gcNow.getTime();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.SIMPLIFIED_CHINESE);
        return df.format(dNow);
    }

    /**
     * <p>
     * 返回当前年
     * </p>
     * 
     * @return int
     */
    public static int getThisYear() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.YEAR);
    }

    /**
     * 返回本月
     * 
     * @return int
     */
    public static int getThisMonth() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.MONTH) + 1;
    }

    /**
     * 返回今天是本月的第几天
     * 
     * @return int 从1开始
     */
    public static int getToDayOfMonth() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.DAY_OF_MONTH);
    }

    /**
     * 返回当前的小时
     * 
     * @return int
     */
    public static int getHour() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.HOUR);
    }

    /**
     * 返回当前的分钟
     * 
     * @return int 返回当前的分钟
     */
    public static int getMinute() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.MINUTE);
    }

    /**
     * 返回当前的秒数
     * 
     * @return int 第几秒
     */
    public static int getSecond() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.SECOND);
    }

    /**
     * 返回今天是本年的第几周
     * 
     * @return int 从1开始
     */

    public static int getToWeekOfYear() {
        GregorianCalendar gcNow = new GregorianCalendar();
        return gcNow.get(GregorianCalendar.WEEK_OF_YEAR);
    }

    /**
     * 返回一格式化的日期
     * 
     * @param time
     *            long
     * @return String yyyy-mm-dd 格式
     */
    public static String formatDate(java.util.Date date) {
        if (date == null) {
            return EMPTY;
        } else {
            return format(date, FT_DATE);
        }
    }

    /**
     * 返回一格式化的日期时间
     * 
     * @param time
     *            long
     * @return String  yyyy-MM-dd HH:mm:ss
     */
    public static String formatSDate(java.util.Date date) {
        try {
            if (null != date) {
                SimpleDateFormat bartDateFormat = new SimpleDateFormat(FT_LONG_DATE_TIME);
                return bartDateFormat.format(date);
            }
        } catch (Exception e) {
        }
        return EMPTY;
    }

    /** 返回一格式化的时间
    * @param date
    * @return
    */
    public static String formatTime(java.util.Date date) {
        try {
            if (null != date) {
                SimpleDateFormat bartDateFormat = new SimpleDateFormat(FT_TIME);
                return bartDateFormat.format(date);
            }
        } catch (Exception e) {
        }
        return EMPTY;
    }

    /**
     * 返回已添加指定时间间隔的日期
     * 
     * @param interval
     *            表示要添加的时间间隔("y":年;"d":天;"m":月;如有必要可以自行增加)
     * @param number
     *            表示要添加的时间间隔的个数
     * @param date
     *            java.util.Date()
     * @return String 2005-5-12格式的日期字串
     */
    public static Date dateAdd1(String interval, int number, java.util.Date date) {
        Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        // 加若干年
        if (interval.equals("y")) {
            int currYear = gc.get(Calendar.YEAR);
            gc.set(Calendar.YEAR, currYear + number);
        }
        // 加若干月
        else if (interval.equals("m")) {
            int currMonth = gc.get(Calendar.MONTH);
            gc.set(Calendar.MONTH, currMonth + number);
        }
        // 加若干天
        else if (interval.equals("d")) {
            int currDay = gc.get(Calendar.DATE);
            gc.set(Calendar.DATE, currDay + number);
        }
        // 加若小时
        else if (interval.equals("h")) {
            int currDay = gc.get(Calendar.HOUR);
            gc.set(Calendar.HOUR, currDay + number);
        }
        // 加若周
        else if (interval.equals("w")) {
            int currDay = gc.get(Calendar.WEEK_OF_YEAR);
            gc.set(Calendar.WEEK_OF_YEAR, currDay + number);
        }
        return gc.getTime();
    }

    /**
     * 返回已添加指定时间间隔的日期
     * 
     * @param interval
     *            表示要添加的时间间隔("y":年;"d":天;"m":月;如有必要可以自行增加)
     * @param number
     *            表示要添加的时间间隔的个数
     * @param date
     *            java.util.Date()
     * @return String 2005-5-12格式的日期字串
     */
    public static String dateAdd(String interval, int number, java.util.Date date) {
        Date date1 = dateAdd1(interval, number, date);
        SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        String strTmp = bartDateFormat.format(date1);
        return strTmp;
    }

    public static int getYear(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return gc.get(GregorianCalendar.YEAR);
    }

    public static int getMonth(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return gc.get(GregorianCalendar.MONTH) + 1;
    }

    public static int compareMonth(Date firstDate, Date secondDate) {
        int firstYear;
        int firstMonth;
        int secondYear;
        int secondMonth;
        firstYear = getYear(firstDate);
        firstMonth = getMonth(firstDate);

        secondYear = getYear(secondDate);
        secondMonth = getMonth(secondDate);

        int year = secondYear - firstYear;
        int month = secondMonth - firstMonth;

        return Math.abs(year * 12 + month);
    }

    /**
     * <p>
     * 返回两个日期之间的单位间隔数
     * </p>
     * 
     * @param a
     *            java.util.Date
     * @param b
     *            java.util.Date
     * @return int 间隔数
     */
    public static int dateDiff(java.util.Date a, java.util.Date b) {
        int tempDifference = 0;
        int difference = 0;
        Calendar earlier = Calendar.getInstance();
        Calendar later = Calendar.getInstance();

        if (a.compareTo(b) < 0) {
            earlier.setTime(a);
            later.setTime(b);
        } else {
            earlier.setTime(b);
            later.setTime(a);
        }

        while (earlier.get(Calendar.YEAR) != later.get(Calendar.YEAR)) {
            tempDifference = 365 * (later.get(Calendar.YEAR) - earlier.get(Calendar.YEAR));
            difference += tempDifference;

            earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
        }

        if (earlier.get(Calendar.DAY_OF_YEAR) != later.get(Calendar.DAY_OF_YEAR)) {
            tempDifference = later.get(Calendar.DAY_OF_YEAR) - earlier.get(Calendar.DAY_OF_YEAR);
            difference += tempDifference;

            earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
        }

        return difference;
    }

    /**
     * <p>
     * 该方法是获得到每月1号星期一的数据
     * </p>
     * 
     * @return -返回一个数字
     */
    /**
     * <p>
     * 该方法是获得到每月1号星期一的数据
     * </p>
     * 
     * @return -返回一个数字
     */
    public static int getDate(int curYear, int curMonth, int curDate) {
        int day1 = 0;
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(curYear, curMonth - 1, curDate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
        case 1: // 星期天
            day1 = 0;
            break;
        case 2: // 星期一
            day1 = 1;
            break;
        case 3: // 星期二
            day1 = 2;
            break;
        case 4: // 星期三
            day1 = 3;
            break;
        case 5: // 星期四
            day1 = 4;
            break;
        case 6: // 星期五
            day1 = 5;
            break;
        case 7: // 星期六
            day1 = 6;
            break;
        }
        return day1;
    }

    /**
     * <p>
     * 比较两日期字符串的大小
     * </p>
     * 
     * @param d1
     * @param d2
     * @return (d1>d2)?2:(d1=d2)?1:0
     */
    public static int compareDate(String d1, String d2) {
        int vl = 1;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(parseToDate(d1, FT_TIME));
        int year = gc.get(GregorianCalendar.YEAR);
        int month = gc.get(GregorianCalendar.MONTH);
        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
        gc.setTime(parseToDate(d2, FT_TIME));
        int tempYear = gc.get(GregorianCalendar.YEAR);
        int tempMonth = gc.get(GregorianCalendar.MONTH);
        int tempDay = gc.get(GregorianCalendar.DAY_OF_MONTH);
        if (year != tempYear) {
            vl = (year > tempYear) ? 2 : 0;
        } else {
            if (month != tempMonth) {
                vl = (month > tempMonth) ? 2 : 0;
            } else if (day != tempDay) {
                vl = (day > tempDay) ? 2 : 0;
            }
        }
        return vl;
    }

    /**
     * 获取现在时间
     * 
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public static java.sql.Timestamp getNowSqlDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(FT_LONG_DATE_TIME);
        String dateString = formatter.format(currentTime);
        java.sql.Timestamp currentTime_2 = java.sql.Timestamp.valueOf(dateString);
        return currentTime_2;
    }

    public static java.sql.Timestamp getNowSqlDate2() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(FT_DATE_TIME);
        String dateString = formatter.format(currentTime);
        java.sql.Timestamp currentTime_2 = java.sql.Timestamp.valueOf(dateString);
        return currentTime_2;
    }

    /**
     * 获取现在日期
     * 
     * @return 返回时间类型 yyyy-MM-dd
     */
    public static java.sql.Timestamp getSqlDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String dateString = formatter.format(currentTime);
        java.sql.Timestamp currentTime_2 = java.sql.Timestamp.valueOf(dateString);
        return currentTime_2;
    }

    public static String getWeekName(int week) {
        return weeks[week - 1];
    }

}
