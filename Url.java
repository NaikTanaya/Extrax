from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import requests
import re

# Step 1: Set up Selenium WebDriver
driver = webdriver.Chrome()
wait = WebDriverWait(driver, 10)

# Step 2: Navigate to the Agora platform and log in if necessary
driver.get("https://your-agora-platform-url.com")

# (Optional) Login steps if required
# username = driver.find_element(By.ID, "username")
# password = driver.find_element(By.ID, "password")
# username.send_keys("your-username")
# password.send_keys("your-password")
# login_button = driver.find_element(By.ID, "loginButton")
# login_button.click()

# Step 3: Wait until the desired page loads
approved_configs_button = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[text()='APPROVED CONFIGURATIONS']")))
approved_configs_button.click()

# Step 4: Locate the `teamTechnicalId` element and retrieve its value
team_technical_id_element = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".team-technical-id-selector")))  # Adjust selector
team_technical_id = team_technical_id_element.text

# Step 5: Retrieve the `agorausertoken` dynamically from cookies
agora_user_token = None
for cookie in driver.get_cookies():
    if cookie['name'] == 'agorausertoken':
        agora_user_token = cookie['value']
        break

# Close Selenium as we have retrieved the necessary data
driver.quit()

# Step 6: Define the API endpoints using the extracted `teamTechnicalId`
base_url = "https://your-agora-platform-url.com/api.v1"
config_types = ["approved", "active", "draft"]
api_urls = {
    config_type: f"{base_url}/{config_type}-aaf-config?teamtechnicalid={team_technical_id}"
    for config_type in config_types
}

# Define headers and dynamically set cookies for the `requests` session
headers = {"Accept": "application/json"}
cookies = {"agorausertoken": agora_user_token}

# Define regex for name validation
api_pattern = re.compile(r'^cbil-([a-zA-Z]*-){5}[a-zA-Z]*-v[1-9]$')

# Step 7: Fetch data for each configuration type using `requests`
config_data_map = {}
for config_type, url in api_urls.items():
    response = requests.get(url, headers=headers, cookies=cookies, verify=False)  # Set verify=False if SSL verification issues occur
    
    if response.status_code == 200:
        data = response.json()
        config_data_map[config_type] = []

        for entry in data:
            entry_data = {
                "Technical ID": entry.get('technicalId'),
                "Name": entry.get('name'),
                "Created At": entry.get('createdAt'),
                "Created By": entry.get('createdBy'),
                "Updated At": entry.get('updatedAt'),
                "Updated By": entry.get('updatedBy'),
                "Name Valid": bool(api_pattern.match(entry.get('name', '')))
            }
            config_data_map[config_type].append(entry_data)
    else:
        print(f"Failed to retrieve data for {config_type}. Status code: {response.status_code}")

# Print the collected configuration data
for config_type, entries in config_data_map.items():
    print(f"\nConfiguration Type: {config_type}")
    for entry in entries:
        print(entry)
