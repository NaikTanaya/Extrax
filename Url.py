import pandas as pd

def extract_api_details(file_path):
    # Load the Excel file
    sheet_data = pd.read_excel(file_path, header=None)
    
    api_details = {}
    headers = []  # To store header keys for request/response parameters
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
            key = row[2]
            value = row[3] if len(row) > 1 else None
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
                print(f"Request Parameter: {key_value_dict}")  # Debug print for request parameters
            
            # If the row has 'RSP'
            if any('RSP' in str(cell) for cell in row):
                # Create a key-value mapping using stored headers
                key_value_dict = {headers[i]: row[i] for i in range(len(headers)) 
                                  if i < len(row) and pd.notna(row[i])}
                print(f"Response Property: {key_value_dict}")  # Debug print for response properties

    return api_details

# Replace 'demo.xlsx' with the actual file path of your Excel file
file_path = 'C:\\Users\\sai\\Downloads\\demo.xlsx'
api_details = extract_api_details(file_path)
