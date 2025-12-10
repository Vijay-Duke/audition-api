package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuditionApiProperties Tests")
@SuppressWarnings("PMD.TooManyMethods") // Test class with comprehensive coverage
class AuditionApiPropertiesTest {

    private static final String TEST_BASE_URL = "https://api.example.com";
    private static final String TEST_POSTS_PATH = "/posts";
    private static final String TEST_COMMENTS_PATH = "/comments";

    private AuditionApiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AuditionApiProperties();
    }

    @Test
    @DisplayName("Should have default connect timeout of 5000ms")
    void shouldHaveDefaultConnectTimeout() {
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(5000);
    }

    @Test
    @DisplayName("Should have default read timeout of 10000ms")
    void shouldHaveDefaultReadTimeout() {
        assertThat(properties.getReadTimeoutMs()).isEqualTo(10_000);
    }

    @Test
    @DisplayName("Should allow setting and getting baseUrl")
    void shouldSetAndGetBaseUrl() {
        // Act
        properties.setBaseUrl(TEST_BASE_URL);

        // Assert
        assertThat(properties.getBaseUrl()).isEqualTo(TEST_BASE_URL);
    }

    @Test
    @DisplayName("Should allow setting and getting postsPath")
    void shouldSetAndGetPostsPath() {
        // Act
        properties.setPostsPath(TEST_POSTS_PATH);

        // Assert
        assertThat(properties.getPostsPath()).isEqualTo(TEST_POSTS_PATH);
    }

    @Test
    @DisplayName("Should allow setting and getting commentsPath")
    void shouldSetAndGetCommentsPath() {
        // Act
        properties.setCommentsPath(TEST_COMMENTS_PATH);

        // Assert
        assertThat(properties.getCommentsPath()).isEqualTo(TEST_COMMENTS_PATH);
    }

    @Test
    @DisplayName("Should allow setting custom connect timeout")
    void shouldSetCustomConnectTimeout() {
        // Act
        properties.setConnectTimeoutMs(3000);

        // Assert
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(3000);
    }

    @Test
    @DisplayName("Should allow setting custom read timeout")
    void shouldSetCustomReadTimeout() {
        // Act
        properties.setReadTimeoutMs(15_000);

        // Assert
        assertThat(properties.getReadTimeoutMs()).isEqualTo(15_000);
    }

    @Test
    @DisplayName("Should have default circuit breaker name of auditionApi")
    void shouldHaveDefaultCircuitBreakerName() {
        assertThat(properties.getCircuitBreakerName()).isEqualTo("auditionApi");
    }

    @Test
    @DisplayName("Should allow setting and getting circuitBreakerName")
    void shouldSetAndGetCircuitBreakerName() {
        // Act
        properties.setCircuitBreakerName("customCircuitBreaker");

        // Assert
        assertThat(properties.getCircuitBreakerName()).isEqualTo("customCircuitBreaker");
    }

    @Test
    @DisplayName("Should have correct equals when properties match")
    void shouldBeEqualWhenPropertiesMatch() {
        // Arrange
        final AuditionApiProperties props1 = new AuditionApiProperties();
        props1.setBaseUrl(TEST_BASE_URL);
        props1.setPostsPath(TEST_POSTS_PATH);
        props1.setCommentsPath(TEST_COMMENTS_PATH);

        final AuditionApiProperties props2 = new AuditionApiProperties();
        props2.setBaseUrl(TEST_BASE_URL);
        props2.setPostsPath(TEST_POSTS_PATH);
        props2.setCommentsPath(TEST_COMMENTS_PATH);

        // Assert
        assertThat(props1).isEqualTo(props2);
        assertThat(props1.hashCode()).isEqualTo(props2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when properties differ")
    void shouldNotBeEqualWhenPropertiesDiffer() {
        // Arrange
        final AuditionApiProperties props1 = new AuditionApiProperties();
        props1.setBaseUrl("https://api1.example.com");

        final AuditionApiProperties props2 = new AuditionApiProperties();
        props2.setBaseUrl("https://api2.example.com");

        // Assert
        assertThat(props1).isNotEqualTo(props2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        assertThat(properties).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        assertThat(properties).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        assertThat(properties).isEqualTo(properties);
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void shouldGenerateMeaningfulToString() {
        // Arrange
        properties.setBaseUrl(TEST_BASE_URL);
        properties.setPostsPath(TEST_POSTS_PATH);
        properties.setCommentsPath(TEST_COMMENTS_PATH);

        // Act
        final String toString = properties.toString();

        // Assert
        assertThat(toString).contains("baseUrl=" + TEST_BASE_URL);
        assertThat(toString).contains("postsPath=" + TEST_POSTS_PATH);
        assertThat(toString).contains("commentsPath=" + TEST_COMMENTS_PATH);
        assertThat(toString).contains("connectTimeoutMs=5000");
        assertThat(toString).contains("readTimeoutMs=10000");
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {
        // Arrange
        properties.setBaseUrl(TEST_BASE_URL);
        final int hashCode1 = properties.hashCode();
        final int hashCode2 = properties.hashCode();

        // Assert
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("Should implement canEqual correctly")
    void shouldImplementCanEqual() {
        // Arrange
        final AuditionApiProperties props1 = new AuditionApiProperties();
        final AuditionApiProperties props2 = new AuditionApiProperties();

        // Assert - both should be able to equal each other
        assertThat(props1.canEqual(props2)).isTrue();
        assertThat(props2.canEqual(props1)).isTrue();
    }
}
