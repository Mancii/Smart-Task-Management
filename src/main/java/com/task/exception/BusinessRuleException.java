package com.task.exception;

import org.springframework.http.HttpStatus;

import com.task.dto.ErrorCode;

public class BusinessRuleException extends BaseException {

public BusinessRuleException(String message) {
	super(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message);
}

public BusinessRuleException(String message, String details) {
	super(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message, details);
}

public BusinessRuleException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
	super(errorCode, httpStatus, message);
}

public BusinessRuleException(
	ErrorCode errorCode, HttpStatus httpStatus, String message, String details) {
	super(errorCode, httpStatus, message, details);
}
}
