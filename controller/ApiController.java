package com.example.api.controller;

import com.example.api.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ApiController {

    @Autowired
    private ApiService apiService;

    // Serve the HTML form
    @RequestMapping("/upload-form")
    public String uploadForm() {
        return "form.html";
    }

    // Handle the file upload
    @PostMapping("/upload-excel")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String yamlContent = apiService.generateOasYaml(file);
            return ResponseEntity.ok(yamlContent);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing the Excel file: " + e.getMessage());
        }
    }
}
