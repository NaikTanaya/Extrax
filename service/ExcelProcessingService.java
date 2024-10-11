            
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

            // Variables to track whether we are parsing request or response parameters
            boolean parsingRequest = false;
            boolean parsingResponse = false;

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String firstCellValue = getCellValueAsString(row.getCell(0)).trim();

                // Check if we are switching to the request or response section
                if (firstCellValue.equalsIgnoreCase("Request")) {
                    parsingRequest = true;
                    parsingResponse = false;
                    queryParameters = new ArrayList<>();  // Start collecting request parameters
                    continue;
                } else if (firstCellValue.equalsIgnoreCase("Response")) {
                    parsingRequest = false;
                    parsingResponse = true;
                    responsePayloads = new ArrayList<>();  // Start collecting response payloads
                    continue;
                }

                // Parse row-wise API metadata (assumed to be at the top)
                if (!parsingRequest && !parsingResponse) {
                    if (apiDefinition == null) {
                        apiDefinition = new ApiDefinition();
                    }
                    handleApiDetails(apiDefinition, row);  // Handling row-wise API details
                }

                // Parse request parameters (column-wise)
                if (parsingRequest) {
                    ApiDefinition.QueryParameter queryParameter = new ApiDefinition.QueryParameter();
                    queryParameter.setParameterCode(getCellValueAsString(row.getCell(0)));
                    queryParameter.setParameterSegmentLevel(getCellValueAsString(row.getCell(1)));
                    queryParameter.setParameterElementName(getCellValueAsString(row.getCell(2)));
                    queryParameter.setParameterFieldDescription(getCellValueAsString(row.getCell(3)));
                    queryParameter.setParameterNLSField(getCellValueAsString(row.getCell(4)));
                    queryParameter.setParameterTechnicalName(getCellValueAsString(row.getCell(5)));
                    queryParameter.setParameterMadatory(getCellValueAsString(row.getCell(6)));
                    queryParameter.setParameterBusinessDescription(getCellValueAsString(row.getCell(7)));
                    queryParameter.setParameterObjectType(getCellValueAsString(row.getCell(8)));
                    queryParameter.setParameterOccurenceCount(getCellValueAsString(row.getCell(9)));
                    queryParameter.setParameterSampleValues(getCellValueAsString(row.getCell(10)));
                    queryParameter.setParameterRemarks(getCellValueAsString(row.getCell(11)));

                    queryParameters.add(queryParameter);  // Add to the list of request parameters
                }

                // Parse response parameters (column-wise)
                if (parsingResponse) {
                    ApiDefinition.ResponsePayload responsePayload = new ApiDefinition.ResponsePayload();
                    responsePayload.setResponseCode(getCellValueAsString(row.getCell(0)));
                    responsePayload.setResponseSegmentLevel(getCellValueAsString(row.getCell(1)));
                    responsePayload.setResponseElementName(getCellValueAsString(row.getCell(2)));
                    responsePayload.setResponseFieldDescription(getCellValueAsString(row.getCell(3)));
                    responsePayload.setResponseNLSField(getCellValueAsString(row.getCell(4)));
                    responsePayload.setResponseTechnicalName(getCellValueAsString(row.getCell(5)));
                    responsePayload.setResponseMadatory(getCellValueAsString(row.getCell(6)));
                    responsePayload.setResponseDescription(getCellValueAsString(row.getCell(7)));
                    responsePayload.setResponseObjectType(getCellValueAsString(row.getCell(8)));
                    responsePayload.setResponseOccurenceCount(getCellValueAsString(row.getCell(9)));
                    responsePayload.setResponseSampleValues(getCellValueAsString(row.getCell(10)));
                    responsePayload.setResponseRemarks(getCellValueAsString(row.getCell(11)));

                    responsePayloads.add(responsePayload);  // Add to the list of response payloads
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
