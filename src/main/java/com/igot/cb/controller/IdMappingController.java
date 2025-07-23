package com.igot.cb.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.igot.cb.service.IdMappingService;

import io.micrometer.core.annotation.Timed;

@RestController
public class IdMappingController {
    private final IdMappingService idMappingService;

    public IdMappingController(IdMappingService idMappingService) {
        this.idMappingService = idMappingService;
    }

    @Timed(value = "idmapping.lookup.timer")
    @GetMapping("/idmapping/v1/lookup")
    public ResponseEntity<Map<String, Integer>> lookup(@RequestParam String name) {
        return ResponseEntity.ok(idMappingService.getOrInsertId(name));
    }

    @Timed(value = "idmapping.bulk.lookup.timer")
    @PostMapping("/idmapping/v1/bulk/lookup")
    public ResponseEntity<List<Map<String, Integer>>> bulkLookup(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(idMappingService.bulkGetOrInsert(file));
    }

    @Timed(value = "idmapping.bulk.lookup.timer")
    @GetMapping("/idmapping/v1/list/lookup")
    public ResponseEntity<List<Map<String, Integer>>> bulkLookup(@RequestParam String paramList, @RequestParam String paramSeparator) {
        return ResponseEntity.ok(idMappingService.bulkGetOrInsert(paramList, paramSeparator));
    }
}