import sys
import subprocess
from kafka_config import ikp_clusters  # Assuming this holds the cluster info

def main():
    if len(sys.argv) < 2:
        print('Usage:', sys.argv[0], 'cluster_name')
        exit()

    cluster_name = sys.argv[1]

    # Check if cluster exists in the configuration
    if cluster_name in ikp_clusters:
        cluster_info = ikp_clusters[cluster_name]
        namespaces = cluster_info[1]  # List of namespaces
        user = cluster_info[0]  # Assuming user info is the first element

        print(f"alias = {cluster_name}")
        print(f"user = {user}")

        if len(namespaces) > 1:
            # Multiple namespaces, ask user to select one
            print("Multiple namespaces found, please select one:")
            for idx, namespace in enumerate(namespaces, 1):
                print(f"{idx}: {namespace}")

            # Get user selection
            try:
                selection = int(input("Enter the number of your selected namespace: "))
                if 1 <= selection <= len(namespaces):
                    namespace = namespaces[selection - 1]
                    print(f"namespace = {namespace}")
                    login(cluster_name, namespace, user)
                else:
                    print("Invalid selection.")
                    exit()
            except ValueError:
                print("Invalid input.")
                exit()
        else:
            # Only one namespace, no need to ask for selection
            namespace = namespaces[0]
            print(f"namespace = {namespace}")
            login(cluster_name, namespace, user)
    else:
        print('Invalid cluster name. Please provide a valid cluster.')
        exit()

def login(cluster, namespace, user):
    try:
        # Here we print the cluster, namespace, and user that were retrieved
        print(f"Logging into cluster {cluster} with namespace {namespace} as user {user}")

        # Set the kubectl context
        set_kubectl_context(namespace)

    except Exception as err:
        print(f"Error during login: {err}")
        exit()

def set_kubectl_context(namespace):
    try:
        # Set the kubectl context for the given namespace
        kubectl_command = f'kubectl config set-context --current --namespace={namespace}'
        subprocess.run(["powershell.exe", kubectl_command], check=True)
        print(f'Switched to namespace {namespace} using kubectl.')
    except Exception as err:
        print(f"Error setting kubectl context: {err}")
        exit()

if __name__ == '__main__':
    main()
