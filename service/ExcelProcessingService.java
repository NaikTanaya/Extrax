package com.example.api.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelProcessingService {

    public List<List<String>> parseExcelFile(MultipartFile file) throws IOException {
        List<List<String>> apiInfo = new ArrayList<>();  // List of rows, each row is a List of cell values

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Processing sheet: " + sheet.getSheetName());  // Debugging line

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        List<String> rowData = new ArrayList<>();  // Store cell values of each row
                        for (int colIndex = 0; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                            Cell cell = row.getCell(colIndex);
                            String cellValue = getCellValueAsString(cell);
                            rowData.add(cellValue);  // Add cell value to the row data
                        }
                        apiInfo.add(rowData);  // Add row data to the list of rows
                        System.out.println("Row " + rowIndex + ": " + rowData);  // Debugging output
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
