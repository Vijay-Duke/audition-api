package com.audition.web.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

import com.audition.common.exception.SystemException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String STATUS_CODE_ERROR = "Error Code from Exception could not be mapped to a valid HttpStatus Code - {}";

    private final ProblemDetailFactory problemDetailFactory;

    public ExceptionControllerAdvice(final ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    @ExceptionHandler(HttpClientErrorException.class)
    ProblemDetail handleHttpClientException(final HttpClientErrorException e) {
        return problemDetailFactory.createProblemDetail(e, e.getStatusCode());
    }

    @ExceptionHandler(BindException.class)
    ProblemDetail handleBindException(final BindException e) {
        LOG.warn("Validation error: {}", e.getMessage());
        final String message = e.getBindingResult().getAllErrors().stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return problemDetailFactory.formatFieldError(fieldError);
                }
                return error.getDefaultMessage();
            })
            .collect(Collectors.joining("; "));
        return problemDetailFactory.createValidationProblemDetail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolationException(final ConstraintViolationException e) {
        LOG.warn("Constraint violation: {}", e.getMessage());
        final String message = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));
        return problemDetailFactory.createValidationProblemDetail(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        LOG.warn("Type mismatch: {}", e.getMessage());
        final String message = String.format("Invalid value '%s' for parameter '%s'. Please provide a valid number.",
            e.getValue(), e.getName());
        return problemDetailFactory.createValidationProblemDetail(message);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleMainException(final Exception e) {
        final HttpStatusCode status = getHttpStatusCodeFromException(e);
        LOG.error("Unhandled exception occurred: {}", e.getMessage(), e);
        return problemDetailFactory.createProblemDetail(e, status);
    }

    @ExceptionHandler(SystemException.class)
    ResponseEntity<ProblemDetail> handleSystemException(final SystemException e) {
        final HttpStatusCode status = getHttpStatusCodeFromSystemException(e);
        logSystemException(e, status);
        final ProblemDetail problemDetail = problemDetailFactory.createProblemDetail(e, status);

        if (status.value() == TOO_MANY_REQUESTS.value()) {
            return ResponseEntity.status(status)
                .headers(problemDetailFactory.createRateLimitHeaders())
                .body(problemDetail);
        }

        return ResponseEntity.status(status).body(problemDetail);
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

    private HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
        try {
            return HttpStatusCode.valueOf(exception.getStatusCode());
        } catch (final IllegalArgumentException iae) {
            LOG.info(STATUS_CODE_ERROR, exception.getStatusCode());
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
