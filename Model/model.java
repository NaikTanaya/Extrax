openapi: 3.0.0
info:
  title: "${functionalName}"
  version: "${version}"
  description: "${description}"
paths:
  /${urn}/:
    ${method}:
      summary: "${functionalName} Summary"
      operationId: "${technicalName}"
      description: "${description}"
      parameters:
        ${parameters}
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                ${requestBody}
      responses:
        '200':
          description: "Successful response"
          content:
            application/json:
              schema:
                type: object
                properties:
                  ${responseProperties}
