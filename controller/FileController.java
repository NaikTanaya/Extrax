package com.example.api.controller;

import com.example.api.model.ApiDefinition;
import com.example.api.service.ExcelProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/api/excel")
public class FileController {

    @Autowired
    private ExcelProcessingService excelProcessingService;

    // Endpoint to serve the HTML form
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "upload"; // This corresponds to upload.html in the templates directory
    }

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





<!DOCTYPE html>
<html>
<head>
    <title>Upload Excel File</title>
</head>
<body>
    <h1>Upload Excel to Convert to OAS</h1>
    <form method="POST" action="/api/excel/upload" enctype="multipart/form-data">
        <label>Select Excel file:</label><br><br>
        <input type="file" name="file" required><br><br>
        <button type="submit">Upload and Convert</button>
    </form>

    <h1>Upload Excel and Convert to YAML</h1>
    <form method="POST" action="/api/excel/upload/yaml" enctype="multipart/form-data">
        <label>Select Excel file:</label><br><br>
        <input type="file" name="file" required><br><br>
        <button type="submit">Upload and Convert to YAML</button>
    </form>
</body>
</html>
