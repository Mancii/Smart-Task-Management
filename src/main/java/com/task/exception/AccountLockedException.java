package com.task.exception;

public class AccountLockedException extends BusinessException {
    public AccountLockedException(String message) {
        super(message);
    }
}