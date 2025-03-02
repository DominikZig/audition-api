package com.audition.web;

import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AuditionController {

    private final transient AuditionService auditionService;

    public AuditionController(final AuditionService auditionService) {
        this.auditionService = auditionService;
    }

    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts(
        @RequestParam(defaultValue = "0") @PositiveOrZero final int offset,
        @RequestParam(defaultValue = "100") @Max(500) @Positive final int limit) {

        return auditionService.getPosts(offset, limit);
    }

    @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPosts(@PathVariable("id") @Positive final String postId) {
        return auditionService.getPostById(postId);
    }

    @GetMapping(value = "/posts/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostsWithComments(@PathVariable("id") @Positive final String postId) {
        return auditionService.getPostWithCommentsById(postId);
    }

    @GetMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionComment> getComments(@RequestParam @Positive final String postId) {
        return auditionService.getCommentsByPostId(postId);
    }
}
