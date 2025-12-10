package com.audition.web;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import com.audition.service.AuditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for audition post and comment operations.
 * Provides endpoints for retrieving posts with optional filtering, pagination, and sorting.
 *
 * <p>API versioning uses URL path strategy: /api/v1/*</p>
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Posts", description = "Operations for managing posts and comments (API v1)")
public class AuditionController {

    private static final Logger LOG = LoggerFactory.getLogger(AuditionController.class);

    private final AuditionService auditionService;

    public AuditionController(final AuditionService auditionService) {
        this.auditionService = auditionService;
    }

    /**
     * Retrieves a list of posts based on the provided search criteria.
     *
     * @param criteria the search criteria including optional filters (userId, titleContains),
     *                 pagination (page, size), and sorting (sort, order)
     * @return a list of posts matching the criteria, or an empty list if none found
     */
    @Operation(
        summary = "Get posts",
        description = "Retrieves posts with optional filtering by userId, title search, pagination, and sorting"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "503", description = "Upstream service unavailable",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts(@Valid @ModelAttribute final PostSearchCriteria criteria) {
        LOG.debug("Getting posts with criteria: {}", criteria);
        final List<AuditionPost> posts = auditionService.getPosts(criteria);
        LOG.debug("Returning {} posts", posts.size());
        return posts;
    }

    /**
     * Retrieves a single post by its ID, optionally including its comments.
     *
     * <p>Uses JSON:API style inclusion pattern: {@code ?include=comments}
     * This allows fetching related resources in a single request to reduce round trips.</p>
     *
     * @param postId the ID of the post to retrieve (must be a positive integer)
     * @param include comma-separated list of related resources to include (supported: "comments")
     * @return the post with the specified ID
     * @throws com.audition.common.exception.SystemException with status 404 if post not found
     */
    @Operation(
        summary = "Get post by ID",
        description = "Retrieves a single post by its ID. Use ?include=comments to embed comments in the response."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved post"),
        @ApiResponse(responseCode = "400", description = "Invalid post ID (must be positive integer)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Post not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "503", description = "Upstream service unavailable",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostById(
            @Parameter(description = "Post ID", example = "1", schema = @Schema(minimum = "1"))
            @PathVariable("id") @Positive(message = "Post id must be a positive integer") final Long postId,
            @Parameter(description = "Related resources to include (e.g., 'comments')", example = "comments")
            @RequestParam(value = "include", required = false) final String include) {
        final boolean includeComments = "comments".equalsIgnoreCase(include);
        LOG.debug("Getting post by id: {}, includeComments: {}", postId, includeComments);
        final AuditionPost post = auditionService.getPostById(postId, includeComments);
        LOG.debug("Returning post: {}", post.getId());
        return post;
    }

    /**
     * Retrieves all comments for a specific post.
     *
     * @param postId the ID of the post whose comments to retrieve (must be a positive integer)
     * @return a list of comments for the specified post, or an empty list if none exist
     */
    @Operation(
        summary = "Get comments for post",
        description = "Retrieves all comments associated with a specific post"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
        @ApiResponse(responseCode = "400", description = "Invalid post ID (must be positive integer)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "503", description = "Upstream service unavailable",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Comment> getCommentsForPost(
            @Parameter(description = "Post ID", example = "1", schema = @Schema(minimum = "1"))
            @PathVariable("postId") @Positive(message = "Post id must be a positive integer") final Long postId) {
        LOG.debug("Getting comments for post: {}", postId);
        final List<Comment> comments = auditionService.getCommentsForPost(postId);
        LOG.debug("Returning {} comments for post: {}", comments.size(), postId);
        return comments;
    }
}
