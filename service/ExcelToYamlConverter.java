package com.example.apidetails.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ApiDetailsExtractorService {

    public String extractApiDetails(MultipartFile file) throws IOException {
        Map<String, String> apiDetails = new LinkedHashMap<>();
        List<Map<String, String>> requestParameters = new ArrayList<>();
        Map<String, Map<String, Object>> responseProperties = new LinkedHashMap<>();
        List<String> headers = new ArrayList<>();
        boolean inRequestSection = false;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (isRowEmpty(row)) {
                    continue;
                }

                // Extract API details
                if (!inRequestSection) {
                    if (containsCellValue(row, "request")) {
                        inRequestSection = true;
                        continue;
                    }
                    String key = getCellValue(row, 0);
                    String value = getCellValue(row, 1);
                    if (value != null) {
                        apiDetails.put(key, value);
                    }
                } else {
                    // Extract request and response headers and parameters
                    if (containsCellValue(row, "REQ/RSP")) {
                        headers = extractHeaders(row);
                    } else if (containsCellValue(row, "REQ")) {
                        requestParameters.add(mapHeadersToValues(headers, row));
                    } else if (containsCellValue(row, "RSP")) {
                        responseProperties.put(mapHeadersToValues(headers, row).get("ElementName"), 
                                generateResponseSchema(mapHeadersToValues(headers, row)));
                    }
                }
            }
        }

        return generateOasYaml(apiDetails, requestParameters, responseProperties);
    }

    private boolean isRowEmpty(Row row) {
        return row == null || row.getPhysicalNumberOfCells() == 0;
    }

    private boolean containsCellValue(Row row, String value) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            if (row.getCell(i).toString().toLowerCase().contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String getCellValue(Row row, int cellIndex) {
        return row.getCell(cellIndex) != null ? row.getCell(cellIndex).toString() : null;
    }

    private List<String> extractHeaders(Row row) {
        List<String> headers = new ArrayList<>();
        row.forEach(cell -> headers.add(cell.toString()));
        return headers;
    }

    private Map<String, String> mapHeadersToValues(List<String> headers, Row row) {
        Map<String, String> keyValueMap = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String cellValue = getCellValue(row, i);
            if (cellValue != null) {
                keyValueMap.put(headers.get(i), cellValue);
            }
        }
        return keyValueMap;
    }

    private Map<String, Object> generateResponseSchema(Map<String, String> responseDetails) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", responseDetails.getOrDefault("ObjectType", "string"));
        schema.put("description", responseDetails.getOrDefault("FieldDescription", "No description provided"));
        schema.put("sampleValue", responseDetails.getOrDefault("SampleValues", ""));
        schema.put("remarks", responseDetails.getOrDefault("Remarks", ""));
        return schema;
    }

    private String generateOasYaml(Map<String, String> apiDetails,
                                   List<Map<String, String>> requestParameters,
                                   Map<String, Map<String, Object>> responseProperties) throws IOException {

        Map<String, Object> oasTemplate = new LinkedHashMap<>();
        oasTemplate.put("openapi", "3.0.0");
        oasTemplate.put("info", Map.of(
                "title", apiDetails.getOrDefault("Functional Name", "API Title"),
                "version", apiDetails.getOrDefault("Version", "1.0.0"),
                "description", apiDetails.getOrDefault("Description", "API Description")
        ));

        List<Map<String, Object>> parametersList = new ArrayList<>();
        for (Map<String, String> param : requestParameters) {
            parametersList.add(Map.of(
                    "name", param.getOrDefault("ElementName", "parameter"),
                    "in", "query",
                    "required", "Y".equalsIgnoreCase(param.getOrDefault("Mandatory", "N")),
                    "schema", Map.of(
                            "type", param.getOrDefault("ObjectType", "string"),
                            "description", param.getOrDefault("BusinessDescription", "No description available"),
                            "nlsField", param.getOrDefault("NLSField", ""),
                            "technicalName", param.getOrDefault("TechnicalName", ""),
                            "occurrenceCount", param.getOrDefault("OccurrenceCount", ""),
                            "sampleValues", param.getOrDefault("SampleValues", ""),
                            "remarks", param.getOrDefault("Remarks", "")
                    )
            ));
        }

        oasTemplate.put("paths", Map.of(
                "/" + apiDetails.getOrDefault("API URN", "default") + "/", Map.of(
                        apiDetails.getOrDefault("Method", "get").toLowerCase(), Map.of(
                                "summary", apiDetails.getOrDefault("Functional Name", "No summary provided"),
                                "description", apiDetails.getOrDefault("Description", "No description provided"),
                                "operationId", apiDetails.getOrDefault("Technical Name", "defaultOperationId"),
                                "parameters", parametersList,
                                "responses", Map.of(
                                        "200", Map.of(
                                                "description", "Successful response",
                                                "content", Map.of(
                                                        "application/json", Map.of(
                                                                "schema", Map.of(
                                                                        "type", "object",
                                                                        "properties", responseProperties
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));

        // Save the OAS YAML to a file
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter("oasfile.yaml")) {
            yaml.dump(oasTemplate, writer);
        }

        return yaml.dump(oasTemplate);
    }
}
