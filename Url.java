from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import requests
import re

# Set up Selenium WebDriver (e.g., Chrome, Firefox)
driver = webdriver.Chrome()
wait = WebDriverWait(driver, 10)

# Step 1: Navigate to the Agora platform and log in if necessary
driver.get("https://your-agora-platform-url.com")

# Add login steps if required
# username = driver.find_element(By.ID, "username")
# password = driver.find_element(By.ID, "password")
# username.send_keys("your-username")
# password.send_keys("your-password")
# login_button = driver.find_element(By.ID, "loginButton")
# login_button.click()

# Step 2: Navigate to the Approved Configurations page
approved_configs_button = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[text()='APPROVED CONFIGURATIONS']")))
approved_configs_button.click()

# Step 3: Locate the `teamTechnicalId` element or extract from a relevant HTML element
team_technical_id_element = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".team-technical-id-selector")))  # Adjust selector
team_technical_id = team_technical_id_element.text  # Or use `get_attribute("value")` if in an attribute

# Step 4: Define the API URL for each configuration type
base_url = "https://your-agora-platform-url.com"
config_types = ["approved", "active", "draft"]
api_urls = {config_type: f"{base_url}/{config_type}-aaf-configs?teamTechnicalId={team_technical_id}" for config_type in config_types}

# Get session cookies from Selenium to maintain the authenticated session
session_cookies = driver.get_cookies()
session = requests.Session()
for cookie in session_cookies:
    session.cookies.set(cookie['name'], cookie['value'])

# Step 5: Define regex for name validation
api_pattern = re.compile(r'^cbil-([a-zA-Z]*-){5}[a-zA-Z]*-v[1-9]$')

# Step 6: Fetch data, validate, and store in a dictionary
config_data_map = {}  # Dictionary to hold data for each config type

for config_type, url in api_urls.items():
    response = session.get(url)
    data = response.json()  # Assuming JSON format

    # Create a list to hold entries for this config type
    config_data_map[config_type] = []

    # Parse each configuration entry
    for entry in data:
        # Extract relevant fields
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

# Step 7: Close the Selenium driver as it is no longer needed
driver.quit()

# The data is now stored in `config_data_map`
# Example of accessing data:
for config_type, entries in config_data_map.items():
    print(f"\nConfiguration Type: {config_type}")
    for entry in entries:
        print(entry)
