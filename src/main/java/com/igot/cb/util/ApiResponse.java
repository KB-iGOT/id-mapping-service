package com.igot.cb.util;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ApiResponse {
    private String id;
    private String ver;
    private String ts;
    private ApiRespParam params;
    private HttpStatus responseCode;

    private transient Map<String, Object> response = new HashMap<>();

    public ApiResponse() {
        this.ver = "v1";
        this.ts = new Timestamp(System.currentTimeMillis()).toString();
        this.params = new ApiRespParam(UUID.randomUUID().toString());
    }

    public ApiResponse(String id) {
        this();
        this.id = id;
    }

    public Map<String, Object> getResult() {
        return response;
    }

    public void setResult(Map<String, Object> result) {
        response = result;
    }
}
