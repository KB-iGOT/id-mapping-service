package com.igot.cb.util;

import java.util.HashMap;
import java.util.Map;

public class ProjectUtil {

    public static Map<String, Object> createDefaultMapResponse(String serviceName, boolean healthy, Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put(Constants.NAME, serviceName);
        response.put(Constants.HEALTHY, healthy);
        response.put(Constants.ERR, healthy ? "" : Constants.ERR_CODE_SERVER_ERROR);
        response.put(Constants.ERRMSG, healthy ? "" : (e != null ? e.getMessage() : "Service is unavailable"));
        return response;
    }

}