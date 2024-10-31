from seleniumwire import webdriver  # Import from selenium-wire to capture network requests
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
import json
import time
import re

# Step 1: Set up Chrome WebDriver with WebDriver Manager
options = Options()
options.add_argument('--headless')  # Run in headless mode (no GUI)
options.add_argument('--no-sandbox')
options.add_argument('--disable-dev-shm-usage')
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)

# Step 2: Navigate to Agora Homepage
driver.get('https://your-agora-url.com/')  # Replace with the actual Agora homepage URL
time.sleep(3)  # Wait for the page to load

# Step 3: Navigate to Drafts List URL
driver.get('https://your-agora-url.com/aaf-configs/drafts/list')  # This page loads but doesn't give required details
time.sleep(3)

# Step 4: Navigate to Approved List URL
driver.get('https://your-agora-url.com/aaf-configs/approved/list')  # This will allow capturing the necessary data
time.sleep(3)

# Step 5: Intercept network requests for approved configurations
approved_data = None
headers_info = None
teamtechnicalid = None

# Iterate through all captured network requests
for request in driver.requests:
    if 'approved-aaf-configs' in request.url:
        # Capture teamtechnicalid from the URL
        match = re.search(r'teamtechnicalid=([^&]+)', request.url)
        if match:
            teamtechnicalid = match.group(1)  # Extract the dynamic value of teamtechnicalid

        # Capture dynamic values from headers
        headers_info = {
            "url": request.url,
            "method": request.method,
            "path": request.path,
            "cookies": request.headers.get('Cookie', ''),
            "user-agent": request.headers.get('User-Agent', ''),
            "x-hsbc-request-correlation-id": request.headers.get('x-hsbc-request-correlation-id', ''),
        }
        
        # Capture response JSON for approved configurations
        if request.response:
            approved_data = request.response.body.decode('utf-8')  # Decode response body
        break  # Exit loop after capturing the necessary request

# Step 6: Parse JSON for approved configurations if available and save
if approved_data:
    approved_data_json = json.loads(approved_data)  # Convert response body to JSON
    with open('approved_configs.json', 'w') as file:
        json.dump(approved_data_json, file, indent=2)  # Save JSON to file
else:
    print("Approved Configurations data not found.")

# Step 7: Click "Draft Configurations" button to load draft configurations data
draft_config_button = driver.find_element(By.XPATH, '//*[text()="Draft Configurations"]')
draft_config_button.click()  # Simulate clicking the button
time.sleep(3)  # Wait for the new page to load

# Step 8: Capture network requests for draft configurations
draft_data = None

for request in driver.requests:
    # Check for draft configurations and ensure it contains the same teamtechnicalid
    if 'draft-aaf-config' in request.url and teamtechnicalid in request.url:
        # Capture response JSON for draft configurations
        if request.response:
            draft_data = request.response.body.decode('utf-8')  # Decode response body
        break  # Exit loop after capturing the necessary request

# Step 9: Parse JSON for draft configurations if available and save
if draft_data:
    draft_data_json = json.loads(draft_data)  # Convert response body to JSON
    with open('draft_configs.json', 'w') as file:
        json.dump(draft_data_json, file, indent=2)  # Save JSON to file
else:
    print("Draft Configurations data not found.")

# Step 10: Cleanup
driver.quit()  # Close the WebDriver

# Print captured headers (Optional)
if headers_info:
    print("Captured Headers Information:", headers_info)
