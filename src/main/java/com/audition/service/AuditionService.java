package com.audition.service;

import com.audition.common.exception.SystemException;
import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    private final transient AuditionIntegrationClient auditionIntegrationClient;

    public AuditionService(final AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    public List<AuditionPost> getPosts(final int offset, final int limit) {
        final List<AuditionPost> posts = auditionIntegrationClient.getPosts();

        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        if (offset >= posts.size()) {
            throw new SystemException("Offset Param cannot be greater than amount of Posts: " + offset, "Bad Request",
                HttpStatus.BAD_REQUEST.value());
        }

        final int endIndex = Math.min(offset + limit, posts.size());

        return posts.subList(offset, endIndex);
    }

    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    public AuditionPost getPostWithCommentsById(final String id) {
        return auditionIntegrationClient.getPostWithCommentsById(id);
    }

    public List<AuditionComment> getCommentsByPostId(final String id) {
        return auditionIntegrationClient.getCommentsByPostId(id);
    }
}
