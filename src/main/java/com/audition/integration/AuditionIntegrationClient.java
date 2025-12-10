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
 * Provides methods to fetch posts and comments with filtering, pagination,
 * circuit breaker, and retry support.
 */
@Slf4j
@Component
public class AuditionIntegrationClient {

    private final RestTemplate restTemplate;
    private final AuditionApiProperties apiProperties;

    public AuditionIntegrationClient(final RestTemplate restTemplate,
                                     final AuditionApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    @CircuitBreaker(name = "#{@auditionApiProperties.circuitBreakerName}", fallbackMethod = "getPostsFallback")
    @Retry(name = "#{@auditionApiProperties.circuitBreakerName}")
    public List<AuditionPost> getPosts(final PostSearchCriteria criteria) {
        final String url = buildPostsUrl(criteria);
        log.debug("Fetching posts from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AuditionPost[].class))
                .map(Arrays::asList)
                .orElse(List.of());
        } catch (final HttpServerErrorException | ResourceAccessException e) {
            log.error("Transient error fetching posts: {}", e.getMessage());
            throw e;
        } catch (final HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limited while fetching posts: {}", e.getMessage());
            throw e;
        } catch (final RestClientException e) {
            log.error("Error fetching posts: {}", e.getMessage());
            throw SystemException.internalError("Error fetching posts: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    List<AuditionPost> getPostsFallback(final PostSearchCriteria criteria, final Throwable t) {
        log.warn("Circuit breaker fallback for getPosts: {}", t.getMessage());
        if (t instanceof HttpClientErrorException.TooManyRequests) {
            throw SystemException.rateLimitExceeded("Upstream API rate limit exceeded. Please try again later.", t);
        }
        throw SystemException.serviceUnavailable("Posts service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "#{@auditionApiProperties.circuitBreakerName}", fallbackMethod = "getPostByIdFallback")
    @Retry(name = "#{@auditionApiProperties.circuitBreakerName}")
    public AuditionPost getPostById(final Long id) {
        final String url = buildPostByIdUrl(id);
        log.debug("Fetching post by id from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AuditionPost.class))
                .orElseThrow(() -> SystemException.notFound("Cannot find a Post with id " + id));
        } catch (final HttpClientErrorException e) {
            log.error("Client error fetching post {}: {}", id, e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw SystemException.notFound("Cannot find a Post with id " + id, e);
            }
            if (e instanceof HttpClientErrorException.TooManyRequests) {
                throw e;
            }
            throw SystemException.withCause(e.getMessage(), "API Error", e.getStatusCode().value(), e);
        } catch (final HttpServerErrorException | ResourceAccessException e) {
            log.error("Transient error fetching post {}: {}", id, e.getMessage());
            throw e;
        } catch (final RestClientException e) {
            log.error("Error fetching post {}: {}", id, e.getMessage());
            throw SystemException.internalError("Error fetching post: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    AuditionPost getPostByIdFallback(final Long id, final Throwable t) {
        log.warn("Circuit breaker fallback for getPostById({}): {}", id, t.getMessage());
        if (t instanceof HttpClientErrorException.TooManyRequests) {
            throw SystemException.rateLimitExceeded("Upstream API rate limit exceeded. Please try again later.", t);
        }
        throw SystemException.serviceUnavailable("Post service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "#{@auditionApiProperties.circuitBreakerName}", fallbackMethod = "getPostWithCommentsFallback")
    @Retry(name = "#{@auditionApiProperties.circuitBreakerName}")
    public AuditionPost getPostWithComments(final Long postId) {
        final String url = buildPostWithCommentsUrl(postId);
        log.debug("Fetching post with comments from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AuditionPost.class))
                .orElseThrow(() -> SystemException.notFound("Cannot find a Post with id " + postId));
        } catch (final HttpClientErrorException e) {
            log.error("Client error fetching post {} with comments: {}", postId, e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw SystemException.notFound("Cannot find a Post with id " + postId, e);
            }
            if (e instanceof HttpClientErrorException.TooManyRequests) {
                throw e;
            }
            throw SystemException.withCause(e.getMessage(), "API Error", e.getStatusCode().value(), e);
        } catch (final HttpServerErrorException | ResourceAccessException e) {
            log.error("Transient error fetching post {} with comments: {}", postId, e.getMessage());
            throw e;
        } catch (final RestClientException e) {
            log.error("Error fetching post {} with comments: {}", postId, e.getMessage());
            throw SystemException.internalError("Error fetching post: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    AuditionPost getPostWithCommentsFallback(final Long postId, final Throwable t) {
        log.warn("Circuit breaker fallback for getPostWithComments({}): {}", postId, t.getMessage());
        if (t instanceof HttpClientErrorException.TooManyRequests) {
            throw SystemException.rateLimitExceeded("Upstream API rate limit exceeded. Please try again later.", t);
        }
        throw SystemException.serviceUnavailable("Post service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "#{@auditionApiProperties.circuitBreakerName}", fallbackMethod = "getCommentsForPostFallback")
    @Retry(name = "#{@auditionApiProperties.circuitBreakerName}")
    public List<Comment> getCommentsForPost(final Long postId) {
        final String url = buildCommentsUrl(postId);
        log.debug("Fetching comments for post from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, Comment[].class))
                .map(Arrays::asList)
                .orElse(List.of());
        } catch (final HttpServerErrorException | ResourceAccessException e) {
            log.error("Transient error fetching comments for post {}: {}", postId, e.getMessage());
            throw e;
        } catch (final HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limited while fetching comments for post {}: {}", postId, e.getMessage());
            throw e;
        } catch (final RestClientException e) {
            log.error("Error fetching comments for post {}: {}", postId, e.getMessage());
            throw SystemException.internalError("Error fetching comments: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    List<Comment> getCommentsForPostFallback(final Long postId, final Throwable t) {
        log.warn("Circuit breaker fallback for getCommentsForPost({}): {}", postId, t.getMessage());
        if (t instanceof HttpClientErrorException.TooManyRequests) {
            throw SystemException.rateLimitExceeded("Upstream API rate limit exceeded. Please try again later.", t);
        }
        throw SystemException.serviceUnavailable("Comments service is temporarily unavailable. Please try again later.", t);
    }

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
}
