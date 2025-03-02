package com.audition.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;

class ExceptionControllerAdviceTest {

    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    @Test
    void givenHandleSystemExceptionWhenInvalidStatusThenReturn500Error() {
        final AuditionLogger mockLogger = mock(AuditionLogger.class);
        final ExceptionControllerAdvice advice = new ExceptionControllerAdvice(mockLogger);

        final SystemException exception = new SystemException("Bad Status Code Error", "Bad Status Code", 999999);

        final ProblemDetail problemDetailResult = advice.handleSystemException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetailResult.getStatus());
        assertEquals("Bad Status Code", problemDetailResult.getTitle());
        assertEquals("Bad Status Code Error", problemDetailResult.getDetail());
        verify(mockLogger).info(any(),
            contains("Error Code from Exception could not be mapped to a valid HttpStatus Code - 999999"));
        verify(mockLogger).logStandardProblemDetail(any(), eq(problemDetailResult), eq(exception));
    }

    @Test
    void givenHandleMainExceptionWhenHttpRequestMethodNotSupportedExceptionThenMethodNotAllowedError() {
        final AuditionLogger mockLogger = mock(AuditionLogger.class);
        final ExceptionControllerAdvice advice = new ExceptionControllerAdvice(mockLogger);

        final HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException(
            "Method not allowed");

        final ProblemDetail problemDetailResult = advice.handleMainException(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), problemDetailResult.getStatus());
        assertEquals("API Error Occurred", problemDetailResult.getTitle());
        assertEquals("Request method 'Method not allowed' is not supported", problemDetailResult.getDetail());
        verify(mockLogger).logStandardProblemDetail(any(), eq(problemDetailResult), eq(exception));
    }
}
