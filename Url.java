import subprocess
import sys

# Predefined cluster details
clusters = {
    "hk-azr102": {
        "url": "https://hk-azr102.example.com",
        "namespaces": ["shpx-sct", "shpx-cert"]
    },
    "us-azr201": {
        "url": "https://us-azr201.example.com",
        "namespaces": ["app-dev", "app-prod"]
    }
}

def login_to_cluster(cluster_url, username, password):
    """
    Logs in to the cluster using kubectl and sets the credentials.
    """
    try:
        # Simulate the login process
        login_command = f"kubectl login --server={cluster_url} --username={username} --password={password}"
        subprocess.run(login_command, shell=True, check=True, text=True)
        print(f"Logged into cluster {cluster_url}")
    except subprocess.CalledProcessError as e:
        print(f"Error logging into cluster {cluster_url}: {e}")
        sys.exit(1)

def set_namespace(namespace):
    """
    Sets the default namespace for kubectl.
    """
    try:
        namespace_command = f"kubectl config set-context --current --namespace={namespace}"
        subprocess.run(namespace_command, shell=True, check=True, text=True)
        print(f"Namespace set to {namespace}")
    except subprocess.CalledProcessError as e:
        print(f"Error setting namespace {namespace}: {e}")
        sys.exit(1)

def run_kubectl_command(command):
    """
    Runs a kubectl command.
    """
    try:
        result = subprocess.run(f"kubectl {command}", shell=True, check=True, text=True, capture_output=True)
        print(f"Command output:\n{result.stdout}")
    except subprocess.CalledProcessError as e:
        print(f"Error running kubectl command: {e.stderr}")
        sys.exit(1)

def main():
    print("Available clusters:")
    for i, cluster_name in enumerate(clusters.keys(), start=1):
        print(f"{i}. {cluster_name}")

    # Select a cluster
    try:
        cluster_choice = int(input("Select a cluster by number: ")) - 1
        selected_cluster_name = list(clusters.keys())[cluster_choice]
    except (ValueError, IndexError):
        print("Invalid selection. Exiting.")
        sys.exit(1)

    # Retrieve cluster details
    selected_cluster = clusters[selected_cluster_name]
    cluster_url = selected_cluster["url"]
    namespaces = selected_cluster["namespaces"]

    # Get username and password
    username = input("Enter your username: ")
    password = input("Enter your password: ")

    # Login to the selected cluster
    login_to_cluster(cluster_url, username, password)

    # Namespace selection
    print("Available namespaces:")
    for i, namespace in enumerate(namespaces, start=1):
        print(f"{i}. {namespace}")

    try:
        namespace_choice = int(input("Select a namespace by number: ")) - 1
        selected_namespace = namespaces[namespace_choice]
    except (ValueError, IndexError):
        print("Invalid selection. Exiting.")
        sys.exit(1)

    # Set the namespace and run a sample command
    set_namespace(selected_namespace)
    run_kubectl_command("get pods")

if __name__ == "__main__":
    main()
