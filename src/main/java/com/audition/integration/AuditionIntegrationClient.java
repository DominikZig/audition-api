package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AuditionIntegrationClient {

    private final transient RestTemplate restTemplate;
    private final transient AuditionLogger logger;

    @Value("${integration.client.url}")
    private transient String baseUrl;

    private static final String POSTS_ENDPOINT = "/posts";
    private static final String COMMENTS_ENDPOINT = "/comments";

    public AuditionIntegrationClient(final RestTemplate restTemplate, final AuditionLogger logger) {
        this.restTemplate = restTemplate;
        this.logger = logger;
    }

    public List<AuditionPost> getPosts() {
        return Optional.ofNullable(restTemplate.exchange(
                baseUrl + POSTS_ENDPOINT,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<AuditionPost>>() {
                }).getBody())
            .orElse(Collections.emptyList());
    }

    public AuditionPost getPostById(final String id) {
        try {
            return restTemplate.getForObject(baseUrl + POSTS_ENDPOINT + "/" + id, AuditionPost.class);
        } catch (final HttpClientErrorException e) {
            logger.logErrorWithException(log, e.getMessage(), e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, SystemException.RESOURCE_NOT_FOUND_STR,
                    HttpStatus.NOT_FOUND.value(), e);
            }
            throw new SystemException(e.getMessage(), e.getStatusCode().value(), e);
        }
    }

    public AuditionPost getPostWithCommentsById(final String id) {
        try {
            final AuditionPost auditionPost = getPostById(id);
            final List<AuditionComment> comments = Optional.ofNullable(restTemplate.exchange(
                    baseUrl + POSTS_ENDPOINT + "/" + id + COMMENTS_ENDPOINT,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<AuditionComment>>() {
                    }).getBody())
                .orElse(Collections.emptyList());

            auditionPost.setComments(comments);
            return auditionPost;
        } catch (final HttpClientErrorException e) {
            logger.logErrorWithException(log, e.getMessage(), e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with Comments with id " + id,
                    SystemException.RESOURCE_NOT_FOUND_STR,
                    HttpStatus.NOT_FOUND.value(), e);
            }
            throw new SystemException(e.getMessage(), e.getStatusCode().value(), e);
        }
    }

    public List<AuditionComment> getCommentsByPostId(final String id) {
        try {
            //confirm post exists
            getPostById(id);

            return Optional.ofNullable(restTemplate.exchange(
                    baseUrl + COMMENTS_ENDPOINT + "?postId=" + id,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<AuditionComment>>() {
                    }).getBody())
                .orElse(Collections.emptyList());

        } catch (final HttpClientErrorException e) {
            logger.logErrorWithException(log, e.getMessage(), e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find Comments for a Post with id " + id,
                    SystemException.RESOURCE_NOT_FOUND_STR,
                    HttpStatus.NOT_FOUND.value(), e);
            }
            throw new SystemException(e.getMessage(), e.getStatusCode().value(), e);
        }
    }
}
