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

    // Getters and Setters
    public String getApiUrnNumber() { return apiUrnNumber; }
    public void setApiUrnNumber(String apiUrnNumber) { this.apiUrnNumber = apiUrnNumber; }

    public String getFunctionalName() { return functionalName; }
    public void setFunctionalName(String functionalName) { this.functionalName = functionalName; }

    public String getTechnicalName() { return technicalName; }
    public void setTechnicalName(String technicalName) { this.technicalName = technicalName; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCbApiId() { return cbApiId; }
    public void setCbApiId(String cbApiId) { this.cbApiId = cbApiId; }

    public String getSapiUrl() { return sapiUrl; }
    public void setSapiUrl(String sapiUrl) { this.sapiUrl = sapiUrl; }

    public List<QueryParameter> getQueryParameters() { return queryParameters; }
    public void setQueryParameters(List<QueryParameter> queryParameters) { this.queryParameters = queryParameters; }

    public List<ResponsePayload> getResponsePayloads() { return responsePayloads; }
    public void setResponsePayloads(List<ResponsePayload> responsePayloads) { this.responsePayloads = responsePayloads; }

    // Nested classes for QueryParameter and ResponsePayload
    public static class QueryParameter {
        private String name;
        private String description;
        private String type;
        private boolean required;
        private String example;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }
    }

    public static class ResponsePayload {
        private String statusCode;
        private String description;
        private String schema;

        // Getters and Setters
        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
    }
}
