package com.audition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditionService Tests")
class AuditionServiceTest {

    private static final String FIELD_STATUS_CODE = "statusCode";

    @Mock
    private AuditionIntegrationClient integrationClient;

    private AuditionService service;

    @BeforeEach
    void setUp() {
        service = new AuditionService(integrationClient);
    }

    @Nested
    @DisplayName("getPosts()")
    class GetPostsTests {

        @Test
        @DisplayName("Should delegate to integration client and return posts")
        void shouldDelegateToClientAndReturnPosts() {
            // Arrange
            final List<AuditionPost> expectedPosts = List.of(createPost(1), createPost(2));
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();
            when(integrationClient.getPosts(criteria)).thenReturn(expectedPosts);

            // Act
            final List<AuditionPost> result = service.getPosts(criteria);

            // Assert
            assertThat(result).isEqualTo(expectedPosts);
            assertThat(result).hasSize(2);
            verify(integrationClient).getPosts(criteria);
        }

        @Test
        @DisplayName("Should return empty list when client returns empty list")
        void shouldReturnEmptyListWhenClientReturnsEmptyList() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();
            when(integrationClient.getPosts(criteria)).thenReturn(List.of());

            // Act
            final List<AuditionPost> result = service.getPosts(criteria);

            // Assert
            assertThat(result).isEmpty();
            verify(integrationClient).getPosts(criteria);
        }

        @Test
        @DisplayName("Should propagate exception from client")
        void shouldPropagateExceptionFromClient() {
            // Arrange
            when(integrationClient.getPosts(any(PostSearchCriteria.class)))
                .thenThrow(SystemException.serviceUnavailable("API Error"));

            // Act & Assert
            assertThatThrownBy(() -> service.getPosts(PostSearchCriteria.builder().build()))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 503);
        }

        @Test
        @DisplayName("Should pass criteria with filters to integration client")
        void shouldPassCriteriaWithFiltersToClient() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains("test")
                .build();
            final List<AuditionPost> expectedPosts = List.of(createPost(1));
            when(integrationClient.getPosts(criteria)).thenReturn(expectedPosts);

            // Act
            final List<AuditionPost> result = service.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
            verify(integrationClient).getPosts(criteria);
        }

        @Test
        @DisplayName("Should pass criteria with pagination to integration client")
        void shouldPassCriteriaWithPaginationToClient() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(10)
                .build();
            final List<AuditionPost> expectedPosts = List.of(createPost(1));
            when(integrationClient.getPosts(criteria)).thenReturn(expectedPosts);

            // Act
            final List<AuditionPost> result = service.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
            verify(integrationClient).getPosts(criteria);
        }

        @Test
        @DisplayName("Should pass criteria with sort to integration client")
        void shouldPassCriteriaWithSortToClient() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("id")
                .order("desc")
                .build();
            final List<AuditionPost> expectedPosts = List.of(createPost(1));
            when(integrationClient.getPosts(criteria)).thenReturn(expectedPosts);

            // Act
            final List<AuditionPost> result = service.getPosts(criteria);

            // Assert
            assertThat(result).hasSize(1);
            verify(integrationClient).getPosts(criteria);
        }
    }

    @Nested
    @DisplayName("getPostById()")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should call getPostById on client when includeComments is false")
        void shouldCallGetPostByIdWhenIncludeCommentsFalse() {
            // Arrange
            final AuditionPost expectedPost = createPost(1);
            when(integrationClient.getPostById(1L)).thenReturn(expectedPost);

            // Act
            final AuditionPost result = service.getPostById(1L, false);

            // Assert
            assertThat(result).isEqualTo(expectedPost);
            assertThat(result.getId()).isEqualTo(1);
            verify(integrationClient).getPostById(1L);
        }

        @Test
        @DisplayName("Should call getPostWithComments on client when includeComments is true")
        void shouldCallGetPostWithCommentsWhenIncludeCommentsTrue() {
            // Arrange
            final AuditionPost expectedPost = AuditionPost.builder()
                .id(1L)
                .userId(1L)
                .title("Test Post 1")
                .body("Test Body 1")
                .comments(List.of(createComment(1), createComment(2)))
                .build();
            when(integrationClient.getPostWithComments(1L)).thenReturn(expectedPost);

            // Act
            final AuditionPost result = service.getPostById(1L, true);

            // Assert
            assertThat(result).isEqualTo(expectedPost);
            assertThat(result.getComments()).hasSize(2);
            verify(integrationClient).getPostWithComments(1L);
        }

        @Test
        @DisplayName("Should propagate 404 exception from client")
        void shouldPropagate404ExceptionFromClient() {
            // Arrange
            when(integrationClient.getPostById(999L))
                .thenThrow(SystemException.notFound("Cannot find Post with id 999"));

            // Act & Assert
            assertThatThrownBy(() -> service.getPostById(999L, false))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404)
                .hasFieldOrPropertyWithValue("title", "Resource Not Found");
        }

        @Test
        @DisplayName("Should propagate exception when fetching with comments")
        void shouldPropagateExceptionWhenFetchingWithComments() {
            // Arrange
            when(integrationClient.getPostWithComments(999L))
                .thenThrow(SystemException.notFound("Cannot find Post with id 999"));

            // Act & Assert
            assertThatThrownBy(() -> service.getPostById(999L, true))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 404);
        }
    }

    @Nested
    @DisplayName("getCommentsForPost()")
    class GetCommentsForPostTests {

        @Test
        @DisplayName("Should delegate to integration client and return comments")
        void shouldDelegateToClientAndReturnComments() {
            // Arrange
            final List<Comment> expectedComments = List.of(createComment(1), createComment(2));
            when(integrationClient.getCommentsForPost(1L)).thenReturn(expectedComments);

            // Act
            final List<Comment> result = service.getCommentsForPost(1L);

            // Assert
            assertThat(result).isEqualTo(expectedComments);
            assertThat(result).hasSize(2);
            verify(integrationClient).getCommentsForPost(1L);
        }

        @Test
        @DisplayName("Should return empty list when client returns empty list")
        void shouldReturnEmptyListWhenClientReturnsEmptyList() {
            // Arrange
            when(integrationClient.getCommentsForPost(1L)).thenReturn(List.of());

            // Act
            final List<Comment> result = service.getCommentsForPost(1L);

            // Assert
            assertThat(result).isEmpty();
            verify(integrationClient).getCommentsForPost(1L);
        }

        @Test
        @DisplayName("Should propagate exception from client")
        void shouldPropagateExceptionFromClient() {
            // Arrange
            when(integrationClient.getCommentsForPost(1L))
                .thenThrow(SystemException.serviceUnavailable("Service Unavailable"));

            // Act & Assert
            assertThatThrownBy(() -> service.getCommentsForPost(1L))
                .isInstanceOf(SystemException.class)
                .hasFieldOrPropertyWithValue(FIELD_STATUS_CODE, 503);
        }
    }

    private AuditionPost createPost(final long id) {
        return AuditionPost.builder()
            .id(id)
            .userId(1L)
            .title("Test Post " + id)
            .body("Test Body " + id)
            .build();
    }

    private Comment createComment(final long id) {
        return Comment.builder()
            .id(id)
            .postId(1L)
            .name("Test Comment " + id)
            .email("test@example.com")
            .body("Comment body " + id)
            .build();
    }
}
