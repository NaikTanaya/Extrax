package com.example.api.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class ExcelToYamlConverter {

    public String convertToYaml(MultipartFile file) throws IOException {
        // Load the YAML template from resources
        String yamlTemplate = new String(getClass().getClassLoader().getResourceAsStream("oas_template.yaml").readAllBytes());

        // Load the Excel file from MultipartFile
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Map<String, String> apiDetails = new HashMap<>();
        List<Map<String, String>> queryParameters = new ArrayList<>();
        List<Map<String, String>> bodyParameters = new ArrayList<>();
        List<Map<String, String>> responseProperties = new ArrayList<>();
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
                // Map API details to the appropriate fields
                apiDetails.put(row.getCell(0).getStringCellValue(), row.getCell(1).getStringCellValue());
            } else {
                // Extract Request/Response Headers
                if (row.getCell(0).getStringCellValue().contains("REQ/RSP")) {
                    headers = getHeaders(row);
                    continue;
                }

                // Handle Request Parameters (REQ)
                if (row.getCell(0).getStringCellValue().equalsIgnoreCase("REQ")) {
                    Map<String, String> rowData = getRowData(headers, row);
                    if (rowData.get("SegmentLevel").equalsIgnoreCase("query")) {
                        queryParameters.add(rowData);
                    } else if (rowData.get("SegmentLevel").equalsIgnoreCase("body")) {
                        bodyParameters.add(rowData);
                    }
                }

                // Handle Response Properties (RSP)
                if (row.getCell(0).getStringCellValue().equalsIgnoreCase("RSP")) {
                    responseProperties.add(getRowData(headers, row));
                }
            }
        }

        workbook.close();

        // Populate the template with the extracted values
        return populateTemplate(yamlTemplate, apiDetails, queryParameters, bodyParameters, responseProperties);
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

    private String populateTemplate(String template, Map<String, String> apiDetails,
                                    List<Map<String, String>> queryParams,
                                    List<Map<String, String>> bodyParams,
                                    List<Map<String, String>> responseProps) {
        // Replace placeholders with actual values from the Excel file
        template = template.replace("${urn}", apiDetails.getOrDefault("API URN", "default"))
                .replace("${functionalName}", apiDetails.getOrDefault("Functional Name", "API Title"))
                .replace("${technicalName}", apiDetails.getOrDefault("Technical Name", "defaultOperationId"))
                .replace("${method}", apiDetails.getOrDefault("Method", "get").toLowerCase())
                .replace("${version}", apiDetails.getOrDefault("Version", "1.0.0"))
                .replace("${description}", apiDetails.getOrDefault("Description", "API Description"));

        // Generate query and body parameters
        String paramsYaml = generateParamsYaml(queryParams, "query");
        String bodyYaml = generateParamsYaml(bodyParams, "body");

        // Generate response properties
        String responseYaml = generateResponseYaml(responseProps);

        return template.replace("${parameters}", paramsYaml)
                       .replace("${requestBody}", bodyYaml)
                       .replace("${responseProperties}", responseYaml);
    }

    private String generateParamsYaml(List<Map<String, String>> params, String paramType) {
        StringBuilder sb = new StringBuilder();

        // Iterate through all parameters and append them to the YAML
        for (Map<String, String> param : params) {
            sb.append("- name: ").append(param.get("ElementName")).append("\n")
              .append("  in: ").append(paramType).append("\n")
              .append("  description: ").append(param.get("FieldDescription")).append("\n")
              .append("  required: ").append(param.get("Mandatory").equalsIgnoreCase("y")).append("\n")
              .append("  schema:\n")
              .append("    type: ").append(param.get("ObjectType")).append("\n")
              .append("    example: ").append(param.get("SampleValues")).append("\n")
              .append("  nlsField: ").append(param.get("NLSField")).append("\n")  // Include NLSField
              .append("  technicalName: ").append(param.get("TechnicalName")).append("\n")  // Include TechnicalName
              .append("  businessDescription: ").append(param.get("BusinessDescription")).append("\n")  // Include BusinessDescription
              .append("  occurrenceCount: ").append(param.get("OccurrenceCount")).append("\n")  // Include OccurrenceCount
              .append("  remarks: ").append(param.get("Remarks")).append("\n");  // Include Remarks
        }

        return sb.toString();
    }

    private String generateResponseYaml(List<Map<String, String>> responseProps) {
        StringBuilder sb = new StringBuilder();
        sb.append("  content:\n")
          .append("    application/json:\n")
          .append("      schema:\n")
          .append("        type: object\n")
          .append("        properties:\n");

        // Iterate through all response properties and append them to the YAML
        for (Map<String, String> prop : responseProps) {
            sb.append("          ").append(prop.get("ElementName")).append(":\n")
              .append("            type: ").append(prop.get("ObjectType")).append("\n")
              .append("            description: ").append(prop.get("FieldDescription")).append("\n")
              .append("            example: ").append(prop.get("SampleValues")).append("\n")
              .append("            nlsField: ").append(prop.get("NLSField")).append("\n")  // Include NLSField
              .append("            technicalName: ").append(prop.get("TechnicalName")).append("\n")  // Include TechnicalName
              .append("            businessDescription: ").append(prop.get("BusinessDescription")).append("\n")  // Include BusinessDescription
              .append("            occurrenceCount: ").append(prop.get("OccurrenceCount")).append("\n")  // Include OccurrenceCount
              .append("            remarks: ").append(prop.get("Remarks")).append("\n");  // Include Remarks
        }

        return sb.toString();
    }

     public void extractApiDetails(String filePath) {
        try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet
            
            for (Row row : sheet) {
                // Skip empty rows
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                // Read API details or request/response parameters
                for (Cell cell : row) {
                    String cellValue = getCellValueAsString(cell);
                    // Process cellValue as needed
                    System.out.println(cellValue); // For debugging or processing
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            if (row.getCell(i) != null && row.getCell(i).getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Check if the cell is a date
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // Convert to string as needed
                } else {
                    return String.valueOf(cell.getNumericCellValue()); // Convert numeric to string
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "Unknown cell type";
        }
    }
}
