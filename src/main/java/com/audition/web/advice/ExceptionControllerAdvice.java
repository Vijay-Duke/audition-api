package com.audition.web.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import com.audition.common.exception.SystemException;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ExceptionControllerAdvice {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_MESSAGE = "Error Code from Exception could not be mapped to a valid HttpStatus Code - {}";
    private static final String DEFAULT_MESSAGE = "API Error occurred. Please contact support or administrator.";

    private static final String HEADER_RETRY_AFTER = "Retry-After";
    private static final String HEADER_RATELIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RATELIMIT_RESET = "X-RateLimit-Reset";
    private static final int DEFAULT_RETRY_AFTER_SECONDS = 60;

    @ExceptionHandler(HttpClientErrorException.class)
    ProblemDetail handleHttpClientException(final HttpClientErrorException e) {
        return createProblemDetail(e, e.getStatusCode());
    }

    @ExceptionHandler(BindException.class)
    ProblemDetail handleBindException(final BindException e) {
        LOG.warn("Validation error: {}", e.getMessage());
        final String message = e.getBindingResult().getAllErrors().stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                }
                return error.getDefaultMessage();
            })
            .collect(Collectors.joining("; "));
        return createValidationProblemDetail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolationException(final ConstraintViolationException e) {
        LOG.warn("Constraint violation: {}", e.getMessage());
        final String message = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));
        return createValidationProblemDetail(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        LOG.warn("Type mismatch: {}", e.getMessage());
        final String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            e.getValue(), e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        return createValidationProblemDetail(message);
    }

    private ProblemDetail createValidationProblemDetail(final String message) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(BAD_REQUEST);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail(message);
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleMainException(final Exception e) {
        final HttpStatusCode status = getHttpStatusCodeFromException(e);
        LOG.error("Unhandled exception occurred: {}", e.getMessage(), e);
        return createProblemDetail(e, status);
    }

    @ExceptionHandler(SystemException.class)
    ResponseEntity<ProblemDetail> handleSystemException(final SystemException e) {
        final HttpStatusCode status = getHttpStatusCodeFromSystemException(e);
        logSystemException(e, status);
        final ProblemDetail problemDetail = createProblemDetail(e, status);

        if (status.value() == TOO_MANY_REQUESTS.value()) {
            return ResponseEntity.status(status)
                .headers(createRateLimitHeaders())
                .body(problemDetail);
        }

        return ResponseEntity.status(status).body(problemDetail);
    }

    private HttpHeaders createRateLimitHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_RETRY_AFTER, String.valueOf(DEFAULT_RETRY_AFTER_SECONDS));
        headers.set(HEADER_RATELIMIT_LIMIT, "100");
        headers.set(HEADER_RATELIMIT_REMAINING, "0");
        headers.set(HEADER_RATELIMIT_RESET, String.valueOf(Instant.now().plusSeconds(DEFAULT_RETRY_AFTER_SECONDS).getEpochSecond()));
        return headers;
    }

    private void logSystemException(final SystemException e, final HttpStatusCode status) {
        if (status.is5xxServerError()) {
            LOG.error("System exception occurred [{}]: {}", e.getTitle(), e.getMessage(), e);
        } else if (status.is4xxClientError()) {
            LOG.warn("Client error [{}]: {}", e.getTitle(), e.getMessage());
        } else {
            LOG.info("System exception [{}]: {}", e.getTitle(), e.getMessage());
        }
    }

    private ProblemDetail createProblemDetail(final Exception exception,
                                              final HttpStatusCode statusCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail(getMessageFromException(exception));
        if (exception instanceof SystemException systemEx) {
            problemDetail.setTitle(systemEx.getTitle());
        } else {
            problemDetail.setTitle(DEFAULT_TITLE);
        }
        return problemDetail;
    }

    private String getMessageFromException(final Exception exception) {
        if (StringUtils.isNotBlank(exception.getMessage())) {
            return exception.getMessage();
        }
        return DEFAULT_MESSAGE;
    }

    private HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
        try {
            return HttpStatusCode.valueOf(exception.getStatusCode());
        } catch (final IllegalArgumentException iae) {
            LOG.info(ERROR_MESSAGE, exception.getStatusCode());
            return INTERNAL_SERVER_ERROR;
        }
    }

    private HttpStatusCode getHttpStatusCodeFromException(final Exception exception) {
        if (exception instanceof HttpRequestMethodNotSupportedException) {
            return METHOD_NOT_ALLOWED;
        }
        return INTERNAL_SERVER_ERROR;
    }
}
