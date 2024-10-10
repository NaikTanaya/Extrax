package com.example.api.service;

import com.example.api.model.ApiDefinition;
import org.apache.poi.ss.usermodel.*;
import org.yaml.snakeyaml.Yaml;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelProcessingService {

    public List<ApiDefinition> parseExcelFile(MultipartFile file) throws IOException {
        List<ApiDefinition> apiDefinitions = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Processing sheet: " + sheet.getSheetName()); // Debugging line

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Start at 1 if the first row is headers
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) { // Check if row is not null
                        // Ensure the first cell exists
                        Cell apiNameCell = row.getCell(0);
                        if (apiNameCell != null) {
                            ApiDefinition apiDefinition = new ApiDefinition(getCellValueAsString(apiNameCell)); // Assuming the API name is in the first column
                            
                            for (int colIndex = 1; colIndex < row.getPhysicalNumberOfCells(); colIndex++) { // Start at 1 to skip the API name
                                Cell keyCell = sheet.getRow(0).getCell(colIndex); // Get the header for the column
                                if (keyCell != null) {
                                    String key = getCellValueAsString(keyCell); // Get header value
                                    String value = getCellValueAsString(row.getCell(colIndex));
                                    apiDefinition.setAttribute(key, value);
                                } else {
                                    System.out.println("Header cell is null at column index: " + colIndex);
                                }
                            }

                            apiDefinitions.add(apiDefinition);
                        } else {
                            System.out.println("API Name cell is null at row index: " + rowIndex);
                        }
                    } else {
                        System.out.println("Row is null at index: " + rowIndex);
                    }
                }
            }
        }
        return apiDefinitions;
    }

    public String convertToYaml(List<ApiDefinition> apiDefinitions) {
        Yaml yaml = new Yaml();
        return yaml.dump(apiDefinitions);
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
