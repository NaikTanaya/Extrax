package com.example.api.model;

import java.util.List;
import java.util.Map;

public class ApiDefinition {
    private String apiName;
    private String description;
    private String requestMethod;
    private Map<String, String> parameters; // Key-value pairs for parameters
    private Map<String, String> errorSchema; // Error schema, if available
    private List<String> responses; // List of response information

    // Constructor
    public ApiDefinition(String apiName, String description, String requestMethod, 
                         Map<String, String> parameters, Map<String, String> errorSchema, List<String> responses) {
        this.apiName = apiName;
        this.description = description;
        this.requestMethod = requestMethod;
        this.parameters = parameters;
        this.errorSchema = errorSchema;
        this.responses = responses;
    }

    // Getters and setters
    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getErrorSchema() {
        return errorSchema;
    }

    public void setErrorSchema(Map<String, String> errorSchema) {
        this.errorSchema = errorSchema;
    }

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    @Override
    public String toString() {
        return "ApiDefinition{" +
                "apiName='" + apiName + '\'' +
                ", description='" + description + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", parameters=" + parameters +
                ", errorSchema=" + errorSchema +
                ", responses=" + responses +
                '}';
    }
}
