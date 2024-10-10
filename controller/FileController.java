package com.example.api.controller;

import com.example.api.service.ExcelProcessingService;
import com.example.api.service.OASGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/api/excel")
public class FileController {

    @Autowired
    private ExcelProcessingService excelProcessingService;

    @Autowired
    private OASGeneratorService oasGeneratorService;

    // Endpoint to serve the HTML upload form
    @GetMapping("/upload")
    public String uploadForm(Model model) {
        return "upload"; // This will return the upload.html template
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Parse the Excel file
            Map<String, Object> apiInfo = excelProcessingService.parseExcelFile(file);

            // Step 2: Generate OAS (OpenAPI YAML)
            String openApiYaml = oasGeneratorService.generateOAS(apiInfo);

            // Return the generated OAS YAML
            return new ResponseEntity<>(openApiYaml, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}




<!DOCTYPE html>
<html>
<head>
    <title>Upload Excel File</title>
</head>
<body>
    <h1>Upload Excel to Convert to OAS</h1>
    <form method="POST" action="http://localhost:8080/api/excel/upload" enctype="multipart/form-data">
        <label>Select Excel file:</label><br><br>
        <input type="file" name="file" required><br><br>
        <button type="submit">Upload and Convert</button>
    </form>
</body>
</html>
