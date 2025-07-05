package com.igot.cb.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.igot.cb.util.ApiResponse;
import com.igot.cb.util.Constants;
import com.igot.cb.util.ProjectUtil;
import com.igot.cb.util.PropertiesCache;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IdMappingService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    public ApiResponse getOrInsertId(String name) {
        ApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_IDMAP_LOOKUP);
        int result = cache.computeIfAbsent(name, this::fetchOrInsertFromDb);
        if (result < 0) {
            ProjectUtil.setErrorDetails(response, "Failed to perform lookup.");
        } else {
            response.getResult().put(name, result);
        }
        return response;
    }

    @PostMapping
    public ApiResponse bulkGetOrInsert(MultipartFile file) {
        ApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_IDMAP_BULK_LOOKUP);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> names = reader.lines().map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            Map<String, Integer> result = new LinkedHashMap<>();
            for (String name : names) {
                result.put(name, fetchOrInsertFromDb(name));
            }
        } catch (Exception e) {
            log.error("Failed to perform bulk lookup. Exception: ", e);
            ProjectUtil.setErrorDetails(response, "Failed to perform bulk lookup. Exception: " + e.getMessage());
        }
        return response;
    }

    private int fetchOrInsertFromDb(String name) {
        String sql = PropertiesCache.getInstance().getProperty(Constants.IP_MAP_LOOKUP_QUERY);
        try {
            List<Integer> results = jdbcTemplate.query(
                    sql,
                    new PreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps) throws SQLException {
                            ps.setString(1, name);
                            ps.setString(2, name);
                        }
                    },
                    new SingleColumnRowMapper<>(Integer.class));

            return results.isEmpty() ? -1 : results.get(0);
        } catch (Exception e) {
            log.error("Database error for name: {}", name, e);
            throw e;
        }
    }
}