package com.igot.cb.service.impl;

import com.igot.cb.model.SBApiResponse;
import com.igot.cb.service.HealthCheckService;
import com.igot.cb.util.Constants;
import com.igot.cb.util.ProjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public HealthCheckServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SBApiResponse getHealthCheck() {
        SBApiResponse response = new SBApiResponse("api.health");
        List<Map<String, Object>> checks = new ArrayList<>();

        // Check PostgreSQL
        Map<String, Object> dbHealthCheck = checkDatabaseHealth();

        // ✅ Remove exception before adding to checks — prevents leaking into response
        Exception dbException = (Exception) dbHealthCheck.remove(Constants.EXCEPTION);
        checks.add(dbHealthCheck);  // ← clean map, no exception field

        boolean dbHealthy = (Boolean) dbHealthCheck.get(Constants.HEALTHY);

        // ✅ Service level check — pass extracted exception
        checks.add(ProjectUtil.createDefaultMapResponse(
                "id-mapping-service",
                dbHealthy,
                dbHealthy ? null : dbException  // ← use extracted exception
        ));

        // ✅ Always SUCCESS — health API itself is running
        response.getParams().setStatus(Constants.SUCCESS);
        response.getParams().setErr(null);
        response.getParams().setErrmsg(null);
        response.setResponseCode(HttpStatus.OK);

        Map<String, Object> responseObj = new HashMap<>();
        responseObj.put(Constants.HEALTHY, true);                    // ← always true
        responseObj.put(Constants.CHECKS, checks);
        responseObj.put(Constants.NAME, Constants.HEALTH_CHECK_NAME); // ← fixed constant
        response.put(Constants.RESPONSE, responseObj);               // ← use constant

        return response;
    }

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> result = new HashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            logger.debug("Database health check: SUCCESS");
            result.put(Constants.EXCEPTION, null);              // ← null on success
            result.putAll(ProjectUtil.createDefaultMapResponse(
                    "PostgreSQL",
                    true,
                    null                                        // ← no exception
            ));
        } catch (Exception e) {
            logger.error("Database health check: FAILED - {}", e.getMessage());
            result.put(Constants.EXCEPTION, e);                 // ← store exception
            result.putAll(ProjectUtil.createDefaultMapResponse(
                    "PostgreSQL",
                    false,
                    e                                           // ← pass exception
            ));
        }
        return result;
    }
}