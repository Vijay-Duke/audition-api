package com.audition.configuration;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter that enriches MDC with request context for structured logging.
 * Adds requestId, clientIp, httpMethod, requestUri, and userAgent to MDC.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class MdcLoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";
    private static final String CLIENT_IP = "clientIp";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String REQUEST_URI = "requestUri";
    private static final String USER_AGENT = "userAgent";
    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";

    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private final Tracer tracer;

    public MdcLoggingFilter(final Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            enrichMdc(httpRequest);
            chain.doFilter(request, response);
        } finally {
            clearMdc();
        }
    }

    private void enrichMdc(final HttpServletRequest request) {
        MDC.put(REQUEST_ID, extractOrGenerateRequestId(request));
        MDC.put(CLIENT_IP, extractClientIp(request));
        MDC.put(HTTP_METHOD, request.getMethod());
        MDC.put(REQUEST_URI, request.getRequestURI());
        MDC.put(USER_AGENT, extractUserAgent(request));

        final Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            MDC.put(TRACE_ID, currentSpan.context().traceId());
            MDC.put(SPAN_ID, currentSpan.context().spanId());
        }
    }

    private String extractOrGenerateRequestId(final HttpServletRequest request) {
        final String requestId = request.getHeader(X_REQUEST_ID_HEADER);
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }

    private String extractClientIp(final HttpServletRequest request) {
        final String forwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(final HttpServletRequest request) {
        final String userAgent = request.getHeader(USER_AGENT_HEADER);
        return userAgent != null ? userAgent : "unknown";
    }

    private void clearMdc() {
        MDC.remove(REQUEST_ID);
        MDC.remove(CLIENT_IP);
        MDC.remove(HTTP_METHOD);
        MDC.remove(REQUEST_URI);
        MDC.remove(USER_AGENT);
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
    }
}
