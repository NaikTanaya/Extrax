import requests
import re
from selenium import webdriver
from selenium.webdriver.common.by import By
import time

# Function to get the teamtechnicalid from the approved configurations page
def get_teamtechnicalid():
    # Initialize the WebDriver
    driver = webdriver.Chrome()  # You can use webdriver-manager if desired
    try:
        # Step 1: Load the Agora homepage
        driver.get('https://your-agora-url.com/')  # Replace with the actual URL
        time.sleep(3)  # Wait for the page to load

        # Step 2: Click on the "Approved Configurations" span
        approved_configs_span = driver.find_element(By.XPATH, '//*[text()="Approved Configurations"]')
        approved_configs_span.click()
        time.sleep(3)  # Wait for the network requests to settle

        # Step 3: Capture the teamtechnicalid from the request URL
        teamtechnicalid = None
        for request in driver.requests:
            if 'approved-aaf-configs' in request.url:
                # Capture teamtechnicalid from the URL
                match = re.search(r'teamtechnicalid=([^&]+)', request.url)
                if match:
                    teamtechnicalid = match.group(1)
                    print(f"Team Technical ID: {teamtechnicalid}")
                    break

        return teamtechnicalid

    finally:
        # Close the browser
        driver.quit()

# Function to get JSON response from the endpoint
def get_json_response(teamtechnicalid):
    url = f'https://your-agora-url.com/approved-aaf-configs?teamtechnicalid={teamtechnicalid}'  # Replace with actual URL
    response = requests.get(url)
    
    # Check for a successful response
    if response.status_code == 200:
        return response.json()  # Return JSON response
    else:
        print(f"Failed to retrieve data: {response.status_code}")
        return None

# Main script
if __name__ == '__main__':
    # Step 1: Get the teamtechnicalid
    teamtechnicalid = get_teamtechnicalid()

    # Step 2: Use the teamtechnicalid to get the JSON response
    if teamtechnicalid:
        json_response = get_json_response(teamtechnicalid)
        print("Approved Configurations JSON Response:")
        print(json_response)
