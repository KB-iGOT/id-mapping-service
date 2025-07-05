package com.igot.cb.util;

import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ProjectUtil {
    public static ApiResponse createDefaultResponse(String api) {
        ApiResponse response = new ApiResponse();
        response.setId(api);
        response.setVer(Constants.API_VERSION_1);
        response.setParams(new ApiRespParam(UUID.randomUUID().toString()));
        response.getParams().setStatus(Constants.SUCCESS);
        response.setResponseCode(HttpStatus.OK);
        response.setTs(Instant.now().toString());
        return response;
    }

    public static void setErrorDetails(ApiResponse response, String errMsg) {
        response.getParams().setErrMsg(errMsg);
        response.getParams().setStatus(Constants.FAILED);
        response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}