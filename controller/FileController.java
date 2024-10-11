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
    try {
        // Parse the Excel file and get the list of rows with their cell values
        List<ApiDefinition> apiInfo = excelProcessingService.parseExcelFile(file);

        // Debugging the parsed API info
        System.out.println("Parsed API Info: " + apiInfo.size());

        if (apiInfo.isEmpty()) {
            System.out.println("No API data parsed!");
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
        String yamlOutput = yaml.dump(apiInfo);

        // Add the YAML output to the model to render in the result.html page
        model.addAttribute("yamlOutput", yamlOutput);
    } catch (IOException e) {
        e.printStackTrace();
        // In case of an error, add an error message to the model
        model.addAttribute("message", "Failed to process the Excel file");
        return "error";  // Render the error.html page if there's an issue
    }

    // Render the result.html page showing the YAML output
    return "result";
}

