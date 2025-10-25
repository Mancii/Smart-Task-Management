package com.task.dto;

/**
 * Standardized error codes for the application.
 * Follows HTTP status code patterns with application-specific codes.
 */
public enum ErrorCode {
    // Success (2xx)
    SUCCESS(200, "Operation completed successfully"),
    
    // 4xx Client Errors
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Resource not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    NOT_ACCEPTABLE(406, "Not acceptable"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable entity"),
    TOO_MANY_REQUESTS(429, "Too many requests"),
    
    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    NOT_IMPLEMENTED(501, "Not implemented"),
    BAD_GATEWAY(502, "Bad gateway"),
    SERVICE_UNAVAILABLE(503, "Service unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway timeout"),
    
    // Business-specific errors (using custom codes in 6xxx range)
    VALIDATION_ERROR(6001, "Validation failed"),
    INVALID_EMAIL_FORMAT(6002, "Invalid email format"),
    PASSWORD_TOO_WEAK(6003, "Password does not meet requirements"),
    DUPLICATE_EMAIL(6004, "Email already in use"),
    INVALID_CREDENTIALS(6005, "Invalid username or password"),
    ACCOUNT_LOCKED(6006, "User account is locked"),
    INVALID_TOKEN(6007, "Invalid or expired token"),
    TOKEN_EXPIRED(6008, "Token has expired"),
    INVALID_REFRESH_TOKEN(6009, "Invalid refresh token"),
    DATABASE_ERROR(6010, "Database operation failed"),
    EXTERNAL_SERVICE_ERROR(6011, "Error calling external service"),
    RESOURCE_NOT_FOUND(6012, "Requested resource not found");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
