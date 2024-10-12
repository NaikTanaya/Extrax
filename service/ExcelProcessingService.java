import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ExcelProcessingService {

    // Main function to process the Excel file input stream
    public Map<String, Object> processExcelFile(InputStream inputStream) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> apiDetails = new HashMap<>(); // To store API details
        List<Map<String, String>> requestParameters = new ArrayList<>();
        List<Map<String, String>> responseParameters = new ArrayList<>();

        try {
            // Create workbook from the input stream
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming we are working with the first sheet

            boolean isRequestSection = false;
            boolean isResponseSection = false;

            Iterator<Row> rowIterator = sheet.iterator();
            
            // First handle API details from the top rows
            handleAPIDetails(sheet, apiDetails);

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Check if the row is part of the "Request" or "Response" section
                String sectionHeader = getCellValueAsString(row.getCell(0));

                if (sectionHeader != null) {
                    if (sectionHeader.equalsIgnoreCase("Request")) {
                        isRequestSection = true;
                        isResponseSection = false;
                        continue;
                    } else if (sectionHeader.equalsIgnoreCase("Response")) {
                        isRequestSection = false;
                        isResponseSection = true;
                        continue;
                    }
                }

                // Process each section independently
                if (isRequestSection) {
                    Map<String, String> reqParam = extractRowData(row);
                    if (!reqParam.isEmpty()) {
                        requestParameters.add(reqParam);
                    }
                } else if (isResponseSection) {
                    Map<String, String> resParam = extractRowData(row);
                    if (!resParam.isEmpty()) {
                        responseParameters.add(resParam);
                    }
                }
            }

            response.put("apiDetails", apiDetails); // Add API details to response
            response.put("requestParameters", requestParameters);
            response.put("responseParameters", responseParameters);
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error processing Excel file: " + e.getMessage());
        }

        return response;
    }

    // Function to extract and handle API details from the top rows
    private void handleAPIDetails(Sheet sheet, Map<String, String> apiDetails) {
        for (int i = 1; i <= 13; i++) { // Assuming first 13 rows have API details
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String key = getCellValueAsString(row.getCell(0));
            String value = getCellValueAsString(row.getCell(1));

            if (key != null && !key.trim().isEmpty()) {
                apiDetails.put(key.trim(), value.trim());
            }
        }
    }

    // Helper method to extract data from a row into a map for request/response sections
    private Map<String, String> extractRowData(Row row) {
        Map<String, String> rowData = new HashMap<>();

        String parameterCode = getCellValueAsString(row.getCell(0));
        String segmentLevel = getCellValueAsString(row.getCell(1));
        String elementName = getCellValueAsString(row.getCell(2));
        String fieldDescription = getCellValueAsString(row.getCell(3));
        String mandatory = getCellValueAsString(row.getCell(4));
        String objectType = getCellValueAsString(row.getCell(5));

        if (parameterCode != null && !parameterCode.isEmpty()) {
            rowData.put("parameterCode", parameterCode);
            rowData.put("segmentLevel", segmentLevel);
            rowData.put("elementName", elementName);
            rowData.put("fieldDescription", fieldDescription);
            rowData.put("mandatory", mandatory);
            rowData.put("objectType", objectType);
        }

        return rowData;
    }

    // Helper method to get cell value as string
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
