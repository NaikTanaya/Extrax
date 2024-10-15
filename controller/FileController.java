package com.example.api.controller;

import com.example.api.model.ApiDefinition;
import com.example.api.service.ExcelProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            // Call the method in the ExcelProcessingService that handles the Excel processing
            List<ApiDefinition> apiInfo = excelProcessingService.parseExcelFile(file);

            // Debugging the parsed API info
            System.out.println("Parsed API Info: " + apiInfo.size());

            if (apiInfo.isEmpty()) {
                model.addAttribute("message", "No API data parsed from the file.");
                return "error";  // Render the error.html page if no data is found
            } else {
                // Iterate through the parsed API info for debugging
                for (ApiDefinition def : apiInfo) {
                    System.out.println("API URN: " + def.getApiUrnNumber());

                    // Print out details of query parameters
                    if (def.getQueryParameters() != null) {
                        for (ApiDefinition.QueryParameter qp : def.getQueryParameters()) {
                            System.out.println("Query Param: " + qp.getParameterElementName());
                        }
                    }

                    // Print out details of response payloads
                    if (def.getResponsePayloads() != null) {
                        for (ApiDefinition.ResponsePayload rp : def.getResponsePayloads()) {
                            System.out.println("Response Payload: " + rp.getResponseElementName());
                        }
                    }

                    // Generate OAS YAML from the parsed API info
                    excelProcessingService.generateOasFromTemplate(def);
                }
            }

            // Add success message to the model
            model.addAttribute("message", "API data parsed and YAML generated successfully!");
            return "success";  // Render the success.html page

        } catch (IOException e) {
            model.addAttribute("message", "Error processing file: " + e.getMessage());
            return "error";  // Render the error.html page if an exception occurs
        }
    }
}

