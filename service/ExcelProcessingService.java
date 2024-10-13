package com.example.api.service;

import com.example.api.model.ApiDefinition;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelProcessingService {

    public List<ApiDefinition> parseExcelFile(MultipartFile file) throws IOException {
        List<ApiDefinition> apiDefinitions = new ArrayList<>();
        ApiDefinition apiDefinition = null; // Holds the current API definition being processed
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

                    // Add to the list if at least one field is filled
                    if (!isQueryParameterEmpty(queryParameter)) {
                        queryParameters.add(queryParameter);
                    }
                }

                // Parse response parameters (only if in the RES section)
                if (parsingResponse) {
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

                    // Add to the list if at least one field is filled
                    if (!isResponsePayloadEmpty(responsePayload)) {
                        responsePayloads.add(responsePayload);
                    }
                }
            }

            // Set the collected parameters to the API definition
            if (apiDefinition != null) {
                apiDefinition.setQueryParameters(queryParameters);
                apiDefinition.setResponsePayloads(responsePayloads);
                apiDefinitions.add(apiDefinition);
            }
        }

        return apiDefinitions;
    }

    private void handleApiDetails(ApiDefinition apiDefinition, Row row) {
        // Assuming the key is in the first column and the value in the second column for API metadata
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
                break;  // Ignore any unrecognized keys
        }
    }

    // Check if all fields of the query parameter are empty
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

    // Check if all fields of the response payload are empty
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


import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public String convertToYaml(List<ApiDefinition> apiInfo) {
    // Configure YAML output options
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    options.setIndent(2);
    options.setWidth(120);

    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true); // Skip null properties in YAML
    Yaml yaml = new Yaml(representer, options);

    // Root OpenAPI structure
    Map<String, Object> openApiSpec = new HashMap<>();
    openApiSpec.put("openapi", "3.0.0");

    // Info section
    Map<String, String> info = new HashMap<>();
    info.put("title", "API Documentation");
    info.put("description", "Generated API Documentation");
    info.put("version", "1.0.0");
    openApiSpec.put("info", info);

    // Paths section
    Map<String, Object> paths = new HashMap<>();

    // Loop through the parsed API definitions
    for (ApiDefinition apiDefinition : apiInfo) {
        Map<String, Object> pathItem = new HashMap<>();
        Map<String, Object> operation = new HashMap<>();

        // Add API details
        operation.put("operationId", apiDefinition.getApiUrnNumber());
        operation.put("summary", apiDefinition.getFunctionalName());
        operation.put("description", apiDefinition.getDescription());
        operation.put("method", apiDefinition.getMethod());
        operation.put("version", apiDefinition.getVersion());
        operation.put("apiUrnNumber", apiDefinition.getApiUrnNumber());
        operation.put("technicalName", apiDefinition.getTechnicalName());
        operation.put("cbApiId", apiDefinition.getCbApiId());

        // Handle query parameters
        if (apiDefinition.getQueryParameters() != null && !apiDefinition.getQueryParameters().isEmpty()) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (ApiDefinition.QueryParameter queryParameter : apiDefinition.getQueryParameters()) {
                Map<String, Object> parameter = new HashMap<>();
                parameter.put("name", queryParameter.getParameterCode());
                parameter.put("segmentLevel", queryParameter.getParameterSegmentLevel());
                parameter.put("elementName", queryParameter.getParameterElementName());
                parameter.put("description", queryParameter.getParameterFieldDescription());
                parameter.put("NLSField", queryParameter.getParameterNLSField());
                parameter.put("technicalName", queryParameter.getParameterTechnicalName());
                parameter.put("mandatory", queryParameter.getParameterMandatory());
                parameter.put("businessDescription", queryParameter.getParameterBusinessDescription());
                parameter.put("objectType", queryParameter.getParameterObjectType());
                parameter.put("occurrenceCount", queryParameter.getParameterOccurrenceCount());
                parameter.put("sampleValues", queryParameter.getParameterSampleValues());
                parameter.put("remarks", queryParameter.getParameterRemarks());

                // Add to parameters list
                parameters.add(parameter);
            }
            operation.put("queryParameters", parameters);
        }

        // Handle response payloads
        if (apiDefinition.getResponsePayloads() != null && !apiDefinition.getResponsePayloads().isEmpty()) {
            List<Map<String, Object>> responses = new ArrayList<>();
            for (ApiDefinition.ResponsePayload responsePayload : apiDefinition.getResponsePayloads()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", responsePayload.getResponseCode());
                response.put("segmentLevel", responsePayload.getResponseSegmentLevel());
                response.put("elementName", responsePayload.getResponseElementName());
                response.put("description", responsePayload.getResponseFieldDescription());
                response.put("NLSField", responsePayload.getResponseNLSField());
                response.put("technicalName", responsePayload.getResponseTechnicalName());
                response.put("mandatory", responsePayload.getResponseMandatory());
                response.put("responseDescription", responsePayload.getResponseDescription());
                response.put("objectType", responsePayload.getResponseObjectType());
                response.put("occurrenceCount", responsePayload.getResponseOccurrenceCount());
                response.put("sampleValues", responsePayload.getResponseSampleValues());
                response.put("remarks", responsePayload.getResponseRemarks());

                // Add to responses list
                responses.add(response);
            }
            operation.put("responsePayloads", responses);
        }

        // Assuming it's a GET request (adjust based on the method)
        pathItem.put(apiDefinition.getMethod().toLowerCase(), operation);

        // Add the pathItem to paths with the corresponding API URI
        paths.put(apiDefinition.getSapiUrl(), pathItem);
    }

    openApiSpec.put("paths", paths);

    // Convert the OpenAPI object to YAML
    StringWriter writer = new StringWriter();
    yaml.dump(openApiSpec, writer);
    return writer.toString();
}


}
