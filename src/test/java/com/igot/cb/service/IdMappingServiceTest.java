package com.igot.cb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.igot.cb.dao.BitPositionDao;

class IdMappingServiceTest {
    @Mock
    private BitPositionDao bitPositionDao;

    @InjectMocks
    private IdMappingService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrInsertId_ValidName_ReturnsMap() {
        when(bitPositionDao.getOrInsert("foo")).thenReturn(7);
        Map<String, Integer> result = service.getOrInsertId("foo");

        assertEquals(1, result.size());
        assertEquals(7, result.get("foo"));
    }

    @Test
    void getOrInsertId_EmptyName_Throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getOrInsertId(""),
                "Name must not be null or empty");
    }

    @Test
    void bulkGetOrInsert_FromList_TrimsAndFilters() {
        // prepare three names, one blank, one padded
        List<String> names = Arrays.asList("A", " B ", "", "C");
        when(bitPositionDao.getOrInsert("a")).thenReturn(1);
        when(bitPositionDao.getOrInsert("b")).thenReturn(2);
        when(bitPositionDao.getOrInsert("c")).thenReturn(3);

        List<Map<String, Integer>> results = service.bulkGetOrInsert(names);
        assertEquals(3, results.size());
        assertEquals(1, results.get(0).get("A"));
        assertEquals(2, results.get(1).get("B"));
        assertEquals(3, results.get(2).get("C"));
    }

    @Test
    void bulkGetOrInsert_FromFile_Success() {
        String csv = "X\nY\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "list.csv", "text/plain", csv.getBytes(StandardCharsets.UTF_8));
        when(bitPositionDao.getOrInsert("x")).thenReturn(10);
        when(bitPositionDao.getOrInsert("y")).thenReturn(20);

        List<Map<String, Integer>> out = service.bulkGetOrInsert(file);
        assertEquals(2, out.size());
        assertEquals(10, out.get(0).get("X"));
        assertEquals(20, out.get(1).get("Y"));
    }

    @Test
    void bulkGetOrInsert_FileReadError_Throws() throws Exception {
        MultipartFile badFile = mock(MultipartFile.class);
        // simulate exception on getInputStream
        when(badFile.getInputStream()).thenThrow(new IOException("stream failed"));

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.bulkGetOrInsert(badFile),
                "Should wrap any IO error in IllegalStateException");
        assertTrue(ex.getMessage().contains("Failed to process given file"),
                "Exception text should come from your service catch block");
    }

    @Test
    void bulkGetOrInsert_EmptyFile_ReturnsEmptyList() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.csv", "text/plain", new byte[0]);
        List<Map<String, Integer>> result = service.bulkGetOrInsert(empty);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Empty file → empty result list");
    }

    @Test
    void bulkGetOrInsert_OnlyBlankLines_ReturnsEmptyList() {
        // file of blank/whitespace-only lines
        String content = "   \n\t\n   \n";
        MockMultipartFile blanks = new MockMultipartFile(
                "file", "blanks.csv", "text/plain",
                content.getBytes(StandardCharsets.UTF_8));

        List<Map<String, Integer>> result = service.bulkGetOrInsert(blanks);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Whitespace-only lines should be filtered out");
        verifyNoInteractions(bitPositionDao);
    }

    @Test
    void blulkGetOrInsert_ParamList_Success() {
        String paramList = "A,B,C";
        String separator = ",";

        when(bitPositionDao.getOrInsert("a")).thenReturn(1);
        when(bitPositionDao.getOrInsert("b")).thenReturn(2);
        when(bitPositionDao.getOrInsert("c")).thenReturn(3);

        List<Map<String, Integer>> result = service.bulkGetOrInsert(paramList, separator);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).get("A"));
        assertEquals(2, result.get(1).get("B"));
        assertEquals(3, result.get(2).get("C"));
    }

    @Test
    void bulkGetOrInsert_EmptyParamList_Throws() {
        String emptyList = "";
        String separator = ",";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.bulkGetOrInsert(emptyList, separator),
                "Should throw if paramList is empty");

        assertEquals("Parameter list must not be null or empty", ex.getMessage());
    }
}