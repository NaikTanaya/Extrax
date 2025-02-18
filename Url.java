import yaml
import json

file_path = "/mnt/data/file-XJtKbYYjacfjm2S8cVdvYT"

# Read and clean YAML (replace tabs with spaces)
with open(file_path, "r", encoding="utf-8") as file:
    content = file.read().replace("\t", " ").strip()  # Remove tabs and trim spaces

try:
    # Load YAML safely
    yaml_content = yaml.safe_load(content)

    # Navigate to deployment/env
    env_data = yaml_content.get("kubernetes", {}).get("deployment", {}).get("env", {})

    # Extract top-level authz values
    authz_data = {
        "authz_mode": env_data.get("authz.mode"),
        "authz_ignored": env_data.get("authz.ignored"),
        "authz_url": env_data.get("authz.url"),
        "authz_teammail": env_data.get("authz.teamMail"),
        "authz_rule_raw": env_data.get("authz.rule", "").strip(),  # Raw JSON string
    }

    # Parse authz.rule JSON safely
    authz_rule = {}
    if authz_data["authz_rule_raw"]:
        try:
            # Convert single quotes to double quotes (if any exist) and remove newlines
            authz_rule = json.loads(authz_data["authz_rule_raw"].replace("'", "\"").replace("\n", ""))
        except json.JSONDecodeError as e:
            print("\n❌ Error parsing authz.rule JSON:", e)
            authz_rule = {}

    # Extract deeper values inside authz.rule
    extracted_data = {
        "owned_by_team": authz_rule.get("ownedByTeam"),
        "jira": authz_rule.get("jira"),
        "auth_resource_service": authz_rule.get("authResourceService"),
        "auth_resources": [],
    }

    # Process multiple authResources
    for resource in authz_rule.get("authResources", []):
        extracted_data["auth_resources"].append({
            "method": resource.get("method", "N/A"),
            "path": resource.get("path", "N/A"),
            "groups_at_least_one": resource.get("groups", {}).get("groupsAtLeastOne", []),
            "requires_internal_token": resource.get("groups", {}).get("requiresInternalToken", "false"),
        })

    # Merge extracted data
    final_data = {**authz_data, **extracted_data}

    # Print extracted values in readable format
    print("\n✅ Extracted Data:")
    print(json.dumps(final_data, indent=4))

    # Print detailed authResources separately
    print("\n🔹 Stored Auth Resources:")
    for index, res in enumerate(final_data["auth_resources"], start=1):
        print(f"  Resource {index}:")
        print(f"    Method: {res['method']}")
        print(f"    Path: {res['path']}")
        print("    Groups At Least One:")
        for group in res["groups_at_least_one"]:
            print(f"      - {group}")
        print(f"    Requires Internal Token: {res['requires_internal_token']}")
    
    # (Optional) Save extracted data as JSON file
    with open("/mnt/data/extracted_data.json", "w", encoding="utf-8") as f:
        json.dump(final_data, f, indent=4)
        print("\n💾 Extracted data saved to extracted_data.json")

except yaml.YAMLError as e:
    print("\n❌ YAML Parsing Error:", e)
except Exception as e:
    print("\n❌ Unexpected Error:", e)





              
if total_groups > 0:
    # Find and enter the first value
    first_input = driver.find_element(By.XPATH, "//input[contains(@name,'static-value') and contains(@name, '-0')]")
    first_input.send_keys(group_values[0])

# Loop through remaining values and dynamically add them
for i, val in enumerate(group_values[1:], start=1):  # Start from index 1
    try:
        # Click "Add more"
        add_more_button = driver.find_element(By.XPATH, '//span[text()="Add more"]')
        add_more_button.click()
        time.sleep(2)  # Wait for new input to appear

        # Find new input field based on the incrementing number in 'name' attribute
        new_input_xpath = f"//input[contains(@name, 'static-value') and contains(@name, '-{i}')]"
        new_input_field = driver.find_element(By.XPATH, new_input_xpath)
        new_input_field.send_keys(val)

    except Exception as e:
        print(f"Error adding more fields: {e}")
