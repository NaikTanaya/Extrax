import os
import platform
import requests
import json
import shutil
import subprocess
import sys
from requests.auth import HTTPBasicAuth

def get_config_file(url, namespace):
    # Manually set user credentials for testing
    user = "your_username"
    pw = "your_password"

    api_url = url + "/apitoken/token/user"
    auth = HTTPBasicAuth(user, pw)

    if platform.system() == "Windows":
        kubectl_token_key = "kubectl Windows Command"
        oidc_config = f'C:\\Users\\{user}\\.kube\\config'
        namespace_config = f'C:\\Users\\{user}\\.kube\\{namespace}.config'
    else:
        kubectl_token_key = "kubectl Command"

    try:
        # Step 1: Call the OIDC API
        print(f"Fetching token from: {api_url}")
        response = requests.get(api_url, auth=auth)
        
        if response.status_code != 200:
            print(f"Error: API returned status code {response.status_code}")
            print("Response:", response.text)
            return

        # Step 2: Parse JSON response
        response_text = response.text
        print("API Response:", response_text)  # Debugging step
        loaded_data = json.loads(response_text)

        # Step 3: Extract the correct kubectl command
        config_cmd = loaded_data.get("token")
        if not config_cmd:
            print("Error: 'token' field not found in API response.")
            return
        
        kubectl_token_command = config_cmd.get(kubectl_token_key)
        if not kubectl_token_command:
            print(f"Error: Key '{kubectl_token_key}' not found in API response.")
            return
        
        print(f"Kubectl command extracted: {kubectl_token_command}")

        # Step 4: Execute the kubectl command
        print("Executing kubectl token command...")
        p = subprocess.Popen(["powershell.exe", kubectl_token_command], stdout=sys.stdout)
        p.communicate()

        # Step 5: Copy OIDC config
        print(f"Copying config from {oidc_config} to {namespace_config}...")
        shutil.copy(oidc_config, namespace_config)

        print("✅ Test completed successfully!")
        return namespace_config

    except Exception as err:
        print("Error:", err)

# --- Run the test ---
if __name__ == "__main__":
    test_url = "https://oidc.ikp301x.cloud.hk.hsbc/"
    test_namespace = "shpx-cbsa-hub-sct"
    
    print("=== Running get_config_file() Test ===")
    get_config_file(test_url, test_namespace)
