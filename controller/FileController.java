package com.example.api.controller;

import com.example.api.service.ExcelProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            // Process the uploaded file and extract API details
            List<ApiDefinition> apiInfo = excelProcessingService.handleApiDetails(file);

            if (apiInfo.isEmpty()) {
                model.addAttribute("message", "No API data parsed from the file.");
                return "error";
            }

            // Load the OAS YAML template
            String oasTemplate = loadYamlTemplate();

            // Use the parsed API information to replace placeholders in the OAS template
            String filledYaml = fillYamlTemplate(oasTemplate, apiInfo);

            // Add the filled YAML to the model to display it
            model.addAttribute("oasYaml", filledYaml);

            return "displayOas";  // Renders the displayOas.html to show the YAML
        } catch (IOException e) {
            model.addAttribute("message", "Error processing the file: " + e.getMessage());
            return "error";
        }
    }

    // Helper method to load the OAS YAML template from classpath
    private String loadYamlTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/oas_template.yaml");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Helper method to replace placeholders in the OAS template
    private String fillYamlTemplate(String oasTemplate, List<ApiDefinition> apiInfo) {
        // Replace placeholders in oasTemplate using data from apiInfo
        // Example of how you might replace placeholders:
        ApiDefinition api = apiInfo.get(0);  // Assume one API definition
        return oasTemplate
                .replace("{{API_URN}}", api.getApiUrnNumber())
                .replace("{{API_FUNCTIONAL_NAME}}", api.getFunctionalName())
                .replace("{{API_VERSION}}", api.getVersion())
                .replace("{{QUERY_PARAMETERS}}", formatQueryParameters(api.getQueryParameters()))
                .replace("{{RESPONSE_PAYLOADS}}", formatResponsePayloads(api.getResponsePayloads()));
    }

    // Helper method to format query parameters for YAML
    private String formatQueryParameters(List<ApiDefinition.QueryParameter> queryParameters) {
        StringBuilder builder = new StringBuilder();
        for (ApiDefinition.QueryParameter param : queryParameters) {
            builder.append("  - name: ").append(param.getParameterElementName()).append("\n")
                   .append("    description: ").append(param.getParameterFieldDescription()).append("\n")
                   .append("    required: ").append(param.getParameterMandatory()).append("\n");
        }
        return builder.toString();
    }

    // Helper method to format response payloads for YAML
    private String formatResponsePayloads(List<ApiDefinition.ResponsePayload> responsePayloads) {
        StringBuilder builder = new StringBuilder();
        for (ApiDefinition.ResponsePayload payload : responsePayloads) {
            builder.append("  - name: ").append(payload.getResponseElementName()).append("\n")
                   .append("    description: ").append(payload.getResponseFieldDescription()).append("\n")
                   .append("    required: ").append(payload.getResponseMandatory()).append("\n");
        }
        return builder.toString();
    }
}
