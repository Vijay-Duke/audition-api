package com.audition;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Audition API Integration Tests")
class AuditionApplicationIntegrationTest {

    private static final String CONTENT_TYPE = "Content-Type";

    @SuppressWarnings("PMD.FieldNamingConventions") // WireMock conventionally uses camelCase for server instances
    private static final WireMockServer wireMockServer;

    static {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() {
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("audition.api.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    class GetPostsTests {

        @Test
        @DisplayName("Should return posts from upstream API")
        void shouldReturnPostsFromUpstreamApi() {
            // Arrange
            wireMockServer.stubFor(get(urlPathEqualTo("/posts"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        [
                            {"userId": 1, "id": 1, "title": "Test Post", "body": "Test Body"},
                            {"userId": 1, "id": 2, "title": "Another Post", "body": "Another Body"}
                        ]
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Test Post");
            assertThat(response.getBody()).contains("Another Post");
        }

        @Test
        @DisplayName("Should filter posts by userId")
        void shouldFilterPostsByUserId() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts?userId=1"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        [{"userId": 1, "id": 1, "title": "User 1 Post", "body": "Body"}]
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts?userId=1", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("User 1 Post");
        }

        @Test
        @DisplayName("Should support pagination")
        void shouldSupportPagination() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts?_page=1&_limit=10"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        [{"userId": 1, "id": 1, "title": "Page 1", "body": "Body"}]
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts?page=1&size=10", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Page 1");
        }

        @Test
        @DisplayName("Should support sorting")
        void shouldSupportSorting() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts?_sort=title&_order=desc"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        [{"userId": 1, "id": 1, "title": "Zebra", "body": "Body"}]
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts?sort=title&order=desc", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Zebra");
        }

        @Test
        @DisplayName("Should return 400 for invalid userId")
        void shouldReturn400ForInvalidUserId() {
            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts?userId=-1", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("User id must be a positive integer");
        }

        @Test
        @DisplayName("Should return 400 for page without size")
        void shouldReturn400ForPageWithoutSize() {
            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts?page=1", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Both page and size must be provided together");
        }

        @Test
        @DisplayName("Should return 400 for invalid sort field")
        void shouldReturn400ForInvalidSortField() {
            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts?sort=invalid", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Sort field must be one of");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{id}")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should return post by id")
        void shouldReturnPostById() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts/1"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        {"userId": 1, "id": 1, "title": "Single Post", "body": "Body"}
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts/1", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Single Post");
        }

        @Test
        @DisplayName("Should return post with comments when requested")
        void shouldReturnPostWithComments() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts/1?_embed=comments"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        {
                            "userId": 1,
                            "id": 1,
                            "title": "Post With Comments",
                            "body": "Body",
                            "comments": [
                                {"postId": 1, "id": 1, "name": "Comment 1", "email": "a@b.com", "body": "Nice"}
                            ]
                        }
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts/1?include=comments", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Post With Comments");
            assertThat(response.getBody()).contains("Comment 1");
        }

        @Test
        @DisplayName("Should return 404 when post not found")
        void shouldReturn404WhenPostNotFound() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts/999"))
                .willReturn(aResponse()
                    .withStatus(404)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts/999", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return 400 for invalid post id")
        void shouldReturn400ForInvalidPostId() {
            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts/abc", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 for negative post id")
        void shouldReturn400ForNegativePostId() {
            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts/-1", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}/comments")
    class GetCommentsForPostTests {

        @Test
        @DisplayName("Should return comments for post")
        void shouldReturnCommentsForPost() {
            // Arrange
            wireMockServer.stubFor(get(urlEqualTo("/posts/1/comments"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody("""
                        [
                            {"postId": 1, "id": 1, "name": "Comment 1", "email": "a@b.com", "body": "Great"},
                            {"postId": 1, "id": 2, "name": "Comment 2", "email": "c@d.com", "body": "Nice"}
                        ]
                        """)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts/1/comments", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Comment 1");
            assertThat(response.getBody()).contains("Comment 2");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 503 when upstream API is unavailable")
        void shouldReturn503WhenUpstreamUnavailable() {
            // Arrange
            wireMockServer.stubFor(get(urlPathEqualTo("/posts"))
                .willReturn(aResponse()
                    .withStatus(500)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }

        @Test
        @DisplayName("Should return 429 when rate limited with rate limit headers")
        void shouldReturn429WhenRateLimited() {
            // Arrange
            wireMockServer.stubFor(get(urlPathEqualTo("/posts"))
                .willReturn(aResponse()
                    .withStatus(429)));

            // Act
            final ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/posts", String.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody()).contains("rate limit");
            assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("60");
            assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("100");
            assertThat(response.getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");
            assertThat(response.getHeaders().getFirst("X-RateLimit-Reset")).isNotNull();
        }
    }
}
