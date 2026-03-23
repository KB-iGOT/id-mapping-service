package com.igot.cb.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.igot.cb.model.SBApiResponse;
import com.igot.cb.service.HealthCheckService;
import com.igot.cb.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class HealthControllerTest {

    @Mock
    private HealthCheckService healthCheckService;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void health_ReturnsOkAndSBApiResponse() {
        SBApiResponse mockResponse = new SBApiResponse("api.health");
        mockResponse.setResponseCode(HttpStatus.OK);
        mockResponse.put(Constants.HEALTHY, true);

        when(healthCheckService.getHealthCheck()).thenReturn(mockResponse);

        ResponseEntity<SBApiResponse> response = healthController.health();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("api.health", response.getBody().getId());
        assertEquals(HttpStatus.OK, response.getBody().getResponseCode());
    }
}