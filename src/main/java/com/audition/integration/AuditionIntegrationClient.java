package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.configuration.AuditionApiProperties;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Integration client for the JSONPlaceholder API.
 * Provides methods to fetch posts and comments with filtering and pagination support.
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

    public List<AuditionPost> getPosts(final PostSearchCriteria criteria) {
        final String url = buildPostsUrl(criteria);
        log.debug("Fetching posts from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AuditionPost[].class))
                .map(Arrays::asList)
                .orElse(List.of());
        } catch (final RestClientException e) {
            log.error("Error fetching posts: {}", e.getMessage());
            throw new SystemException("Error fetching posts: " + e.getMessage(), 500);
        }
    }

    public AuditionPost getPostById(final Long id) {
        final String url = buildPostByIdUrl(id);
        log.debug("Fetching post by id from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AuditionPost.class))
                .orElseThrow(() -> new SystemException("Cannot find a Post with id " + id, "Resource Not Found", 404));
        } catch (final HttpClientErrorException e) {
            log.error("Client error fetching post {}: {}", id, e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found", 404);
            }
            throw new SystemException(e.getMessage(), "API Error", e.getStatusCode().value());
        } catch (final RestClientException e) {
            log.error("Error fetching post {}: {}", id, e.getMessage());
            throw new SystemException("Error fetching post: " + e.getMessage(), 500);
        }
    }

    public AuditionPost getPostWithComments(final Long postId) {
        final String url = buildPostWithCommentsUrl(postId);
        log.debug("Fetching post with comments from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AuditionPost.class))
                .orElseThrow(() -> new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404));
        } catch (final HttpClientErrorException e) {
            log.error("Client error fetching post {} with comments: {}", postId, e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404);
            }
            throw new SystemException(e.getMessage(), "API Error", e.getStatusCode().value());
        } catch (final RestClientException e) {
            log.error("Error fetching post {} with comments: {}", postId, e.getMessage());
            throw new SystemException("Error fetching post: " + e.getMessage(), 500);
        }
    }

    public List<Comment> getCommentsForPost(final Long postId) {
        final String url = buildCommentsUrl(postId);
        log.debug("Fetching comments for post from: {}", url);
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, Comment[].class))
                .map(Arrays::asList)
                .orElse(List.of());
        } catch (final RestClientException e) {
            log.error("Error fetching comments for post {}: {}", postId, e.getMessage());
            throw new SystemException("Error fetching comments: " + e.getMessage(), 500);
        }
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
