import yaml
import json

file_path = "/mnt/data/file-XJtKbYYjacfjm2S8cVdvYT"

# Read and clean YAML (replace tabs with spaces)
with open(file_path, "r", encoding="utf-8") as file:
    content = file.read().replace("\t", " ")  # Remove tabs

try:
    # Load YAML
    yaml_content = yaml.safe_load(content)

    # Navigate to deployment/env
    env_data = yaml_content.get("kubernetes", {}).get("deployment", {}).get("env", {})

    # Extract authz-related values
    authz_data = {
        "authz_mode": env_data.get("authz.mode"),
        "authz_ignored": env_data.get("authz.ignored"),
        "authz_url": env_data.get("authz.url"),
        "authz_teammail": env_data.get("authz.teamMail"),
        "authz_rule_raw": env_data.get("authz.rule"),  # Raw JSON string
    }

    # Parse authz.rule JSON from string
    authz_rule = {}
    if authz_data["authz_rule_raw"]:
        try:
            authz_rule = json.loads(authz_data["authz_rule_raw"].replace("'", "\""))  # Convert single quotes to double
        except json.JSONDecodeError as e:
            print("Error parsing authz.rule JSON:", e)

    # Extract deeper values
    extracted_data = {
        "owned_by_team": authz_rule.get("ownedByTeam"),
        "jira": authz_rule.get("jira"),
        "auth_resource_service": authz_rule.get("authResourceService"),
        "auth_resources": authz_rule.get("authResources", []),
    }

    # Extract values inside authResources
    extracted_resources = []
    for resource in extracted_data["auth_resources"]:
        method = resource.get("method")
        path = resource.get("path")
        groups = resource.get("groups", {})

        extracted_resources.append({
            "method": method,
            "path": path,
            "groups_at_least_one": groups.get("groupsAtLeastOne"),
            "requires_internal_token": groups.get("requiresInternalToken"),
        })

    extracted_data["auth_resources"] = extracted_resources  # Replace with detailed resources

    # Merge both extracted dictionaries
    final_data = {**authz_data, **extracted_data}

    # Print extracted values
    print("Extracted Data:")
    print(json.dumps(final_data, indent=4))

except yaml.YAMLError as e:
    print("YAML Parsing Error:", e)
