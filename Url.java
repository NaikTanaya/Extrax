openapi: 3.0.0
info:
  title: {{functional_name}}
  version: {{version}}
  description: {{description}}
paths:
  /{{api_urn}}:
    {{method}}:
      summary: {{functional_name}} API
      parameters:
        {{query_parameters}}
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  {{response_payloads}}
components:
  schemas:
    QueryParameter:
      type: object
      properties:
        parameterCode:
          type: string
        # Add other properties as needed
    ResponsePayload:
      type: object
      properties:
        responseCode:
          type: string
        # Add other properties as needed



<!DOCTYPE html>
<html>
<head>
    <title>API YAML</title>
</head>
<body>
    <h1>Generated OpenAPI Specification</h1>
    <pre>{{ oasYaml }}</pre>
</body>
</html>

  
