package com.igot.cb.controller;


import com.igot.cb.service.IdMappingService;
import com.igot.cb.util.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdMappingControllerTest {

    @Mock
    private IdMappingService idMappingService;

    @InjectMocks
    private IdMappingController idMappingController;

    @Test
    public void testLookupSuccess() {
        String name = "Group A";
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.OK);
        response.setResult(Map.of(name, 123));

        when(idMappingService.getOrInsertId(name)).thenReturn(response);

        ResponseEntity<ApiResponse> result = idMappingController.lookup(name);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(123, result.getBody().getResult().get(name));
    }

    @Test
    public void testBulkLookup() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", "Group A\nGroup B".getBytes());
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.OK);
        response.setResult(Map.of("Group A", 1, "Group B", 2));

        when(idMappingService.bulkGetOrInsert(mockFile)).thenReturn(response);

        ResponseEntity<ApiResponse> result = idMappingController.bulkLookup(mockFile);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getResult().get("Group A"));
        assertEquals(2, result.getBody().getResult().get("Group B"));
    }
}
