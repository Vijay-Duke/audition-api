package com.audition.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Comment Tests")
class CommentTest {

    private static final String TEST_EMAIL = "email@test.com";
    private static final String TEST_BODY = "Body";

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create comment with all fields using builder")
        void shouldCreateCommentWithAllFields() {
            // Act
            final Comment comment = Comment.builder()
                .postId(42L)
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .body("This is a comment body")
                .build();

            // Assert
            assertThat(comment.getPostId()).isEqualTo(42);
            assertThat(comment.getId()).isEqualTo(1);
            assertThat(comment.getName()).isEqualTo("John Doe");
            assertThat(comment.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(comment.getBody()).isEqualTo("This is a comment body");
        }

        @Test
        @DisplayName("Should create comment with default values")
        void shouldCreateCommentWithDefaultValues() {
            // Act
            final Comment comment = Comment.builder().build();

            // Assert
            assertThat(comment.getPostId()).isNull();
            assertThat(comment.getId()).isNull();
            assertThat(comment.getName()).isNull();
            assertThat(comment.getEmail()).isNull();
            assertThat(comment.getBody()).isNull();
        }

        @Test
        @DisplayName("Should create comment with partial fields")
        void shouldCreateCommentWithPartialFields() {
            // Act
            final Comment comment = Comment.builder()
                .id(5L)
                .postId(10L)
                .build();

            // Assert
            assertThat(comment.getId()).isEqualTo(5);
            assertThat(comment.getPostId()).isEqualTo(10);
            assertThat(comment.getName()).isNull();
            assertThat(comment.getEmail()).isNull();
            assertThat(comment.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            // Arrange
            final Comment comment1 = Comment.builder()
                .postId(1L)
                .id(1L)
                .name("Name")
                .email(TEST_EMAIL)
                .body(TEST_BODY)
                .build();

            final Comment comment2 = Comment.builder()
                .postId(1L)
                .id(1L)
                .name("Name")
                .email(TEST_EMAIL)
                .body(TEST_BODY)
                .build();

            // Assert
            assertThat(comment1).isEqualTo(comment2);
            assertThat(comment1.hashCode()).isEqualTo(comment2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Arrange
            final Comment comment1 = Comment.builder()
                .postId(1L)
                .id(1L)
                .name("Name 1")
                .email(TEST_EMAIL)
                .body(TEST_BODY)
                .build();

            final Comment comment2 = Comment.builder()
                .postId(1L)
                .id(1L)
                .name("Name 2")
                .email(TEST_EMAIL)
                .body(TEST_BODY)
                .build();

            // Assert
            assertThat(comment1).isNotEqualTo(comment2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            final Comment comment = Comment.builder()
                .postId(10L)
                .id(5L)
                .name("John")
                .email("john@test.com")
                .body("Comment text")
                .build();

            // Act
            final String toString = comment.toString();

            // Assert
            assertThat(toString).contains("postId=10");
            assertThat(toString).contains("id=5");
            assertThat(toString).contains("name=John");
            assertThat(toString).contains("email=john@test.com");
            assertThat(toString).contains("body=Comment text");
        }
    }
}
