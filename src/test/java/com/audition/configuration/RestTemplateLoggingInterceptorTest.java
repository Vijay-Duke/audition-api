package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RestTemplateLoggingInterceptor Tests")
@SuppressWarnings("PMD.CloseResource") // Mock responses don't need closing
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"NP_NULL_PARAM_DEREF_NONVIRTUAL", "NP_NULL_PARAM_DEREF"})
class RestTemplateLoggingInterceptorTest {

    private RestTemplateLoggingInterceptor interceptor;

    @Mock
    private HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ClientHttpResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new RestTemplateLoggingInterceptor();
    }

    @Test
    @DisplayName("Should intercept and execute request")
    void shouldInterceptAndExecuteRequest() throws IOException {
        // Arrange
        final byte[] body = "test body".getBytes(StandardCharsets.UTF_8);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://api.example.com/posts"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getBody()).thenReturn(new ByteArrayInputStream("response".getBytes(StandardCharsets.UTF_8)));
        when(execution.execute(any(), any())).thenReturn(response);

        // Act
        final ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // Assert
        assertThat(result).isEqualTo(response);
        verify(execution).execute(request, body);
    }

    @Test
    @DisplayName("Should handle null body")
    void shouldHandleNullBody() throws IOException {
        // Arrange
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://api.example.com/posts"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(execution.execute(any(), any())).thenReturn(response);

        // Act
        final ClientHttpResponse result = interceptor.intercept(request, null, execution);

        // Assert
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("Should handle empty body")
    void shouldHandleEmptyBody() throws IOException {
        // Arrange
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(URI.create("https://api.example.com/posts"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getStatusCode()).thenReturn(HttpStatus.CREATED);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(execution.execute(any(), any())).thenReturn(response);

        // Act
        final ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution);

        // Assert
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("Should handle different HTTP methods")
    void shouldHandleDifferentHttpMethods() throws IOException {
        // Arrange
        when(request.getMethod()).thenReturn(HttpMethod.DELETE);
        when(request.getURI()).thenReturn(URI.create("https://api.example.com/posts/1"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(execution.execute(any(), any())).thenReturn(response);

        // Act
        final ClientHttpResponse result = interceptor.intercept(request, null, execution);

        // Assert
        assertThat(result).isEqualTo(response);
        verify(execution).execute(request, null);
    }

    @Test
    @DisplayName("Should handle error responses")
    void shouldHandleErrorResponses() throws IOException {
        // Arrange
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://api.example.com/posts/999"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getBody()).thenReturn(new ByteArrayInputStream("Not Found".getBytes(StandardCharsets.UTF_8)));
        when(execution.execute(any(), any())).thenReturn(response);

        // Act
        final ClientHttpResponse result = interceptor.intercept(request, null, execution);

        // Assert
        assertThat(result).isEqualTo(response);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
