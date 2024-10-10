package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiDefinition {
    private String apiUrnNumber;
    private String functionalName;
    private String technicalName;
    private String method;
    private String version;
    private String description;
    private String cbApiId;
    private String sapiUrl;
    private List<QueryParameter> queryParameters;
    private List<ResponsePayload> responsePayloads;

    // Getters and Setters for the above fields...

    // Inner class for Query Parameters
    public static class QueryParameter {
        private String parameterCode;
        private String parameterSegmentLevel;
        private String parameterElementName;
        private String parameterFieldDescription;
        private String parameterNLSField;
        private String parameterTechnicalName;
        private String parameterMandatory; // Note: Correct spelling here
        private String parameterBusinessDescription;
        private String parameterObjectType;
        private String parameterOccurrenceCount;
        private String parameterSampleValues;
        private String parameterRemarks;

        // Getters and Setters for the above fields...
    }

    // Inner class for Response Payloads
    public static class ResponsePayload {
        private String responseCode;
        private String responseSegmentLevel;
        private String responseElementName;
        private String responseFieldDescription;
        private String responseNLSField;
        private String responseTechnicalName;
        private String responseMandatory; // Note: Correct spelling here
        private String responseDescription;
        private String responseObjectType;
        private String responseOccurrenceCount;
        private String responseSampleValues;
        private String responseRemarks;

        // Getters and Setters for the above fields...
    }
}
