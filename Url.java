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





              
# Step 3: Deselect all currently selected options **only if they are not the selected method**
selected_options = driver.find_elements(By.XPATH, '//mat-option[@aria-selected="true"]')

for option in selected_options:
    option_text = option.find_element(By.XPATH, './/span[contains(@class, "mdc-list-item_primary-text")]').text.strip().upper()

    if option_text != selected_method:  # Only click if it's NOT the desired method
        checkbox = option.find_element(By.XPATH, './/mat-pseudo-checkbox')
        checkbox.click()
        print(f"❌ Deselected: {option_text}")

time.sleep(1)  # Small delay for UI update

# Step 4: Select the correct method only if it's not already selected
try:
    method_option = WebDriverWait(driver, 5).until(
        EC.presence_of_element_located((By.XPATH, f'//mat-option[.//span[contains(text(), "{selected_method}")]]'))
    )
    
    # Check if already selected
    if method_option.get_attribute("aria-selected") != "true":
        checkbox = method_option.find_element(By.XPATH, './/mat-pseudo-checkbox')
        ActionChains(driver).move_to_element(checkbox).click().perform()
        print(f"✅ Selected Method: {selected_method}")
    else:
        print(f"✔️ {selected_method} was already selected, no need to click.")

except Exception as e:
    print(f"⚠️ Error selecting method {selected_method}: {e}")

time.sleep(2)  # Allow UI update before proceeding
