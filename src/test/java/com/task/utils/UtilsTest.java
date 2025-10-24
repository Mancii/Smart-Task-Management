package com.task.utils;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void getRandomNumberInRange_withInvalidRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> Utils.getRandomNumberInRange(5, 5));
        assertThrows(IllegalArgumentException.class, () -> Utils.getRandomNumberInRange(10, 5));
    }

    @RepeatedTest(5)
    void getRandomNumberInRange_returnsWithinRange_andNotStartingWithZero() {
        int min = 1;
        int max = 9999;
        String value = Utils.getRandomNumberInRange(min, max);
        assertFalse(value.startsWith("0"));
        int parsed = Integer.parseInt(value);
        assertTrue(parsed >= min && parsed <= max);
    }

    @Test
    void isNotEmpty_stringVariants() {
        assertTrue(Utils.isNotEmpty("a"));
        assertFalse(Utils.isNotEmpty(""));
        assertFalse(Utils.isNotEmpty((String) null));
    }

    @Test
    void isNotEmpty_listVariants() {
        List<String> list = Arrays.asList("a");
        List<String> empty = new ArrayList<>();
        assertTrue(Utils.isNotEmpty(list));
        assertFalse(Utils.isNotEmpty(empty));
        assertFalse(Utils.isNotEmpty((List<String>) null));
    }

    @Test
    void isNotEmpty_objectVariant() {
        assertTrue(Utils.isNotEmpty(new Object()));
        assertFalse(Utils.isNotEmpty((Object) null));
    }
}
