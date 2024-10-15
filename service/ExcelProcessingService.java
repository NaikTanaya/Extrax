import org.springframework.core.io.ClassPathResource;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class ExcelProcessingService {

    public List<ApiDefinition> parseExcelFile(MultipartFile file) throws IOException {
        List<ApiDefinition> apiDefinitions = new ArrayList<>();
        ApiDefinition apiDefinition = null; // Holds the current API definition being processedpackage com.example.api.service;

import com.example.api.model.ApiDefinition;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelProcessingService {

    public List<ApiDefinition> parseExcelFile(MultipartFile file) throws IOException {
        List<ApiDefinition> apiDefinitions = new ArrayList<>();
        ApiDefinition apiDefinition = null;
        List<ApiDefinition.QueryParameter> queryParameters = new ArrayList<>();
        List<ApiDefinition.ResponsePayload> responsePayloads = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            boolean parsingRequest = false;
            boolean parsingResponse = false;

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String firstCellValue = getCellValueAsString(row.getCell(0)).trim();

                if (firstCellValue.equalsIgnoreCase("REQ")) {
                    parsingRequest = true;
                    parsingResponse = false;
                    queryParameters = new ArrayList<>();
                    continue;
                } else if (firstCellValue.equalsIgnoreCase("RES")) {
                    parsingRequest = false;
                    parsingResponse = true;
                    responsePayloads = new ArrayList<>();
                    continue;
                }

                if (!parsingRequest && !parsingResponse) {
                    if (apiDefinition == null) {
                        apiDefinition = new ApiDefinition();
                    }
                    handleApiDetails(apiDefinition, row);
                }

                if (parsingRequest) {
                    ApiDefinition.QueryParameter queryParameter = new ApiDefinition.QueryParameter();
                    fillQueryParameter(queryParameter, row);
                    if (!isQueryParameterEmpty(queryParameter)) {
                        queryParameters.add(queryParameter);
                    }
                }

                if (parsingResponse) {
                    ApiDefinition.ResponsePayload responsePayload = new ApiDefinition.ResponsePayload();
                    fillResponsePayload(responsePayload, row);
                    if (!isResponsePayloadEmpty(responsePayload)) {
                        responsePayloads.add(responsePayload);
                    }
                }
            }

            if (apiDefinition != null) {
                apiDefinition.setQueryParameters(queryParameters);
                apiDefinition.setResponsePayloads(responsePayloads);
                apiDefinitions.add(apiDefinition);
            }
        }

        return apiDefinitions;
    }

    private void handleApiDetails(ApiDefinition apiDefinition, Row row) {
        String key = getCellValueAsString(row.getCell(0)).trim();
        String value = getCellValueAsString(row.getCell(1)).trim();

        switch (key) {
            case "API URN (Unique Reference Number):":
                apiDefinition.setApiUrnNumber(value);
                break;
            case "API Functional Name:":
                apiDefinition.setFunctionalName(value);
                break;
            case "API Technical Name:":
                apiDefinition.setTechnicalName(value);
                break;
            case "Method:":
                apiDefinition.setMethod(value);
                break;
            case "API Version:":
                apiDefinition.setVersion(value);
                break;
            case "Description:":
                apiDefinition.setDescription(value);
                break;
            case "Core Banking API ID:":
                apiDefinition.setCbApiId(value);
                break;
            case "Core Banking SAPI URI:":
                apiDefinition.setSapiUrl(value);
                break;
            default:
                break;
        }
    }

    private void fillQueryParameter(ApiDefinition.QueryParameter queryParameter, Row row) {
        queryParameter.setParameterCode(getCellValueAsString(row.getCell(0)));
        queryParameter.setParameterSegmentLevel(getCellValueAsString(row.getCell(1)));
        queryParameter.setParameterElementName(getCellValueAsString(row.getCell(2)));
        queryParameter.setParameterFieldDescription(getCellValueAsString(row.getCell(3)));
        queryParameter.setParameterNLSField(getCellValueAsString(row.getCell(4)));
        queryParameter.setParameterTechnicalName(getCellValueAsString(row.getCell(5)));
        queryParameter.setParameterMandatory(getCellValueAsString(row.getCell(6)));
        queryParameter.setParameterBusinessDescription(getCellValueAsString(row.getCell(7)));
        queryParameter.setParameterObjectType(getCellValueAsString(row.getCell(8)));
        queryParameter.setParameterOccurrenceCount(getCellValueAsString(row.getCell(9)));
        queryParameter.setParameterSampleValues(getCellValueAsString(row.getCell(10)));
        queryParameter.setParameterRemarks(getCellValueAsString(row.getCell(11)));
    }

    private void fillResponsePayload(ApiDefinition.ResponsePayload responsePayload, Row row) {
        responsePayload.setResponseCode(getCellValueAsString(row.getCell(0)));
        responsePayload.setResponseSegmentLevel(getCellValueAsString(row.getCell(1)));
        responsePayload.setResponseElementName(getCellValueAsString(row.getCell(2)));
        responsePayload.setResponseFieldDescription(getCellValueAsString(row.getCell(3)));
        responsePayload.setResponseNLSField(getCellValueAsString(row.getCell(4)));
        responsePayload.setResponseTechnicalName(getCellValueAsString(row.getCell(5)));
        responsePayload.setResponseMandatory(getCellValueAsString(row.getCell(6)));
        responsePayload.setResponseDescription(getCellValueAsString(row.getCell(7)));
        responsePayload.setResponseObjectType(getCellValueAsString(row.getCell(8)));
        responsePayload.setResponseOccurrenceCount(getCellValueAsString(row.getCell(9)));
        responsePayload.setResponseSampleValues(getCellValueAsString(row.getCell(10)));
        responsePayload.setResponseRemarks(getCellValueAsString(row.getCell(11)));
    }

    private boolean isQueryParameterEmpty(ApiDefinition.QueryParameter queryParameter) {
        return queryParameter.getParameterCode().isEmpty() &&
               queryParameter.getParameterSegmentLevel().isEmpty() &&
               queryParameter.getParameterElementName().isEmpty() &&
               queryParameter.getParameterFieldDescription().isEmpty() &&
               queryParameter.getParameterNLSField().isEmpty() &&
               queryParameter.getParameterTechnicalName().isEmpty() &&
               queryParameter.getParameterMandatory().isEmpty() &&
               queryParameter.getParameterBusinessDescription().isEmpty() &&
               queryParameter.getParameterObjectType().isEmpty() &&
               queryParameter.getParameterOccurrenceCount().isEmpty() &&
               queryParameter.getParameterSampleValues().isEmpty() &&
               queryParameter.getParameterRemarks().isEmpty();
    }

    private boolean isResponsePayloadEmpty(ApiDefinition.ResponsePayload responsePayload) {
        return responsePayload.getResponseCode().isEmpty() &&
               responsePayload.getResponseSegmentLevel().isEmpty() &&
               responsePayload.getResponseElementName().isEmpty() &&
               responsePayload.getResponseFieldDescription().isEmpty() &&
               responsePayload.getResponseNLSField().isEmpty() &&
               responsePayload.getResponseTechnicalName().isEmpty() &&
               responsePayload.getResponseMandatory().isEmpty() &&
               responsePayload.getResponseDescription().isEmpty() &&
               responsePayload.getResponseObjectType().isEmpty() &&
               responsePayload.getResponseOccurrenceCount().isEmpty() &&
               responsePayload.getResponseSampleValues().isEmpty() &&
               responsePayload.getResponseRemarks().isEmpty();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String loadTemplate() throws IOException {
        return new String(Files.readAllBytes(Paths.get("oas_template.yaml")), StandardCharsets.UTF_8);
    }

    private String populateTemplate(String template, ApiDefinition apiDefinition) {
        String populatedTemplate = template
                .replace("{{api_urn}}", apiDefinition.getApiUrnNumber())
                .replace("{{functional_name}}", apiDefinition.getFunctionalName())
                .replace("{{technical_name}}", apiDefinition.getTechnicalName())
                .replace("{{method}}", apiDefinition.getMethod())
                .replace("{{version}}", apiDefinition.getVersion())
                .replace("{{description}}", apiDefinition.getDescription())
                .replace("{{cb_api_id}}", apiDefinition.getCbApiId())
                .replace("{{sapi_url}}", apiDefinition.getSapiUrl());
        
        // Handle query parameters
        StringBuilder queryParams = new StringBuilder();
        for (ApiDefinition.QueryParameter param : apiDefinition.getQueryParameters()) {
            queryParams.append("- name: ").append(param.getParameterCode()).append("\n");
            queryParams.append("  in: query\n");
            queryParams.append("  required: ").append(param.getParameterMandatory()).append("\n");
            queryParams.append("  schema:\n");
            queryParams.append("    type: string\n");
        }
        
        populatedTemplate = populatedTemplate.replace("{{query_parameters}}", queryParams.toString());

        // Handle response payloads
        StringBuilder responsePayloads = new StringBuilder();
        for (ApiDefinition.ResponsePayload payload : apiDefinition.getResponsePayloads()) {
            responsePayloads.append("- code: ").append(payload.getResponseCode()).append("\n");
            responsePayloads.append("  description: ").append(payload.getResponseDescription()).append("\n");
            responsePayloads.append("  schema:\n");
            responsePayloads.append("    type: object\n");
        }
        
        populatedTemplate = populatedTemplate.replace("{{response_payloads}}", responsePayloads.toString());

        return populatedTemplate;
    }

    public void generateOasFromTemplate(ApiDefinition apiDefinition) throws IOException {
        String template = loadTemplate();
        String populatedYaml = populateTemplate(template, apiDefinition);

        // Save the populated YAML to a file
        saveYamlToFile(populatedYaml, "output_oas.yaml");
    }

    private void saveYamlToFile(String yamlContent, String fileName) throws IOException {
        Files.write(Paths.get(fileName), yamlContent.getBytes(StandardCharsets.UTF_8));
    }
}

        List<ApiDefinition.QueryParameter> queryParameters = new ArrayList<>();
        List<ApiDefinition.ResponsePayload> responsePayloads = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);  // Assuming the first sheet contains the relevant data

            boolean parsingRequest = false;
            boolean parsingResponse = false;

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String firstCellValue = getCellValueAsString(row.getCell(0)).trim();

                // Check if we are switching to the request or response section
                if (firstCellValue.equalsIgnoreCase("REQ")) {
                    parsingRequest = true;
                    parsingResponse = false;
                    queryParameters = new ArrayList<>();  // Start collecting request parameters
                    continue;
                } else if (firstCellValue.equalsIgnoreCase("RES")) {
                    parsingRequest = false;
                    parsingResponse = true;
                    responsePayloads = new ArrayList<>();  // Start collecting response payloads
                    continue;
                }

                // Skip rows that contain "REQ" or "RES" in a way that indicates they are headers
                if (firstCellValue.equalsIgnoreCase("REQ/RES")) {
                    continue;  // Ignore header row
                }

                // Parse row-wise API metadata (assumed to be at the top)
                if (!parsingRequest && !parsingResponse) {
                    if (apiDefinition == null) {
                        apiDefinition = new ApiDefinition();
                    }
                    handleApiDetails(apiDefinition, row);  // Handling row-wise API details
                }

                // Parse request parameters (only if in the REQ section)
                if (parsingRequest) {
                    ApiDefinition.QueryParameter queryParameter = createQueryParameter(row);
                    if (!isQueryParameterEmpty(queryParameter)) {
                        queryParameters.add(queryParameter);
                    }
                }

                // Parse response parameters (only if in the RES section)
                if (parsingResponse) {
                    ApiDefinition.ResponsePayload responsePayload = createResponsePayload(row);
                    if (!isResponsePayloadEmpty(responsePayload)) {
                        responsePayloads.add(responsePayload);
                    }
                }
            }

            // Set the collected parameters and populate the YAML template
            if (apiDefinition != null) {
                apiDefinition.setQueryParameters(queryParameters);
                apiDefinition.setResponsePayloads(responsePayloads);

                // Load the YAML template from resources
                String yamlTemplate = loadYamlTemplate();

                // Replace placeholders in the YAML template
                String finalYaml = populateYamlTemplate(yamlTemplate, apiDefinition);

                apiDefinition.setYamlTemplate(finalYaml); // Store the final YAML with replaced values
                apiDefinitions.add(apiDefinition);
            }
        }

        return apiDefinitions;
    }

    private ApiDefinition.QueryParameter createQueryParameter(Row row) {
        ApiDefinition.QueryParameter queryParameter = new ApiDefinition.QueryParameter();
        queryParameter.setParameterCode(getCellValueAsString(row.getCell(0)));
        queryParameter.setParameterSegmentLevel(getCellValueAsString(row.getCell(1)));
        queryParameter.setParameterElementName(getCellValueAsString(row.getCell(2)));
        queryParameter.setParameterFieldDescription(getCellValueAsString(row.getCell(3)));
        queryParameter.setParameterNLSField(getCellValueAsString(row.getCell(4)));
        queryParameter.setParameterTechnicalName(getCellValueAsString(row.getCell(5)));
        queryParameter.setParameterMandatory(getCellValueAsString(row.getCell(6)));
        queryParameter.setParameterBusinessDescription(getCellValueAsString(row.getCell(7)));
        queryParameter.setParameterObjectType(getCellValueAsString(row.getCell(8)));
        queryParameter.setParameterOccurrenceCount(getCellValueAsString(row.getCell(9)));
        queryParameter.setParameterSampleValues(getCellValueAsString(row.getCell(10)));
        queryParameter.setParameterRemarks(getCellValueAsString(row.getCell(11)));
        return queryParameter;
    }

    private ApiDefinition.ResponsePayload createResponsePayload(Row row) {
        ApiDefinition.ResponsePayload responsePayload = new ApiDefinition.ResponsePayload();
        responsePayload.setResponseCode(getCellValueAsString(row.getCell(0)));
        responsePayload.setResponseSegmentLevel(getCellValueAsString(row.getCell(1)));
        responsePayload.setResponseElementName(getCellValueAsString(row.getCell(2)));
        responsePayload.setResponseFieldDescription(getCellValueAsString(row.getCell(3)));
        responsePayload.setResponseNLSField(getCellValueAsString(row.getCell(4)));
        responsePayload.setResponseTechnicalName(getCellValueAsString(row.getCell(5)));
        responsePayload.setResponseMandatory(getCellValueAsString(row.getCell(6)));
        responsePayload.setResponseDescription(getCellValueAsString(row.getCell(7)));
        responsePayload.setResponseObjectType(getCellValueAsString(row.getCell(8)));
        responsePayload.setResponseOccurrenceCount(getCellValueAsString(row.getCell(9)));
        responsePayload.setResponseSampleValues(getCellValueAsString(row.getCell(10)));
        responsePayload.setResponseRemarks(getCellValueAsString(row.getCell(11)));
        return responsePayload;
    }

    // Load YAML template from resources
    private String loadYamlTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("template.yaml");
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }

    // Replace placeholders in the YAML template
    private String populateYamlTemplate(String yamlTemplate, ApiDefinition apiDefinition) {
        yamlTemplate = yamlTemplate.replace("${functionalName}", apiDefinition.getFunctionalName());
        yamlTemplate = yamlTemplate.replace("${description}", apiDefinition.getDescription());
        yamlTemplate = yamlTemplate.replace("${version}", apiDefinition.getVersion());

        // Replace query parameters placeholder
        String queryParams = buildQueryParametersYaml(apiDefinition.getQueryParameters());
        yamlTemplate = yamlTemplate.replace("${queryParameters}", queryParams);

        // Replace responses placeholder
        String responsePayloads = buildResponsePayloadsYaml(apiDefinition.getResponsePayloads());
        yamlTemplate = yamlTemplate.replace("${responses}", responsePayloads);

        return yamlTemplate;
    }

    private String buildQueryParametersYaml(List<ApiDefinition.QueryParameter> queryParameters) {
        StringBuilder queryYaml = new StringBuilder();
        for (ApiDefinition.QueryParameter param : queryParameters) {
            queryYaml.append("- name: ").append(param.getParameterCode()).append("\n")
                     .append("  in: query\n")
                     .append("  description: ").append(param.getParameterFieldDescription()).append("\n")
                     .append("  required: ").append(param.getParameterMandatory().equalsIgnoreCase("yes")).append("\n")
                     .append("  schema:\n")
                     .append("    type: ").append(param.getParameterObjectType()).append("\n");
        }
        return queryYaml.toString();
    }

    private String buildResponsePayloadsYaml(List<ApiDefinition.ResponsePayload> responsePayloads) {
        StringBuilder responseYaml = new StringBuilder();
        for (ApiDefinition.ResponsePayload payload : responsePayloads) {
            responseYaml.append(payload.getResponseCode()).append(":\n")
                        .append("  description: ").append(payload.getResponseFieldDescription()).append("\n")
                        .append("  content:\n")
                        .append("    application/json:\n")
                        .append("      schema:\n")
                        .append("        type: object\n");
        }
        return responseYaml.toString();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();  // Format as needed
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();  // Handle formulas if needed
            default:
                return "";
        }
    }

      private void saveYamlToFile(String yamlContent, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(yamlContent);
        }
    }

    // Modify your logic to call this after populating the YAML template
    public List<ApiDefinition> parseExcelFile(MultipartFile file) throws IOException {
        // Your existing parsing logic...

        // After populating the YAML template with values
        String finalYaml = populateYamlTemplate(yamlTemplate, apiDefinition);

        // Save the final YAML to a file
        String outputFileName = "output.yaml";  // You can choose the file name
        saveYamlToFile(finalYaml, outputFileName);

        // Store final YAML to the API definition if needed
        apiDefinition.setYamlTemplate(finalYaml);
        
        // Add apiDefinition to list
        apiDefinitions.add(apiDefinition);

        return apiDefinitions;
    }
}
