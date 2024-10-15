package com.example.api.service;

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
