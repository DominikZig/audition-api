package com.audition.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

class SystemExceptionTest {

    private static final String INTERNAL_SERVER_ERROR_STR = "Internal Server Error";

    @Test
    void emptySystemException() {
        final SystemException exception = new SystemException();

        assertNull(exception.getMessage());
        assertNull(exception.getStatusCode());
        assertNull(exception.getTitle());
        assertNull(exception.getDetail());
    }

    @Test
    void nullMessageSystemException() {
        final SystemException exception = new SystemException(null);

        assertNull(exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void setMessageSystemException() {
        final String message = "An error occurred";
        final SystemException exception = new SystemException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void setMessageAndCodeSystemException() {
        final String message = "An error occurred";
        final SystemException exception = new SystemException(message, 500);

        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void setMessageAndExceptionSystemException() {
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(
            HttpStatusCode.valueOf(404),
            "Not Found");
        final SystemException exception = new SystemException(httpClientErrorException.getMessage(),
            httpClientErrorException);

        assertEquals(httpClientErrorException.getMessage(), exception.getMessage());
        assertEquals("API Error Occurred", exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void setDetailedSystemException() {
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(
            HttpStatusCode.valueOf(500),
            INTERNAL_SERVER_ERROR_STR);
        final SystemException exception = new SystemException(INTERNAL_SERVER_ERROR_STR, "Server Error",
            httpClientErrorException);

        assertEquals(INTERNAL_SERVER_ERROR_STR, exception.getMessage());
        assertEquals("Server Error", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_STR, exception.getDetail());
    }
}
