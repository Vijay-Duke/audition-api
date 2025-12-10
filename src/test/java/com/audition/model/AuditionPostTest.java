package com.audition.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AuditionPost Tests")
class AuditionPostTest {

    private static final String TEST_TITLE = "Title";
    private static final String TEST_BODY = "Body";

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create post with all fields using builder")
        void shouldCreatePostWithAllFields() {
            // Arrange
            final Comment comment = Comment.builder()
                .id(1)
                .postId(42)
                .name("Test Comment")
                .email("test@example.com")
                .body("Comment body")
                .build();

            // Act
            final AuditionPost post = AuditionPost.builder()
                .userId(10)
                .id(42)
                .title("Test Title")
                .body("Test body content")
                .comments(List.of(comment))
                .build();

            // Assert
            assertThat(post.getUserId()).isEqualTo(10);
            assertThat(post.getId()).isEqualTo(42);
            assertThat(post.getTitle()).isEqualTo("Test Title");
            assertThat(post.getBody()).isEqualTo("Test body content");
            assertThat(post.getComments()).hasSize(1);
            assertThat(post.getComments().get(0)).isEqualTo(comment);
        }

        @Test
        @DisplayName("Should create post with default values")
        void shouldCreatePostWithDefaultValues() {
            // Act
            final AuditionPost post = AuditionPost.builder().build();

            // Assert
            assertThat(post.getUserId()).isZero();
            assertThat(post.getId()).isZero();
            assertThat(post.getTitle()).isNull();
            assertThat(post.getBody()).isNull();
            assertThat(post.getComments()).isNull();
        }

        @Test
        @DisplayName("Should create post without comments")
        void shouldCreatePostWithoutComments() {
            // Act
            final AuditionPost post = AuditionPost.builder()
                .userId(1)
                .id(1)
                .title(TEST_TITLE)
                .body(TEST_BODY)
                .build();

            // Assert
            assertThat(post.getComments()).isNull();
        }

        @Test
        @DisplayName("Should create post with empty comments list")
        void shouldCreatePostWithEmptyCommentsList() {
            // Act
            final AuditionPost post = AuditionPost.builder()
                .userId(1)
                .id(1)
                .title(TEST_TITLE)
                .body(TEST_BODY)
                .comments(List.of())
                .build();

            // Assert
            assertThat(post.getComments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            // Arrange
            final AuditionPost post1 = AuditionPost.builder()
                .userId(1)
                .id(1)
                .title(TEST_TITLE)
                .body(TEST_BODY)
                .build();

            final AuditionPost post2 = AuditionPost.builder()
                .userId(1)
                .id(1)
                .title(TEST_TITLE)
                .body(TEST_BODY)
                .build();

            // Assert
            assertThat(post1).isEqualTo(post2);
            assertThat(post1.hashCode()).isEqualTo(post2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Arrange
            final AuditionPost post1 = AuditionPost.builder()
                .userId(1)
                .id(1)
                .title(TEST_TITLE + " 1")
                .body(TEST_BODY)
                .build();

            final AuditionPost post2 = AuditionPost.builder()
                .userId(1)
                .id(1)
                .title(TEST_TITLE + " 2")
                .body(TEST_BODY)
                .build();

            // Assert
            assertThat(post1).isNotEqualTo(post2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            final AuditionPost post = AuditionPost.builder()
                .userId(1)
                .id(42)
                .title("Test Title")
                .body("Test Body")
                .build();

            // Act
            final String toString = post.toString();

            // Assert
            assertThat(toString).contains("userId=1");
            assertThat(toString).contains("id=42");
            assertThat(toString).contains("title=Test Title");
            assertThat(toString).contains("body=Test Body");
        }
    }
}
