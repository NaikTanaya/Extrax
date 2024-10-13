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

            boolean parsingRequest = false;
            boolean parsingResponse = false;

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String firstCellValue = getCellValueAsString(row.getCell(0)).trim();

                // Check if we are switching to the request or response section
                if (firstCellValue.equalsIgnoreCase("REQ")) {
                    parsingRequest = true;
                    parsingResponse = false;
                    queryParameters = new ArrayList<>();  // Start collecting request parameters
                    continue;
                } else if (firstCellValue.equalsIgnoreCase("RES")) {
                    parsingRequest = false;
                    parsingResponse = true;
                    responsePayloads = new ArrayList<>();  // Start collecting response payloads
                    continue;
                }

                // Skip rows that contain "REQ" or "RES" in a way that indicates they are headers
                if (firstCellValue.equalsIgnoreCase("REQ/RES")) {
                    continue;  // Ignore header row
                }

                // Parse row-wise API metadata (assumed to be at the top)
                if (!parsingRequest && !parsingResponse) {
                    if (apiDefinition == null) {
                        apiDefinition = new ApiDefinition();
                    }
                    handleApiDetails(apiDefinition, row);  // Handling row-wise API details
                }

                // Parse request parameters (only if in the REQ section)
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

                    // Add to the list if at least one field is filled
                    if (!isQueryParameterEmpty(queryParameter)) {
                        queryParameters.add(queryParameter);
                    }
                }

                // Parse response parameters (only if in the RES section)
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

                    // Add to the list if at least one field is filled
                    if (!isResponsePayloadEmpty(responsePayload)) {
                        responsePayloads.add(responsePayload);
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

    // Check if all fields of the query parameter are empty
    private boolean isQueryParameterEmpty(ApiDefinition.QueryParameter queryParameter) {
        return queryParameter.getParameterCode().isEmpty() &&
               queryParameter.getParameterSegmentLevel().isEmpty() &&
               queryParameter.getParameterElementName().isEmpty() &&
               queryParameter.getParameterFieldDescription().isEmpty() &&
               queryParameter.getParameterNLSField().isEmpty() &&
               queryParameter.getParameterTechnicalName().isEmpty() &&
               queryParameter.getParameterMandatory().isEmpty() &&
               queryParameter.getParameterBusinessDescription().isEmpty() &&
               queryParameter.getParameterObjectType().isEmpty() &&
               queryParameter.getParameterOccurrenceCount().isEmpty() &&
               queryParameter.getParameterSampleValues().isEmpty() &&
               queryParameter.getParameterRemarks().isEmpty();
    }

    // Check if all fields of the response payload are empty
    private boolean isResponsePayloadEmpty(ApiDefinition.ResponsePayload responsePayload) {
        return responsePayload.getResponseCode().isEmpty() &&
               responsePayload.getResponseSegmentLevel().isEmpty() &&
               responsePayload.getResponseElementName().isEmpty() &&
               responsePayload.getResponseFieldDescription().isEmpty() &&
               responsePayload.getResponseNLSField().isEmpty() &&
               responsePayload.getResponseTechnicalName().isEmpty() &&
               responsePayload.getResponseMandatory().isEmpty() &&
               responsePayload.getResponseDescription().isEmpty() &&
               responsePayload.getResponseObjectType().isEmpty() &&
               responsePayload.getResponseOccurrenceCount().isEmpty() &&
               responsePayload.getResponseSampleValues().isEmpty() &&
               responsePayload.getResponseRemarks().isEmpty();
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


import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.List;

public String convertToYaml(List<ApiDefinition> apiInfo) {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    options.setIndent(4);
    options.setWidth(120); // Set width for better readability

    Representer representer = new Representer();
    Yaml yaml = new Yaml(representer, options);

    // Constructing the OAS YAML structure
    OpenApiSpec openApi = new OpenApiSpec(); // Replace with your actual OpenApiSpec class
    openApi.setOpenapi("3.0.0");
    openApi.setInfo(new Info(apiInfo.get(0).getFunctionalName(), apiInfo.get(0).getDescription())); // Assuming first item for info
    openApi.setPaths(new Paths());

    for (ApiDefinition apiDefinition : apiInfo) {
        // Create a path item for each API definition
        String sapiUrl = apiDefinition.getSapiUrl(); // Extracted URL for the path
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setOperationId(apiDefinition.getApiUrnNumber());
        operation.setSummary(apiDefinition.getFunctionalName());
        operation.setDescription(apiDefinition.getDescription());

        // Set parameters for the operation
        for (ApiDefinition.QueryParameter queryParameter : apiDefinition.getQueryParameters()) {
            Parameter parameter = new Parameter();
            parameter.setName(queryParameter.getParameterCode());
            parameter.setIn("query");
            parameter.setRequired(queryParameter.getParameterMandatory().equalsIgnoreCase("yes"));
            parameter.setDescription(queryParameter.getParameterFieldDescription());
            operation.addParameter(parameter);
        }

        // Add responses to the operation
        Response response = new Response();
        response.setDescription("Successful response");
        response.setContent(new Content()); // Assuming you have a Content class to define media types
        response.getContent().addMediaType("application/json", createResponseSchema(apiDefinition.getResponsePayloads()));

        // Set response on the operation
        operation.addResponse("200", response);
        operation.addResponse("400", createErrorResponse("Bad Request"));
        operation.addResponse("404", createErrorResponse("Not Found"));
        operation.addResponse("500", createErrorResponse("Internal Server Error"));

        pathItem.setGet(operation); // Assuming a GET request; modify if needed
        openApi.getPaths().put(sapiUrl, pathItem);
    }

    // Convert the OpenApiSpec object to YAML
    StringWriter writer = new StringWriter();
    yaml.dump(openApi, writer);
    return writer.toString(); // Return the YAML representation
}

private Content createResponseSchema(List<ApiDefinition.ResponsePayload> responsePayloads) {
    Content content = new Content();
    // Here, define your schema for response payloads based on the list of responsePayloads
    Schema responseSchema = new Schema();
    responseSchema.setType("object");
    Map<String, Property> properties = new HashMap<>();
    for (ApiDefinition.ResponsePayload responsePayload : responsePayloads) {
        properties.put("responseCode", new StringProperty().description("Response code."));
        properties.put("responseSegmentLevel", new StringProperty().description("Segment level of the response."));
        properties.put("responseElementName", new StringProperty().description("Element name of the response."));
        properties.put("responseFieldDescription", new StringProperty().description("Description of the response field."));
        properties.put("responseNLSField", new StringProperty().description("NLS field associated with the response."));
        properties.put("responseTechnicalName", new StringProperty().description("Technical name of the response."));
        properties.put("responseMandatory", new StringProperty().description("Indicates if the response is mandatory."));
        properties.put("responseDescription", new StringProperty().description("Business description of the response."));
        properties.put("responseObjectType", new StringProperty().description("Object type associated with the response."));
        properties.put("responseOccurrenceCount", new StringProperty().description("Occurrence count of the response."));
        properties.put("responseSampleValues", new StringProperty().description("Sample values for the response."));
        properties.put("responseRemarks", new StringProperty().description("Remarks regarding the response."));
    }
    responseSchema.setProperties(properties);
    content.addMediaType("application/json", new MediaType().schema(responseSchema));
    return content;
}

private Response createErrorResponse(String description) {
    Response response = new Response();
    response.setDescription(description);
    return response;
}

public String convertToYaml(List<ApiDefinition> apiInfo) {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    options.setIndent(4);
    options.setWidth(120); // Set width for better readability

    Representer representer = new Representer();
    Yaml yaml = new Yaml(representer, options);

    // Constructing the OAS YAML structure
    OpenApiSpec openApi = new OpenApiSpec(); 
    openApi.setOpenapi("3.0.0");
    openApi.setInfo(new Info("API Documentation", "This API allows users to interact with various functionalities.", "1.0.0")); 

    for (ApiDefinition apiDefinition : apiInfo) {
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setOperationId(apiDefinition.getApiUrnNumber());
        operation.setSummary(apiDefinition.getFunctionalName());
        operation.setDescription(apiDefinition.getDescription());

        // Set query parameters for the operation
        for (ApiDefinition.QueryParameter queryParameter : apiDefinition.getQueryParameters()) {
            Parameter parameter = new Parameter();
            parameter.setName(queryParameter.getParameterCode());
            parameter.setIn("query");
            parameter.setRequired(queryParameter.getParameterMandatory().equalsIgnoreCase("yes"));
            parameter.setDescription(queryParameter.getParameterFieldDescription());

            // Include all additional query parameter fields
            Schema schema = new Schema();
            schema.setType("string"); // Adjust type if needed (e.g., integer, boolean)
            schema.setDescription(queryParameter.getParameterFieldDescription());
            schema.addExtension("x-segmentLevel", queryParameter.getParameterSegmentLevel());
            schema.addExtension("x-elementName", queryParameter.getParameterElementName());
            schema.addExtension("x-nlsField", queryParameter.getParameterNLSField());
            schema.addExtension("x-technicalName", queryParameter.getParameterTechnicalName());
            schema.addExtension("x-businessDescription", queryParameter.getParameterBusinessDescription());
            schema.addExtension("x-objectType", queryParameter.getParameterObjectType());
            schema.addExtension("x-occurrenceCount", queryParameter.getParameterOccurrenceCount());
            schema.addExtension("x-sampleValues", queryParameter.getParameterSampleValues());
            schema.addExtension("x-remarks", queryParameter.getParameterRemarks());
            
            parameter.setSchema(schema);
            operation.addParameter(parameter);
        }

        // Add detailed response payloads
        ApiResponse response = new ApiResponse();
        response.setDescription("Successful response");
        
        Content content = new Content();
        MediaType mediaType = new MediaType();
        
        Schema responseSchema = new Schema();
        responseSchema.setType("object");
        
        // Add all response fields
        for (ApiDefinition.ResponsePayload responsePayload : apiDefinition.getResponsePayloads()) {
            responseSchema.addProperty("responseCode", new StringProperty().description(responsePayload.getResponseFieldDescription()));
            responseSchema.addProperty("responseSegmentLevel", new StringProperty().description(responsePayload.getResponseSegmentLevel()));
            responseSchema.addProperty("responseElementName", new StringProperty().description(responsePayload.getResponseElementName()));
            responseSchema.addProperty("responseNLSField", new StringProperty().description(responsePayload.getResponseNLSField()));
            responseSchema.addProperty("responseTechnicalName", new StringProperty().description(responsePayload.getResponseTechnicalName()));
            responseSchema.addProperty("responseMandatory", new StringProperty().description(responsePayload.getResponseMandatory()));
            responseSchema.addProperty("responseDescription", new StringProperty().description(responsePayload.getResponseDescription()));
            responseSchema.addProperty("responseObjectType", new StringProperty().description(responsePayload.getResponseObjectType()));
            responseSchema.addProperty("responseOccurrenceCount", new StringProperty().description(responsePayload.getResponseOccurrenceCount()));
            responseSchema.addProperty("responseSampleValues", new StringProperty().description(responsePayload.getResponseSampleValues()));
            responseSchema.addProperty("responseRemarks", new StringProperty().description(responsePayload.getResponseRemarks()));
        }
        
        mediaType.setSchema(responseSchema);
        content.addMediaType("application/json", mediaType);
        
        response.setContent(content);
        operation.addResponse("200", response);
        
        // Error responses (400, 404, 500)
        operation.addResponse("400", createErrorResponse("Bad Request"));
        operation.addResponse("404", createErrorResponse("Not Found"));
        operation.addResponse("500", createErrorResponse("Internal Server Error"));
        
        pathItem.setGet(operation); // Assuming a GET request
        openApi.getPaths().put(apiDefinition.getSapiUrl(), pathItem);
    }

    // Convert the OpenApiSpec object to YAML
    StringWriter writer = new StringWriter();
    yaml.dump(openApi, writer);
    return writer.toString(); // Return the YAML representation
}

}
