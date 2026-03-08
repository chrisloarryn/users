package com.chrisloarryn.users.error;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void databaseConstraintViolationsAreSanitized() {
        ProblemDetail detail = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value violates unique constraint users_email_key"));

        assertEquals(409, detail.getStatus());
        assertEquals("Conflict", detail.getTitle());
        assertEquals(
                "The request could not be completed because it violates persisted data constraints.",
                detail.getDetail());
    }

    @Test
    void malformedPayloadErrorsAreSanitized() {
        ProblemDetail detail = handler.handleUnreadableMessage(
                new HttpMessageNotReadableException("JSON parse error", new EmptyHttpInputMessage()));

        assertEquals(400, detail.getStatus());
        assertEquals("Bad Request", detail.getTitle());
        assertEquals("Malformed request body.", detail.getDetail());
    }

    @Test
    void unexpectedErrorsDoNotLeakInternalMessages() {
        ProblemDetail detail = handler.handleUnexpected(new RuntimeException("select * from users"));

        assertEquals(500, detail.getStatus());
        assertEquals("Internal Server Error", detail.getTitle());
        assertEquals("The service could not complete the operation. Please try again later.", detail.getDetail());
    }

    private static final class EmptyHttpInputMessage implements HttpInputMessage {

        @Override
        public ByteArrayInputStream getBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public HttpHeaders getHeaders() {
            return HttpHeaders.EMPTY;
        }
    }
}
