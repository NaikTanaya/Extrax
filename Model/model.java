package com.example.api.model;

import java.util.HashMap;
import java.util.Map;

public class ApiDefinition {
    private String apiName; // e.g., "API Name"
    private Map<String, String> attributes; // Key-value pairs for other attributes

    public ApiDefinition(String apiName) {
        this.apiName = apiName;
        this.attributes = new HashMap<>();
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }
}
