package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class OASGeneratorService {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public String generateOASFile(Map<String, Object> apiDetails) throws IOException {
        Map<String, String> apiInfo = (Map<String, String>) apiDetails.get("apiInfo");
        Map<String, Object> requestParams = (Map<String, Object>) apiDetails.get("requestParams");
        Map<String, Object> responseSchema = (Map<String, Object>) apiDetails.get("responseSchema");

        // Constructing OpenAPI YAML structure
        Map<String, Object> openApiSpec = Map.of(
                "openapi", "3.0.0",
                "info", Map.of(
                        "title", apiInfo.get("apiName"),
                        "version", "1.0.0",
                        "description", apiInfo.get("description")
                ),
                "paths", Map.of(
                        "/api/v1/" + apiInfo.get("apiName"), Map.of(
                                apiInfo.get("method").toLowerCase(), Map.of(
                                        "description", "Generated API",
                                        "parameters", requestParams,   // Map request parameters here
                                        "responses", responseSchema   // Map response schema here
                                )
                        )
                )
        );

        // Write OpenAPI YAML to file
        String outputPath = System.getProperty("java.io.tmpdir") + "openapi.yaml";
        yamlMapper.writeValue(new File(outputPath), openApiSpec);

        return outputPath;
    }
}
