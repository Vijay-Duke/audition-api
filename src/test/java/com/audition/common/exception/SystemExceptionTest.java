package com.audition.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SystemException Tests")
class SystemExceptionTest {

    private static final String TEST_MESSAGE = "Test error message";
    private static final String CUSTOM_TITLE = "Custom Title";
    private static final String CAUSE_MESSAGE = "Cause";

    @Nested
    @DisplayName("Factory Method: of()")
    class OfMethodTests {

        @Test
        @DisplayName("Should create exception with message and default values")
        void shouldCreateExceptionWithMessageAndDefaults() {
            // Act
            final SystemException exception = SystemException.of(TEST_MESSAGE);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getDetail()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);
            assertThat(exception.getStatusCode()).isEqualTo(500);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Factory Method: withStatus()")
    class WithStatusMethodTests {

        @Test
        @DisplayName("Should create exception with custom status code")
        void shouldCreateExceptionWithCustomStatusCode() {
            // Act
            final SystemException exception = SystemException.withStatus(TEST_MESSAGE, 422);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getStatusCode()).isEqualTo(422);
            assertThat(exception.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);
        }
    }

    @Nested
    @DisplayName("Factory Method: withTitleAndStatus()")
    class WithTitleAndStatusMethodTests {

        @Test
        @DisplayName("Should create exception with custom title and status")
        void shouldCreateExceptionWithCustomTitleAndStatus() {
            // Act
            final SystemException exception = SystemException.withTitleAndStatus(
                TEST_MESSAGE, CUSTOM_TITLE, 403);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getTitle()).isEqualTo(CUSTOM_TITLE);
            assertThat(exception.getStatusCode()).isEqualTo(403);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Factory Method: withCause()")
    class WithCauseMethodTests {

        @Test
        @DisplayName("Should create exception with all fields and cause")
        void shouldCreateExceptionWithAllFieldsAndCause() {
            // Arrange
            final Throwable cause = new RuntimeException("Root cause");

            // Act
            final SystemException exception = SystemException.withCause(
                TEST_MESSAGE, CUSTOM_TITLE, 503, cause);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getTitle()).isEqualTo(CUSTOM_TITLE);
            assertThat(exception.getStatusCode()).isEqualTo(503);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Convenience Factory Methods")
    class ConvenienceFactoryMethodTests {

        @Test
        @DisplayName("badRequest() should create 400 exception")
        void badRequestShouldCreate400Exception() {
            // Act
            final SystemException exception = SystemException.badRequest(TEST_MESSAGE);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getStatusCode()).isEqualTo(400);
            assertThat(exception.getTitle()).isEqualTo("Bad Request");
        }

        @Test
        @DisplayName("notFound() should create 404 exception")
        void notFoundShouldCreate404Exception() {
            // Act
            final SystemException exception = SystemException.notFound(TEST_MESSAGE);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getStatusCode()).isEqualTo(404);
            assertThat(exception.getTitle()).isEqualTo("Resource Not Found");
        }

        @Test
        @DisplayName("notFound() with cause should create 404 exception with cause")
        void notFoundWithCauseShouldCreate404ExceptionWithCause() {
            // Arrange
            final Throwable cause = new RuntimeException(CAUSE_MESSAGE);

            // Act
            final SystemException exception = SystemException.notFound(TEST_MESSAGE, cause);

            // Assert
            assertThat(exception.getStatusCode()).isEqualTo(404);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("rateLimitExceeded() should create 429 exception")
        void rateLimitExceededShouldCreate429Exception() {
            // Act
            final SystemException exception = SystemException.rateLimitExceeded(TEST_MESSAGE);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getStatusCode()).isEqualTo(429);
            assertThat(exception.getTitle()).isEqualTo("Rate Limit Exceeded");
        }

        @Test
        @DisplayName("rateLimitExceeded() with cause should create 429 exception with cause")
        void rateLimitExceededWithCauseShouldCreate429ExceptionWithCause() {
            // Arrange
            final Throwable cause = new RuntimeException(CAUSE_MESSAGE);

            // Act
            final SystemException exception = SystemException.rateLimitExceeded(TEST_MESSAGE, cause);

            // Assert
            assertThat(exception.getStatusCode()).isEqualTo(429);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("serviceUnavailable() should create 503 exception")
        void serviceUnavailableShouldCreate503Exception() {
            // Act
            final SystemException exception = SystemException.serviceUnavailable(TEST_MESSAGE);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getStatusCode()).isEqualTo(503);
            assertThat(exception.getTitle()).isEqualTo("Service Unavailable");
        }

        @Test
        @DisplayName("serviceUnavailable() with cause should create 503 exception with cause")
        void serviceUnavailableWithCauseShouldCreate503ExceptionWithCause() {
            // Arrange
            final Throwable cause = new RuntimeException(CAUSE_MESSAGE);

            // Act
            final SystemException exception = SystemException.serviceUnavailable(TEST_MESSAGE, cause);

            // Assert
            assertThat(exception.getStatusCode()).isEqualTo(503);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("internalError() should create 500 exception")
        void internalErrorShouldCreate500Exception() {
            // Act
            final SystemException exception = SystemException.internalError(TEST_MESSAGE);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(exception.getStatusCode()).isEqualTo(500);
            assertThat(exception.getTitle()).isEqualTo("Internal Server Error");
        }

        @Test
        @DisplayName("internalError() with cause should create 500 exception with cause")
        void internalErrorWithCauseShouldCreate500ExceptionWithCause() {
            // Arrange
            final Throwable cause = new RuntimeException(CAUSE_MESSAGE);

            // Act
            final SystemException exception = SystemException.internalError(TEST_MESSAGE, cause);

            // Assert
            assertThat(exception.getStatusCode()).isEqualTo(500);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("DEFAULT_TITLE Constant Tests")
    class DefaultTitleConstantTests {

        @Test
        @DisplayName("DEFAULT_TITLE should have expected value")
        void defaultTitleShouldHaveExpectedValue() {
            // Assert
            assertThat(SystemException.DEFAULT_TITLE).isEqualTo("API Error Occurred");
        }
    }

    @Nested
    @DisplayName("RuntimeException Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Act
            final SystemException exception = SystemException.of(TEST_MESSAGE);

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            // Act
            final SystemException exception = SystemException.of(TEST_MESSAGE);

            // Assert
            assertThat(exception).isInstanceOf(Throwable.class);
        }
    }
}
