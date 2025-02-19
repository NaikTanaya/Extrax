import os
import subprocess
import json
import platform
import requests
from requests.auth import HTTPBasicAuth

# Define clusters and namespaces
ikp_clusters = {
    'HK-AZX-301': ('https://oidc.ikp301x.cloud.hk.hsbc/', ['shpx-cbsa-hub-cert', 'shpx-cbsa-hub-sct']),
    'HK-AZR-101': ('https://oidc.ikp101r.cloud.hk.hsbc/', ['shpx-cbsa-hub-cert', 'shpx-cbsa-hub-sct'])
}

def login_to_cluster(cluster):
    """Logs into the cluster and retrieves kubectl token"""
    if cluster not in ikp_clusters:
        print("Invalid cluster name.")
        return None, None

    url, namespaces = ikp_clusters[cluster]
    user = os.environ.get('USERNAME', '')
    password = os.environ.get('PASSWORD', '')

    api_url = f"{url}/apitoken/token/user"
    auth = HTTPBasicAuth(user, password)

    try:
        response = requests.get(api_url, auth=auth)
        response.raise_for_status()

        loaded_data = json.loads(response.text)
        kubectl_token_command = loaded_data.get('token', {}).get('kubectl Windows Command')

        if not kubectl_token_command:
            print("Failed to get Kubernetes token.")
            return None, None

        # Execute kubectl token command in PowerShell
        subprocess.run(["powershell.exe", kubectl_token_command], stdout=subprocess.PIPE, text=True)

        print(f"\n✅ Logged into {cluster}. Select a namespace:")
        for idx, ns in enumerate(namespaces, 1):
            print(f"{idx}: {ns}")

        selection = int(input("Enter your choice: "))
        if 1 <= selection <= len(namespaces):
            namespace = namespaces[selection - 1]
        else:
            print("❌ Invalid selection.")
            return None, None

        return cluster, namespace

    except Exception as err:
        print(f"Error logging into cluster: {err}")
        return None, None

def get_pods(cluster, namespace):
    """Fetch all pods in the namespace"""
    execute_kubectl_command(cluster, f'kubectl get pods -n {namespace}')

def describe_pod(cluster, namespace):
    """Describe a specific pod"""
    pod_name = input("Enter pod name: ")
    execute_kubectl_command(cluster, f'kubectl describe pod {pod_name} -n {namespace}')

def get_nodes(cluster):
    """List all nodes"""
    execute_kubectl_command(cluster, 'kubectl get nodes')

def execute_kubectl_command(cluster, command):
    """Executes kubectl commands"""
    try:
        print(f"\n==== EXECUTING: {command} on {cluster} ====")
        result = subprocess.run(["powershell.exe", command], stdout=subprocess.PIPE, text=True)
        print("\n==== KUBECTL OUTPUT ====")
        print(result.stdout)
    except Exception as err:
        print(f"Error executing kubectl command: {err}")

if __name__ == "__main__":
    while True:
        print("\n🔹 Select cluster to log in:")
        for idx, cluster in enumerate(ikp_clusters.keys(), 1):
            print(f"{idx}: {cluster}")
        print("3: Exit")

        choice = int(input("Enter your choice: "))
        if choice == 3:
            print("👋 Exiting program...")
            break

        selected_cluster = list(ikp_clusters.keys())[choice - 1] if 1 <= choice <= 2 else None

        if selected_cluster:
            cluster, namespace = login_to_cluster(selected_cluster)

            if cluster and namespace:
                while True:
                    print("\n🔹 Available actions:")
                    print("1: Get all pods")
                    print("2: Describe a specific pod")
                    print("3: Get all nodes")
                    print("4: Exit to main menu")

                    action = int(input("Enter your choice: "))
                    
                    if action == 1:
                        get_pods(cluster, namespace)
                    elif action == 2:
                        describe_pod(cluster, namespace)
                    elif action == 3:
                        get_nodes(cluster)
                    elif action == 4:
                        print("\n🔄 Returning to cluster selection...\n")
                        break  # Go back to selecting a cluster
                    else:
                        print("❌ Invalid choice. Try again.")
