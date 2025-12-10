package com.audition.web;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditionController {

    private final AuditionService auditionService;

    public AuditionController(final AuditionService auditionService) {
        this.auditionService = auditionService;
    }

    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts() {
        return auditionService.getPosts();
    }

    @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostById(
            @PathVariable("id") final Long postId,
            @RequestParam(value = "embed", defaultValue = "false") final boolean includeComments) {
        return auditionService.getPostById(postId, includeComments);
    }

    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Comment> getCommentsForPost(@PathVariable("postId") final Long postId) {
        return auditionService.getCommentsForPost(postId);
    }
}
