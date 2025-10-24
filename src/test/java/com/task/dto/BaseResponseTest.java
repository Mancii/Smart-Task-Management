package com.task.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTest {

    @Test
    void success_WithDataOnly_ShouldCreateSuccessResponse() {
        // Given
        String testData = "test data";

        // When
        BaseResponse<String> response = BaseResponse.success(testData);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Operation completed successfully", response.getMessage());
        assertEquals(testData, response.getData());
        assertNull(response.getError());
        assertNull(response.getMeta());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_WithMessageAndData_ShouldCreateSuccessResponse() {
        // Given
        String message = "Custom success message";
        Integer testData = 42;

        // When
        BaseResponse<Integer> response = BaseResponse.success(message, testData);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(testData, response.getData());
        assertNull(response.getError());
        assertNull(response.getMeta());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_WithNullData_ShouldCreateSuccessResponse() {
        // Given
        String message = "Success with null data";

        // When
        BaseResponse<Object> response = BaseResponse.success(message, null);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNull(response.getError());
        assertNull(response.getMeta());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_WithComplexData_ShouldCreateSuccessResponse() {
        // Given
        List<String> complexData = Arrays.asList("item1", "item2", "item3");
        String message = "List retrieved successfully";

        // When
        BaseResponse<List<String>> response = BaseResponse.success(message, complexData);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(complexData, response.getData());
        assertEquals(3, response.getData().size());
        assertNull(response.getError());
        assertNull(response.getMeta());
    }

    @Test
    void error_ShouldCreateErrorResponse() {
        // Given
        String message = "Something went wrong";
        String errorCode = "ERR001";
        String details = "Detailed error information";

        // When
        BaseResponse<Object> response = BaseResponse.error(message, errorCode, details);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(errorCode, response.getError().getCode());
        assertEquals(details, response.getError().getDetails());
        assertNull(response.getMeta());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_WithNullValues_ShouldCreateErrorResponse() {
        // Given
        String message = "Error occurred";

        // When
        BaseResponse<Object> response = BaseResponse.error(message, null, null);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertNull(response.getError().getCode());
        assertNull(response.getError().getDetails());
    }

    @Test
    void constructor_WithAllParameters_ShouldCreateResponse() {
        // Given
        boolean success = true;
        String message = "Test message";
        String data = "test data";
        BaseResponse.ErrorDetails error = new BaseResponse.ErrorDetails("ERR001", "Error details");
        BaseResponse.Meta meta = new BaseResponse.Meta(0, 10, 100, 10);

        // When
        BaseResponse<String> response = new BaseResponse<>(success, message, data, error, meta);

        // Then
        assertEquals(success, response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(error, response.getError());
        assertEquals(meta, response.getMeta());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyResponse() {
        // When
        BaseResponse<String> response = new BaseResponse<>();

        // Then
        assertFalse(response.isSuccess()); // Default boolean value
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertNull(response.getError());
        assertNull(response.getMeta());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void setters_ShouldWorkCorrectly() {
        // Given
        BaseResponse<String> response = new BaseResponse<>();
        String message = "Updated message";
        String data = "updated data";
        BaseResponse.ErrorDetails error = new BaseResponse.ErrorDetails("ERR002", "Updated error");
        BaseResponse.Meta meta = new BaseResponse.Meta(1, 5, 50, 10);

        // When
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setError(error);
        response.setMeta(meta);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(error, response.getError());
        assertEquals(meta, response.getMeta());
    }

    @Test
    void timestamp_ShouldBeSetAutomatically() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        BaseResponse<String> response = new BaseResponse<>();
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        // Then
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isAfter(beforeCreation));
        assertTrue(response.getTimestamp().isBefore(afterCreation));
    }

    @Test
    void errorDetails_ShouldWorkCorrectly() {
        // Given
        String code = "VALIDATION_ERROR";
        String details = "Field validation failed";

        // When
        BaseResponse.ErrorDetails errorDetails = new BaseResponse.ErrorDetails(code, details);

        // Then
        assertEquals(code, errorDetails.getCode());
        assertEquals(details, errorDetails.getDetails());
    }

    @Test
    void errorDetails_EqualsAndHashCode_ShouldWork() {
        // Given
        BaseResponse.ErrorDetails error1 = new BaseResponse.ErrorDetails("ERR001", "Details");
        BaseResponse.ErrorDetails error2 = new BaseResponse.ErrorDetails("ERR001", "Details");
        BaseResponse.ErrorDetails error3 = new BaseResponse.ErrorDetails("ERR002", "Different");

        // Then
        assertEquals(error1, error2);
        assertNotEquals(error1, error3);
        assertEquals(error1.hashCode(), error2.hashCode());
        assertNotEquals(error1.hashCode(), error3.hashCode());
    }

    @Test
    void meta_ShouldWorkCorrectly() {
        // Given
        int page = 2;
        int size = 20;
        long totalItems = 150;
        int totalPages = 8;

        // When
        BaseResponse.Meta meta = new BaseResponse.Meta(page, size, totalItems, totalPages);

        // Then
        assertEquals(page, meta.getPage());
        assertEquals(size, meta.getSize());
        assertEquals(totalItems, meta.getTotalItems());
        assertEquals(totalPages, meta.getTotalPages());
    }

    @Test
    void meta_EqualsAndHashCode_ShouldWork() {
        // Given
        BaseResponse.Meta meta1 = new BaseResponse.Meta(1, 10, 100, 10);
        BaseResponse.Meta meta2 = new BaseResponse.Meta(1, 10, 100, 10);
        BaseResponse.Meta meta3 = new BaseResponse.Meta(2, 20, 200, 20);

        // Then
        assertEquals(meta1, meta2);
        assertNotEquals(meta1, meta3);
        assertEquals(meta1.hashCode(), meta2.hashCode());
        assertNotEquals(meta1.hashCode(), meta3.hashCode());
    }

    @Test
    void response_WithGenericType_ShouldWork() {
        // Test with different generic types
        
        // Integer type
        BaseResponse<Integer> intResponse = BaseResponse.success("Number response", 42);
        assertEquals(Integer.class, intResponse.getData().getClass());
        assertEquals(42, intResponse.getData());

        // List type
        List<String> list = Arrays.asList("a", "b", "c");
        BaseResponse<List<String>> listResponse = BaseResponse.success("List response", list);
        assertEquals(list, listResponse.getData());

        // Custom object type
        UserDto user = new UserDto();
        user.setUsername("testuser");
        BaseResponse<UserDto> userResponse = BaseResponse.success("User response", user);
        assertEquals(user, userResponse.getData());
        assertEquals("testuser", userResponse.getData().getUsername());
    }

    @Test
    void response_ToString_ShouldWork() {
        // Given
        BaseResponse<String> response = BaseResponse.success("Test message", "test data");

        // When
        String toString = response.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("test data"));
    }
}