package com.task.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtil {
    public static Date addDaysToNow(long days) {
        long nowInMs = System.currentTimeMillis();
        long daysInMs = TimeUnit.DAYS.toMillis(days);
        return new Date(nowInMs + daysInMs);
    }

    public static boolean isDateBeforeNow(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }
}
