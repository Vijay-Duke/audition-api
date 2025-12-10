package com.audition.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.configuration.AuditionApiProperties;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditionIntegrationClient Tests")
@SuppressWarnings({"PMD.UseVarargs", "PMD.TooManyMethods"}) // Array parameters match RestTemplate API; test class with comprehensive coverage
class AuditionIntegrationClientTest {

    private static final String BASE_URL = "https://api.example.com";
    private static final String POSTS_PATH = "/posts";
    private static final String COMMENTS_PATH = "/comments";
    private static final String POST_WITH_COMMENTS_URL = BASE_URL + POSTS_PATH + "/1?_embed=comments";
    private static final String CONNECTION_REFUSED_MSG = "Connection refused";
    private static final String FIELD_STATUS_CODE = "statusCode";
    private static final String FIELD_TITLE = "title";

    @Mock
    private RestTemplate restTemplate;

    private AuditionIntegrationClient client;

    @BeforeEach
    void setUp() {
        final AuditionApiProperties apiProperties = new AuditionApiProperties();
        apiProperties.setBaseUrl(BASE_URL);
        apiProperties.setPostsPath(POSTS_PATH);
        apiProperties.setCommentsPath(COMMENTS_PATH);
        client = new AuditionIntegrationClient(restTemplate, apiProperties);
    }

    @Nested
    @DisplayName("getPosts()")
    class GetPostsTests {

        @Test
        @DisplayName("Should return list of posts when API returns data")
        void shouldReturnListOfPosts() {
            // Arrange
            final AuditionPost[] posts = {createPost(1), createPost(2)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH, posts);

            // Act
            final List<AuditionPost> result = client.getPosts(PostSearchCriteria.builder().build());

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(1).getId()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty list when API returns null")
        void shouldReturnEmptyListWhenApiReturnsNull() {
            // Arrange
            stubPostsArrayResponse(BASE_URL + POSTS_PATH, null);

            // Act
            final List<AuditionPost> result = client.getPosts(PostSearchCriteria.builder().build());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should add userId query param when provided")
        void shouldAddUserIdQueryParam() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?userId=1", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should add title_like query param when titleContains provided")
        void shouldAddTitleLikeQueryParam() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?title_like=hello", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .titleContains("hello")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should trim whitespace from titleContains")
        void shouldTrimWhitespaceFromTitleContains() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?title_like=hello", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .titleContains("  hello  ")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should add pagination params when provided")
        void shouldAddPaginationParams() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?_page=1&_limit=10", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(10)
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should pass page number directly to upstream API (1-indexed)")
        void shouldPassPageNumberDirectly() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?_page=3&_limit=5", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(3)
                .size(5)
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should add sort and order params when provided")
        void shouldAddSortAndOrderParams() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?_sort=id&_order=desc", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("id")
                .order("desc")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should default order to asc when only sort is provided")
        void shouldDefaultOrderToAsc() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?_sort=title&_order=asc", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("title")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should combine all query params")
        void shouldCombineAllQueryParams() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            final String expectedUrl = BASE_URL + POSTS_PATH
                + "?userId=1&title_like=test&_page=1&_limit=10&_sort=id&_order=desc";
            stubPostsArrayResponse(expectedUrl, posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains("test")
                .page(1)
                .size(10)
                .sort("id")
                .order("desc")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should not add title_like when titleContains is blank")
        void shouldNotAddTitleLikeWhenBlank() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH, posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .titleContains("   ")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getPostById()")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should return post when API returns data")
        void shouldReturnPost() {
            // Arrange
            final AuditionPost expectedPost = createPost(1);
            when(restTemplate.getForObject(BASE_URL + POSTS_PATH + "/1", AuditionPost.class))
                .thenReturn(expectedPost);

            // Act
            final AuditionPost result = client.getPostById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw 404 when API returns null")
        void shouldThrow404WhenApiReturnsNull() {
            // Arrange
            when(restTemplate.getForObject(BASE_URL + POSTS_PATH + "/999", AuditionPost.class))
                .thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> client.getPostById(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404)
                .hasMessageContaining("Cannot find a Post with id 999");
        }

        @Test
        @DisplayName("Should throw 404 when API returns HttpClientErrorException with NOT_FOUND")
        void shouldThrow404WhenApiReturnsNotFound() {
            // Arrange
            when(restTemplate.getForObject(BASE_URL + POSTS_PATH + "/999", AuditionPost.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

            // Act & Assert
            assertThatThrownBy(() -> client.getPostById(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404)
                .hasFieldOrPropertyWithValue(FIELD_TITLE, "Resource Not Found");
        }
    }

    @Nested
    @DisplayName("getPostWithComments()")
    class GetPostWithCommentsTests {

        @Test
        @DisplayName("Should return post with embedded comments using _embed param")
        void shouldReturnPostWithEmbeddedComments() {
            // Arrange
            final AuditionPost post = AuditionPost.builder()
                .id(1)
                .userId(1)
                .title("Test Post 1")
                .body("Test Body 1")
                .comments(List.of(createComment(1), createComment(2)))
                .build();
            when(restTemplate.getForObject(POST_WITH_COMMENTS_URL, AuditionPost.class))
                .thenReturn(post);

            // Act
            final AuditionPost result = client.getPostWithComments(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getComments()).hasSize(2);
        }

        @Test
        @DisplayName("Should throw 404 when post not found")
        void shouldThrow404WhenPostNotFound() {
            // Arrange
            when(restTemplate.getForObject(BASE_URL + POSTS_PATH + "/999?_embed=comments", AuditionPost.class))
                .thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> client.getPostWithComments(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404);
        }
    }

    @Nested
    @DisplayName("getCommentsForPost()")
    class GetCommentsForPostTests {

        @Test
        @DisplayName("Should return comments for post")
        void shouldReturnComments() {
            // Arrange
            final Comment[] comments = {createComment(1), createComment(2)};
            stubCommentsArrayResponse(BASE_URL + POSTS_PATH + "/1" + COMMENTS_PATH, comments);

            // Act
            final List<Comment> result = client.getCommentsForPost(1L);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when API returns null")
        void shouldReturnEmptyListWhenApiReturnsNull() {
            // Arrange
            stubCommentsArrayResponse(BASE_URL + POSTS_PATH + "/1" + COMMENTS_PATH, null);

            // Act
            final List<Comment> result = client.getCommentsForPost(1L);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @ParameterizedTest(name = "HTTP {0} should throw SystemException with status {0}")
        @ValueSource(ints = {400, 401, 403})
        @DisplayName("Should handle 4xx client errors")
        void shouldHandle4xxClientErrors(final int statusCode) {
            // Arrange
            stubPostsArrayResponseThrows(new HttpClientErrorException(HttpStatus.valueOf(statusCode)));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, statusCode);
        }

        @Test
        @DisplayName("Should handle 429 Too Many Requests by rethrowing for retry")
        void shouldHandle429TooManyRequests() {
            // Arrange
            stubPostsArrayResponseThrows(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

            // Act & Assert - 429 is rethrown to allow Resilience4j retry (in unit test, no AOP)
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(HttpClientErrorException.class)
                .satisfies(e -> assertThat(((HttpClientErrorException) e).getStatusCode())
                    .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        }

        @ParameterizedTest(name = "HTTP {0} should be rethrown for retry")
        @ValueSource(ints = {500, 502, 503, 504})
        @DisplayName("Should rethrow 5xx server errors for retry")
        void shouldHandle5xxServerErrors(final int statusCode) {
            // Arrange
            stubPostsArrayResponseThrows(new HttpServerErrorException(HttpStatus.valueOf(statusCode)));

            // Act & Assert - 5xx errors are rethrown to allow Resilience4j retry (in unit test, no AOP)
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(HttpServerErrorException.class)
                .satisfies(e -> assertThat(((HttpServerErrorException) e).getStatusCode().value())
                    .isEqualTo(statusCode));
        }

        @Test
        @DisplayName("Should rethrow connection timeout for retry")
        void shouldHandleConnectionTimeout() {
            // Arrange
            stubPostsArrayResponseThrows(new ResourceAccessException("Connection timed out"));

            // Act & Assert - ResourceAccessException is rethrown to allow Resilience4j retry (in unit test, no AOP)
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Connection timed out");
        }

        @Test
        @DisplayName("Should handle generic RestClientException with 500")
        void shouldHandleGenericRestClientException() {
            // Arrange
            stubPostsArrayResponseThrows(new RestClientException("Unexpected error"));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 500)
                .hasFieldOrPropertyWithValue(FIELD_TITLE, "Internal Server Error")
                .hasMessageContaining("An unexpected error occurred");
        }

        @Test
        @DisplayName("Should rethrow server errors in getPostById for retry")
        void shouldHandleErrorsInGetPostById() {
            // Arrange
            when(restTemplate.getForObject(eq(BASE_URL + POSTS_PATH + "/1"), any(Class.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

            // Act & Assert - server errors are rethrown to allow Resilience4j retry (in unit test, no AOP)
            assertThatThrownBy(() -> client.getPostById(1L))
                .isInstanceOf(HttpServerErrorException.class);
        }

        @Test
        @DisplayName("Should rethrow server errors in getPostWithComments for retry")
        void shouldHandleErrorsInGetPostWithComments() {
            // Arrange
            when(restTemplate.getForObject(eq(POST_WITH_COMMENTS_URL), any(Class.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

            // Act & Assert - server errors are rethrown to allow Resilience4j retry (in unit test, no AOP)
            assertThatThrownBy(() -> client.getPostWithComments(1L))
                .isInstanceOf(HttpServerErrorException.class);
        }

        @Test
        @DisplayName("Should rethrow connection errors in getCommentsForPost for retry")
        void shouldHandleErrorsInGetCommentsForPost() {
            // Arrange
            stubCommentsArrayResponseThrows(new ResourceAccessException(CONNECTION_REFUSED_MSG));

            // Act & Assert
            assertThatThrownBy(() -> client.getCommentsForPost(1L))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining(CONNECTION_REFUSED_MSG);
        }
    }

    @Nested
    @DisplayName("Fallback Methods")
    class FallbackMethodTests {

        @Test
        @DisplayName("getPostByIdFallback should throw SystemException when cause is not SystemException")
        void fallbackShouldThrowServiceUnavailableForNonSystemException() {
            // Arrange - simulate what happens when circuit breaker calls fallback
            final HttpServerErrorException cause = new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);

            // Act & Assert - test the fallback logic via getPostById that catches and handles errors
            when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenThrow(new RestClientException("Circuit breaker triggered", cause));

            assertThatThrownBy(() -> client.getPostById(1L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 500);
        }

        @Test
        @DisplayName("Should preserve SystemException when it is the cause")
        void shouldPreserveSystemExceptionAsCause() {
            // Arrange - SystemException thrown directly should be preserved
            when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(null); // This triggers orElseThrow with notFound

            // Act & Assert
            assertThatThrownBy(() -> client.getPostById(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404)
                .hasMessageContaining("Cannot find a Post with id 999");
        }

        @Test
        @DisplayName("Should handle TooManyRequests in fallback as rate limit error")
        void shouldHandleTooManyRequestsInFallback() {
            // Arrange
            when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

            // Act & Assert - 429 should be rethrown for retry mechanism
            assertThatThrownBy(() -> client.getPostById(1L))
                .isInstanceOf(HttpClientErrorException.class);
        }

        @Test
        @DisplayName("getPostWithComments fallback should throw when null returned")
        void fallbackShouldThrowNotFoundWhenNullReturned() {
            // Arrange
            when(restTemplate.getForObject(eq(BASE_URL + POSTS_PATH + "/999?_embed=comments"), any(Class.class)))
                .thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> client.getPostWithComments(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404);
        }

        @Test
        @DisplayName("getCommentsForPost fallback should handle server errors")
        void shouldRethrowServerErrorsForRetry() {
            // Arrange
            stubCommentsArrayResponseThrows(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

            // Act & Assert - server errors should be rethrown for retry
            assertThatThrownBy(() -> client.getCommentsForPost(1L))
                .isInstanceOf(HttpServerErrorException.class);
        }

        @Test
        @DisplayName("getPosts fallback should handle generic errors")
        void shouldHandleGenericRestClientException() {
            // Arrange - generic RestClientException should result in 500
            stubPostsArrayResponseThrows(new RestClientException("Unknown error"));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 500)
                .hasMessageContaining("unexpected error");
        }
    }

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Should handle empty posts array")
        void shouldHandleEmptyPostsArray() {
            // Arrange
            stubPostsArrayResponse(BASE_URL + POSTS_PATH, new AuditionPost[0]);

            // Act
            final List<AuditionPost> result = client.getPosts(PostSearchCriteria.builder().build());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty comments array")
        void shouldHandleEmptyCommentsArray() {
            // Arrange
            stubCommentsArrayResponse(BASE_URL + POSTS_PATH + "/1" + COMMENTS_PATH, new Comment[0]);

            // Act
            final List<Comment> result = client.getCommentsForPost(1L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle very large post ID")
        void shouldHandleVeryLargePostId() {
            // Arrange
            final Long largeId = Long.MAX_VALUE;
            final AuditionPost post = createPost(1);
            when(restTemplate.getForObject(BASE_URL + POSTS_PATH + "/" + largeId, AuditionPost.class))
                .thenReturn(post);

            // Act
            final AuditionPost result = client.getPostById(largeId);

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle post with null optional fields")
        void shouldHandlePostWithNullOptionalFields() {
            // Arrange
            final AuditionPost postWithNulls = AuditionPost.builder()
                .id(1)
                .userId(1)
                .title(null)  // null title
                .body(null)   // null body
                .build();
            when(restTemplate.getForObject(BASE_URL + POSTS_PATH + "/1", AuditionPost.class))
                .thenReturn(postWithNulls);

            // Act
            final AuditionPost result = client.getPostById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isNull();
            assertThat(result.getBody()).isNull();
        }

        @Test
        @DisplayName("Should handle post with empty comments list")
        void shouldHandlePostWithEmptyCommentsList() {
            // Arrange
            final AuditionPost postWithEmptyComments = AuditionPost.builder()
                .id(1)
                .userId(1)
                .title("Test")
                .body("Body")
                .comments(List.of())
                .build();
            when(restTemplate.getForObject(POST_WITH_COMMENTS_URL, AuditionPost.class))
                .thenReturn(postWithEmptyComments);

            // Act
            final AuditionPost result = client.getPostWithComments(1L);

            // Assert
            assertThat(result.getComments()).isEmpty();
        }

        @Test
        @DisplayName("Should handle titleContains with special characters")
        void shouldHandleTitleContainsWithSpecialCharacters() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            // Note: Special characters would be URL-encoded
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?title_like=test%26query", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .titleContains("test&query")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle empty titleContains string")
        void shouldHandleEmptyTitleContainsString() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH, posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .titleContains("")
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle page 1 as first page")
        void shouldHandlePageOneAsFirstPage() {
            // Arrange
            final AuditionPost[] posts = {createPost(1)};
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?_page=1&_limit=1", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(1)
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle large page numbers")
        void shouldHandleLargePageNumbers() {
            // Arrange
            final AuditionPost[] posts = {};  // Empty result for page beyond data
            stubPostsArrayResponse(BASE_URL + POSTS_PATH + "?_page=9999&_limit=10", posts);
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(9999)
                .size(10)
                .build();

            // Act
            final List<AuditionPost> result = client.getPosts(criteria);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Handling")
    class HttpStatusCodeHandlingTests {

        @Test
        @DisplayName("Should handle 400 Bad Request")
        void shouldHandle400BadRequest() {
            // Arrange
            stubPostsArrayResponseThrows(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 400);
        }

        @Test
        @DisplayName("Should handle 401 Unauthorized")
        void shouldHandle401Unauthorized() {
            // Arrange
            stubPostsArrayResponseThrows(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 401);
        }

        @Test
        @DisplayName("Should handle 403 Forbidden")
        void shouldHandle403Forbidden() {
            // Arrange
            stubPostsArrayResponseThrows(new HttpClientErrorException(HttpStatus.FORBIDDEN));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 403);
        }

        @Test
        @DisplayName("Should handle 404 in getPostWithComments")
        void shouldHandle404InGetPostWithComments() {
            // Arrange
            when(restTemplate.getForObject(eq(BASE_URL + POSTS_PATH + "/999?_embed=comments"), any(Class.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

            // Act & Assert
            assertThatThrownBy(() -> client.getPostWithComments(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404)
                .hasFieldOrPropertyWithValue(FIELD_TITLE, "Resource Not Found");
        }

        @Test
        @DisplayName("Should handle 404 in getCommentsForPost")
        void shouldHandle404InGetCommentsForPost() {
            // Arrange
            stubCommentsArrayResponseThrows(new HttpClientErrorException(HttpStatus.NOT_FOUND));

            // Act & Assert
            assertThatThrownBy(() -> client.getCommentsForPost(999L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404);
        }

        @Test
        @DisplayName("Should rethrow 500 Internal Server Error for retry")
        void shouldRethrow500ForRetry() {
            // Arrange
            stubPostsArrayResponseThrows(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(HttpServerErrorException.class);
        }

        @Test
        @DisplayName("Should rethrow 502 Bad Gateway for retry")
        void shouldRethrow502ForRetry() {
            // Arrange
            when(restTemplate.getForObject(anyString(), any(Class.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

            // Act & Assert
            assertThatThrownBy(() -> client.getPostById(1L))
                .isInstanceOf(HttpServerErrorException.class);
        }

        @Test
        @DisplayName("Should rethrow 503 Service Unavailable for retry")
        void shouldRethrow503ForRetry() {
            // Arrange
            when(restTemplate.getForObject(eq(POST_WITH_COMMENTS_URL), any(Class.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

            // Act & Assert
            assertThatThrownBy(() -> client.getPostWithComments(1L))
                .isInstanceOf(HttpServerErrorException.class);
        }

        @Test
        @DisplayName("Should rethrow 504 Gateway Timeout for retry")
        void shouldRethrow504ForRetry() {
            // Arrange
            stubCommentsArrayResponseThrows(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

            // Act & Assert
            assertThatThrownBy(() -> client.getCommentsForPost(1L))
                .isInstanceOf(HttpServerErrorException.class);
        }
    }

    @Nested
    @DisplayName("Connection Error Handling")
    class ConnectionErrorHandlingTests {

        @Test
        @DisplayName("Should handle connection refused")
        void shouldHandleConnectionRefused() {
            // Arrange
            stubPostsArrayResponseThrows(new ResourceAccessException(CONNECTION_REFUSED_MSG));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining(CONNECTION_REFUSED_MSG);
        }

        @Test
        @DisplayName("Should handle read timeout")
        void shouldHandleReadTimeout() {
            // Arrange
            stubPostsArrayResponseThrows(new ResourceAccessException("Read timed out"));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Read timed out");
        }

        @Test
        @DisplayName("Should handle unknown host")
        void shouldHandleUnknownHost() {
            // Arrange
            stubPostsArrayResponseThrows(new ResourceAccessException("Unknown host: api.example.com"));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Unknown host");
        }

        @Test
        @DisplayName("Should handle SSL handshake failure")
        void shouldHandleSslHandshakeFailure() {
            // Arrange
            stubPostsArrayResponseThrows(new ResourceAccessException("SSL handshake failed"));

            // Act & Assert
            assertThatThrownBy(() -> client.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("SSL handshake");
        }
    }

    private AuditionPost createPost(final int id) {
        return AuditionPost.builder()
            .id(id)
            .userId(1)
            .title("Test Post " + id)
            .body("Test Body " + id)
            .build();
    }

    private Comment createComment(final int id) {
        return Comment.builder()
            .id(id)
            .postId(1)
            .name("Test Comment " + id)
            .email("test@example.com")
            .body("Comment body " + id)
            .build();
    }

    /**
     * Stubs RestTemplate response for posts array requests.
     * Uses URL matching to decouple test from implementation details of how arrays are fetched.
     */
    @SuppressWarnings("PMD.UseVarargs") // Array parameter needed to match RestTemplate return type
    private void stubPostsArrayResponse(final String expectedUrl, final AuditionPost[] response) {
        when(restTemplate.getForObject(eq(expectedUrl), any(Class.class)))
            .thenAnswer((Answer<AuditionPost[]>) invocation -> response);
    }

    /**
     * Stubs RestTemplate to throw an exception for posts array requests.
     * Uses anyString() URL matching for error scenarios where URL doesn't matter.
     */
    private void stubPostsArrayResponseThrows(final RuntimeException exception) {
        when(restTemplate.getForObject(anyString(), any(Class.class)))
            .thenThrow(exception);
    }

    /**
     * Stubs RestTemplate response for comments array requests.
     * Uses URL matching to decouple test from implementation details of how arrays are fetched.
     */
    @SuppressWarnings("PMD.UseVarargs") // Array parameter needed to match RestTemplate return type
    private void stubCommentsArrayResponse(final String expectedUrl, final Comment[] response) {
        when(restTemplate.getForObject(eq(expectedUrl), any(Class.class)))
            .thenAnswer((Answer<Comment[]>) invocation -> response);
    }

    /**
     * Stubs RestTemplate to throw an exception for comments array requests.
     */
    private void stubCommentsArrayResponseThrows(final RuntimeException exception) {
        when(restTemplate.getForObject(anyString(), any(Class.class)))
            .thenThrow(exception);
    }
}
