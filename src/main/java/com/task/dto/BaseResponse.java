package com.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    private Meta meta;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructor for success responses
    public BaseResponse(boolean success, String message, T data, ErrorDetails error, Meta meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public static <T> BaseResponse<T> success(T data) {
        return success("Operation completed successfully", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> BaseResponse<T> error(String message, String errorCode, String details) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(new ErrorDetails(errorCode, details));
        response.setMessage(message);
        response.setError(new ErrorDetails(errorCode, details));
        return response;
    }

    @Data
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String details;
    }

    @Data
    @AllArgsConstructor
    public static class Meta {
        private int page;
        private int size;
        private long totalItems;
        private int totalPages;
    }
}
