package com.audition.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final transient ObjectMapper objectMapper;

    public RestTemplateLoggingInterceptor(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
        final ClientHttpRequestExecution execution)
        throws IOException {

        logRequest(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);

        return response;
    }

    private void logRequest(final HttpRequest request, final byte[] body) {
        //only log request in debug mode due to performance from loading the entire request body into memory
        if (log.isDebugEnabled()) {
            log.debug("Request URI: {}", request.getURI());
            log.debug("Request Method: {}", request.getMethod());
            log.debug("Request Headers: {}", request.getHeaders());
            log.debug("Request Body: {}", new String(body, StandardCharsets.UTF_8));
        }
    }

    private void logResponse(final ClientHttpResponse response) throws IOException {
        //only log response in debug mode due to performance from loading the entire response body into memory
        if (log.isDebugEnabled()) {

            log.debug("Response Status Code: {}", response.getStatusCode());
            log.debug("Response Headers: {}", response.getHeaders());

            final String responseBodyAsString = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            log.debug("Response body: {}", objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readValue(responseBodyAsString, Object.class)));
        }
    }
}
