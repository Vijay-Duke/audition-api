package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@SuppressWarnings({"PMD.AvoidUsingHardCodedIP", "PMD.AvoidThrowingRawExceptionTypes"}) // Testing IP handling and exception scenarios
class MdcLoggingFilterTest {

    private static final String HTTP_GET = "GET";
    private static final String API_POSTS_URI = "/api/posts";
    private static final String TEST_REQUEST_ID = "test-request-123";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_CLIENT_IP = "clientIp";
    private static final String MDC_HTTP_METHOD = "httpMethod";
    private static final String MDC_REQUEST_URI = "requestUri";
    private static final String MDC_USER_AGENT = "userAgent";

    private MdcLoggingFilter filter;
    private Tracer tracer;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        tracer = mock(Tracer.class);
        filter = new MdcLoggingFilter(tracer);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @Test
    void shouldEnrichMdcWithRequestContext() throws ServletException, IOException {
        request.setMethod(HTTP_GET);
        request.setRequestURI(API_POSTS_URI);
        request.addHeader("X-Request-ID", TEST_REQUEST_ID);
        request.addHeader("User-Agent", "TestAgent/1.0");
        request.setRemoteAddr("localhost");

        final String[] capturedRequestId = new String[1];
        final String[] capturedHttpMethod = new String[1];
        final String[] capturedRequestUri = new String[1];
        final String[] capturedClientIp = new String[1];
        final String[] capturedUserAgent = new String[1];

        final FilterChain chain = (req, res) -> {
            capturedRequestId[0] = MDC.get(MDC_REQUEST_ID);
            capturedHttpMethod[0] = MDC.get(MDC_HTTP_METHOD);
            capturedRequestUri[0] = MDC.get(MDC_REQUEST_URI);
            capturedClientIp[0] = MDC.get(MDC_CLIENT_IP);
            capturedUserAgent[0] = MDC.get(MDC_USER_AGENT);
        };

        filter.doFilter(request, response, chain);

        assertEquals(TEST_REQUEST_ID, capturedRequestId[0]);
        assertEquals(HTTP_GET, capturedHttpMethod[0]);
        assertEquals(API_POSTS_URI, capturedRequestUri[0]);
        assertEquals("localhost", capturedClientIp[0]);
        assertEquals("TestAgent/1.0", capturedUserAgent[0]);
    }

    @Test
    void shouldGenerateRequestIdWhenNotProvided() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI(API_POSTS_URI);

        final String[] capturedRequestId = new String[1];

        final FilterChain chain = (req, res) -> capturedRequestId[0] = MDC.get(MDC_REQUEST_ID);

        filter.doFilter(request, response, chain);

        assertNotNull(capturedRequestId[0]);
        assertEquals(36, capturedRequestId[0].length()); // UUID format
    }

    @Test
    void shouldExtractClientIpFromXForwardedForHeader() throws ServletException, IOException {
        request.addHeader("X-Forwarded-For", "proxy1.example.com, proxy2.example.com, proxy3.example.com");
        request.setRemoteAddr("localhost");

        final String[] capturedClientIp = new String[1];

        final FilterChain chain = (req, res) -> capturedClientIp[0] = MDC.get(MDC_CLIENT_IP);

        filter.doFilter(request, response, chain);

        assertEquals("proxy1.example.com", capturedClientIp[0]);
    }

    @Test
    void shouldFallbackToRemoteAddrWhenXForwardedForNotPresent() throws ServletException, IOException {
        request.setRemoteAddr("client.example.com");

        final String[] capturedClientIp = new String[1];

        final FilterChain chain = (req, res) -> capturedClientIp[0] = MDC.get(MDC_CLIENT_IP);

        filter.doFilter(request, response, chain);

        assertEquals("client.example.com", capturedClientIp[0]);
    }

    @Test
    void shouldEnrichMdcWithTraceContext() throws ServletException, IOException {
        final Span span = mock(Span.class);
        final TraceContext traceContext = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("abc123trace");
        when(traceContext.spanId()).thenReturn("def456span");

        final String[] capturedTraceId = new String[1];
        final String[] capturedSpanId = new String[1];

        final FilterChain chain = (req, res) -> {
            capturedTraceId[0] = MDC.get("traceId");
            capturedSpanId[0] = MDC.get("spanId");
        };

        filter.doFilter(request, response, chain);

        assertEquals("abc123trace", capturedTraceId[0]);
        assertEquals("def456span", capturedSpanId[0]);
    }

    @Test
    void shouldClearMdcAfterFilterChain() throws ServletException, IOException {
        request.setMethod(HTTP_GET);
        request.setRequestURI(API_POSTS_URI);
        request.addHeader("X-Request-ID", TEST_REQUEST_ID);

        final FilterChain chain = (req, res) -> assertNotNull(MDC.get(MDC_REQUEST_ID));

        filter.doFilter(request, response, chain);

        assertNull(MDC.get(MDC_REQUEST_ID));
        assertNull(MDC.get(MDC_HTTP_METHOD));
        assertNull(MDC.get(MDC_REQUEST_URI));
        assertNull(MDC.get(MDC_CLIENT_IP));
        assertNull(MDC.get(MDC_USER_AGENT));
    }

    @Test
    void shouldClearMdcEvenWhenExceptionOccurs() throws ServletException, IOException {
        request.setMethod(HTTP_GET);
        request.addHeader("X-Request-ID", TEST_REQUEST_ID);

        final FilterChain chain = (req, res) -> {
            throw new IllegalStateException("Test exception");
        };

        try {
            filter.doFilter(request, response, chain);
        } catch (IllegalStateException ignored) {
            // Expected
        }

        assertNull(MDC.get(MDC_REQUEST_ID));
        assertNull(MDC.get(MDC_HTTP_METHOD));
    }

    @Test
    void shouldHandleMissingUserAgent() throws ServletException, IOException {
        request.setMethod(HTTP_GET);
        request.setRequestURI(API_POSTS_URI);

        final String[] capturedUserAgent = new String[1];

        final FilterChain chain = (req, res) -> capturedUserAgent[0] = MDC.get(MDC_USER_AGENT);

        filter.doFilter(request, response, chain);

        assertEquals("unknown", capturedUserAgent[0]);
    }
}
