from selenium import webdriver
from selenium.webdriver.common.by import By
import requests
import json
import time

# Step 1: Set up Selenium WebDriver with Chrome
driver = webdriver.Chrome()

# Step 2: Navigate to the Agora platform and log in if necessary
driver.get("https://agora-0.hsbc-auth-aaf-cert.svc.aplapg02.shp.apet.pre-prod.aws.cloud.hsbc")

# Optional: Add login steps here
time.sleep(5)  # Adjust timing as necessary

# Step 3: Extract the AgoraUserToken from cookies
cookies = driver.get_cookies()
agora_user_token = None
for cookie in cookies:
    if cookie['name'] == 'AgoraUserToken':
        agora_user_token = cookie['value']
        break

# Check if AgoraUserToken was found
if agora_user_token:
    print("Extracted AgoraUserToken:", agora_user_token)
else:
    print("AgoraUserToken not found in cookies.")
    driver.quit()
    exit()

# Step 4: Define the base URL for the API
base_url = "https://agora-0.hsbc-auth-aaf-cert.svc.aplapg02.shp.apet.pre-prod.aws.cloud.hsbc/api/v1"
config_types = ["approved", "active", "draft"]
api_urls = {config_type: f"{base_url}/{config_type}-aaf-configs" for config_type in config_types}

# Step 5: Set up headers with the token for API requests
headers = {
    "Authorization": f"Bearer {agora_user_token}",
    "Content-Type": "application/json"
}

# Step 6: Make an initial API call to get the payload containing teamTechnicalId
# You need to replace '/initial-endpoint' with the actual endpoint that returns the payload with teamTechnicalId
initial_url = f"{base_url}/initial-endpoint"  # Adjust this endpoint
response = requests.get(initial_url, headers=headers)

if response.status_code == 200:
    initial_data = response.json()
    # Assuming teamTechnicalId is returned in the payload under a specific key
    team_technical_id = initial_data.get('teamTechnicalId', None)
    print("Extracted teamTechnicalId:", team_technical_id)
else:
    print("Failed to fetch initial data to get teamTechnicalId.")
    driver.quit()
    exit()

# Step 7: Update API URLs to include teamTechnicalId if it was extracted
if team_technical_id:
    api_urls_with_id = {config_type: f"{url}?teamTechnicalId={team_technical_id}" for config_type, url in api_urls.items()}
else:
    print("No teamTechnicalId available; cannot construct API URLs.")
    driver.quit()
    exit()

# Step 8: Fetch data from each API URL
config_data_map = {}

for config_type, url in api_urls_with_id.items():
    response = requests.get(url, headers=headers)

    # Check if the request was successful
    if response.status_code == 200:
        data = response.json()  # Assuming JSON format
        config_data_map[config_type] = data
    else:
        print(f"Failed to fetch {config_type} configurations: {response.status_code} - {response.text}")

# Close the Selenium driver
driver.quit()

# Step 9: Display the fetched data
for config_type, entries in config_data_map.items():
    print(f"\nConfiguration Type: {config_type}")
    print(json.dumps(entries, indent=2))
