package com.example.api.controller;

import com.example.api.model.ApiDefinition;
import com.example.api.service.ExcelProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class FileController {

    @Autowired
    private ExcelProcessingService excelProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<List<ApiDefinition>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<ApiDefinition> apiDefinitions = excelProcessingService.parseExcelFile(file);
            return ResponseEntity.ok(apiDefinitions);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/upload/yaml")
    public ResponseEntity<String> uploadExcelAndConvertToYaml(@RequestParam("file") MultipartFile file) {
        try {
            List<ApiDefinition> apiDefinitions = excelProcessingService.parseExcelFile(file);
            String yamlOutput = excelProcessingService.convertToYaml(apiDefinitions);
            return ResponseEntity.ok(yamlOutput);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process file: " + e.getMessage());
        }
    }
}
