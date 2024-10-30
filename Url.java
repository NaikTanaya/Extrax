import requests
import re

# Define base URL and team technical ID
base_url = "https://your-agora-platform-url.com/api.v1"  # Use the actual base URL
team_technical_id = "your-team-technical-id"  # Set your actual team technical ID
config_types = ["approved", "active", "draft"]

# Define the API endpoints for each configuration type
api_urls = {
    config_type: f"{base_url}/{config_type}-aaf-config?teamtechnicalid={team_technical_id}"
    for config_type in config_types
}

# Define headers and cookies
headers = {
    "Accept": "application/json",  # or other headers if needed
    "User-Agent": "your-user-agent-string"  # Optionally specify if needed
}
cookies = {
    "agorausertoken": "your-agora-user-token"  # Replace with the actual token from network tab
}

# Define regex for name validation
api_pattern = re.compile(r'^cbil-([a-zA-Z]*-){5}[a-zA-Z]*-v[1-9]$')

# Dictionary to store configuration data for each type
config_data_map = {}

# Fetch data for each configuration type
for config_type, url in api_urls.items():
    response = requests.get(url, headers=headers, cookies=cookies)
    
    # Check if the request was successful
    if response.status_code == 200:
        data = response.json()  # Parse the JSON response
        
        # List to hold entries for this configuration type
        config_data_map[config_type] = []
        
        # Iterate over each entry in the data
        for entry in data:
            technical_id = entry.get('technicalId')
            name = entry.get('name')
            created_at = entry.get('createdAt')
            created_by = entry.get('createdBy')
            updated_at = entry.get('updatedAt', None)
            updated_by = entry.get('updatedBy', None)

            # Validate name syntax
            name_valid = bool(api_pattern.match(name))

            # Store the entry data in a dictionary
            entry_data = {
                "Technical ID": technical_id,
                "Name": name,
                "Created At": created_at,
                "Created By": created_by,
                "Updated At": updated_at,
                "Updated By": updated_by,
                "Name Valid": name_valid
            }

            # Append the entry data to the list for the config type
            config_data_map[config_type].append(entry_data)
    else:
        print(f"Failed to retrieve data for {config_type}. Status code: {response.status_code}")

# Output the collected data
for config_type, entries in config_data_map.items():
    print(f"\nConfiguration Type: {config_type}")
    for entry in entries:
        print(entry)
