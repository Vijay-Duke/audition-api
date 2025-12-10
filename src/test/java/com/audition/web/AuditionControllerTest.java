package com.audition.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import com.audition.service.AuditionService;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditionController Tests")
class AuditionControllerTest {

    @Mock
    private AuditionService auditionService;

    private AuditionController controller;

    @BeforeEach
    void setUp() {
        controller = new AuditionController(auditionService);
    }

    @Nested
    @DisplayName("getPosts()")
    class GetPostsTests {

        @Test
        @DisplayName("Should return list of posts from service without filters")
        void shouldReturnListOfPosts() {
            // Arrange
            final List<AuditionPost> expectedPosts = List.of(createPost(1), createPost(2));
            when(auditionService.getPosts(any(PostSearchCriteria.class))).thenReturn(expectedPosts);

            // Act
            final List<AuditionPost> result = controller.getPosts(PostSearchCriteria.builder().build());

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expectedPosts);
        }

        @Test
        @DisplayName("Should return empty list when no posts exist")
        void shouldReturnEmptyListWhenNoPosts() {
            // Arrange
            when(auditionService.getPosts(any(PostSearchCriteria.class))).thenReturn(List.of());

            // Act
            final List<AuditionPost> result = controller.getPosts(PostSearchCriteria.builder().build());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should pass criteria to service")
        void shouldPassCriteriaToService() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains("test")
                .page(1)
                .size(10)
                .sort("id")
                .order("desc")
                .build();
            when(auditionService.getPosts(any(PostSearchCriteria.class))).thenReturn(List.of());

            // Act
            controller.getPosts(criteria);

            // Assert
            verify(auditionService).getPosts(criteria);
        }
    }

    @Nested
    @DisplayName("getPostById()")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should delegate to service with includeComments false when include param is null")
        void shouldDelegateToServiceWithoutComments() {
            // Arrange
            final AuditionPost expectedPost = createPost(1);
            when(auditionService.getPostById(1L, false)).thenReturn(expectedPost);

            // Act
            final AuditionPost result = controller.getPostById(1L, null);

            // Assert
            assertThat(result).isEqualTo(expectedPost);
            verify(auditionService).getPostById(1L, false);
        }

        @Test
        @DisplayName("Should delegate to service with includeComments true when include=comments")
        void shouldDelegateToServiceWithComments() {
            // Arrange
            final AuditionPost expectedPost = AuditionPost.builder()
                .id(1)
                .userId(1)
                .title("Test Post 1")
                .body("Test Body 1")
                .comments(List.of(createComment(1)))
                .build();
            when(auditionService.getPostById(1L, true)).thenReturn(expectedPost);

            // Act
            final AuditionPost result = controller.getPostById(1L, "comments");

            // Assert
            assertThat(result).isEqualTo(expectedPost);
            assertThat(result.getComments()).hasSize(1);
            verify(auditionService).getPostById(1L, true);
        }
    }

    @Nested
    @DisplayName("getCommentsForPost()")
    class GetCommentsForPostTests {

        @Test
        @DisplayName("Should return comments for valid post id")
        void shouldReturnCommentsForValidPostId() {
            // Arrange
            final List<Comment> expectedComments = List.of(createComment(1), createComment(2));
            when(auditionService.getCommentsForPost(1L)).thenReturn(expectedComments);

            // Act
            final List<Comment> result = controller.getCommentsForPost(1L);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expectedComments);
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidationTests {

        @ParameterizedTest(name = "postId = {0} should be accepted")
        @ValueSource(longs = {1L, 100L, 2_147_483_647L})
        @DisplayName("Should accept valid positive post ids")
        void shouldAcceptValidPostId(final Long postId) {
            // Arrange
            final AuditionPost expectedPost = createPost(1);
            when(auditionService.getPostById(postId, false)).thenReturn(expectedPost);

            // Act
            final AuditionPost result = controller.getPostById(postId, null);

            // Assert
            assertThat(result).isEqualTo(expectedPost);
        }

        @Test
        @DisplayName("Should call service with correct post id for getCommentsForPost")
        void shouldCallServiceWithCorrectPostId() {
            // Arrange
            final List<Comment> expectedComments = List.of(createComment(1));
            when(auditionService.getCommentsForPost(42L)).thenReturn(expectedComments);

            // Act
            final List<Comment> result = controller.getCommentsForPost(42L);

            // Assert
            assertThat(result).isEqualTo(expectedComments);
            verify(auditionService).getCommentsForPost(42L);
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
}
