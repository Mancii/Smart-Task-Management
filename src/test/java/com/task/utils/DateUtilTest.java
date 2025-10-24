package com.task.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void addDaysToNow_returnsDateCloseToExpectedOffset() {
        long days = 2;
        Date result = DateUtil.addDaysToNow(days);
        long expectedDelta = TimeUnit.DAYS.toMillis(days);
        long actualDelta = result.getTime() - System.currentTimeMillis();
        long toleranceMs = 15_000; // 15 seconds tolerance for test runtime
        assertTrue(Math.abs(actualDelta - expectedDelta) <= toleranceMs,
                "Date offset should be within tolerance");
    }

    @Test
    void isDateBeforeNow_handlesPastFutureAndNull() {
        Date past = new Date(System.currentTimeMillis() - 1_000);
        Date future = new Date(System.currentTimeMillis() + 60_000);
        assertTrue(DateUtil.isDateBeforeNow(past));
        assertFalse(DateUtil.isDateBeforeNow(future));
        assertFalse(DateUtil.isDateBeforeNow(null));
    }
}
