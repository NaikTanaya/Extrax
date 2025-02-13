import re
import json

# Function to extract key-value pairs from plain text
def extract_authz_values(text):
    # Regular expressions to match key-value pairs
    authz_mode = re.search(r"authz\.mode:\s*(\S+)", text)
    authz_ignored = re.search(r"authz\.ignored:\s*(\S+)", text)
    authz_url = re.search(r"authz\.url:\s*(\S+)", text)
    authz_teammail = re.search(r"authz\.teammail:\s*(\S+)", text)
    
    # Extract JSON-like part for authz.rule
    authz_rule_match = re.search(r"authz\.rule:\s*({.*?})", text, re.DOTALL)
    authz_rule = json.loads(authz_rule_match.group(1)) if authz_rule_match else {}

    # Extract nested values from authz.rule
    owned_by_team = authz_rule.get("ownedbyteam")
    jira = authz_rule.get("jira")
    auth_resources = authz_rule.get("authResources", [])
    auth_resource_service = authz_rule.get("authResourceService")

    extracted_resources = []
    for resource in auth_resources:
        method = resource.get("method")
        path = resource.get("path")
        groups = resource.get("groups", {})

        # Extract values from nested groups
        groups_at_least_one = groups.get("groupsatleastone")
        required_internal_token = groups.get("requiredinternaltoken")

        extracted_resources.append({
            "method": method,
            "path": path,
            "groups_at_least_one": groups_at_least_one,
            "required_internal_token": required_internal_token
        })

    # Store extracted values
    extracted_data = {
        "authz_mode": authz_mode.group(1) if authz_mode else None,
        "authz_ignored": authz_ignored.group(1) if authz_ignored else None,
        "authz_url": authz_url.group(1) if authz_url else None,
        "authz_teammail": authz_teammail.group(1) if authz_teammail else None,
        "owned_by_team": owned_by_team,
        "jira": jira,
        "auth_resource_service": auth_resource_service,
        "auth_resources": extracted_resources
    }

    return extracted_data

# Function to read a plain text file
def read_plain_text_file(file_path):
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            text = file.read()  # Read full content
            return extract_authz_values(text)
    except Exception as e:
        print(f"Error reading file: {e}")

# Example usage
file_path = "your_file.txt"  # Replace with your file path
result = read_plain_text_file(file_path)

# Display extracted values
if result:
    print(json.dumps(result, indent=4))
