package com.task.exception;

import com.task.dto.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              HttpStatus.NOT_FOUND, 
              String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
