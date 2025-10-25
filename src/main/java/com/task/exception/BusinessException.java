package com.task.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

@Slf4j
@Getter
public class BusinessException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    public final transient Object[] args;
    private final Exception originalException;

    public BusinessException(String message, Object... args) {
        super(message);
        this.args = args;
        this.originalException = null;
        log.error("BusinessException: {}, args: {}", message, args);
    }

    public BusinessException(String message) {
        this(message, new Object[]{});
    }

    public BusinessException(Exception e) {
        super(e.getMessage(), e);
        this.args = new Object[]{};
        this.originalException = e;
        log.error("BusinessException caused by: {}", e.getMessage(), e);
    }

    public BusinessException(String message, Exception e, Object... args) {
        super(message, e);
        this.args = args;
        this.originalException = e;
        log.error("BusinessException: {}, args: {}", message, args, e);
    }
}
