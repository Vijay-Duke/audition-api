package com.audition.web.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.audition.common.exception.SystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;

@DisplayName("ProblemDetailFactory Tests")
class ProblemDetailFactoryTest {

    private ProblemDetailFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ProblemDetailFactory();
    }

    @Nested
    @DisplayName("createProblemDetail Tests")
    class CreateProblemDetailTests {

        @Test
        @DisplayName("Should create problem detail with exception message")
        void shouldCreateProblemDetailWithExceptionMessage() {
            final Exception exception = new RuntimeException("Something went wrong");

            final ProblemDetail result = factory.createProblemDetail(exception, INTERNAL_SERVER_ERROR);

            assertThat(result.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.value());
            assertThat(result.getDetail()).isEqualTo("Something went wrong");
            assertThat(result.getTitle()).isEqualTo(ProblemDetailFactory.DEFAULT_TITLE);
        }

        @Test
        @DisplayName("Should use SystemException title")
        void shouldUseSystemExceptionTitle() {
            final SystemException exception = SystemException.notFound("Resource not found");

            final ProblemDetail result = factory.createProblemDetail(exception, NOT_FOUND);

            assertThat(result.getStatus()).isEqualTo(NOT_FOUND.value());
            assertThat(result.getTitle()).isEqualTo("Resource Not Found");
            assertThat(result.getDetail()).isEqualTo("Resource not found");
        }

        @Test
        @DisplayName("Should use default message when exception message is blank")
        void shouldUseDefaultMessageWhenBlank() {
            final Exception exception = new RuntimeException("");

            final ProblemDetail result = factory.createProblemDetail(exception, INTERNAL_SERVER_ERROR);

            assertThat(result.getDetail()).isEqualTo(ProblemDetailFactory.DEFAULT_MESSAGE);
        }

        @Test
        @DisplayName("Should use default message when exception message is null")
        void shouldUseDefaultMessageWhenNull() {
            final Exception exception = new RuntimeException((String) null);

            final ProblemDetail result = factory.createProblemDetail(exception, INTERNAL_SERVER_ERROR);

            assertThat(result.getDetail()).isEqualTo(ProblemDetailFactory.DEFAULT_MESSAGE);
        }
    }

    @Nested
    @DisplayName("createValidationProblemDetail Tests")
    class CreateValidationProblemDetailTests {

        @Test
        @DisplayName("Should create validation problem detail with BAD_REQUEST status")
        void shouldCreateValidationProblemDetail() {
            final String message = "Field 'name' is required";

            final ProblemDetail result = factory.createValidationProblemDetail(message);

            assertThat(result.getStatus()).isEqualTo(BAD_REQUEST.value());
            assertThat(result.getTitle()).isEqualTo("Validation Error");
            assertThat(result.getDetail()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("formatFieldError Tests")
    class FormatFieldErrorTests {

        @Test
        @DisplayName("Should format type mismatch error with user-friendly message")
        void shouldFormatTypeMismatchError() {
            final FieldError fieldError = new FieldError(
                "postSearchCriteria", "userId", "abc", true,
                new String[]{"typeMismatch"}, null, "Type mismatch");

            final String result = factory.formatFieldError(fieldError);

            assertThat(result).isEqualTo("'abc' is not a valid value for 'userId'. Please provide a valid number.");
        }

        @Test
        @DisplayName("Should format regular field error with default message")
        void shouldFormatRegularFieldError() {
            final FieldError fieldError = new FieldError(
                "postSearchCriteria", "userId", null, false,
                new String[]{"Min"}, null, "must be greater than 0");

            final String result = factory.formatFieldError(fieldError);

            assertThat(result).isEqualTo("userId: must be greater than 0");
        }
    }

    @Nested
    @DisplayName("createRateLimitHeaders Tests")
    class CreateRateLimitHeadersTests {

        @Test
        @DisplayName("Should create rate limit headers")
        void shouldCreateRateLimitHeaders() {
            final HttpHeaders headers = factory.createRateLimitHeaders();

            assertThat(headers.getFirst("Retry-After")).isEqualTo("60");
            assertThat(headers.getFirst("X-RateLimit-Limit")).isEqualTo("100");
            assertThat(headers.getFirst("X-RateLimit-Remaining")).isEqualTo("0");
            assertThat(headers.getFirst("X-RateLimit-Reset")).isNotNull();
        }
    }
}
