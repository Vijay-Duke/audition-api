package com.audition.web.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.audition.common.exception.SystemException;
import io.micrometer.common.util.StringUtils;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

/**
 * Factory for creating RFC 7807 ProblemDetail responses.
 */
@Component
public class ProblemDetailFactory {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    public static final String DEFAULT_MESSAGE = "API Error occurred. Please contact support or administrator.";

    private static final String HEADER_RETRY_AFTER = "Retry-After";
    private static final String HEADER_RATELIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RATELIMIT_RESET = "X-RateLimit-Reset";
    private static final int DEFAULT_RETRY_AFTER_SECONDS = 60;

    public ProblemDetail createProblemDetail(final Exception exception, final HttpStatusCode statusCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail(getMessageFromException(exception));
        if (exception instanceof SystemException systemEx) {
            problemDetail.setTitle(systemEx.getTitle());
        } else {
            problemDetail.setTitle(DEFAULT_TITLE);
        }
        return problemDetail;
    }

    public ProblemDetail createValidationProblemDetail(final String message) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(BAD_REQUEST);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail(message);
        return problemDetail;
    }

    public String formatFieldError(final FieldError fieldError) {
        final String field = fieldError.getField();
        final Object rejectedValue = fieldError.getRejectedValue();

        if (fieldError.getCode() != null && fieldError.getCode().contains("typeMismatch")) {
            return String.format("'%s' is not a valid value for '%s'. Please provide a valid number.",
                rejectedValue, field);
        }

        return field + ": " + fieldError.getDefaultMessage();
    }

    public HttpHeaders createRateLimitHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_RETRY_AFTER, String.valueOf(DEFAULT_RETRY_AFTER_SECONDS));
        headers.set(HEADER_RATELIMIT_LIMIT, "100");
        headers.set(HEADER_RATELIMIT_REMAINING, "0");
        headers.set(HEADER_RATELIMIT_RESET, String.valueOf(Instant.now().plusSeconds(DEFAULT_RETRY_AFTER_SECONDS).getEpochSecond()));
        return headers;
    }

    private String getMessageFromException(final Exception exception) {
        if (StringUtils.isNotBlank(exception.getMessage())) {
            return exception.getMessage();
        }
        return DEFAULT_MESSAGE;
    }
}
