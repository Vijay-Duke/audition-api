package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    private final AuditionIntegrationClient auditionIntegrationClient;

    public AuditionService(final AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    public List<AuditionPost> getPosts() {
        return auditionIntegrationClient.getPosts();
    }

    public AuditionPost getPostById(final Long postId, final boolean includeComments) {
        return includeComments
            ? auditionIntegrationClient.getPostWithComments(postId)
            : auditionIntegrationClient.getPostById(postId);
    }

    public List<Comment> getCommentsForPost(final Long postId) {
        return auditionIntegrationClient.getCommentsForPost(postId);
    }
}
