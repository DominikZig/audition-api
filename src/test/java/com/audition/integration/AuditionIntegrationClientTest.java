package com.audition.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods") //need the test cases for thorough coverage
class AuditionIntegrationClientTest {

    private static final int SOME_ID = 1;
    private static final int INVALID_ID = 2513;
    private static final String BODY_1 = "body1";
    private static final String TITLE_1 = "title1";
    private static final String MOCK_POST_ENDPOINT = "null/posts";
    private static final String MOCK_COMMENTS_ENDPOINT = "/comments";
    private static final String MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM = "null/comments?postId=";
    private static final String NOT_FOUND = "Not Found";
    private static final String RESOURCE_NOT_FOUND = "Resource " + NOT_FOUND;
    private static final String CANNOT_FIND_POST_WITH_ID = "Cannot find a Post with id ";
    private static final String UNHANDLED_ERROR = "500 Unhandled Error";

    @Mock
    private transient RestTemplate restTemplate;

    @Mock
    private transient AuditionLogger logger;

    @InjectMocks
    private transient AuditionIntegrationClient auditionIntegrationClient;

    @BeforeEach
    void setup() {
        auditionIntegrationClient = new AuditionIntegrationClient(restTemplate, logger);
    }

    @Test
    void givenGetPostsWhenHasPostsThenReturnsPosts() {
        final List<AuditionPost> expectedPosts = Arrays.asList(
            new AuditionPost(1, 1, TITLE_1, BODY_1, null),
            new AuditionPost(2, 2, "title2", "body2", null)
        );

        final ResponseEntity<List<AuditionPost>> response = new ResponseEntity<>(expectedPosts, HttpStatus.OK);

        when(restTemplate.exchange(
            MOCK_POST_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionPost>>() {
            }
        )).thenReturn(response);

        final List<AuditionPost> actualPosts = auditionIntegrationClient.getPosts();

        assertEquals(expectedPosts, actualPosts);
        verify(restTemplate).exchange(
            MOCK_POST_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionPost>>() {
            }
        );
    }

    @Test
    void givenGetPostsWhenNoPostsThenReturnEmptyList() {
        final ResponseEntity<List<AuditionPost>> response = new ResponseEntity<>(Collections.emptyList(),
            HttpStatus.OK);

        when(restTemplate.exchange(
            MOCK_POST_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionPost>>() {
            }
        )).thenReturn(response);

        final List<AuditionPost> actualPosts = auditionIntegrationClient.getPosts();

        assertTrue(actualPosts.isEmpty());
        verify(restTemplate).exchange(
            MOCK_POST_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionPost>>() {
            }
        );
    }

    @Test
    void givenGetPostsByIdWhenHasPostThenReturnPost() {
        final AuditionPost expectedPost = new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1, null);

        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(expectedPost);

        final AuditionPost actualPost = auditionIntegrationClient.getPostById(String.valueOf(SOME_ID));

        assertEquals(expectedPost, actualPost);
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
    }

    @Test
    void givenGetPostsByIdWhenNonExistentIdThenThrowNotFound() {
        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + INVALID_ID,
            AuditionPost.class
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), NOT_FOUND));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostById(String.valueOf(INVALID_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals(CANNOT_FIND_POST_WITH_ID + INVALID_ID, exception.getMessage());
        assertEquals(CANNOT_FIND_POST_WITH_ID + INVALID_ID, exception.getDetail());
    }

    @Test
    void givenGetPostsByIdWhenUnHandledErrorThenThrowSystemException() {
        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "Unhandled Error"));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostById(String.valueOf(SOME_ID)));
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
        final AuditionPost expectedPostWithComments = new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1,
            auditionComments);

        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(expectedPostWithComments);
        when(restTemplate.exchange(
            MOCK_POST_ENDPOINT + "/" + SOME_ID + MOCK_COMMENTS_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            }
        )).thenReturn(new ResponseEntity<>(auditionComments, HttpStatus.OK));

        final AuditionPost actualPost = auditionIntegrationClient.getPostWithCommentsById(String.valueOf(SOME_ID));

        assertEquals(expectedPostWithComments, actualPost);
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
        verify(restTemplate).exchange(
            MOCK_POST_ENDPOINT + "/" + SOME_ID + MOCK_COMMENTS_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            });
    }

    @Test
    void givenGetPostWithCommentsByIdWhenNonExistentIdThenThrowNotFound() {
        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + INVALID_ID,
            AuditionPost.class
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), NOT_FOUND));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostWithCommentsById(String.valueOf(INVALID_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals(CANNOT_FIND_POST_WITH_ID + INVALID_ID, exception.getMessage());
        assertEquals(CANNOT_FIND_POST_WITH_ID + INVALID_ID, exception.getDetail());
    }

    @Test
    void givenGetPostWithCommentsByIdWhenPostHasNoCommentsThenThrowNotFound() {
        final AuditionPost expectedPostWithoutComments = new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1,
            Collections.emptyList());

        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(expectedPostWithoutComments);
        when(restTemplate.exchange(
            MOCK_POST_ENDPOINT + "/" + SOME_ID + MOCK_COMMENTS_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            }
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), NOT_FOUND));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostWithCommentsById(String.valueOf(SOME_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals("Cannot find a Post with Comments with id " + SOME_ID, exception.getMessage());
        assertEquals("Cannot find a Post with Comments with id " + SOME_ID, exception.getDetail());
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
    }

    @Test
    void givenGetPostWithCommentsWhenUnHandledErrorThenThrowSystemException() {
        final AuditionPost expectedPostWithoutComments = new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1,
            Collections.emptyList());

        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(expectedPostWithoutComments);
        when(restTemplate.exchange(
            MOCK_POST_ENDPOINT + "/" + SOME_ID + MOCK_COMMENTS_ENDPOINT,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            }
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "Unhandled Error"));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostWithCommentsById(String.valueOf(SOME_ID)));
        assertEquals("API Error Occurred", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(UNHANDLED_ERROR, exception.getMessage());
        assertEquals(UNHANDLED_ERROR, exception.getDetail());
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
    }

    @Test
    void givenGetCommentsByPostIdWhenHasCommentsThenReturnComments() {
        final List<AuditionComment> expectedComments = IntStream.range(0, 3)
            .mapToObj(i -> new AuditionComment(SOME_ID, i, "Comment " + i, "test@test.com", "Comment Body " + i))
            .toList();

        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1, expectedComments));
        when(restTemplate.exchange(
            MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM + SOME_ID,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            }
        )).thenReturn(new ResponseEntity<>(expectedComments, HttpStatus.OK));

        final List<AuditionComment> actualComments = auditionIntegrationClient.getCommentsByPostId(
            String.valueOf(SOME_ID));

        assertEquals(expectedComments, actualComments);
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
        verify(restTemplate).exchange(
            MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM + SOME_ID,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            });
    }

    @Test
    void givenGetCommentsByPostIdWhenNonExistentIdThenThrowNotFound() {
        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + INVALID_ID,
            AuditionPost.class
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), NOT_FOUND));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getCommentsByPostId(String.valueOf(INVALID_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals(CANNOT_FIND_POST_WITH_ID + INVALID_ID, exception.getMessage());
        assertEquals(CANNOT_FIND_POST_WITH_ID + INVALID_ID, exception.getDetail());
    }

    @Test
    void givenGetCommentsByPostIdWhenPostHasNoCommentsThenThrowNotFound() {
        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1, Collections.emptyList()));
        when(restTemplate.exchange(
            MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM + SOME_ID,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            }
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), NOT_FOUND));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getCommentsByPostId(String.valueOf(SOME_ID)));
        assertEquals(RESOURCE_NOT_FOUND, exception.getTitle());
        assertEquals(404, exception.getStatusCode());
        assertEquals("Cannot find Comments for a Post with id " + SOME_ID, exception.getMessage());
        assertEquals("Cannot find Comments for a Post with id " + SOME_ID, exception.getDetail());
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
        verify(restTemplate).exchange(
            MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM + SOME_ID,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            });
    }

    @Test
    void givenGetCommentsByPostIdWhenUnHandledErrorThenThrowSystemException() {
        when(restTemplate.getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        )).thenReturn(new AuditionPost(SOME_ID, SOME_ID, TITLE_1, BODY_1, Collections.emptyList()));
        when(restTemplate.exchange(
            MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM + SOME_ID,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            }
        )).thenThrow(new HttpClientErrorException(
            HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "Unhandled Error"));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getCommentsByPostId(String.valueOf(SOME_ID)));
        assertEquals("API Error Occurred", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(UNHANDLED_ERROR, exception.getMessage());
        assertEquals(UNHANDLED_ERROR, exception.getDetail());
        verify(restTemplate).getForObject(
            MOCK_POST_ENDPOINT + "/" + SOME_ID,
            AuditionPost.class
        );
        verify(restTemplate).exchange(
            MOCK_COMMENTS_ENDPOINT_WITH_QUERY_PARAM + SOME_ID,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            });
    }
}
