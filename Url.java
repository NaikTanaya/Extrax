import java.util.HashMap;
import java.util.Map;

public class OpenApiSpec {

    private String openapi;
    private Info info;
    private Map<String, PathItem> paths = new HashMap<>();

    // Getters and Setters
    public String getOpenapi() {
        return openapi;
    }

    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Map<String, PathItem> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, PathItem> paths) {
        this.paths = paths;
    }

    // Inner classes for Info, PathItem, Operation, etc.
    public static class Info {
        private String title;
        private String description;
        private String version;

        public Info(String title, String description, String version) {
            this.title = title;
            this.description = description;
            this.version = version;
        }

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class PathItem {
        private Operation get;

        // Getters and Setters
        public Operation getGet() {
            return get;
        }

        public void setGet(Operation get) {
            this.get = get;
        }
    }

    public static class Operation {
        private String operationId;
        private String summary;
        private String description;
        private List<Parameter> parameters = new ArrayList<>();
        private Map<String, ApiResponse> responses = new HashMap<>();

        // Getters and Setters
        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public Map<String, ApiResponse> getResponses() {
            return responses;
        }

        public void setResponses(Map<String, ApiResponse> responses) {
            this.responses = responses;
        }

        // Method to add a parameter
        public void addParameter(Parameter parameter) {
            this.parameters.add(parameter);
        }

        // Method to add a response
        public void addResponse(String statusCode, ApiResponse response) {
            this.responses.put(statusCode, response);
        }
    }

    public static class Parameter {
        private String name;
        private String in;
        private String description;
        private boolean required;
        private Schema schema;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIn() {
            return in;
        }

        public void setIn(String in) {
            this.in = in;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Schema getSchema() {
            return schema;
        }

        public void setSchema(Schema schema) {
            this.schema = schema;
        }
    }

    public static class Schema {
        private String type;
        private String description;
        private Map<String, Object> extensions = new HashMap<>();

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getExtensions() {
            return extensions;
        }

        public void addExtension(String key, Object value) {
            this.extensions.put(key, value);
        }
    }

    public static class ApiResponse {
        private String description;
        private Content content;

        // Getters and Setters
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }

    public static class Content {
        private Map<String, MediaType> mediaTypes = new HashMap<>();

        // Getters and Setters
        public Map<String, MediaType> getMediaTypes() {
            return mediaTypes;
        }

        public void addMediaType(String mediaType, MediaType type) {
            this.mediaTypes.put(mediaType, type);
        }
    }

    public static class MediaType {
        private Schema schema;

        // Getters and Setters
        public Schema getSchema() {
            return schema;
        }

        public void setSchema(Schema schema) {
            this.schema = schema;
        }
    }
}
