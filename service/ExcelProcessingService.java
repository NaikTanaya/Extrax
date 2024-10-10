package com.example.api.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExcelProcessingService {

    public Map<String, Object> parseExcelFile(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Map<String, Object> apiData = new HashMap<>();

        // Loop through all sheets in the workbook
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            System.out.println("Reading sheet: " + sheetName);

            // Loop through rows of the current sheet
            Map<String, Object> sheetData = new HashMap<>();
            for (Row row : sheet) {
                // Handling null checks for cells
                if (row.getCell(0) != null && row.getCell(1) != null) {
                    String key = row.getCell(0).getStringCellValue();
                    String value = row.getCell(1).getStringCellValue();
                    sheetData.put(key, value);
                }
            }
            apiData.put(sheetName, sheetData);
        }
        workbook.close();
        return apiData;
    }
}
