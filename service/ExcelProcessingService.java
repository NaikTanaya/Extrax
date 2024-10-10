package com.example.demo.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExcelProcessingService {

    public Map<String, Object> readExcel(MultipartFile file) throws IOException {
        Map<String, Object> apiDetails = new HashMap<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Contract");  // Adjust based on sheet name
            
            // Step 1: Extract API details (API Name, Method, etc.)
            apiDetails.put("apiInfo", readApiDetails(sheet));

            // Step 2: Extract Request parameters
            apiDetails.put("requestParams", readRequestParameters(sheet));

            // Step 3: Extract Response schema
            apiDetails.put("responseSchema", readResponseSchema(sheet));
        }

        return apiDetails;
    }

    private Map<String, String> readApiDetails(Sheet sheet) {
        Map<String, String> apiInfo = new HashMap<>();
        Row apiInfoRow = sheet.getRow(0);  // Example row for API details
        
        apiInfo.put("apiName", getStringValue(sheet, 1, 2));  // Adjust cell indices
        apiInfo.put("method", getStringValue(sheet, 2, 2));
        apiInfo.put("description", getStringValue(sheet, 3, 2));

        return apiInfo;
    }

    private Map<String, Object> readRequestParameters(Sheet sheet) {
        Map<String, Object> requestParams = new HashMap<>();

        for (int i = 10; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            // Read request parameter details like Path, Query, and Payload
            String paramType = getStringValue(row, 1);  // Column for type
            String paramName = getStringValue(row, 3);  // Column for name

            if (paramType.equals("PathParam")) {
                requestParams.put(paramName, "path");
            } else if (paramType.equals("QueryParam")) {
                requestParams.put(paramName, "query");
            }
            // Handle other segments...
        }

        return requestParams;
    }

    private Map<String, Object> readResponseSchema(Sheet sheet) {
        Map<String, Object> responseSchema = new HashMap<>();

        for (int i = 30; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String statusCode = getStringValue(row, 1); // Column for response code
            String message = getStringValue(row, 3);    // Column for message

            responseSchema.put(statusCode, message);
        }

        return responseSchema;
    }

    private String getStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        return (cell == null) ? "" : cell.getStringCellValue();
    }
}
