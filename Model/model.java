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


import pandas as pd
import yaml

def extract_api_details(file_path):
    # Load the Excel file
    sheet_data = pd.read_excel(file_path, header=None)
    
    api_details = {}
    headers = []  # To store header keys for request/response parameters
    request_parameters = []
    response_properties = {}
    in_request_section = False  # Flag to indicate if we are in the request section

    # Iterate over the rows of the sheet
    for index, row in sheet_data.iterrows():
        # Skip empty rows
        if row.isnull().all():
            continue

        # If we are not in the request section, extract API details
        if not in_request_section:
            # Check for the end of API details when "request" is encountered
            if any('request' in str(cell).lower() for cell in row):
                in_request_section = True  # Set the flag to true
                continue  # Move to next row after finding "request"

            # Extract API details as key-value pairs
            key = row[0]
            value = row[1] if len(row) > 1 else None
            if pd.notna(value):  # Only store non-null values
                api_details[key] = value
                print(f"API Detail: {key} = {value}")  # Debug print for API details
        
        # If we are in the request section, handle headers and parameters
        else:
            # If the row contains "REQ/RSP" as a header
            if any('REQ/RSP' in str(cell) for cell in row):
                headers = row.dropna().tolist()  # Store all non-null values in this row as headers
                continue  # Move to the next row after storing headers
            
            # If the row has 'REQ'
            if any('REQ' in str(cell) for cell in row):
                # Create a key-value mapping using stored headers
                key_value_dict = {headers[i]: row[i] for i in range(len(headers)) 
                                  if i < len(row) and pd.notna(row[i])}
                request_parameters.append(key_value_dict)
                print(f"Request Parameter: {key_value_dict}")  # Debug print for request parameters
            
            # If the row has 'RSP'
            if any('RSP' in str(cell) for cell in row):
                # Create a key-value mapping using stored headers
                key_value_dict = {headers[i]: row[i] for i in range(len(headers)) 
                                  if i < len(row) and pd.notna(row[i])}
                response_properties.update(key_value_dict)
                print(f"Response Property: {key_value_dict}")  # Debug print for response properties

    # Generate OAS YAML structure
    oas = {
        'openapi': '3.0.0',
        'info': {
            'title': api_details.get('api functional name', 'API Title'),
            'version': '1.0.0',
            'description': api_details.get('api description', 'API Description'),
        },
        'paths': {
            f"/{api_details.get('api urn', 'default')}/": {
                api_details.get('api type', 'get').lower(): {
                    'summary': api_details.get('api summary', 'No summary provided'),
                    'description': api_details.get('api description', 'No description provided'),
                    'operationId': api_details.get('api technical name', 'defaultOperationId'),
                    'parameters': [
                        {
                            'name': param['api name'],
                            'in': 'query',
                            'required': param.get('NLS FIELD YES/NO', 'n') == 'y',
                            'schema': {
                                'type': param['OBJECT TYPE']
                            }
                        } for param in request_parameters if 'api name' in param
                    ],
                    'responses': {
                        '200': {
                            'description': 'Successful response',
                            'content': {
                                'application/json': {
                                    'schema': {
                                        'type': 'object',
                                        'properties': {
                                            rsp_key: {
                                                'type': 'array' if response_properties.get(rsp_key) == 'Array' else 'string'
                                            } for rsp_key in response_properties.keys()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    # Print the generated OAS for verification
    print("\nGenerated OAS YAML:")
    print(yaml.dump(oas, sort_keys=False))

    return api_details

# Replace 'demo.xlsx' with the actual file path of your Excel file
file_path = 'C:\\Users\\sai\\Downloads\\demo.xlsx'
api_details = extract_api_details(file_path)
