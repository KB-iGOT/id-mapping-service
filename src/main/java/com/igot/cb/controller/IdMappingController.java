package com.igot.cb.controller;

import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.igot.cb.service.IdMappingService;
import com.igot.cb.util.ApiResponse;

@RestController
public class IdMappingController {

    @Autowired
    private IdMappingService idMappingService;

    @Timed(value = "idmapping.lookup.timer")
    @GetMapping("/idmapping/lookup")
    public ResponseEntity<ApiResponse> lookup(@RequestParam String name) {
        ApiResponse response = idMappingService.getOrInsertId(name);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @Timed(value = "idmapping.bulk.lookup.timer")
    @PostMapping("/idmapping/bulk/lookup")
    public ResponseEntity<ApiResponse> bulkLookup(@RequestParam("file") MultipartFile file) {
        ApiResponse response = idMappingService.bulkGetOrInsert(file);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}