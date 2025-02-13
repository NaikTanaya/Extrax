import yaml

# Define the file path
file_path = "/mnt/data/file-XJtKbYYjacfjm2S8cVdvYT"

try:
    # Open the file with UTF-16 encoding
    with open(file_path, "r", encoding="utf-16") as file:
        yaml_content = yaml.safe_load(file)  # Parse YAML safely

    # Navigate through the structure: kubernetes -> deployment -> Env
    env_data = yaml_content.get("kubernetes", {}).get("deployment", {}).get("Env", {})

    # Extract all authz-related values
    extracted_data = {
        "authz_mode": env_data.get("authz.mode"),
        "authz_ignored": env_data.get("authz.ignored"),
        "authz_url": env_data.get("authz.url"),
        "authz_teammail": env_data.get("authz.teammail"),
        "authz_rule": env_data.get("authz.rule", {}),  # Rule contains nested JSON
    }

    # Extract deeper nested values from authz.rule
    authz_rule = extracted_data["authz_rule"]
    extracted_data.update({
        "owned_by_team": authz_rule.get("ownedbyteam"),
        "jira": authz_rule.get("jira"),
        "auth_resource_service": authz_rule.get("authResourceService"),
        "auth_resources": authz_rule.get("authResources", []),
    })

    # Extract values inside authResources if available
    extracted_resources = []
    for resource in extracted_data["auth_resources"]:
        method = resource.get("method")
        path = resource.get("path")
        groups = resource.get("groups", {})

        extracted_resources.append({
            "method": method,
            "path": path,
            "groups_at_least_one": groups.get("groupsatleastone"),
            "required_internal_token": groups.get("requiredinternaltoken"),
        })

    extracted_data["auth_resources"] = extracted_resources  # Replace with detailed resources

    # Print extracted values
    print(extracted_data)

except yaml.YAMLError as e:
    print("YAML Parsing Error:", e)

except UnicodeDecodeError as e:
    print("Encoding Error:", e)
