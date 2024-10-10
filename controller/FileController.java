package com.example.api.controller;

import com.example.api.service.ExcelProcessingService;
import com.example.api.service.OASGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/excel")
public class FileController {

    @Autowired
    private ExcelProcessingService excelProcessingService;

    @Autowired
    private OASGeneratorService oasGeneratorService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Parse the Excel file
            var apiInfo = excelProcessingService.parseExcelFile(file);

            // Step 2: Generate OAS (OpenAPI YAML)
            String openApiYaml = oasGeneratorService.generateOAS(apiInfo);

            // Return the generated OAS YAML
            return new ResponseEntity<>(openApiYaml, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
