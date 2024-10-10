package com.example.demo.controller;

import com.example.api.service.ExcelProcessingService;
import com.example.api.service.OASGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private ExcelProcessingService excelProcessingService;

    @Autowired
    private OASGeneratorService oasGeneratorService;

    @PostMapping("/convert")
    public ResponseEntity<String> convertExcelToOAS(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Process Excel and extract API data
            Map<String, Object> apiDetails = excelProcessingService.readExcel(file);

            // Step 2: Generate OAS file
            String oasFilePath = oasGeneratorService.generateOASFile(apiDetails);

            return new ResponseEntity<>("OpenAPI YAML generated at: " + oasFilePath, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + e.getMessage());
        }
    }
}
