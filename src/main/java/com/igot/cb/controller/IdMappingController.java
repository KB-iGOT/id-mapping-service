package com.igot.cb.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IdMappingService idMappingService;

    @Timed(value = "idmapping.lookup.timer")
    @GetMapping("/idmapping/lookup")
    public ResponseEntity<Map<String, Long>> lookup(@RequestParam String name) {
        return ResponseEntity.ok(idMappingService.getOrInsertId(name));
    }

    @Timed(value = "idmapping.bulk.lookup.timer")
    @PostMapping("/idmapping/bulk/lookup")
    public ResponseEntity<List<Map<String, Long>>> bulkLookup(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(idMappingService.bulkGetOrInsert(file));
    }
}