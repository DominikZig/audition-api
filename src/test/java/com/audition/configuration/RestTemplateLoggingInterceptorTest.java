package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class RestTemplateLoggingInterceptorTest {

    @SuppressWarnings("PMD.CloseResource") //mock object
    @Test
    void givenInterceptWhenRequestIsMadeThenLogRequestIsCalled() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final RestTemplateLoggingInterceptor interceptor = new RestTemplateLoggingInterceptor(objectMapper);

        final HttpRequest request = mock(HttpRequest.class);
        final byte[] body = "test body".getBytes();
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        final ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);

        when(execution.execute(request, body)).thenReturn(mockResponse);
        when(mockResponse.getBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

        final ClientHttpResponse response = interceptor.intercept(request, body, execution);

        verify(execution).execute(request, body);
        assertEquals(mockResponse, response);
    }
}
