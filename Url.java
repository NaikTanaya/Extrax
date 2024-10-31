from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
import json
import time
import requests
from chromedriver_py import binary_path  # Automatically fetches the path to the ChromeDriver binary

# Set up Chrome with the chromedriver_py binary path
service = Service(binary_path)
options = webdriver.ChromeOptions()
# Uncomment the following line if you want to run Chrome in headless mode
# options.add_argument('--headless')
options.add_argument('--no-sandbox')
options.add_argument('--disable-dev-shm-usage')

# Enable performance logging
capabilities = webdriver.DesiredCapabilities.CHROME
capabilities["goog:loggingPrefs"] = {"performance": "ALL"}

driver = webdriver.Chrome(service=service, options=options, desired_capabilities=capabilities)

try:
    # Step 1: Load the Agora homepage
    driver.get('https://your-agora-url.com')  # Replace with the actual URL

    login_button = driver.find_element(By.XPATH, '//button[contains(text(), "Login")]')  # Adjust the XPath if necessary
    login_button.click()


    

    # Step 2: Navigate directly to draft/list URL
    driver.get('https://your-agora-url.com/draft/list')  # Replace with the actual draft/list URL

    time.sleep(3)  # Wait for the page to load

    # Step 3: Click on approved-configs span
    approved_configs_span = driver.find_element(By.XPATH, "//span[text()='Approved Configurations']")
    approved_configs_span.click()

    # Step 4: Capture the response for approved-aaf-configs
    time.sleep(3)  # Wait for the request to complete
    logs = driver.get_log("performance")

    teamtechnicalid = None  # Initialize the variable to store the technical ID

   for entry in logs:
    log_message = json.loads(entry["message"])["message"]
    # Ensure this log entry is a network request and has the required fields
    if log_message.get("method") == "Network.requestWillBeSent" and "request" in log_message["params"]:
        request_url = log_message["params"]["request"].get("url", "")
        if 'approved-aaf-configs' in request_url:
            # Extract the teamtechnicalid from the end of the request URL
            teamtechnicalid = request_url.split("teamtechnicalid=")[-1]
            print(f'Technical ID: {teamtechnicalid}')
            break  # Exit the loop if the ID is found

# Retrieve all cookies
cookies = driver.get_cookies()
cookie_value = None

# Find the specific cookie
for cookie in cookies:
    if cookie['name'] == 'your_cookie_name':  # Replace with the actual cookie name you're looking for
        cookie_value = cookie['value']
        print(f'Cookie: {cookie_value}')
        break  # Stop if the cookie is found

if not cookie_value:
    print("Cookie not found.")

    
    # Step 6: If the technical ID is found, make a call to the new URL
    if teamtechnicalid:
        new_url = f'https://your-agora-url.com/approved-aaf-configs?teamtechnicalid={teamtechnicalid}'  # Construct the new URL
        response = requests.get(new_url)  # Make a GET request to the new URL

        # Step 7: Store the JSON response into a dictionary (or map)
        if response.status_code == 200:
            api_response = response.json()  # Parse the JSON response
            # Store the response in a map
            response_data_map = {
                'response': api_response,
                'teamtechnicalid': teamtechnicalid  # Optionally store the technical ID
            }
            print("Stored API Response:", response_data_map)
        else:
            print(f'Failed to retrieve data. Status Code: {response.status_code}')

finally:
    driver.quit()  # Clean up and close the browser
