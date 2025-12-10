package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    private final AuditionIntegrationClient auditionIntegrationClient;

    public AuditionService(final AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    /**
     * Retrieves posts based on the provided search criteria.
     *
     * @param criteria the search criteria including filters, pagination, and sorting options
     * @return a list of posts matching the criteria, or an empty list if none found
     */
    public List<AuditionPost> getPosts(final PostSearchCriteria criteria) {
        return auditionIntegrationClient.getPosts(criteria);
    }

    /**
     * Retrieves a single post by its ID, optionally including comments.
     *
     * @param postId the ID of the post to retrieve
     * @param includeComments whether to include comments in the response
     * @return the post with the specified ID, with comments if requested
     * @throws com.audition.common.exception.SystemException with status 404 if post not found
     */
    public AuditionPost getPostById(final Long postId, final boolean includeComments) {
        return includeComments
            ? auditionIntegrationClient.getPostWithComments(postId)
            : auditionIntegrationClient.getPostById(postId);
    }

    /**
     * Retrieves all comments for a specific post.
     *
     * @param postId the ID of the post whose comments to retrieve
     * @return a list of comments for the specified post, or an empty list if none exist
     */
    public List<Comment> getCommentsForPost(final Long postId) {
        return auditionIntegrationClient.getCommentsForPost(postId);
    }
}
