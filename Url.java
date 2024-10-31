import requests
import re

# Step 1: Make an initial request to load the homepage or draft/list page
initial_url = "https://your-agora-url.com/aaf-configs/drafts/list"  # Replace with actual URL
session = requests.Session()  # Use a session to handle cookies automatically
response = session.get(initial_url)

# Step 2: Extract the AAF-AgoraUserToken from cookies
aaf_token = session.cookies.get('AAF-AgoraUserToken')

if not aaf_token:
    print("AAF-AgoraUserToken not found in cookies!")
    exit()

# Step 3: Manually construct the URL for the approved configurations
# Assuming you already know the teamtechnicalid after clicking the span in the UI.
# You may need to do another request to get the teamtechnicalid dynamically if not known.
# Here's an example of what the request would look like:
teamtechnicalid_pattern = r'teamtechnicalid=([^&]+)'
approved_config_url = f"https://your-agora-url.com/api.v1/approved-aaf-configs?teamtechnicalid={teamtechnicalid}"  # Replace with the actual dynamic ID.

# Step 4: Prepare headers for API requests
headers = {
    'Authorization': f'Bearer {aaf_token}',  # Depending on the API requirements, the token format might differ.
    'Content-Type': 'application/json',
}

# Step 5: Make the API call for Approved Configurations
approved_response = session.get(approved_config_url, headers=headers)

# Check if the response was successful
if approved_response.status_code == 200:
    approved_data = approved_response.json()
    print("Approved Configurations Data:", approved_data)
else:
    print(f"Failed to retrieve approved configurations: {approved_response.status_code} - {approved_response.text}")

# Optional: If you want to get Draft Configurations similarly, construct the URL and repeat
draft_config_url = f"https://your-agora-url.com/api.v1/draft-aaf-config?teamtechnicalid={teamtechnicalid}"  # Replace with the actual dynamic ID.
draft_response = session.get(draft_config_url, headers=headers)

# Check if the response was successful
if draft_response.status_code == 200:
    draft_data = draft_response.json()
    print("Draft Configurations Data:", draft_data)
else:
    print(f"Failed to retrieve draft configurations: {draft_response.status_code} - {draft_response.text}")
