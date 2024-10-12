package com.example.api.controller;

import com.example.api.service.ExcelProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("/api/excel")
public class FileController {

    @Autowired
    private ExcelProcessingService excelProcessingService;

    // Endpoint to show the file upload form
    @GetMapping("/upload")
    public String showUploadForm() {
        return "form";  // Render the form.html page for file upload
    }

    // Endpoint to handle the Excel file upload and process it
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "error";  // Render the error.html page if no file is provided
        }

        try {
            // Parse the Excel file and get the list of rows with their cell values
            List<ApiDefinition> apiInfo = excelProcessingService.parseExcelFile(file);

            // Debugging the parsed API info
            System.out.println("Parsed API Info: " + apiInfo.size());

            if (apiInfo.isEmpty()) {
                model.addAttribute("message", "No API data parsed from the file.");
                return "error";  // Render the error.html page if no data is found
            } else {
                for (ApiDefinition def : apiInfo) {
                    System.out.println("API URN: " + def.getApiUrnNumber());
                    for (ApiDefinition.QueryParameter qp : def.getQueryParameters()) {
                        System.out.println("Query Param: " + qp.getParameterElementName());
                    }
                    for (ApiDefinition.ResponsePayload rp : def.getResponsePayloads()) {
                        System.out.println("Response Payload: " + rp.getResponseElementName());
                    }
                }
            }

            // Convert the list to YAML using SnakeYAML
            Yaml yaml = new Yaml();
