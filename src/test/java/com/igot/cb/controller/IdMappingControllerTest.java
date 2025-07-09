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
        Map<String, Long> map = Map.of(input, 123L);
        when(idMappingService.getOrInsertId(input)).thenReturn(map);

        // Act
        ResponseEntity<Map<String, Long>> resp = controller.lookup(input);

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
        List<Map<String, Long>> expected = List.of(
                Map.of("A", 1L),
                Map.of("B", 2L));
        when(idMappingService.bulkGetOrInsert(file)).thenReturn(expected);

        // Act
        ResponseEntity<List<Map<String, Long>>> resp = controller.bulkLookup(file);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(expected, resp.getBody());
        verify(idMappingService).bulkGetOrInsert(file);
    }
}