package com.omicseq.utils;

public class ThreadUtils {

    /**
     * Pauses for a given number of milliseconds
     * 
     * @param time
     *            number of milliseconds for which to pause
     */
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}
