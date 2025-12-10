package com.audition.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * Logging interceptor for RestTemplate that logs HTTP request/response details.
 * Logs request method, URI, headers, and response status/body at DEBUG level.
 */
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);
    private static final int MAX_BODY_LOG_LENGTH = 1000;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request,
                                        final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        final long startTime = System.currentTimeMillis();

        final ClientHttpResponse response = execution.execute(request, body);

        final long duration = System.currentTimeMillis() - startTime;
        logResponse(request, response, duration);

        return response;
    }

    private void logRequest(final HttpRequest request, final byte[] body) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> Request: {} {}", request.getMethod(), request.getURI());
            LOG.debug("==> Headers: {}", request.getHeaders());
            if (body != null && body.length > 0) {
                final String bodyStr = new String(body, StandardCharsets.UTF_8);
                LOG.debug("==> Body: {}", truncateIfNeeded(bodyStr));
            }
        }
    }

    private void logResponse(final HttpRequest request,
                             final ClientHttpResponse response,
                             final long durationMs) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("<== Response: {} {} - Status: {} ({}ms)",
                request.getMethod(),
                request.getURI(),
                response.getStatusCode(),
                durationMs);
            LOG.debug("<== Headers: {}", response.getHeaders());

            // Only log body for non-successful responses or when trace is enabled
            if (LOG.isTraceEnabled()) {
                final byte[] bodyBytes = StreamUtils.copyToByteArray(response.getBody());
                final String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
                LOG.trace("<== Body: {}", truncateIfNeeded(bodyStr));
            }
        }
    }

    private String truncateIfNeeded(final String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= MAX_BODY_LOG_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_BODY_LOG_LENGTH) + "...[truncated]";
    }
}
