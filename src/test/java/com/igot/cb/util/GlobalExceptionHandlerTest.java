package com.igot.cb.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;
    private HttpServletRequest req;

    @BeforeEach
    void init() {
        handler = new GlobalExceptionHandler();
        req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/foo/bar");
    }

    @Test
    void handleDataAccessException_Returns500() {
        var ex = new DataAccessResourceFailureException("db down");
        ResponseEntity<Map<String, Object>> resp = handler.handleDataAccessException(ex, req);

        assertEquals(500, resp.getStatusCode().value());
        Map<String, Object> body = assertDoesNotThrow(() -> {
            Map<String, Object> b = resp.getBody();
            assertNotNull(b, "Response body should not be null");
            return b;
        });
        assertEquals(500, body.get("status"));
        assertEquals("Database Error", body.get("error"));
        assertEquals("db down", body.get("message"));
        assertEquals("/foo/bar", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void handleIllegalArgumentException_Returns400() {
        var ex = new IllegalArgumentException("bad name");
        ResponseEntity<Map<String, Object>> resp = handler.handleIllegalArgumentException(ex, req);

        assertEquals(400, resp.getStatusCode().value());
        Map<String, Object> body = assertDoesNotThrow(() -> {
            Map<String, Object> b = resp.getBody();
            assertNotNull(b, "Response body should not be null");
            return b;
        });
        assertEquals(400, body.get("status"));
        assertEquals("Invalid Input", body.get("error"));
        assertEquals("bad name", body.get("message"));
        assertEquals("/foo/bar", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void handleGenericException_Returns500() {
        var ex = new RuntimeException("oops");
        ResponseEntity<Map<String, Object>> resp = handler.handleGenericException(ex, req);

        assertEquals(500, resp.getStatusCode().value());
        Map<String, Object> body = assertDoesNotThrow(() -> {
            Map<String, Object> b = resp.getBody();
            assertNotNull(b, "Response body should not be null");
            return b;
        });
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("oops", body.get("message"));
        assertEquals("/foo/bar", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }
}
