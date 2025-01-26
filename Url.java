import subprocess
import sys
import os
import getpass
import requests
from requests.auth import HTTPBasicAuth

# Define clusters and their namespaces
ikp_clusters = {
    'HK-AZX-301': ('https://301.ikp.example.com', ['shpx-cbsa-hub-cert', 'shpx-cbsa-hub-sct']),
    'HK-AZR-101': ('https://101.ikp.example.com', ['shpx-cbsa-hub-cert', 'shpx-cbsa-hub-sct']),
    'HK-AZR-102': ('https://102.ikp.example.com', ['shpx-cbsa-hub-cert', 'shpx-cbsa-hub-sct'])
}

# Function to verify the cluster and namespaces
def verify_cluster_and_namespaces(cluster_name):
    if cluster_name in ikp_clusters:
        return ikp_clusters[cluster_name]  # URL and list of namespaces
    else:
        print("Error: Cluster not found.")
        print("Please provide one of the following clusters:")
        for key in ikp_clusters.keys():
            print(key)
        exit()

# Function to get the bootstrap server URL from the cluster
def get_namespace_bootstrap_server(cluster_name):
    cluster_info = verify_cluster_and_namespaces(cluster_name)
    if cluster_info:
        return cluster_info[0]  # Returns the URL (first element in tuple)
    else:
        print("Error: No bootstrap server found for this cluster.")
        exit()

# Function to get the configuration file (setup for kubectl)
def get_config_file(url, namespace):
    try:
        user = os.environ.get('USERNAME', 'default_user')
        api_url = f'{url}/apitoken/token/user'
        
        # Ask for credentials based on the namespace environment (prod or preprod)
        if 'prod' in namespace:
            password = os.environ.get('PASSWORD')
            auth = HTTPBasicAuth(user, password)
        else:
            try:
                gen_password = os.environ['GEN_PASSWORD']
            except KeyError:
                gen_password = getpass.getpass(prompt='GEN Password: ').strip()
            auth = HTTPBasicAuth(user, gen_password)
        
        # Make the API request to get the token
        response = requests.get(api_url, auth=auth)
        response.raise_for_status()
        loaded_data = response.json()
        token = loaded_data.get('token')
        
        # Set up the Kubernetes context using the token
        config_cmd = f"kubectl config set-credentials {user} --token={token}"
        subprocess.run(config_cmd, shell=True, check=True)
        
        # Setup kubeconfig for namespace
        kubectl_default_namespace_command = f"kubectl config set-context --current --namespace={namespace}"
        subprocess.run(kubectl_default_namespace_command, shell=True, check=True)

        print(f"Cluster {url} and Namespace {namespace} set successfully.")
        return True
    except Exception as e:
        print(f"Error in setting up config: {e}")
        exit()

# Function to get the current namespace using kubectl
def get_current_namespace():
    try:
        kubectl_command = "kubectl config view --minify -o jsonpath='{..namespace}'"
        result = subprocess.run(["powershell.exe", kubectl_command], stdout=subprocess.PIPE, text=True)
        output = result.stdout.splitlines()[0]
        return output
    except Exception as err:
        print(f"Error: {err}")
        return None

# Main login function to execute the login process
def login(cluster_name):
    try:
        # Get URL and available namespaces for the selected cluster
        url, namespaces = verify_cluster_and_namespaces(cluster_name)
        print(f"Cluster URL: {url}")
        print(f"Available namespaces: {', '.join(namespaces)}")
        
        # If there are multiple namespaces, ask the user to choose one
        if len(namespaces) > 1:
            print("Multiple namespaces found, please select one:")
            for idx, namespace in enumerate(namespaces, start=1):
                print(f"{idx}: {namespace}")
            selected_index = int(input("Enter the number of your selected namespace: ")) - 1
            namespace = namespaces[selected_index]
        else:
            namespace = namespaces[0]  # Only one namespace, use it directly
        
        # Set up the config file for kubectl using the URL and namespace
        if get_config_file(url, namespace):
            print(f"Namespace {namespace} set as default.")
            print(f"Switched to namespace: {namespace}")
    except Exception as err:
        print(f"Error: {err}")
        exit()

# Main function to process the script execution
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <cluster-name>")
        exit()
    else:
        cluster_name = sys.argv[1]  # Get the cluster name from the command line argument
        login(cluster_name)

              
