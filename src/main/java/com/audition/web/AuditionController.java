package com.audition.web;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.model.PostSearchCriteria;
import com.audition.service.AuditionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AuditionController {

    private final AuditionService auditionService;

    public AuditionController(final AuditionService auditionService) {
        this.auditionService = auditionService;
    }

    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts(@Valid @ModelAttribute final PostSearchCriteria criteria) {
        return auditionService.getPosts(criteria);
    }

    @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostById(
            @PathVariable("id") @Positive(message = "Post id must be a positive integer") final Long postId,
            @RequestParam(value = "embed", defaultValue = "false") final boolean includeComments) {
        return auditionService.getPostById(postId, includeComments);
    }

    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Comment> getCommentsForPost(
            @PathVariable("postId") @Positive(message = "Post id must be a positive integer") final Long postId) {
        return auditionService.getCommentsForPost(postId);
    }
}
