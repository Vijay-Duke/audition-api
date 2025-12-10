package com.audition.common.exception;

import lombok.Getter;

/**
 * Custom exception for system-level errors with HTTP status code support.
 * Use static factory methods for creating instances.
 */
@Getter
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ShortMethodName"})
public final class SystemException extends RuntimeException {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    private static final long serialVersionUID = -5876728854007114881L;

    private final Integer statusCode;
    private final String title;
    private final String detail;

    private SystemException(final String message, final String title, final Integer statusCode, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.title = title;
        this.detail = message;
    }

    // --- Static Factory Methods ---

    /**
     * Creates a generic API error with default title and 500 status.
     */
    public static SystemException of(final String message) {
        return new SystemException(message, DEFAULT_TITLE, 500, null);
    }

    /**
     * Creates an error with a specific status code.
     */
    public static SystemException withStatus(final String message, final int statusCode) {
        return new SystemException(message, DEFAULT_TITLE, statusCode, null);
    }

    /**
     * Creates an error with title and status code.
     */
    public static SystemException withTitleAndStatus(final String message, final String title, final int statusCode) {
        return new SystemException(message, title, statusCode, null);
    }

    /**
     * Creates an error with title, status code, and cause.
     */
    public static SystemException withCause(final String message, final String title, final int statusCode,
                                            final Throwable cause) {
        return new SystemException(message, title, statusCode, cause);
    }

    // --- Convenience Factory Methods for Common HTTP Status Codes ---

    /**
     * Creates a 400 Bad Request error.
     */
    public static SystemException badRequest(final String message) {
        return new SystemException(message, "Bad Request", 400, null);
    }

    /**
     * Creates a 404 Not Found error.
     */
    public static SystemException notFound(final String message) {
        return new SystemException(message, "Resource Not Found", 404, null);
    }

    /**
     * Creates a 404 Not Found error with cause.
     */
    public static SystemException notFound(final String message, final Throwable cause) {
        return new SystemException(message, "Resource Not Found", 404, cause);
    }

    /**
     * Creates a 429 Rate Limit Exceeded error.
     */
    public static SystemException rateLimitExceeded(final String message) {
        return new SystemException(message, "Rate Limit Exceeded", 429, null);
    }

    /**
     * Creates a 429 Rate Limit Exceeded error with cause.
     */
    public static SystemException rateLimitExceeded(final String message, final Throwable cause) {
        return new SystemException(message, "Rate Limit Exceeded", 429, cause);
    }

    /**
     * Creates a 503 Service Unavailable error.
     */
    public static SystemException serviceUnavailable(final String message) {
        return new SystemException(message, "Service Unavailable", 503, null);
    }

    /**
     * Creates a 503 Service Unavailable error with cause.
     */
    public static SystemException serviceUnavailable(final String message, final Throwable cause) {
        return new SystemException(message, "Service Unavailable", 503, cause);
    }

    /**
     * Creates a 500 Internal Server Error.
     */
    public static SystemException internalError(final String message) {
        return new SystemException(message, "Internal Server Error", 500, null);
    }

    /**
     * Creates a 500 Internal Server Error with cause.
     */
    public static SystemException internalError(final String message, final Throwable cause) {
        return new SystemException(message, "Internal Server Error", 500, cause);
    }
}
