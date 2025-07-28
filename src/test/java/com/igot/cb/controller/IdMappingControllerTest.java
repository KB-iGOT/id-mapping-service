package com.igot.cb.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.igot.cb.service.IdMappingService;

class IdMappingControllerTest {
    @Mock
    private IdMappingService idMappingService;

    @InjectMocks
    private IdMappingController controller;

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void lookup_WithValidName_ReturnsOkAndMapping() {
        // Arrange
        String input = "GroupA";
        Map<String, Integer> map = Map.of(input, 123);
        when(idMappingService.getOrInsertId(input)).thenReturn(map);

        // Act
        ResponseEntity<Map<String, Integer>> resp = controller.lookup(input);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(map, resp.getBody());
        verify(idMappingService).getOrInsertId(input);
    }

    @Test
    void bulkLookup_WithFile_ReturnsOkAndListOfMappings() {
        // Arrange
        String content = "A\nB\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "names.txt", "text/plain", content.getBytes());
        List<Map<String, Integer>> expected = List.of(
                Map.of("A", 1),
                Map.of("B", 2));
        when(idMappingService.bulkGetOrInsert(file)).thenReturn(expected);

        // Act
        ResponseEntity<List<Map<String, Integer>>> resp = controller.bulkLookup(file);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(expected, resp.getBody());
        verify(idMappingService).bulkGetOrInsert(file);
    }

    @Test
    void bulkLookup_WithParamList_ReturnsOkAndListOfMappings() {
        // Arrange
        String paramList = "A,B,C";
        String paramSeparator = ",";
        List<Map<String, Integer>> expected = List.of(
                Map.of("A", 1),
                Map.of("B", 2),
                Map.of("C", 3));
        when(idMappingService.bulkGetOrInsert(paramList, paramSeparator)).thenReturn(expected);

        // Act
        ResponseEntity<List<Map<String, Integer>>> resp = controller.bulkLookup(paramList, paramSeparator);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(expected, resp.getBody());
        verify(idMappingService).bulkGetOrInsert(paramList, paramSeparator);
    }
}