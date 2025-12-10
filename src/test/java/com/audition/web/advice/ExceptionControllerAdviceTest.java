package com.audition.web.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.audition.common.exception.SystemException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;

@DisplayName("ExceptionControllerAdvice Tests")
class ExceptionControllerAdviceTest {

    private ExceptionControllerAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new ExceptionControllerAdvice();
    }

    @Nested
    @DisplayName("handleHttpClientException Tests")
    class HandleHttpClientExceptionTests {

        @Test
        @DisplayName("Should handle HttpClientErrorException with NOT_FOUND status")
        void shouldHandleNotFoundError() {
            // Arrange
            final HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

            // Act
            final ProblemDetail result = advice.handleHttpClientException(exception);

            // Assert
            assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(result.getTitle()).isEqualTo(ExceptionControllerAdvice.DEFAULT_TITLE);
        }

        @Test
        @DisplayName("Should handle HttpClientErrorException with BAD_REQUEST status")
        void shouldHandleBadRequestError() {
            // Arrange
            final HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

            // Act
            final ProblemDetail result = advice.handleHttpClientException(exception);

            // Assert
            assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    @DisplayName("handleMainException Tests")
    class HandleMainExceptionTests {

        @Test
        @DisplayName("Should handle generic exception with INTERNAL_SERVER_ERROR")
        void shouldHandleGenericException() {
            // Arrange
            final Exception exception = new RuntimeException("Something went wrong");

            // Act
            final ProblemDetail result = advice.handleMainException(exception);

            // Assert
            assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            assertThat(result.getDetail()).isEqualTo("Something went wrong");
            assertThat(result.getTitle()).isEqualTo(ExceptionControllerAdvice.DEFAULT_TITLE);
        }

        @Test
        @DisplayName("Should handle generic exception from client code")
        void shouldHandleGenericClientException() {
            // Arrange
            final Exception exception = new IllegalArgumentException("Invalid argument");

            // Act
            final ProblemDetail result = advice.handleMainException(exception);

            // Assert
            assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            assertThat(result.getDetail()).isEqualTo("Invalid argument");
        }

        @Test
        @DisplayName("Should handle HttpRequestMethodNotSupportedException with METHOD_NOT_ALLOWED")
        void shouldHandleMethodNotSupportedException() {
            // Arrange
            final HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("POST");

            // Act
            final ProblemDetail result = advice.handleMainException(exception);

            // Assert
            assertThat(result.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        }

        @Test
        @DisplayName("Should use default message when exception message is blank")
        void shouldUseDefaultMessageWhenExceptionMessageIsBlank() {
            // Arrange
            final Exception exception = new RuntimeException("");

            // Act
            final ProblemDetail result = advice.handleMainException(exception);

            // Assert
            assertThat(result.getDetail()).isEqualTo("API Error occurred. Please contact support or administrator.");
        }

        @Test
        @DisplayName("Should use default message when exception message is null")
        void shouldUseDefaultMessageWhenExceptionMessageIsNull() {
            // Arrange
            final Exception exception = new RuntimeException((String) null);

            // Act
            final ProblemDetail result = advice.handleMainException(exception);

            // Assert
            assertThat(result.getDetail()).isEqualTo("API Error occurred. Please contact support or administrator.");
        }
    }

    @Nested
    @DisplayName("handleSystemException Tests")
    class HandleSystemExceptionTests {

        @Test
        @DisplayName("Should handle SystemException with 5xx status")
        void shouldHandleSystemExceptionWith5xxStatus() {
            // Arrange
            final SystemException exception = SystemException.internalError("Server error");

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
            assertThat(response.getBody().getDetail()).isEqualTo("Server error");
        }

        @Test
        @DisplayName("Should handle SystemException with 503 status")
        void shouldHandleSystemExceptionWith503Status() {
            // Arrange
            final SystemException exception = SystemException.serviceUnavailable("Service unavailable");

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }

        @Test
        @DisplayName("Should handle SystemException with 4xx status")
        void shouldHandleSystemExceptionWith4xxStatus() {
            // Arrange
            final SystemException exception = SystemException.notFound("Not found");

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Resource Not Found");
        }

        @Test
        @DisplayName("Should handle SystemException with 400 status")
        void shouldHandleSystemExceptionWith400Status() {
            // Arrange
            final SystemException exception = SystemException.badRequest("Bad request");

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should handle SystemException with invalid status code and fallback to 500")
        void shouldHandleSystemExceptionWithInvalidStatusCode() {
            // Arrange - use negative status code which is invalid
            final SystemException exception = SystemException.withTitleAndStatus("Error", "Invalid Status", -1);

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("Should handle SystemException with 3xx status")
        void shouldHandleSystemExceptionWith3xxStatus() {
            // Arrange
            final SystemException exception = SystemException.withTitleAndStatus("Redirect", "Redirect", 301);

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(301);
        }

        @Test
        @DisplayName("Should handle SystemException with 2xx status")
        void shouldHandleSystemExceptionWith2xxStatus() {
            // Arrange
            final SystemException exception = SystemException.withTitleAndStatus("OK", "Success", 200);

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should include rate limit headers for 429 status")
        void shouldIncludeRateLimitHeadersFor429Status() {
            // Arrange
            final SystemException exception = SystemException.rateLimitExceeded("Rate limit exceeded");

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("60");
            assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("100");
            assertThat(response.getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");
            assertThat(response.getHeaders().getFirst("X-RateLimit-Reset")).isNotNull();
        }

        @Test
        @DisplayName("Should not include rate limit headers for non-429 status")
        void shouldNotIncludeRateLimitHeadersForNon429Status() {
            // Arrange
            final SystemException exception = SystemException.notFound("Not found");

            // Act
            final ResponseEntity<ProblemDetail> response = advice.handleSystemException(exception);

            // Assert
            assertThat(response.getHeaders().getFirst("Retry-After")).isNull();
            assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isNull();
        }
    }
}
