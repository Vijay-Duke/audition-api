package com.audition.configuration;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Filter that injects distributed tracing headers into HTTP responses.
 * Supports both W3C Trace Context (traceparent) and legacy headers for observability.
 */
@Component
public class ResponseHeaderInjector implements Filter {

    private static final String W3C_TRACEPARENT_HEADER = "traceparent";
    private static final String W3C_TRACESTATE_HEADER = "tracestate";
    private static final String W3C_VERSION = "00";
    private static final String W3C_FLAGS_SAMPLED = "01";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final int W3C_TRACE_ID_LENGTH = 32;

    private final Tracer tracer;

    public ResponseHeaderInjector(final Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        injectTraceHeaders(httpResponse);
        chain.doFilter(request, response);
    }

    private void injectTraceHeaders(final HttpServletResponse response) {
        final Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            final String traceId = currentSpan.context().traceId();
            final String spanId = currentSpan.context().spanId();

            final String traceparent = String.format("%s-%s-%s-%s",
                W3C_VERSION, padTraceId(traceId), spanId, W3C_FLAGS_SAMPLED);
            response.setHeader(W3C_TRACEPARENT_HEADER, traceparent);
            response.setHeader(W3C_TRACESTATE_HEADER, "audition=" + spanId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            response.setHeader(SPAN_ID_HEADER, spanId);
        }
    }

    /**
     * Pads trace ID to 32 characters (128 bits) as required by W3C Trace Context.
     * Some tracers use 64-bit trace IDs which need zero-padding.
     */
    private String padTraceId(final String traceId) {
        if (traceId.length() < W3C_TRACE_ID_LENGTH) {
            return String.format("%" + W3C_TRACE_ID_LENGTH + "s", traceId).replace(' ', '0');
        }
        return traceId;
    }
}
