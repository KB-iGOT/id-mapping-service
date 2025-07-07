package com.igot.cb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.web.multipart.MultipartFile;

import com.igot.cb.util.ApiResponse;
import com.igot.cb.util.Constants;
import com.igot.cb.util.ProjectUtil;
import com.igot.cb.util.PropertiesCache;

@ExtendWith(MockitoExtension.class)
class IdMappingServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PropertiesCache propertiesCache;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private IdMappingService idMappingService;

    private ApiResponse mockResponse;
    private Map<String, Object> mockResult;

    @BeforeEach
    void setUp() throws Exception {
        // Clear the cache before each test
        Field cacheField = IdMappingService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        ConcurrentHashMap<String, Integer> cache = (ConcurrentHashMap<String, Integer>) cacheField
                .get(idMappingService);
        cache.clear();

        // Setup mock response
        mockResult = new HashMap<>();
        mockResponse = new ApiResponse();
        mockResponse.setResult(mockResult);
    }

    @Test
    void testGetOrInsertId_Success_NewEntry() {
        // Arrange
        String testName = "testName";
        int expectedId = 123;
        String mockQuery = "SELECT id FROM table WHERE name = ?";

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);
            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class)))
                    .thenReturn(Arrays.asList(expectedId));

            // Act
            ApiResponse result = idMappingService.getOrInsertId(testName);

            // Assert
            assertNotNull(result);
            assertEquals(expectedId, result.getResult().get(testName));
            verify(jdbcTemplate, times(1)).query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class));
        }
    }

    @Test
    void testGetOrInsertIdWithEmptyName() {
        // Arrange
        String name = "";

        // Act
        ApiResponse response = idMappingService.getOrInsertId(name);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode(), "HTTP status should be BAD_REQUEST");
        assertEquals("Name cannot be empty.", response.getParams().getErrMsg(), "Error message should match");
    }

    @Test
    void testGetOrInsertId_Success_FromCache() {
        // Arrange
        String testName = "cachedName";
        int expectedId = 456;

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY))
                    .thenReturn("SELECT id FROM table WHERE name = ?");
            when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(SingleColumnRowMapper.class)))
                    .thenReturn(Arrays.asList(expectedId));

            // First call to populate cache
            idMappingService.getOrInsertId(testName);

            // Act - Second call should use cache
            ApiResponse result = idMappingService.getOrInsertId(testName);

            // Assert
            assertNotNull(result);
            assertEquals(expectedId, result.getResult().get(testName));
            // Verify database is called only once (first time)
            verify(jdbcTemplate, times(1)).query(anyString(), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class));
        }
    }

    @Test
    void testGetOrInsertId_DatabaseError() {
        // Arrange
        String testName = "errorName";
        String mockQuery = "SELECT id FROM table WHERE name = ?";

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);
            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> idMappingService.getOrInsertId(testName));
        }
    }

    @Test
    void testGetOrInsertId_EmptyResult() {
        // Arrange
        String testName = "nonExistentName";
        String mockQuery = "SELECT id FROM table WHERE name = ?";

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);
            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class)))
                    .thenReturn(Arrays.asList()); // Empty list

            // Act
            ApiResponse result = idMappingService.getOrInsertId(testName);

            // Assert
            assertNotNull(result);
            projectUtilMock.verify(() -> ProjectUtil.setErrorDetails(mockResponse, "Failed to perform lookup.",
                    HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Test
    void testBulkGetOrInsert_Success() throws IOException {
        // Arrange
        String fileContent = "name1\nname2\nname3\n";
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        String mockQuery = "SELECT id FROM table WHERE name = ?";

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_BULK_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);
            when(multipartFile.getInputStream()).thenReturn(inputStream);

            // Mock database responses for each name
            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class)))
                    .thenReturn(Arrays.asList(1))
                    .thenReturn(Arrays.asList(2))
                    .thenReturn(Arrays.asList(3));

            // Act
            ApiResponse result = idMappingService.bulkGetOrInsert(multipartFile);

            // Assert
            assertNotNull(result);
            verify(jdbcTemplate, times(3)).query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class));
        }
    }

    @Test
    void testBulkGetOrInsert_WithEmptyLines() throws IOException {
        // Arrange
        String fileContent = "name1\n\nname2\n   \nname3\n";
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        String mockQuery = "SELECT id FROM table WHERE name = ?";

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_BULK_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);
            when(multipartFile.getInputStream()).thenReturn(inputStream);

            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class)))
                    .thenReturn(Arrays.asList(1))
                    .thenReturn(Arrays.asList(2))
                    .thenReturn(Arrays.asList(3));

            // Act
            ApiResponse result = idMappingService.bulkGetOrInsert(multipartFile);

            // Assert
            assertNotNull(result);
            // Should only call database 3 times (empty lines filtered out)
            verify(jdbcTemplate, times(3)).query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class));
        }
    }

    @Test
    void testBulkGetOrInsert_IOException() throws IOException {
        // Arrange
        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class)) {
            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_BULK_LOOKUP))
                    .thenReturn(mockResponse);
            when(multipartFile.getInputStream()).thenThrow(new IOException("File read error"));

            // Act
            ApiResponse result = idMappingService.bulkGetOrInsert(multipartFile);

            // Assert
            assertNotNull(result);
            projectUtilMock.verify(() -> ProjectUtil.setErrorDetails(eq(mockResponse),
                    contains("Failed to perform bulk lookup. Exception: File read error"),
                    eq(HttpStatus.INTERNAL_SERVER_ERROR)));
        }
    }

    @Test
    void testBulkGetOrInsert_DatabaseError() throws IOException {
        // Arrange
        String fileContent = "name1\n";
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
        String mockQuery = "SELECT id FROM table WHERE name = ?";

        try (MockedStatic<ProjectUtil> projectUtilMock = mockStatic(ProjectUtil.class);
                MockedStatic<PropertiesCache> propertiesCacheMock = mockStatic(PropertiesCache.class)) {

            projectUtilMock.when(() -> ProjectUtil.createDefaultResponse(Constants.API_IDMAP_BULK_LOOKUP))
                    .thenReturn(mockResponse);
            propertiesCacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);
            when(multipartFile.getInputStream()).thenReturn(inputStream);
            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class),
                    any(SingleColumnRowMapper.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act
            ApiResponse result = idMappingService.bulkGetOrInsert(multipartFile);

            // Assert
            assertNotNull(result);
            projectUtilMock.verify(() -> ProjectUtil.setErrorDetails(eq(mockResponse),
                    contains("Failed to perform bulk lookup. Exception: Database error"),
                    eq(HttpStatus.INTERNAL_SERVER_ERROR)));
        }
    }

    @Test
    void testFetchOrInsertFromDb_PreparedStatementSetter() throws SQLException {
        String testName = "testName";
        String mockQuery = "SELECT id FROM table WHERE name = ? OR name = ?";

        try (MockedStatic<PropertiesCache> cacheMock = mockStatic(PropertiesCache.class)) {
            cacheMock.when(PropertiesCache::getInstance).thenReturn(propertiesCache);
            when(propertiesCache.getProperty(Constants.IP_MAP_LOOKUP_QUERY)).thenReturn(mockQuery);

            when(jdbcTemplate.query(eq(mockQuery), any(PreparedStatementSetter.class), any(SingleColumnRowMapper.class)))
                .thenAnswer(invocation -> {
                    PreparedStatementSetter setter = invocation.getArgument(1);
                    PreparedStatement psMock = mock(PreparedStatement.class);
                    setter.setValues(psMock);
                    // Verify that the setter sets both parameters correctly
                    verify(psMock).setString(1, testName);
                    verify(psMock).setString(2, testName);
                    return Collections.singletonList(7);
                });

            ApiResponse response = idMappingService.getOrInsertId(testName);
            assertNotNull(response);
            assertEquals(7, response.getResult().get(testName));
        }
    }
}