package com.example.api.controller;

import com.example.api.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/generate-oas")
    public ResponseEntity<String> generateOasYaml(@RequestParam String filePath) {
        try {
            String yamlContent = apiService.generateOasYaml(filePath);
            return ResponseEntity.ok(yamlContent);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error generating YAML: " + e.getMessage());
        }
    }
}
