package com.igot.cb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.igot.cb.model.SBApiResponse;
import com.igot.cb.service.impl.HealthCheckServiceImpl;
import com.igot.cb.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HealthCheckServiceImpl healthCheckService;

    @BeforeEach
    void setup() {
        // Mocks are automatically initialized by @ExtendWith(MockitoExtension.class)
    }

    @Test
    void getHealthCheck_WhenDatabaseIsHealthy_ReturnsHealthyResponse() {
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);

        SBApiResponse response = healthCheckService.getHealthCheck();

        verify(jdbcTemplate).queryForObject(eq("SELECT 1"), eq(Integer.class));
        assertNotNull(response);
        assertEquals("api.health", response.getId());
        assertEquals("v1", response.getVer());
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertNotNull(response.getParams());
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());

        // Check response object
        @SuppressWarnings("unchecked")
        Map<String, Object> responseObj = (Map<String, Object>) response.get(Constants.RESPONSE);
        assertNotNull(responseObj);
        assertTrue((Boolean) responseObj.get(Constants.HEALTHY));
        assertEquals(Constants.HEALTH_CHECK_NAME, responseObj.get(Constants.NAME));
    }

    @Test
    void getHealthCheck_WhenDatabaseIsHealthy_ContainsBothChecks() {
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);

        SBApiResponse response = healthCheckService.getHealthCheck();

        verify(jdbcTemplate).queryForObject(eq("SELECT 1"), eq(Integer.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> responseObj = (Map<String, Object>) response.get(Constants.RESPONSE);
        assertNotNull(responseObj.get(Constants.CHECKS));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) responseObj.get(Constants.CHECKS);
        assertEquals(2, checks.size());

        Map<String, Object> dbCheck = checks.get(0);
        assertEquals("PostgreSQL", dbCheck.get(Constants.NAME));
        assertTrue((Boolean) dbCheck.get(Constants.HEALTHY));
        assertEquals("", dbCheck.get(Constants.ERR));
        assertEquals("", dbCheck.get(Constants.ERRMSG));

        Map<String, Object> serviceCheck = checks.get(1);
        assertEquals("id-mapping-service", serviceCheck.get(Constants.NAME));
        assertTrue((Boolean) serviceCheck.get(Constants.HEALTHY));
        assertEquals("", serviceCheck.get(Constants.ERR));
        assertEquals("", serviceCheck.get(Constants.ERRMSG));
    }

    @Test
    void getHealthCheck_WhenDatabaseIsUnhealthy_ReturnsUnhealthyChecks() {
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        SBApiResponse response = healthCheckService.getHealthCheck();

        verify(jdbcTemplate).queryForObject(eq("SELECT 1"), eq(Integer.class));
        assertNotNull(response);
        assertEquals("api.health", response.getId());
        assertEquals("v1", response.getVer());
        assertEquals(HttpStatus.OK, response.getResponseCode()); // Always OK as health API is running
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseObj = (Map<String, Object>) response.get(Constants.RESPONSE);
        assertTrue((Boolean) responseObj.get(Constants.HEALTHY));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) responseObj.get(Constants.CHECKS);
        assertEquals(2, checks.size());

        Map<String, Object> dbCheck = checks.get(0);
        assertEquals("PostgreSQL", dbCheck.get(Constants.NAME));
        assertFalse((Boolean) dbCheck.get(Constants.HEALTHY));
        assertEquals(Constants.ERR_CODE_SERVER_ERROR, dbCheck.get(Constants.ERR));
        assertTrue(((String) dbCheck.get(Constants.ERRMSG)).contains("Connection refused"));

        Map<String, Object> serviceCheck = checks.get(1);
        assertEquals("id-mapping-service", serviceCheck.get(Constants.NAME));
        assertFalse((Boolean) serviceCheck.get(Constants.HEALTHY));
    }
}