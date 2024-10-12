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
            boolean isHeaderRow = true;  // Marks rows like row 7 with column headings
            
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String firstCellValue = getCellValueAsString(row.getCell(0)).trim().toLowerCase();

                // Skip the header row (like row 7)
                if (rowIndex == 7 || isHeaderRowRow(row)) {
                    continue;  // Ignore the header row and move on
                }

                // Start of REQ section, row 11 and onward
                if (firstCellValue.equals("req")) {
                    parsingRequest = true;
                    parsingResponse = false;
                    continue;  // Skip the row marking the start of REQ
                }

                // Start of RES section
                if (firstCellValue.equals("res")) {
                    parsingRequest = false;
                    parsingResponse = true;
                    continue;  // Skip the row marking the start of RES
                }

                // Parse request parameters in REQ section (starting from row 11)
                if (parsingRequest) {
                    ApiDefinition.QueryParameter queryParameter = new ApiDefinition.QueryParameter();
                    queryParameter.setParameterCode(getCellValueAsString(row.getCell(0)));
                    queryParameter.setParameterSegmentLevel(getCellValueAsString(row.getCell(1)));
                    queryParameter.setParameterElementName(getCellValueAsString(row.getCell(2)));
                    queryParameter.setParameterFieldDescription(getCellValueAsString(row.getCell(3)));
                    queryParameter.setParameterNLSField(getCellValueAsString(row.getCell(4)));
                    queryParameter.setParameterTechnicalName(getCellValueAsString(row.getCell(5)));
                    queryParameter.setParameterMandatory(getCellValueAsString(row.getCell(6)));
                    queryParameter.setParameterBusinessDescription(getCellValueAsString(row.getCell(7)));
                    queryParameter.setParameterObjectType(getCellValueAsString(row.getCell(8)));
                    queryParameter.setParameterOccurrenceCount(getCellValueAsString(row.getCell(9)));
                    queryParameter.setParameterSampleValues(getCellValueAsString(row.getCell(10)));
                    queryParameter.setParameterRemarks(getCellValueAsString(row.getCell(11)));

                    if (!isQueryParameterEmpty(queryParameter)) {
                        queryParameters.add(queryParameter);
                    }
                }

                // Parse response parameters in RES section
                if (parsingResponse) {
                    ApiDefinition.ResponsePayload responsePayload = new ApiDefinition.ResponsePayload();
                    responsePayload.setResponseCode(getCellValueAsString(row.getCell(0)));
                    responsePayload.setResponseSegmentLevel(getCellValueAsString(row.getCell(1)));
                    responsePayload.setResponseElementName(getCellValueAsString(row.getCell(2)));
                    responsePayload.setResponseFieldDescription(getCellValueAsString(row.getCell(3)));
                    responsePayload.setResponseNLSField(getCellValueAsString(row.getCell(4)));
                    responsePayload.setResponseTechnicalName(getCellValueAsString(row.getCell(5)));
                    responsePayload.setResponseMandatory(getCellValueAsString(row.getCell(6)));
                    responsePayload.setResponseDescription(getCellValueAsString(row.getCell(7)));
                    responsePayload.setResponseObjectType(getCellValueAsString(row.getCell(8)));
                    responsePayload.setResponseOccurrenceCount(getCellValueAsString(row.getCell(9)));
                    responsePayload.setResponseSampleValues(getCellValueAsString(row.getCell(10)));
                    responsePayload.setResponseRemarks(getCellValueAsString(row.getCell(11)));

                    if (!isResponsePayloadEmpty(responsePayload)) {
                        responsePayloads.add(responsePayload);
                    }
                }
            }

            // Assign collected parameters to the API definition
            if (apiDefinition != null) {
                apiDefinition.setQueryParameters(queryParameters);
                apiDefinition.setResponsePayloads(responsePayloads);
                apiDefinitions.add(apiDefinition);
            }
        }

        return apiDefinitions;
    }

    // Helper method to check for header rows
    private boolean isHeaderRowRow(Row row) {
        // Example check for typical header content, adjust as necessary
        return getCellValueAsString(row.getCell(0)).equalsIgnoreCase("req") ||
               getCellValueAsString(row.getCell(0)).equalsIgnoreCase("res");
    }
 public ApiDefinition handleApiDetails(Sheet sheet) {
        ApiDefinition apiDefinition = new ApiDefinition();

        // Assuming API details like name, version, and description are stored in specific cells
        Row apiDetailsRow = sheet.getRow(1);  // Assuming row 1 contains API metadata
        if (apiDetailsRow != null) {
            apiDefinition.setApiName(getCellValueAsString(apiDetailsRow.getCell(0)));  // Assuming column A has API name
            apiDefinition.setApiVersion(getCellValueAsString(apiDetailsRow.getCell(1)));  // Assuming column B has API version
            apiDefinition.setApiDescription(getCellValueAsString(apiDetailsRow.getCell(2)));  // Assuming column C has API description
        }

        return apiDefinition;
    }

    // Helper method to check if a QueryParameter object is empty (null or empty values in all fields)
    private boolean isQueryParameterEmpty(ApiDefinition.QueryParameter queryParameter) {
        return queryParameter == null ||
               (queryParameter.getParameterCode() == null || queryParameter.getParameterCode().isEmpty()) &&
               (queryParameter.getParameterSegmentLevel() == null || queryParameter.getParameterSegmentLevel().isEmpty()) &&
               (queryParameter.getParameterElementName() == null || queryParameter.getParameterElementName().isEmpty()) &&
               (queryParameter.getParameterFieldDescription() == null || queryParameter.getParameterFieldDescription().isEmpty()) &&
               (queryParameter.getParameterNLSField() == null || queryParameter.getParameterNLSField().isEmpty()) &&
               (queryParameter.getParameterTechnicalName() == null || queryParameter.getParameterTechnicalName().isEmpty()) &&
               (queryParameter.getParameterMandatory() == null || queryParameter.getParameterMandatory().isEmpty()) &&
               (queryParameter.getParameterBusinessDescription() == null || queryParameter.getParameterBusinessDescription().isEmpty()) &&
               (queryParameter.getParameterObjectType() == null || queryParameter.getParameterObjectType().isEmpty()) &&
               (queryParameter.getParameterOccurrenceCount() == null || queryParameter.getParameterOccurrenceCount().isEmpty()) &&
               (queryParameter.getParameterSampleValues() == null || queryParameter.getParameterSampleValues().isEmpty()) &&
               (queryParameter.getParameterRemarks() == null || queryParameter.getParameterRemarks().isEmpty());
    }

    // Helper method to check if a ResponsePayload object is empty (null or empty values in all fields)
    private boolean isResponsePayloadEmpty(ApiDefinition.ResponsePayload responsePayload) {
        return responsePayload == null ||
               (responsePayload.getResponseCode() == null || responsePayload.getResponseCode().isEmpty()) &&
               (responsePayload.getResponseSegmentLevel() == null || responsePayload.getResponseSegmentLevel().isEmpty()) &&
               (responsePayload.getResponseElementName() == null || responsePayload.getResponseElementName().isEmpty()) &&
               (responsePayload.getResponseFieldDescription() == null || responsePayload.getResponseFieldDescription().isEmpty()) &&
               (responsePayload.getResponseNLSField() == null || responsePayload.getResponseNLSField().isEmpty()) &&
               (responsePayload.getResponseTechnicalName() == null || responsePayload.getResponseTechnicalName().isEmpty()) &&
               (responsePayload.getResponseMandatory() == null || responsePayload.getResponseMandatory().isEmpty()) &&
               (responsePayload.getResponseDescription() == null || responsePayload.getResponseDescription().isEmpty()) &&
               (responsePayload.getResponseObjectType() == null || responsePayload.getResponseObjectType().isEmpty()) &&
               (responsePayload.getResponseOccurrenceCount() == null || responsePayload.getResponseOccurrenceCount().isEmpty()) &&
               (responsePayload.getResponseSampleValues() == null || responsePayload.getResponseSampleValues().isEmpty()) &&
               (responsePayload.getResponseRemarks() == null || responsePayload.getResponseRemarks().isEmpty());
    }

    // Helper method to get the cell value as a String
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // The rest of your existing methods: handleApiDetails(), isQueryParameterEmpty(), isResponsePayloadEmpty(), getCellValueAsString()...

}
