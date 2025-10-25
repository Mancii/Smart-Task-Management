package com.task.exception;

import org.springframework.http.HttpStatus;

import com.task.dto.ErrorCode;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
private final ErrorCode errorCode;
private final HttpStatus httpStatus;
private final String details;

protected BaseException(
	ErrorCode errorCode, HttpStatus httpStatus, String message, String details) {
	super(message);
	this.errorCode = errorCode;
	this.httpStatus = httpStatus;
	this.details = details;
}

protected BaseException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
	this(errorCode, httpStatus, message, null);
}
}
