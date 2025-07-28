package com.igot.cb.util;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    static final String TIMESTAMP = "timestamp";
    static final String STATUS = "status";
    static final String ERROR = "error";
    static final String MESSAGE = "message";
    static final String PATH = "path";

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        TIMESTAMP, LocalDateTime.now().toString(),
                        STATUS, 500,
                        ERROR, "Database Error",
                        MESSAGE, ex.getMostSpecificCause().getMessage(),
                        PATH, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        TIMESTAMP, LocalDateTime.now().toString(),
                        STATUS, 400,
                        ERROR, "Invalid Input",
                        MESSAGE, ex.getMessage(),
                        PATH, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        TIMESTAMP, LocalDateTime.now().toString(),
                        STATUS, 500,
                        ERROR, "Internal Server Error",
                        MESSAGE, ex.getMessage(),
                        PATH, request.getRequestURI()));
    }

}
