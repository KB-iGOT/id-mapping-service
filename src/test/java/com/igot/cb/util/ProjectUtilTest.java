package com.igot.cb.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class ProjectUtilTest {

    @Test
    public void testCreateDefaultResponse() {
        ApiResponse response = ProjectUtil.createDefaultResponse("api.test");
        assertEquals("api.test", response.getId());
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
    }

    @Test
    public void testSetErrorDetails() {
        ApiResponse response = ProjectUtil.createDefaultResponse("api.test");
        ProjectUtil.setErrorDetails(response, "error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals("error occurred", response.getParams().getErrMsg());
    }
}