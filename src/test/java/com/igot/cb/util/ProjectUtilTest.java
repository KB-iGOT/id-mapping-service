package com.igot.cb.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class ProjectUtilTest {

    @Test
    public void testCreateDefaultResponse() {
        ApiResponse response = ProjectUtil.createDefaultResponse("api.test");
        assertEquals("api.test", response.getId());
        assertEquals(Constants.API_VERSION_1, response.getVer());
        assertNotNull(response.getTs());
        assertNotNull(response.getParams().getMsgId());
        assertNotNull(response.getParams().getResMsgId());
        assertNull(response.getParams().getErr());
        assertNull(response.getParams().getErrMsg());
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