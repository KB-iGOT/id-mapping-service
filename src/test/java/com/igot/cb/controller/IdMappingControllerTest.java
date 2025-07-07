package com.igot.cb.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.igot.cb.service.IdMappingService;
import com.igot.cb.util.ApiResponse;

@ExtendWith(MockitoExtension.class)
public class IdMappingControllerTest {

    @Mock
    private IdMappingService idMappingService;

    @InjectMocks
    private IdMappingController idMappingController;

    @Test
    public void shouldReturnSuccessResponseWhenNameIsValid() {
        // Arrange
        String name = "Group A";
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.OK);
        response.setResult(Map.of(name, 123));

        when(idMappingService.getOrInsertId(name)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse> result = idMappingController.lookup(name);

        // Assert
        assertNotNull(result, "ResponseEntity should not be null");
        assertEquals(HttpStatus.OK, result.getStatusCode(), "HTTP status code should be OK");
        assertNotNull(result.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, result.getBody().getResponseCode(), "Response code should be OK");
        assertEquals(123, result.getBody().getResult().get(name), "Result map should contain the correct value");

        // Verify
        verify(idMappingService).getOrInsertId(name);
    }

    @Test
    public void shouldReturnErrorResponseWhenNameIsInvalid() {
        // Arrange
        String name = "";
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.BAD_REQUEST);
        response.setResult(Map.of());

        when(idMappingService.getOrInsertId(name)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse> result = idMappingController.lookup(name);

        // Assert
        assertNotNull(result, "ResponseEntity should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode(), "HTTP status code should be BAD_REQUEST");
        assertNotNull(result.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, result.getBody().getResponseCode(), "Response code should be BAD_REQUEST");
        assertTrue(result.getBody().getResult().isEmpty(), "Result map should be empty");

        // Verify
        verify(idMappingService).getOrInsertId(name);
    }

    @Test
    public void shouldReturnSuccessResponseForBulkLookup() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv",
                "Group A\nGroup B".getBytes());
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.OK);
        response.setResult(Map.of("Group A", 1, "Group B", 2));

        when(idMappingService.bulkGetOrInsert(mockFile)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse> result = idMappingController.bulkLookup(mockFile);

        // Assert
        assertNotNull(result, "ResponseEntity should not be null");
        assertEquals(HttpStatus.OK, result.getStatusCode(), "HTTP status code should be OK");
        assertNotNull(result.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.OK, result.getBody().getResponseCode(), "Response code should be OK");
        assertEquals(1, result.getBody().getResult().get("Group A"),
                "Result map should contain the correct value for Group A");
        assertEquals(2, result.getBody().getResult().get("Group B"),
                "Result map should contain the correct value for Group B");

        // Verify
        verify(idMappingService).bulkGetOrInsert(mockFile);
    }

    @Test
    public void shouldReturnErrorResponseForBulkLookupWithInvalidFile() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("file", "invalid.csv", "text/csv", "".getBytes());
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.BAD_REQUEST);
        response.setResult(Map.of());

        when(idMappingService.bulkGetOrInsert(mockFile)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse> result = idMappingController.bulkLookup(mockFile);

        // Assert
        assertNotNull(result, "ResponseEntity should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode(), "HTTP status code should be BAD_REQUEST");
        assertNotNull(result.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, result.getBody().getResponseCode(), "Response code should be BAD_REQUEST");
        assertTrue(result.getBody().getResult().isEmpty(), "Result map should be empty");

        // Verify
        verify(idMappingService).bulkGetOrInsert(mockFile);
    }

    @Test
    public void testLookup_EmptyValue_ReturnsBadRequest() {
        // Do not mock service call because it should not be reached
        ResponseEntity<ApiResponse> result = idMappingController.lookup("");

        assertEquals(HttpStatus.BAD_REQUEST, result.getBody().getResponseCode(), "Response code should be BAD_REQUEST");
        assertTrue(result.getBody().getParams().getErrMsg().contains("Invalid or empty name"));
    }
}