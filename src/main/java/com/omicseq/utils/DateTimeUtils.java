package com.omicseq.utils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DateTimeUtils {
    private static PeriodFormatter formatter = new PeriodFormatterBuilder().appendYears().appendMonths().appendDays()
            .appendSeparator("天").appendHours().appendSeparator("时").appendMinutes().appendSeparator("分")
            .appendSeconds().appendSeparator("秒").appendMillis().appendLiteral("毫秒").toFormatter();

    /**
     * 用于显示两个时间差,比如日志。
     * 
     * @param start
     * @param end
     * @return
     */
    public static String diff(DateTime start, DateTime end) {
        return new Period(start, end).toString(formatter);
    }

    /**
     * 显示使用时间
     * @param dt
     * @return
     */
    public static String used(DateTime dt) {
        return diff(dt,DateTime.now());
    }
    public static void main(String[] args) {
        long begin = System.nanoTime();
        DateTime dt = DateTime.now();
        dt = dt.plusHours(-2);
        dt = dt.plusMinutes(-10).plusSeconds(-5).plusMillis(-30);
        System.out.println(diff(dt, DateTime.now()));
        System.out.println((System.nanoTime() - begin)/1000+"微秒");
    }

}
