package com.example.api.service;

import com.example.api.model.ApiDefinition;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelProcessingService {

    public List<ApiDefinition> parseExcelFile(MultipartFile file) throws IOException {
        List<ApiDefinition> apiDefinitions = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                ApiDefinition apiDefinition = null; // To hold the current API definition being processed
                List<ApiDefinition.QueryParameter> queryParameters = new ArrayList<>();
                List<ApiDefinition.ResponsePayload> responsePayloads = new ArrayList<>();
                
                // Mapping column names to their respective indices
                Map<String, Integer> columnMapping = new HashMap<>();
                
                // Assuming the first row contains headers
                Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    for (int j = 0; j < headerRow.getPhysicalNumberOfCells(); j++) {
                        columnMapping.put(getCellValueAsString(headerRow.getCell(j)).trim(), j);
                    }
                }

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        String firstCellValue = getCellValueAsString(row.getCell(0)).trim();

                        // If it's the API details section
                        if (!"REQ".equalsIgnoreCase(firstCellValue) && !"RES".equalsIgnoreCase(firstCellValue)) {
                            if (apiDefinition == null) {
                                apiDefinition = new ApiDefinition();
                                // Capture API details from the header row or a previous row if applicable
                                handleApiDetails(apiDefinition, row, columnMapping);
                            }
                        } 
                        // Handle request parameters
                        else if ("Request".equalsIgnoreCase(firstCellValue)) {
                            if (apiDefinition == null) {
                                apiDefinition = new ApiDefinition();
                                // Set API info from the header row
                                apiDefinition.setApiUrnNumber(getCellValueAsString(row.getCell(columnMapping.get("API URN"))));
                                apiDefinition.setFunctionalName(getCellValueAsString(row.getCell(columnMapping.get("Functional Name"))));
                                apiDefinition.setTechnicalName(getCellValueAsString(row.getCell(columnMapping.get("Technical Name"))));
                                apiDefinition.setMethod(getCellValueAsString(row.getCell(columnMapping.get("Method"))));
                                apiDefinition.setVersion(getCellValueAsString(row.getCell(columnMapping.get("Version"))));
                                apiDefinition.setDescription(getCellValueAsString(row.getCell(columnMapping.get("Description"))));
                                apiDefinition.setCbApiId(getCellValueAsString(row.getCell(columnMapping.get("Core Banking API ID"))));
                                apiDefinition.setSapiUrl(getCellValueAsString(row.getCell(columnMapping.get("SAPI URL"))));
                            }

                            // Process request parameters
                            ApiDefinition.QueryParameter parameter = new ApiDefinition.QueryParameter();
                            parameter.setParameterCode(getCellValueAsString(row.getCell(columnMapping.get("REQ/RSP"))));
                            parameter.setParameterSegmentLevel(getCellValueAsString(row.getCell(columnMapping.get("Segment Level"))));
                            parameter.setParameterElementName(getCellValueAsString(row.getCell(columnMapping.get("Resource/Element Name"))));
                            parameter.setParameterFieldDescription(getCellValueAsString(row.getCell(columnMapping.get("Field Description"))));
                            parameter.setParameterNLSField(getCellValueAsString(row.getCell(columnMapping.get("NLS Field YES/NO"))));
                            parameter.setParameterTechnicalName(getCellValueAsString(row.getCell(columnMapping.get("Technical Field Indicator"))));
                            parameter.setParameterMadatory(getCellValueAsString(row.getCell(columnMapping.get("Mandatory/Optional"))));
                            parameter.setParameterBusinessDescription(getCellValueAsString(row.getCell(columnMapping.get("Business Description"))));
                            parameter.setParameterObjectType(getCellValueAsString(row.getCell(columnMapping.get("Object Type"))));
                            parameter.setParameterOccurenceCount(getCellValueAsString(row.getCell(columnMapping.get("Length/Occurrence Count"))));
                            parameter.setParameterSampleValues(getCellValueAsString(row.getCell(columnMapping.get("Sample Values"))));
                            parameter.setParameterRemarks(getCellValueAsString(row.getCell(columnMapping.get("Remarks"))));
                            //parameter.setRequired(Boolean.parseBoolean(getCellValueAsString(row.getCell(columnMapping.get("REQ/RSP")))));
                            // parameter.setExample(getCellValueAsString(row.getCell(columnMapping.get("Example"))));
                            queryParameters.add(parameter);
                        } 
                        // Handle response payloads
                        else if ("Response".equalsIgnoreCase(firstCellValue)) {
                            // Process response payloads
                            ApiDefinition.ResponsePayload response = new ApiDefinition.ResponsePayload();
                            response.setResponseCode(getCellValueAsString(row.getCell(columnMapping.get("REQ/RSP"))));
                            response.setResponseSegmentLevel(getCellValueAsString(row.getCell(columnMapping.get("Segment Level"))));
                            response.setResponseElementName(getCellValueAsString(row.getCell(columnMapping.get("Element Name"))));
                            response.setResponseFieldDescription(getCellValueAsString(row.getCell(columnMapping.get("Field Description"))));
                            response.setResponseNLSField(getCellValueAsString(row.getCell(columnMapping.get("NLS Field YES/NO"))));
                            response.setResponseTechnicalName(getCellValueAsString(row.getCell(columnMapping.get("Technical Field Indicator"))));
                            response.setResponseMadatory(getCellValueAsString(row.getCell(columnMapping.get("Mandatory/Optional"))));
                            response.setResponseDescription(getCellValueAsString(row.getCell(columnMapping.get("Business Description"))));
                            response.setResponseObjectType(getCellValueAsString(row.getCell(columnMapping.get("Object Type"))));
                            response.setResponseOccurenceCount(getCellValueAsString(row.getCell(columnMapping.get("Length/Occurrence Count"))));
                            response.setResponseSampleValues(getCellValueAsString(row.getCell(columnMapping.get("Sample Values"))));
                            response.setResponseRemarks(getCellValueAsString(row.getCell(columnMapping.get("Remarks"))));
                            responsePayloads.add(response);
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
        }
        return apiDefinitions;
    }

    private void handleApiDetails(ApiDefinition apiDefinition, Row row, Map<String, Integer> columnMapping) {
        // Assuming the key is in the first column and value in the second column
        String key = getCellValueAsString(row.getCell(0)).trim();
        String value = getCellValueAsString(row.getCell(1)).trim();

        switch (key) {
            case "API URN":
                apiDefinition.setApiUrnNumber(value);
                break;
            case "Functional Name":
                apiDefinition.setFunctionalName(value);
                break;
            case "Technical Name":
                apiDefinition.setTechnicalName(value);
                break;
            case "Method":
                apiDefinition.setMethod(value);
                break;
            case "Version":
                apiDefinition.setVersion(value);
                break;
            case "Description":
                apiDefinition.setDescription(value);
                break;
            case "CB API ID":
                apiDefinition.setCbApiId(value);
                break;
            case "SAPI URL":
                apiDefinition.setSapiUrl(value);
                break;
            default:
                break; // Ignore unrecognized keys
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
