package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponseHeaderInjector Tests")
class ResponseHeaderInjectorTest {

    @Mock
    private Tracer tracer;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ResponseHeaderInjector injector;

    @BeforeEach
    void setUp() {
        injector = new ResponseHeaderInjector(tracer);
    }

    @Nested
    @DisplayName("doFilter() Tests")
    class DoFilterTests {

        @Test
        @DisplayName("Should inject trace and span headers when span is present")
        void shouldInjectTraceAndSpanHeaders() throws ServletException, IOException {
            // Arrange
            final Span span = mock(Span.class);
            final TraceContext context = mock(TraceContext.class);
            when(tracer.currentSpan()).thenReturn(span);
            when(span.context()).thenReturn(context);
            when(context.traceId()).thenReturn("abc123trace");
            when(context.spanId()).thenReturn("def456span");

            // Act
            injector.doFilter(request, response, filterChain);

            // Assert
            verify(response).setHeader("X-Trace-Id", "abc123trace");
            verify(response).setHeader("X-Span-Id", "def456span");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain when no span is present")
        void shouldContinueWhenNoSpanPresent() throws ServletException, IOException {
            // Arrange
            when(tracer.currentSpan()).thenReturn(null);

            // Act
            injector.doFilter(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not inject headers when span is null")
        void shouldNotInjectHeadersWhenSpanIsNull() throws ServletException, IOException {
            // Arrange
            when(tracer.currentSpan()).thenReturn(null);

            // Act
            injector.doFilter(request, response, filterChain);

            // Assert
            verify(response, org.mockito.Mockito.never()).setHeader(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
            );
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with tracer")
        void shouldCreateInstanceWithTracer() {
            // Arrange & Act
            final ResponseHeaderInjector instance = new ResponseHeaderInjector(tracer);

            // Assert
            assertThat(instance).isNotNull();
        }
    }
}
