package com.igot.cb.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.igot.cb.dao.BitPositionDao;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing ID mappings, including looking up and inserting IDs
 * based on names.
 * It uses a cache to avoid redundant database lookups.
 */
@Service
@Slf4j
public class IdMappingService {
    private final BitPositionDao bitPositionDao;

    public IdMappingService(BitPositionDao bitPositionDao) {
        this.bitPositionDao = bitPositionDao;
    }

    /**
     * Cache to store name-to-ID mappings to reduce database lookups.
     * Uses a ConcurrentHashMap for thread-safe operations.
     */
    private final Map<String, Long> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves the ID for a given name, inserting it into the database if it does
     * not exist.
     *
     * @param name The name to look up or insert.
     * @return BitPositionResponse containing the name and its corresponding ID.
     */
    public Map<String, Long> getOrInsertId(String name) {
        if (StringUtils.hasText(name)) {
            return Map.of(name, cache.computeIfAbsent(name.toLowerCase(), this::fetchOrInsertFromDb));
        } else {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
    }

    /**
     * Bulk get or insert method that accepts a file containing names.
     *
     * @param file MultipartFile containing names, one per line.
     * @return List of BitPositionResponse containing the name and its corresponding
     *         ID.
     */
    public List<Map<String, Long>> bulkGetOrInsert(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> names = reader.lines().map(String::trim).filter(s -> !s.isEmpty())
                    .toList();
            return bulkGetOrInsert(names);
        } catch (Exception e) {
            log.error("Failed to perform bulk lookup. Exception: ", e);
            throw new IllegalStateException("Failed to process given file.");
        }
    }

    /**
     * Bulk get or insert method that accepts a list of names.
     *
     * @param names List of names to look up or insert.
     * @return List of BitPositionResponse containing the name and its corresponding
     *         ID.
     */
    public List<Map<String, Long>> bulkGetOrInsert(List<String> names) {
        return names.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(name -> Map.of(name, cache.computeIfAbsent(name.toLowerCase(), this::fetchOrInsertFromDb)))
                .toList();
    }

    /**
     * Fetches the ID from the database or inserts a new record if it does not
     * exist.
     *
     * @param name The name to look up or insert.
     * @return The ID associated with the name.
     */
    private Long fetchOrInsertFromDb(String name) {
        return bitPositionDao.getOrInsert(name);
    }
}