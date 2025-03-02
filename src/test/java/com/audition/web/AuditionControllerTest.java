package com.audition.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditionController.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"}) //need the test cases for thorough coverage
@WithMockUser(username = "default", password = "defaultpassword")
class AuditionControllerTest {

    private static final String TEST_EMAIL = "test@test.com";
    private static final String MOCK_POSTS_ENDPOINT = "/posts";
    private static final String SAMPLE_POST = "Sample Post 1";
    private static final String SAMPLE_BODY = "Sample Body 1";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String TEST = "test";

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private transient AuditionService auditionService;

    @MockBean
    @SuppressWarnings("PMD.UnusedPrivateField")
    private transient AuditionLogger auditionLogger;

    private transient List<AuditionPost> auditionPosts;

    @BeforeEach
    void setup() {
        auditionPosts = IntStream.range(0, 1000)
            .mapToObj(i -> new AuditionPost(i, i, "Sample Post " + i, "Sample Body " + i, Collections.emptyList()))
            .toList();
    }

    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.AvoidDuplicateLiterals"})
    @Test
    void givenGetPostsWhenDefaultParamsThenReturnFirst100Results() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(0, 100);

        when(auditionService.getPosts(0, 100)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(100)));

        verify(auditionService).getPosts(0, 100);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", TEST})
    void givenGetPostsWhenLimitIsInvalidThenThrowBadRequest(final String limitParam) throws Exception {
        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(LIMIT, limitParam))
            .andExpect(status().isBadRequest());

        verify(auditionService, never()).getPosts(anyInt(), anyInt());
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @ParameterizedTest
    @ValueSource(strings = {"-1", TEST})
    void givenGetPostsWhenOffsetIsInvalidThenThrowBadRequest(final String offsetParam) throws Exception {
        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(OFFSET, offsetParam))
            .andExpect(status().isBadRequest());

        verify(auditionService, never()).getPosts(anyInt(), anyInt());
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetPostsWhenLimitExceedsMaxThenThrowBadRequest() throws Exception {
        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(LIMIT, "501"))
            .andExpect(status().isBadRequest());

        verify(auditionService, never()).getPosts(anyInt(), anyInt());
    }

    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.AvoidDuplicateLiterals"})
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is not provided and Limit param is 500 "
        + "Then Return the results from 0 to 500")
    @Test
    void givenGetPostsPaginatedResponse0to500() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(0, 500);

        when(auditionService.getPosts(0, 500)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(LIMIT, "500"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(500)))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(0).getUserId())))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(499).getUserId())))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(500).getUserId()))));

        verify(auditionService).getPosts(0, 500);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is 200 "
        + "Then Return the results from 50 to 250")
    @Test
    void givenGetPostsPaginatedResponse50to250() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(50,
            Math.min(50 + 200, auditionPosts.size()));

        when(auditionService.getPosts(50, 200)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(LIMIT, "200")
                .param(OFFSET, "50"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(200)))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(50).getUserId())))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(249).getUserId())))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(49).getUserId()))))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(250).getUserId()))));

        verify(auditionService).getPosts(50, 200);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param not provided (defaults to 100) "
        + "Then Return the results from 50 to 150")
    @Test
    void givenGetPostsPaginatedResponse50to100() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(50,
            Math.min(50 + 100, auditionPosts.size()));

        when(auditionService.getPosts(50, 100)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(OFFSET, "50"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(100)))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(50).getUserId())))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(149).getUserId())))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(49).getUserId()))))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(150).getUserId()))));

        verify(auditionService).getPosts(50, 100);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is 500 "
        + "Then Return the results from 50 to 550")
    @Test
    void givenGetPostsPaginatedResponse50to500() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(50,
            Math.min(50 + 500, auditionPosts.size()));

        when(auditionService.getPosts(50, 500)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(OFFSET, "50")
                .param(LIMIT, "500"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(500)))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(50).getUserId())))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(549).getUserId())))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(49).getUserId()))))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(550).getUserId()))));

        verify(auditionService).getPosts(50, 500);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 50 and Limit param is 25 "
        + "Then Return the results from 50 to 75")
    @Test
    void givenGetPostsPaginatedResponse50to75() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(50,
            Math.min(50 + 25, auditionPosts.size()));

        when(auditionService.getPosts(50, 25)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(OFFSET, "50")
                .param(LIMIT, "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(25)))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(50).getUserId())))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(74).getUserId())))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(49).getUserId()))))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(75).getUserId()))));

        verify(auditionService).getPosts(50, 25);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @DisplayName("Given Get Posts is Called"
        + "When Offset param is 250 and Limit param is 500 "
        + "Then Return the results from 250 to 750")
    @Test
    void givenGetPostsPaginatedResponse250to750() throws Exception {
        final List<AuditionPost> paginatedPostsResponse = auditionPosts.subList(250,
            Math.min(250 + 500, auditionPosts.size()));

        when(auditionService.getPosts(250, 500)).thenReturn(paginatedPostsResponse);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT)
                .param(OFFSET, "250")
                .param(LIMIT, "500"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(500)))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(250).getUserId())))
            .andExpect(jsonPath("$[*].userId", hasItem(auditionPosts.get(749).getUserId())))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(249).getUserId()))))
            .andExpect(jsonPath("$[*].userId", not(hasItem(auditionPosts.get(750).getUserId()))));

        verify(auditionService).getPosts(250, 500);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetPostByIdWhenValidIdThenReturnSinglePost() throws Exception {
        final AuditionPost auditionPost = new AuditionPost(1, 1, SAMPLE_POST, SAMPLE_BODY,
            Collections.emptyList());
        when(auditionService.getPostById("1")).thenReturn(auditionPost);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT + "/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value(SAMPLE_POST))
            .andExpect(jsonPath("$.body").value(SAMPLE_BODY));

        verify(auditionService).getPostById("1");
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetPostsByIdWhenNonExistentIdThenThrowNotFound() throws Exception {
        final String nonExistentId = "999999";
        when(auditionService.getPostById(nonExistentId)).thenThrow(
            new SystemException("Cannot find a Post with id " + nonExistentId, "Resource Not Found",
                HttpStatus.NOT_FOUND.value()));

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT + "/{id}", nonExistentId))
            .andExpect(status().isNotFound());

        verify(auditionService).getPostById(nonExistentId);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", TEST})
    void givenGetPostsByIdWhenIdIsInvalidThenThrowBadRequest(final String invalidId) throws Exception {
        mockMvc.perform(get(MOCK_POSTS_ENDPOINT + "/{id}", invalidId))
            .andExpect(status().isBadRequest());

        verify(auditionService, never()).getPostById(anyString());
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetPostsWithCommentsByIdWhenValidIdThenReturnSinglePostWithComments() throws Exception {
        final List<AuditionComment> auditionComments = IntStream.range(0, 3)
            .mapToObj(i -> new AuditionComment(1, i, "Comment " + i, TEST_EMAIL, "Comment Body " + i))
            .toList();

        final AuditionPost auditionPostWithComments = new AuditionPost(1, 1, SAMPLE_POST, SAMPLE_BODY,
            auditionComments);
        when(auditionService.getPostWithCommentsById("1")).thenReturn(auditionPostWithComments);

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT + "/1/comments"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value(SAMPLE_POST))
            .andExpect(jsonPath("$.body").value(SAMPLE_BODY))
            .andExpect(jsonPath("$.comments", hasSize(3)))
            .andExpect(jsonPath("$.comments[0].postId").value(1))
            .andExpect(jsonPath("$.comments[0].id").value(0))
            .andExpect(jsonPath("$.comments[0].name").value("Comment 0"))
            .andExpect(jsonPath("$.comments[0].email").value(TEST_EMAIL))
            .andExpect(jsonPath("$.comments[0].body").value("Comment Body 0"))
            .andExpect(jsonPath("$.comments[1].postId").value(1))
            .andExpect(jsonPath("$.comments[1].id").value(1))
            .andExpect(jsonPath("$.comments[1].name").value("Comment 1"))
            .andExpect(jsonPath("$.comments[1].email").value(TEST_EMAIL))
            .andExpect(jsonPath("$.comments[1].body").value("Comment Body 1"))
            .andExpect(jsonPath("$.comments[2].postId").value(1))
            .andExpect(jsonPath("$.comments[2].id").value(2))
            .andExpect(jsonPath("$.comments[2].name").value("Comment 2"))
            .andExpect(jsonPath("$.comments[2].email").value(TEST_EMAIL))
            .andExpect(jsonPath("$.comments[2].body").value("Comment Body 2"));

        verify(auditionService).getPostWithCommentsById("1");
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetPostsWithCommentsByIdWhenNonExistentIdThenThrowNotFound() throws Exception {
        final String nonExistentId = "999999";
        when(auditionService.getPostWithCommentsById(nonExistentId)).thenThrow(
            new SystemException("Cannot find a Post with Comments with id " + nonExistentId, "Resource Not Found",
                HttpStatus.NOT_FOUND.value()));

        mockMvc.perform(get(MOCK_POSTS_ENDPOINT + "/{id}/comments", nonExistentId))
            .andExpect(status().isNotFound());

        verify(auditionService).getPostWithCommentsById(nonExistentId);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", TEST})
    void givenGetPostsWithCommentsByIdIsInvalidThenThrowBadRequest(final String invalidId) throws Exception {
        mockMvc.perform(get(MOCK_POSTS_ENDPOINT + "/{id}/comments", invalidId))
            .andExpect(status().isBadRequest());

        verify(auditionService, never()).getPostWithCommentsById(anyString());
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetCommentsWhenValidIdThenReturnComments() throws Exception {
        final List<AuditionComment> auditionComments = IntStream.range(0, 3)
            .mapToObj(i -> new AuditionComment(1, i, "Comment " + i, TEST_EMAIL, "Comment Body " + i))
            .toList();

        when(auditionService.getCommentsByPostId("1")).thenReturn(auditionComments);

        mockMvc.perform(get("/comments?postId=1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].postId").value(1))
            .andExpect(jsonPath("$[0].id").value(0))
            .andExpect(jsonPath("$[0].name").value("Comment 0"))
            .andExpect(jsonPath("$[0].email").value(TEST_EMAIL))
            .andExpect(jsonPath("$[0].body").value("Comment Body 0"))
            .andExpect(jsonPath("$[1].postId").value(1))
            .andExpect(jsonPath("$[1].id").value(1))
            .andExpect(jsonPath("$[1].name").value("Comment 1"))
            .andExpect(jsonPath("$[1].email").value(TEST_EMAIL))
            .andExpect(jsonPath("$[1].body").value("Comment Body 1"))
            .andExpect(jsonPath("$[2].postId").value(1))
            .andExpect(jsonPath("$[2].id").value(2))
            .andExpect(jsonPath("$[2].name").value("Comment 2"))
            .andExpect(jsonPath("$[2].email").value(TEST_EMAIL))
            .andExpect(jsonPath("$[2].body").value("Comment Body 2"));

        verify(auditionService).getCommentsByPostId("1");
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void givenGetCommentsWhenNonExistentIdThenThrowNotFound() throws Exception {
        final String nonExistentId = "999999";
        when(auditionService.getCommentsByPostId(nonExistentId)).thenThrow(
            new SystemException("Cannot find a Post with id " + nonExistentId, "Resource Not Found",
                HttpStatus.NOT_FOUND.value()));

        mockMvc.perform(get("/comments?postId=" + nonExistentId))
            .andExpect(status().isNotFound());

        verify(auditionService).getCommentsByPostId(nonExistentId);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", TEST})
    void givenGetCommentsIdIsInvalidThenThrowBadRequest(final String invalidId) throws Exception {
        mockMvc.perform(get("/comments?postId=" + invalidId))
            .andExpect(status().isBadRequest());

        verify(auditionService, never()).getPostWithCommentsById(anyString());
    }
}
