package com.audition.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods") //need the test cases for thorough coverage
class AuditionServiceTest {

    private static final int SOME_ID = 1;
    private static final int INVALID_ID = 2513;
    private static final String CANNOT_FIND_POST_ID = "Cannot find a Post with id ";
    private static final String RESOURCE_NOT_FOUND = "Resource Not Found";
    private static final String UNHANDLED_ERROR = "500 Unhandled Error";

    @Mock
    private transient AuditionIntegrationClient auditionIntegrationClient;

    @InjectMocks
    private transient AuditionService auditionService;

    private transient List<AuditionPost> auditionPosts;

    @BeforeEach
    void setup() {
        auditionPosts = IntStream.range(0, 1000)
            .mapToObj(i -> new AuditionPost(i, i, "Sample Post " + i, "Sample Body " + i, Collections.emptyList()))
            .toList();
    }

    @Test
    void givenGetPostsWhenDefaultParamsThenReturnFirst100Results() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(0, 100);

        assertEquals(100, paginatedPostsResponse.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i));
        }
    }

    @Test
    void givenGetPostsWhenLimitParamIs500ThenReturnFirst500Results() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(0, 500);

        assertEquals(500, paginatedPostsResponse.size());

        for (int i = 0; i < 500; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i));
        }
    }

    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is 200 "
        + "Then Return the results from 50 to 250")
    @Test
    void givenGetPostsPaginatedResponse50to250() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(50, 200);

        assertEquals(200, paginatedPostsResponse.size());

        for (int i = 50; i < 250; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i - 50));
        }
    }

    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is the default of 100 "
        + "Then Return the results from 50 to 150")
    @Test
    void givenGPostsPaginatedResponse50to150() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(50, 100);

        assertEquals(100, paginatedPostsResponse.size());

        for (int i = 50; i < 150; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i - 50));
        }
    }

    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is 500 "
        + "Then Return the results from 50 to 550")
    @Test
    void givenGPostsPaginatedResponse50to550() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(50, 500);

        assertEquals(500, paginatedPostsResponse.size());

        for (int i = 50; i < 500; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i - 50));
        }
    }

    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is 25 "
        + "Then Return the results from 50 to 75")
    @Test
    void givenGPostsPaginatedResponse50to75() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(50, 25);

        assertEquals(25, paginatedPostsResponse.size());

        for (int i = 50; i < 75; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i - 50));
        }
    }

    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 250 and Limit param is 500 "
        + "Then Return the results from 250 to 750")
    @Test
    void givenGPostsPaginatedResponse250to750() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(250, 500);

        assertEquals(500, paginatedPostsResponse.size());

        for (int i = 250; i < 750; i++) {
            assertEquals(auditionPosts.get(i), paginatedPostsResponse.get(i - 250));
        }
    }

    @Test
    void givenGetPostsWhenNoPostsThenReturnEmptyResult() {
        when(auditionIntegrationClient.getPosts()).thenReturn(Collections.emptyList());

        final List<AuditionPost> paginatedPostsResponse = auditionService.getPosts(0, 100);

        assertEquals(0, paginatedPostsResponse.size());
    }

    @Test
    void givenGetPostsWhenOffsetInvalidThenThrowBadRequest() {
        when(auditionIntegrationClient.getPosts()).thenReturn(auditionPosts);

        final int invalidOffset = 1001;

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getPosts(invalidOffset, 100));
        assertEquals("Bad Request", exception.getTitle());
        assertEquals(400, exception.getStatusCode());
        assertEquals("Offset Param cannot be greater than amount of Posts: " + invalidOffset, exception.getMessage());
        assertEquals("Offset Param cannot be greater than amount of Posts: " + invalidOffset, exception.getDetail());
    }

    @Test
    void givenGetPostByIdWhenValidIdThenReturnSinglePost() {
        final AuditionPost expectedPost = new AuditionPost(SOME_ID, SOME_ID, "Sample Post 1", "Sample Body 1",
            Collections.emptyList());
        when(auditionIntegrationClient.getPostById(String.valueOf(SOME_ID))).thenReturn(expectedPost);
        final AuditionPost actualPost = auditionService.getPostById(String.valueOf(SOME_ID));
        assertEquals(expectedPost, actualPost);
    }

    @Test
    void givenGetPostsByIdWhenNonExistentIdThenThrowNotFound() {
        when(auditionIntegrationClient.getPostById(String.valueOf(INVALID_ID))).thenThrow(new SystemException(
            CANNOT_FIND_POST_ID + INVALID_ID, SystemException.RESOURCE_NOT_FOUND_STR,
            HttpStatus.NOT_FOUND.value()));
        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getPostById(String.valueOf(INVALID_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals(CANNOT_FIND_POST_ID + INVALID_ID, exception.getMessage());
        assertEquals(CANNOT_FIND_POST_ID + INVALID_ID, exception.getDetail());
    }

    @Test
    void givenGetPostsByIdWhenUnHandledErrorThenThrowSystemException() {
        when(auditionIntegrationClient.getPostById(String.valueOf(SOME_ID))).thenThrow(new SystemException(
            UNHANDLED_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR.value(), new HttpClientErrorException(HttpStatusCode.valueOf(500))));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getPostById(String.valueOf(SOME_ID)));
        assertEquals("API Error Occurred", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(UNHANDLED_ERROR, exception.getMessage());
        assertEquals(UNHANDLED_ERROR, exception.getDetail());
    }

    @Test
    void givenGetPostWithCommentsByIdWhenHasPostWithCommentsThenReturnPostWithComments() {
        final List<AuditionComment> auditionComments = IntStream.range(0, 3)
            .mapToObj(i -> new AuditionComment(SOME_ID, i, "Comment " + i, "test@test.com", "Comment Body " + i))
            .toList();
        final AuditionPost expectedPostWithComments = new AuditionPost(SOME_ID, SOME_ID, "title1", "body1",
            auditionComments);

        when(auditionIntegrationClient.getPostWithCommentsById(String.valueOf(SOME_ID))).thenReturn(
            expectedPostWithComments);

        final AuditionPost actualPost = auditionService.getPostWithCommentsById(String.valueOf(SOME_ID));

        assertEquals(expectedPostWithComments, actualPost);
    }

    @Test
    void givenGetPostWithCommentsByIdWhenNonExistentIdThenThrowNotFound() {
        when(auditionIntegrationClient.getPostWithCommentsById(String.valueOf(INVALID_ID))).thenThrow(
            new SystemException(
                CANNOT_FIND_POST_ID + INVALID_ID, SystemException.RESOURCE_NOT_FOUND_STR,
                HttpStatus.NOT_FOUND.value()));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getPostWithCommentsById(String.valueOf(INVALID_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals(CANNOT_FIND_POST_ID + INVALID_ID, exception.getMessage());
        assertEquals(CANNOT_FIND_POST_ID + INVALID_ID, exception.getDetail());
    }

    @Test
    void givenGetPostWithCommentsByIdWhenPostHasNoCommentsThenThrowNotFound() {
        when(auditionIntegrationClient.getPostWithCommentsById(String.valueOf(SOME_ID))).thenThrow(new SystemException(
            UNHANDLED_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR.value(), new HttpClientErrorException(HttpStatusCode.valueOf(500))));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getPostWithCommentsById(String.valueOf(SOME_ID)));
        assertEquals("API Error Occurred", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(UNHANDLED_ERROR, exception.getMessage());
        assertEquals(UNHANDLED_ERROR, exception.getDetail());
    }

    @Test
    void givenGetCommentsByPostIdWhenHasCommentsThenReturnComments() {
        final List<AuditionComment> expectedComments = IntStream.range(0, 3)
            .mapToObj(i -> new AuditionComment(SOME_ID, i, "Comment " + i, "test@test.com", "Comment Body " + i))
            .toList();

        when(auditionIntegrationClient.getCommentsByPostId(String.valueOf(SOME_ID))).thenReturn(
            expectedComments);
        final List<AuditionComment> actualComments = auditionService.getCommentsByPostId(String.valueOf(SOME_ID));

        assertEquals(expectedComments, actualComments);
    }

    @Test
    void givenGetCommentsByPostIdWhenNonExistentIdThenThrowNotFound() {
        when(auditionIntegrationClient.getCommentsByPostId(String.valueOf(INVALID_ID))).thenThrow(
            new SystemException(
                CANNOT_FIND_POST_ID + INVALID_ID, SystemException.RESOURCE_NOT_FOUND_STR,
                HttpStatus.NOT_FOUND.value()));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getCommentsByPostId(String.valueOf(INVALID_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals(CANNOT_FIND_POST_ID + INVALID_ID, exception.getMessage());
        assertEquals(CANNOT_FIND_POST_ID + INVALID_ID, exception.getDetail());
    }

    @Test
    void givenGetCommentsByPostIdWhenPostHasNoCommentsThenThrowNotFound() {
        when(auditionIntegrationClient.getCommentsByPostId(String.valueOf(SOME_ID))).thenThrow(
            new SystemException(
                "Cannot find Comments for a Post with id " + SOME_ID, SystemException.RESOURCE_NOT_FOUND_STR,
                HttpStatus.NOT_FOUND.value()));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getCommentsByPostId(String.valueOf(SOME_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals("Cannot find Comments for a Post with id " + SOME_ID, exception.getMessage());
        assertEquals("Cannot find Comments for a Post with id " + SOME_ID, exception.getDetail());
    }

    @Test
    void givenGetCommentsByPostIdWhenUnHandledErrorThenThrowSystemException() {
        when(auditionIntegrationClient.getCommentsByPostId(String.valueOf(SOME_ID))).thenThrow(new SystemException(
            UNHANDLED_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR.value(), new HttpClientErrorException(HttpStatusCode.valueOf(500))));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionService.getCommentsByPostId(String.valueOf(SOME_ID)));
        assertEquals("API Error Occurred", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(UNHANDLED_ERROR, exception.getMessage());
        assertEquals(UNHANDLED_ERROR, exception.getDetail());
    }
}
