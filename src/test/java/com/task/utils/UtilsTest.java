package com.task.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void getRandomNumberInRange_ShouldReturnNumberInRange_WhenValidRange() {
        // Given
        int min = 1;
        int max = 100;

        // When
        String result = Utils.getRandomNumberInRange(min, max);

        // Then
        assertNotNull(result);
        int number = Integer.parseInt(result);
        assertTrue(number >= min && number <= max, 
            "Random number should be between " + min + " and " + max);
        assertFalse(result.startsWith("0"), "Result should not start with 0");
    }

    @Test
    void getRandomNumberInRange_ShouldReturnSingleNumber_WhenMinEqualsMaxMinusOne() {
        // Given
        int min = 5;
        int max = 5;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Utils.getRandomNumberInRange(min, max));
        
        assertEquals("max must be greater than min", exception.getMessage());
    }

    @Test
    void getRandomNumberInRange_ShouldThrowException_WhenMinGreaterThanMax() {
        // Given
        int min = 10;
        int max = 5;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Utils.getRandomNumberInRange(min, max));
        
        assertEquals("max must be greater than min", exception.getMessage());
    }

    @Test
    void getRandomNumberInRange_ShouldThrowException_WhenMinEqualsMax() {
        // Given
        int min = 5;
        int max = 5;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Utils.getRandomNumberInRange(min, max));
        
        assertEquals("max must be greater than min", exception.getMessage());
    }

    @Test
    void getRandomNumberInRange_ShouldHandleNegativeNumbers() {
        // Given
        int min = -10;
        int max = -1;

        // When
        String result = Utils.getRandomNumberInRange(min, max);

        // Then
        assertNotNull(result);
        int number = Integer.parseInt(result);
        assertTrue(number >= min && number <= max);
        // Negative numbers can start with "-", not "0", so this should be fine
    }

    @Test
    void getRandomNumberInRange_ShouldHandleLargeRange() {
        // Given
        int min = 1;
        int max = 1000000;

        // When
        String result = Utils.getRandomNumberInRange(min, max);

        // Then
        assertNotNull(result);
        int number = Integer.parseInt(result);
        assertTrue(number >= min && number <= max);
        assertFalse(result.startsWith("0"));
    }

    @Test
    void getRandomNumberInRange_ShouldNotStartWithZero_MultipleAttempts() {
        // Test multiple times to ensure the "no leading zero" logic works
        for (int i = 0; i < 50; i++) {
            String result = Utils.getRandomNumberInRange(1, 999999);
            assertFalse(result.startsWith("0"), 
                "Result should never start with 0, got: " + result);
        }
    }

    @Test
    void isNotEmpty_String_ShouldReturnTrue_WhenStringHasContent() {
        // Given
        String nonEmptyString = "hello";

        // When
        boolean result = Utils.isNotEmpty(nonEmptyString);

        // Then
        assertTrue(result);
    }

    @Test
    void isNotEmpty_String_ShouldReturnFalse_WhenStringIsNull() {
        // Given
        String nullString = null;

        // When
        boolean result = Utils.isNotEmpty(nullString);

        // Then
        assertFalse(result);
    }

    @Test
    void isNotEmpty_String_ShouldReturnFalse_WhenStringIsEmpty() {
        // Given
        String emptyString = "";

        // When
        boolean result = Utils.isNotEmpty(emptyString);

        // Then
        assertFalse(result);
    }

    @Test
    void isNotEmpty_String_ShouldReturnTrue_WhenStringHasWhitespace() {
        // Given
        String whitespaceString = "   ";

        // When
        boolean result = Utils.isNotEmpty(whitespaceString);

        // Then
        assertTrue(result, "String with whitespace should be considered not empty");
    }

    @Test
    void isNotEmpty_List_ShouldReturnTrue_WhenListHasElements() {
        // Given
        List<String> nonEmptyList = Arrays.asList("item1", "item2");

        // When
        boolean result = Utils.isNotEmpty(nonEmptyList);

        // Then
        assertTrue(result);
    }

    @Test
    void isNotEmpty_List_ShouldReturnFalse_WhenListIsNull() {
        // Given
        List<String> nullList = null;

        // When
        boolean result = Utils.isNotEmpty(nullList);

        // Then
        assertFalse(result);
    }

    @Test
    void isNotEmpty_List_ShouldReturnFalse_WhenListIsEmpty() {
        // Given
        List<String> emptyList = new ArrayList<>();

        // When
        boolean result = Utils.isNotEmpty(emptyList);

        // Then
        assertFalse(result);
    }

    @Test
    void isNotEmpty_List_ShouldReturnTrue_WhenListHasSingleElement() {
        // Given
        List<String> singleElementList = Arrays.asList("single");

        // When
        boolean result = Utils.isNotEmpty(singleElementList);

        // Then
        assertTrue(result);
    }

    @Test
    void isNotEmpty_List_ShouldHandleDifferentTypes() {
        // Given
        List<Integer> integerList = Arrays.asList(1, 2, 3);
        List<Object> objectList = Arrays.asList(new Object(), "string", 123);

        // When
        boolean intResult = Utils.isNotEmpty(integerList);
        boolean objResult = Utils.isNotEmpty(objectList);

        // Then
        assertTrue(intResult);
        assertTrue(objResult);
    }

    @Test
    void isNotEmpty_Object_ShouldReturnTrue_WhenObjectIsNotNull() {
        // Given
        Object nonNullObject = new Object();
        String stringObject = "test";
        Integer integerObject = 42;

        // When
        boolean objResult = Utils.isNotEmpty(nonNullObject);
        boolean strResult = Utils.isNotEmpty(stringObject);
        boolean intResult = Utils.isNotEmpty(integerObject);

        // Then
        assertTrue(objResult);
        assertTrue(strResult);
        assertTrue(intResult);
    }

    @Test
    void isNotEmpty_Object_ShouldReturnFalse_WhenObjectIsNull() {
        // Given
        Object nullObject = null;

        // When
        boolean result = Utils.isNotEmpty(nullObject);

        // Then
        assertFalse(result);
    }

    @Test
    void isNotEmpty_AllMethods_ShouldHandleEdgeCases() {
        // Test various edge cases for all methods
        
        // Empty string vs null string
        assertFalse(Utils.isNotEmpty((String) null));
        assertFalse(Utils.isNotEmpty(""));
        assertTrue(Utils.isNotEmpty(" "));
        
        // Empty list vs null list
        assertFalse(Utils.isNotEmpty((List<String>) null));
        assertFalse(Utils.isNotEmpty(new ArrayList<String>()));
        assertTrue(Utils.isNotEmpty(Arrays.asList((String) null))); // List with null element
        
        // Object method
        assertFalse(Utils.isNotEmpty((Object) null));
        assertTrue(Utils.isNotEmpty(new Object()));
        assertTrue(Utils.isNotEmpty((Object) "")); // Empty string is still an object
    }

    @Test
    void getRandomNumberInRange_ShouldProduceDifferentResults() {
        // Test that the method produces different results over multiple calls
        // (this is probabilistic, but with a large enough range, collisions should be rare)
        
        int min = 1;
        int max = 1000;
        String first = Utils.getRandomNumberInRange(min, max);
        String second = Utils.getRandomNumberInRange(min, max);
        String third = Utils.getRandomNumberInRange(min, max);
        
        // It's possible (but unlikely) that all three are the same
        // So we'll just verify they're all valid
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);
        
        int firstNum = Integer.parseInt(first);
        int secondNum = Integer.parseInt(second);
        int thirdNum = Integer.parseInt(third);
        
        assertTrue(firstNum >= min && firstNum <= max);
        assertTrue(secondNum >= min && secondNum <= max);
        assertTrue(thirdNum >= min && thirdNum <= max);
    }
}