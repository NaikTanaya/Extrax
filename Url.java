import yaml
import json

# Function to extract values from nested YAML
def extract_authz_values(data):
    authz_mode = data.get("authz.mode")
    authz_ignored = data.get("authz.ignored")
    authz_url = data.get("authz.url")
    authz_teammail = data.get("authz.teammail")
    authz_rule = data.get("authz.rule", {})

    # Extracting nested values from authz.rule
    owned_by_team = authz_rule.get("ownedbyteam")
    jira = authz_rule.get("jira")
    auth_resources = authz_rule.get("authResources", [])
    auth_resource_service = authz_rule.get("authResourceService")

    extracted_resources = []
    for resource in auth_resources:
        method = resource.get("method")
        path = resource.get("path")
        groups = resource.get("groups", {})

        # Extracting values from nested groups
        groups_at_least_one = groups.get("groupsatleastone")
        required_internal_token = groups.get("requiredinternaltoken")

        extracted_resources.append({
            "method": method,
            "path": path,
            "groups_at_least_one": groups_at_least_one,
            "required_internal_token": required_internal_token
        })

    # Storing values in a dictionary
    extracted_data = {
        "authz_mode": authz_mode,
        "authz_ignored": authz_ignored,
        "authz_url": authz_url,
        "authz_teammail": authz_teammail,
        "owned_by_team": owned_by_team,
        "jira": jira,
        "auth_resource_service": auth_resource_service,
        "auth_resources": extracted_resources
    }

    return extracted_data


# Function to read YAML file
def read_yaml_file(file_path):
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            data = yaml.safe_load(file)  # Load YAML data
            return extract_authz_values(data)
    except yaml.YAMLError:
        print("Error: File does not contain valid YAML")
    except Exception as e:
        print(f"Error reading file: {e}")

# Example usage
file_path = "your_file.yaml"  # Replace with your YAML file path
result = read_yaml_file(file_path)

# Display extracted values
if result:
    print(json.dumps(result, indent=4))
