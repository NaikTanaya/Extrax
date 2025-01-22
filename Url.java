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

def login_to_cluster(cluster_url, username, password, context_name):
    """
    Logs into the cluster using kubectl and sets the credentials with a unique context name.
    """
    try:
        # Simulate the login process
        login_command = f"kubectl login --server={cluster_url} --username={username} --password={password} --context={context_name}"
        subprocess.run(login_command, shell=True, check=True, text=True)
        print(f"Logged into cluster {cluster_url} with context {context_name}")
    except subprocess.CalledProcessError as e:
        print(f"Error logging into cluster {cluster_url}: {e}")
        sys.exit(1)

def set_namespace(context_name, namespace):
    """
    Sets the default namespace for a specific kubectl context.
    """
    try:
        namespace_command = f"kubectl config set-context {context_name} --namespace={namespace}"
        subprocess.run(namespace_command, shell=True, check=True, text=True)
        print(f"Namespace {namespace} set for context {context_name}")
    except subprocess.CalledProcessError as e:
        print(f"Error setting namespace {namespace} for context {context_name}: {e}")
        sys.exit(1)

def run_kubectl_command(context_name, command):
    """
    Runs a kubectl command within a specified context.
    """
    try:
        full_command = f"kubectl --context={context_name} {command}"
        result = subprocess.run(full_command, shell=True, check=True, text=True, capture_output=True)
        print(f"Output from context {context_name}:\n{result.stdout}")
    except subprocess.CalledProcessError as e:
        print(f"Error running kubectl command in context {context_name}: {e.stderr}")
        sys.exit(1)

def main():
    print("Available clusters:")
    for i, cluster_name in enumerate(clusters.keys(), start=1):
        print(f"{i}. {cluster_name}")

    # Select the first cluster
    try:
        cluster1_choice = int(input("Select the first cluster by number: ")) - 1
        cluster1_name = list(clusters.keys())[cluster1_choice]
    except (ValueError, IndexError):
        print("Invalid selection for the first cluster. Exiting.")
        sys.exit(1)

    # Select the second cluster
    try:
        cluster2_choice = int(input("Select the second cluster by number: ")) - 1
        cluster2_name = list(clusters.keys())[cluster2_choice]
    except (ValueError, IndexError):
        print("Invalid selection for the second cluster. Exiting.")
        sys.exit(1)

    # Retrieve cluster details
    cluster1 = clusters[cluster1_name]
    cluster2 = clusters[cluster2_name]

    # Get username and password for both clusters
    username1 = input(f"Enter username for {cluster1_name}: ")
    password1 = input(f"Enter password for {cluster1_name}: ")

    username2 = input(f"Enter username for {cluster2_name}: ")
    password2 = input(f"Enter password for {cluster2_name}: ")

    # Login to both clusters with unique contexts
    login_to_cluster(cluster1["url"], username1, password1, cluster1_name)
    login_to_cluster(cluster2["url"], username2, password2, cluster2_name)

    # Namespace selection for both clusters
    print(f"Available namespaces for {cluster1_name}:")
    for i, namespace in enumerate(cluster1["namespaces"], start=1):
        print(f"{i}. {namespace}")
    try:
        namespace1_choice = int(input(f"Select a namespace for {cluster1_name} by number: ")) - 1
        namespace1 = cluster1["namespaces"][namespace1_choice]
    except (ValueError, IndexError):
        print("Invalid selection for the first namespace. Exiting.")
        sys.exit(1)

    print(f"Available namespaces for {cluster2_name}:")
    for i, namespace in enumerate(cluster2["namespaces"], start=1):
        print(f"{i}. {namespace}")
    try:
        namespace2_choice = int(input(f"Select a namespace for {cluster2_name} by number: ")) - 1
        namespace2 = cluster2["namespaces"][namespace2_choice]
    except (ValueError, IndexError):
        print("Invalid selection for the second namespace. Exiting.")
        sys.exit(1)

    # Set namespaces for both contexts
    set_namespace(cluster1_name, namespace1)
    set_namespace(cluster2_name, namespace2)

    # Allow user to run commands in either cluster
    while True:
        print("\nOptions:")
        print(f"1. Run command in {cluster1_name}")
        print(f"2. Run command in {cluster2_name}")
        print("3. Exit")
        choice = input("Choose an option: ")

        if choice == "1":
            command = input(f"Enter kubectl command for {cluster1_name}: ")
            run_kubectl_command(cluster1_name, command)
        elif choice == "2":
            command = input(f"Enter kubectl command for {cluster2_name}: ")
            run_kubectl_command(cluster2_name, command)
        elif choice == "3":
            print("Exiting.")
            break
        else:
            print("Invalid option. Please try again.")

if __name__ == "__main__":
    main()
