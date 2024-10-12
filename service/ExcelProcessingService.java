package com.example.api.service;

import com.example.api.model.ApiDefinition;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        ApiDefinition apiDefinition = null;  // Holds the current API definition being processed
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

                // Handle "REQ" or "RES" headers to start parsing the correct section
                if (firstCellValue.equalsIgnoreCase("REQ")) {
                    parsingRequest = true;
                    parsingResponse = false;
                    continue;  // Skip the REQ header row
                } else if (firstCellValue.equalsIgnoreCase("RES")) {
                    parsingRequest = false;
                    parsingResponse = true;
                    continue;  // Skip the RES header row
                }

                // If we are not in REQ or RES section, we are dealing with API metadata
                if (!parsingRequest && !parsingResponse) {
                    if (apiDefinition == null) {
                        apiDefinition = new ApiDefinition();
                    }
                    handleApiDetails(apiDefinition, row);  // Collect row-wise API metadata
                }

                // Process the REQ section for Query Parameters
                if (parsingRequest) {
                    ApiDefinition.QueryParameter queryParameter = extractQueryParameter(row);
                    if (!isQueryParameterEmpty(queryParameter)) {
                        queryParameters.add(queryParameter);
                    }
                }

                // Process the RES section for Response Payloads
                if (parsingResponse) {
                    ApiDefinition.ResponsePayload responsePayload = extractResponsePayload(row);
                    if (!isResponsePayloadEmpty(responsePayload)) {
                        responsePayloads.add(responsePayload);
                    }
                }
            }

            // Add the collected parameters to the API definition
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

    // Extract query parameters from a row
    private ApiDefinition.QueryParameter extractQueryParameter(Row row) {
        ApiDefinition.QueryParameter queryParameter = new ApiDefinition.QueryParameter();
        queryParameter.setParameterCode(getCellValueAsString(row.getCell(0)));
        queryParameter.setParameterSegmentLevel(getCellValueAsString(row.getCell(1)));
        queryParameter.setParameterElementName(getCellValueAsString(row.getCell(3)));
        queryParameter.setParameterFieldDescription(getCellValueAsString(row.getCell(4)));
        queryParameter.setParameterNLSField(getCellValueAsString(row.getCell(5)));
        queryParameter.setParameterTechnicalName(getCellValueAsString(row.getCell(6)));
        queryParameter.setParameterMandatory(getCellValueAsString(row.getCell(7)));
        queryParameter.setParameterBusinessDescription(getCellValueAsString(row.getCell(8)));
        queryParameter.setParameterObjectType(getCellValueAsString(row.getCell(9)));
        queryParameter.setParameterOccurrenceCount(getCellValueAsString(row.getCell(10)));
        queryParameter.setParameterSampleValues(getCellValueAsString(row.getCell(11)));
        queryParameter.setParameterRemarks(getCellValueAsString(row.getCell(13)));
        return queryParameter;
    }

    // Extract response payloads from a row
    private ApiDefinition.ResponsePayload extractResponsePayload(Row row) {
        ApiDefinition.ResponsePayload responsePayload = new ApiDefinition.ResponsePayload();
        responsePayload.setResponseCode(getCellValueAsString(row.getCell(0)));
        responsePayload.setResponseSegmentLevel(getCellValueAsString(row.getCell(1)));
        responsePayload.setResponseElementName(getCellValueAsString(row.getCell(3)));
        responsePayload.setResponseFieldDescription(getCellValueAsString(row.getCell(4)));
        responsePayload.setResponseNLSField(getCellValueAsString(row.getCell(5)));
        responsePayload.setResponseTechnicalName(getCellValueAsString(row.getCell(6)));
        responsePayload.setResponseMandatory(getCellValueAsString(row.getCell(7)));
        responsePayload.setResponseDescription(getCellValueAsString(row.getCell(8)));
        responsePayload.setResponseObjectType(getCellValueAsString(row.getCell(9)));
        responsePayload.setResponseOccurrenceCount(getCellValueAsString(row.getCell(10)));
        responsePayload.setResponseSampleValues(getCellValueAsString(row.getCell(11)));
        responsePayload.setResponseRemarks(getCellValueAsString(row.getCell(13)));
        return responsePayload;
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

    // Utility to get cell value as String
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
}
