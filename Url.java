from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
import requests
import json
import time
import re

# Set the path to your ChromeDriver binary
binary_path = '/path/to/chromedriver'  # Replace with your actual path

# Step 1: Set up Chrome options
options = Options()
options.add_argument("--start-maximized")  # This will maximize the window on startup

# Step 2: Start Selenium WebDriver with options
svc = Service(executable_path=binary_path, options=options)
driver = webdriver.Chrome(service=svc)

# Step 3: Navigate to the Agora Homepage
driver.get('https://your-agora-url.com/')  # Replace with actual Agora homepage URL
time.sleep(3)  # Wait for page load

# Step 4: Click on the button to navigate to 'aaf-configs'
aaf_configs_button = driver.find_element(By.XPATH, '//*[text()="AAF Configs"]')  # Adjust XPath as needed
aaf_configs_button.click()
time.sleep(3)  # Wait for page load

# Step 5: Click "Approved Configurations" to trigger network request for approved data
approved_config_button = driver.find_element(By.XPATH, '//*[text()="Approved Configurations"]')  # Adjust XPath as needed
approved_config_button.click()
time.sleep(3)  # Wait for response to load in the network

# Step 6: Capture network logs to find teamTechnicalId and make requests for approved configurations
logs = driver.get_log("performance")
teamTechnicalId = None
approved_data = None

for entry in logs:
    message = entry['message']
    
    # Locate the request for approved configurations
    if 'approved-aaf-configs' in message:
        # Extract teamTechnicalId from the request URL
        teamTechnicalId_match = re.search(r'teamtechnicalid=([^&]+)', message)
        if teamTechnicalId_match:
            teamTechnicalId = teamTechnicalId_match.group(1)
            # Extract response JSON for approved configurations
            response_data_match = re.search(r'"response":\{"status":200,.*?("body":\{.*?\})', message)
            if response_data_match:
                approved_data = response_data_match.group(1)
            break

# Convert the approved data to a JSON object
if approved_data:
    approved_data_json = json.loads(approved_data.replace('\\', ''))
    # Store Approved Configurations Data
    with open('approved_configs.json', 'w') as file:
        json.dump(approved_data_json, file, indent=2)
else:
    print("Approved Configurations data not found in response")

# Step 7: Click "Draft Configurations" to load draft data
draft_config_button = driver.find_element(By.XPATH, '//*[text()="Draft Configurations"]')  # Adjust XPath as needed
draft_config_button.click()
time.sleep(3)  # Wait for response to load in the network

# Step 8: Capture network logs again to find draft configurations
logs = driver.get_log("performance")
draft_data = None

for entry in logs:
    message = entry['message']
    
    # Locate the request for draft configurations
    if 'draft-aaf-config' in message:
        # Extract response JSON for draft configurations
        response_data_match = re.search(r'"response":\{"status":200,.*?("body":\{.*?\})', message)
        if response_data_match:
            draft_data = response_data_match.group(1)
        break

# Convert the draft data to a JSON object
if draft_data:
    draft_data_json = json.loads(draft_data.replace('\\', ''))
    # Store Draft Configurations Data
    with open('draft_configs.json', 'w') as file:
        json.dump(draft_data_json, file, indent=2)
else:
    print("Draft Configurations data not found in response")

# Cleanup
driver.quit()
