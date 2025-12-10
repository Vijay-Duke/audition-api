package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.configuration.AuditionApiProperties;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Integration client for the JSONPlaceholder API.
 * Provides methods to fetch posts and comments with circuit breaker and retry support.
 */
@Slf4j
@Component
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"}) // Resilience4j fallbacks invoked via reflection
public class AuditionIntegrationClient {

    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Service temporarily unavailable. Please try again later.";
    private static final String CIRCUIT_BREAKER_NAME = "${audition.api.circuit-breaker-name}";

    private final RestTemplate restTemplate;
    private final AuditionApiProperties apiProperties;

    public AuditionIntegrationClient(final RestTemplate restTemplate,
                                     final AuditionApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    @Retry(name = CIRCUIT_BREAKER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getPostsFallback")
    public List<AuditionPost> getPosts(final PostSearchCriteria criteria) {
        final String url = buildPostsUrl(criteria);
        log.debug("Fetching posts from: {}", url);
        return executeWithErrorHandling(
            () -> Optional.ofNullable(restTemplate.getForObject(url, AuditionPost[].class))
                .map(Arrays::asList)
                .orElse(List.of()),
            "posts"
        );
    }

    @Retry(name = CIRCUIT_BREAKER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getPostByIdFallback")
    public AuditionPost getPostById(final Long id) {
        final String url = buildPostByIdUrl(id);
        log.debug("Fetching post by id from: {}", url);
        return executeWithErrorHandling(
            () -> Optional.ofNullable(restTemplate.getForObject(url, AuditionPost.class))
                .orElseThrow(() -> SystemException.notFound("Cannot find a Post with id " + id)),
            "Post with id " + id
        );
    }

    @Retry(name = CIRCUIT_BREAKER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getPostWithCommentsFallback")
    public AuditionPost getPostWithComments(final Long postId) {
        final String url = buildPostWithCommentsUrl(postId);
        log.debug("Fetching post with comments from: {}", url);
        return executeWithErrorHandling(
            () -> Optional.ofNullable(restTemplate.getForObject(url, AuditionPost.class))
                .orElseThrow(() -> SystemException.notFound("Cannot find a Post with id " + postId)),
            "Post with id " + postId
        );
    }

    @Retry(name = CIRCUIT_BREAKER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getCommentsForPostFallback")
    public List<Comment> getCommentsForPost(final Long postId) {
        final String url = buildCommentsUrl(postId);
        log.debug("Fetching comments for post from: {}", url);
        return executeWithErrorHandling(
            () -> Optional.ofNullable(restTemplate.getForObject(url, Comment[].class))
                .map(Arrays::asList)
                .orElse(List.of()),
            "comments for post with id " + postId
        );
    }

    // ========== URL Builders ==========

    private String buildPostsUrl(final PostSearchCriteria criteria) {
        final UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(apiProperties.getBaseUrl())
            .path(apiProperties.getPostsPath());

        if (criteria.getUserId() != null) {
            builder.queryParam("userId", criteria.getUserId());
        }
        if (criteria.getTitleContains() != null && !criteria.getTitleContains().isBlank()) {
            builder.queryParam("title_like", criteria.getTitleContains().trim());
        }
        if (criteria.getPage() != null && criteria.getSize() != null) {
            builder.queryParam("_page", criteria.getPage());
            builder.queryParam("_limit", criteria.getSize());
        }
        if (criteria.getSort() != null) {
            builder.queryParam("_sort", criteria.getSort());
            builder.queryParam("_order", criteria.getOrder() != null ? criteria.getOrder() : "asc");
        }

        return builder.toUriString();
    }

    private String buildPostByIdUrl(final Long id) {
        return UriComponentsBuilder
            .fromUriString(apiProperties.getBaseUrl())
            .path(apiProperties.getPostsPath())
            .path("/{id}")
            .buildAndExpand(id)
            .toUriString();
    }

    private String buildPostWithCommentsUrl(final Long postId) {
        return UriComponentsBuilder
            .fromUriString(apiProperties.getBaseUrl())
            .path(apiProperties.getPostsPath())
            .path("/{id}")
            .queryParam("_embed", "comments")
            .buildAndExpand(postId)
            .toUriString();
    }

    private String buildCommentsUrl(final Long postId) {
        return UriComponentsBuilder
            .fromUriString(apiProperties.getBaseUrl())
            .path(apiProperties.getPostsPath())
            .path("/{postId}")
            .path(apiProperties.getCommentsPath())
            .buildAndExpand(postId)
            .toUriString();
    }

    // ========== Error Handling ==========

    private <T> T executeWithErrorHandling(final Supplier<T> operation, final String resource) {
        try {
            return operation.get();
        } catch (final HttpClientErrorException e) {
            log.error("Client error fetching {}: {}", resource, e.getMessage());
            throw handleClientError(e, resource);
        } catch (final HttpServerErrorException e) {
            log.error("Server error fetching {}: {}", resource, e.getMessage());
            throw e;
        } catch (final ResourceAccessException e) {
            log.error("Connection error fetching {}: {}", resource, e.getMessage());
            throw e;
        } catch (final RestClientException e) {
            log.error("Unexpected error fetching {}: {}", resource, e.getMessage(), e);
            throw handleGenericError(e);
        }
    }

    private RuntimeException handleClientError(final HttpClientErrorException e, final String resource) {
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.warn("Rate limited, will retry: {}", e.getMessage());
            throw e;
        }
        final int statusValue = e.getStatusCode().value();
        return switch (statusValue) {
            case 404 -> SystemException.notFound("Cannot find " + resource, e);
            default -> SystemException.withCause(e.getMessage(), "API Error", statusValue, e);
        };
    }

    private SystemException handleGenericError(final RestClientException e) {
        return SystemException.internalError(
            "An unexpected error occurred while communicating with the API.", e);
    }

    // ========== Fallback Methods (invoked via reflection by Resilience4j @CircuitBreaker) ==========

    private List<AuditionPost> getPostsFallback(final PostSearchCriteria criteria, final Throwable t) {
        return handleFallback("getPosts", t);
    }

    private AuditionPost getPostByIdFallback(final Long id, final Throwable t) {
        return handleFallback("getPostById " + id, t);
    }

    private AuditionPost getPostWithCommentsFallback(final Long postId, final Throwable t) {
        return handleFallback("getPostWithComments " + postId, t);
    }

    private List<Comment> getCommentsForPostFallback(final Long postId, final Throwable t) {
        return handleFallback("getCommentsForPost " + postId, t);
    }

    private <T> T handleFallback(final String methodName, final Throwable t) {
        log.warn("Circuit breaker fallback for {}: {}", methodName, t.getMessage());
        throw createFallbackException(t);
    }

    private SystemException createFallbackException(final Throwable cause) {
        if (cause instanceof SystemException systemException) {
            throw systemException;
        }
        if (cause instanceof HttpClientErrorException.TooManyRequests) {
            return SystemException.rateLimitExceeded("API rate limit exceeded. Please try again later.", cause);
        }
        return SystemException.serviceUnavailable(SERVICE_UNAVAILABLE_MESSAGE, cause);
    }
}
