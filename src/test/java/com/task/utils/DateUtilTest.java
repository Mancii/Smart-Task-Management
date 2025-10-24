package com.task.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void addDaysToNow_ShouldReturnCorrectDate_WhenPositiveDays() {
        // Given
        long days = 7;
        long expectedTimeInMs = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days);
        
        // When
        Date result = DateUtil.addDaysToNow(days);
        
        // Then
        assertNotNull(result);
        // Allow for small time difference due to execution time
        long actualTimeInMs = result.getTime();
        assertTrue(Math.abs(actualTimeInMs - expectedTimeInMs) < 1000, 
            "Date should be within 1 second of expected time");
    }

    @Test
    void addDaysToNow_ShouldReturnCorrectDate_WhenZeroDays() {
        // Given
        long days = 0;
        long expectedTimeInMs = System.currentTimeMillis();
        
        // When
        Date result = DateUtil.addDaysToNow(days);
        
        // Then
        assertNotNull(result);
        long actualTimeInMs = result.getTime();
        assertTrue(Math.abs(actualTimeInMs - expectedTimeInMs) < 1000, 
            "Date should be within 1 second of current time");
    }

    @Test
    void addDaysToNow_ShouldReturnCorrectDate_WhenNegativeDays() {
        // Given
        long days = -5;
        long expectedTimeInMs = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days);
        
        // When
        Date result = DateUtil.addDaysToNow(days);
        
        // Then
        assertNotNull(result);
        long actualTimeInMs = result.getTime();
        assertTrue(Math.abs(actualTimeInMs - expectedTimeInMs) < 1000, 
            "Date should be within 1 second of expected time");
        assertTrue(result.before(new Date()), "Result should be in the past");
    }

    @Test
    void addDaysToNow_ShouldReturnCorrectDate_WhenLargeDays() {
        // Given
        long days = 365; // One year
        long expectedTimeInMs = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days);
        
        // When
        Date result = DateUtil.addDaysToNow(days);
        
        // Then
        assertNotNull(result);
        long actualTimeInMs = result.getTime();
        assertTrue(Math.abs(actualTimeInMs - expectedTimeInMs) < 1000, 
            "Date should be within 1 second of expected time");
    }

    @Test
    void isDateBeforeNow_ShouldReturnTrue_WhenDateIsInPast() {
        // Given
        Date pastDate = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        
        // When
        boolean result = DateUtil.isDateBeforeNow(pastDate);
        
        // Then
        assertTrue(result, "Past date should be before now");
    }

    @Test
    void isDateBeforeNow_ShouldReturnFalse_WhenDateIsInFuture() {
        // Given
        Date futureDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        
        // When
        boolean result = DateUtil.isDateBeforeNow(futureDate);
        
        // Then
        assertFalse(result, "Future date should not be before now");
    }

    @Test
    void isDateBeforeNow_ShouldReturnFalse_WhenDateIsNull() {
        // Given
        Date nullDate = null;
        
        // When
        boolean result = DateUtil.isDateBeforeNow(nullDate);
        
        // Then
        assertFalse(result, "Null date should return false");
    }

    @Test
    void isDateBeforeNow_ShouldReturnTrue_WhenDateIsExactlyNow() {
        // Given - Create a date that's very close to now but slightly in the past
        Date nowDate = new Date(System.currentTimeMillis() - 1);
        
        // When
        boolean result = DateUtil.isDateBeforeNow(nowDate);
        
        // Then
        assertTrue(result, "Date slightly in the past should be before now");
    }

    @Test
    void isDateBeforeNow_ShouldHandleEdgeCases() {
        // Test with epoch time
        Date epochDate = new Date(0);
        assertTrue(DateUtil.isDateBeforeNow(epochDate), "Epoch date should be before now");
        
        // Test with very far future
        Date farFuture = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10000));
        assertFalse(DateUtil.isDateBeforeNow(farFuture), "Far future date should not be before now");
        
        // Test with very far past
        Date farPast = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10000));
        assertTrue(DateUtil.isDateBeforeNow(farPast), "Far past date should be before now");
    }

    @Test
    void addDaysToNow_AndIsDateBeforeNow_ShouldWorkTogether() {
        // Test the integration of both methods
        
        // Create a date 5 days in the future
        Date futureDate = DateUtil.addDaysToNow(5);
        assertFalse(DateUtil.isDateBeforeNow(futureDate), 
            "Date created 5 days in future should not be before now");
        
        // Create a date 5 days in the past
        Date pastDate = DateUtil.addDaysToNow(-5);
        assertTrue(DateUtil.isDateBeforeNow(pastDate), 
            "Date created 5 days in past should be before now");
        
        // Create a date for today (0 days)
        Date todayDate = DateUtil.addDaysToNow(0);
        // This might be true or false depending on execution timing, but should be very close to now
        long timeDiff = Math.abs(todayDate.getTime() - System.currentTimeMillis());
        assertTrue(timeDiff < 1000, "Today date should be within 1 second of now");
    }
}