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
                System.out.println("Processing sheet: " + sheet.getSheetName());

                if (sheet.getPhysicalNumberOfRows() == 0) {
                    System.out.println("Sheet " + sheet.getSheetName() + " is empty. Skipping.");
                    continue; // Skip empty sheets
                }

                // Iterate through each row, starting from the second row (index 1) if the first row is headers
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);

                    // Check if the row is null
                    if (row == null) {
                        System.out.println("Row " + rowIndex + " is null, skipping.");
                        continue; // Skip this iteration if the row is null
                    }

                    // Log the number of cells in the row
                    System.out.println("Row " + rowIndex + " has " + row.getPhysicalNumberOfCells() + " cells.");

                    // Ensure the first cell exists for API name
                    Cell apiNameCell = row.getCell(0);
                    if (apiNameCell != null) {
                        ApiDefinition apiDefinition = new ApiDefinition(getCellValueAsString(apiNameCell));

                        // Process each cell in the row, starting from index 1 to skip API name
                        for (int colIndex = 1; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
                            Cell keyCell = sheet.getRow(0).getCell(colIndex); // Get header for the column
                            Cell valueCell = row.getCell(colIndex); // Get the value cell

                            if (keyCell != null && valueCell != null) {
                                String key = getCellValueAsString(keyCell);
                                String value = getCellValueAsString(valueCell);
                                apiDefinition.setAttribute(key, value);
                            } else {
                                System.out.println("Header cell or value cell is null at row " + rowIndex + ", column " + colIndex);
                            }
                        }

                        apiDefinitions.add(apiDefinition);
                    } else {
                        System.out.println("API Name cell is null at row index: " + rowIndex);
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
