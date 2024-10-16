package com.example.api.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class ExcelToYamlConverter {

    public String convertToYaml(String filePath) throws IOException {
        // Load the YAML template
        String yamlTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/oas_template.yaml")));

        // Load the Excel file
        FileInputStream excelFile = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet sheet = workbook.getSheetAt(0);

        Map<String, String> apiDetails = new HashMap<>();
        List<Map<String, String>> requestParameters = new ArrayList<>();
        Map<String, String> responseProperties = new HashMap<>();
        boolean inRequestSection = false;
        List<String> headers = new ArrayList<>();

        for (Row row : sheet) {
            // Skip empty rows
            if (row.getPhysicalNumberOfCells() == 0) continue;

            if (!inRequestSection) {
                // Handle API details extraction until 'request' is encountered
                if (row.getCell(0).getStringCellValue().toLowerCase().contains("request")) {
                    inRequestSection = true;
                    continue;
                }
                apiDetails.put(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue());
            } else {
                // Extract Request/Response Headers
                if (row.getCell(0).getStringCellValue().contains("REQ/RSP")) {
                    headers = getHeaders(row);
                    continue;
                }

                // Handle Request Parameters (REQ)
                if (row.getCell(0).getStringCellValue().contains("REQ")) {
                    requestParameters.add(getRowData(headers, row));
                }

                // Handle Response Properties (RSP)
                if (row.getCell(0).getStringCellValue().contains("RSP")) {
                    responseProperties.putAll(getRowData(headers, row));
                }
            }
        }

        workbook.close();

        // Populate the template with the extracted values
        return populateTemplate(yamlTemplate, apiDetails, requestParameters, responseProperties);
    }

    private List<String> getHeaders(Row row) {
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            headers.add(row.getCell(i).getStringCellValue());
        }
        return headers;
    }

    private Map<String, String> getRowData(List<String> headers, Row row) {
        Map<String, String> rowData = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            rowData.put(headers.get(i), row.getCell(i).getStringCellValue());
        }
        return rowData;
    }

    private String populateTemplate(String template, Map<String, String> apiDetails, List<Map<String, String>> requestParams, Map<String, String> responseProps) {
        // Replace placeholders with actual values from the Excel file
        template = template.replace("${title}", apiDetails.getOrDefault("api functional name", "API Title"))
                .replace("${description}", apiDetails.getOrDefault("api description", "API Description"))
                .replace("${urn}", apiDetails.getOrDefault("api urn", "default"))
                .replace("${method}", apiDetails.getOrDefault("api type", "get").toLowerCase())
                .replace("${summary}", apiDetails.getOrDefault("api summary", "No summary provided"))
                .replace("${operationId}", apiDetails.getOrDefault("api technical name", "defaultOperationId"));

        // Handle request parameters and response properties (convert to YAML format)
        String paramsYaml = generateParamsYaml(requestParams);
        String responseYaml = generateResponseYaml(responseProps);

        return template.replace("${parameters}", paramsYaml)
                       .replace("${responseProperties}", responseYaml);
    }

    private String generateParamsYaml(List<Map<String, String>> params) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> param : params) {
            sb.append("- name: ").append(param.get("api name")).append("\n")
              .append("  in: query\n")
              .append("  required: ").append(param.get("NLS FIELD YES/NO").equalsIgnoreCase("y")).append("\n")
              .append("  schema:\n")
              .append("    type: ").append(param.get("OBJECT TYPE")).append("\n");
        }
        return sb.toString();
    }

    private String generateResponseYaml(Map<String, String> responseProps) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : responseProps.entrySet()) {
            sb.append(entry.getKey()).append(":\n")
              .append("  type: ").append(entry.getValue().equals("Array") ? "array" : "string").append("\n");
        }
        return sb.toString();
    }
}
