package com.example.api.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExcelProcessingService {

    public Map<String, Object> parseExcelFile(MultipartFile file) throws IOException {
        Map<String, Object> apiInfo = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Processing sheet: " + sheet.getSheetName()); // Debugging line

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        StringBuilder rowData = new StringBuilder();
                        for (int colIndex = 0; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                            Cell cell = row.getCell(colIndex);
                            String cellValue = getCellValueAsString(cell);
                            rowData.append("Column " + colIndex + ": " + cellValue + " | "); // Debugging output
                        }
                        System.out.println("Row " + rowIndex + ": " + rowData.toString()); // Debugging output
                        // Assuming you want to add each row's data to apiInfo; modify as needed
                        apiInfo.put("Row " + rowIndex, rowData.toString());
                    }
                }
            }
        }
        return apiInfo;
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
                    return cell.getDateCellValue().toString(); // Format as needed
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula(); // Handle formulas if needed
            default:
                return "";
        }
    }
}
