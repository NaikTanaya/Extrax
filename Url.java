import subprocess
import sys
import multiprocessing

# Define available clusters and namespaces
ikp_clusters = {
    'cbcc-ap-app-uat-403x': 'wsit-payments-cbcc-ap-app-uat',
    'cbcc-ap-kafka-uat-403x': 'wsit-payments-cbcc-ap-kafka-uat',
    'cbcc-eu-app-uat-401x': 'wsit-payments-cbcc-eu-app-uat',
    'cbcc-eu-kafka-uat-401x': 'wsit-payments-cbcc-eu-kafka-uat',
}

def login_to_cluster(alias, namespace):
    """
    Log in to the selected cluster and set the kubectl context.
    """
    try:
        print(f"Logging in to alias: {alias}, namespace: {namespace}")
        
        # Set kubectl context for the cluster
        kubectl_command = f"kubectl config set-context --current --cluster={alias} --namespace={namespace}"
        subprocess.run(kubectl_command, shell=True, check=True)

        # Set the namespace as default
        kubectl_command_namespace = f"kubectl config set-context --current --namespace={namespace}"
        subprocess.run(kubectl_command_namespace, shell=True, check=True)

        print(f"Switched to context for alias {alias} and set namespace {namespace} as default.")
        
    except subprocess.CalledProcessError as e:
        print(f"Error during kubectl configuration for {alias}: {e}")
        sys.exit(1)

def run_kubectl_command_on_cluster(alias, command):
    """
    Run a kubectl command on the specified cluster.
    """
    try:
        print(f"Running command on {alias}: {command}")
        kubectl_command = f"kubectl --context={alias} {command}"
        subprocess.run(kubectl_command, shell=True, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Error running kubectl command on {alias}: {e}")

def handle_multiple_clusters():
    """
    Handle login to multiple clusters and execute commands on them.
    """
    # Take two cluster aliases from the user
    if len(sys.argv) < 3:
        print("Usage: python ikp_login.py <alias_1> <alias_2> [command]")
        sys.exit(1)

    alias_1 = sys.argv[1]
    alias_2 = sys.argv[2]
    command = " ".join(sys.argv[3:]) if len(sys.argv) > 3 else "get pods"

    # Ensure aliases exist in the available clusters
    if alias_1 not in ikp_clusters or alias_2 not in ikp_clusters:
        print("Both aliases must be valid cluster aliases.")
        print("Available clusters:")
        for key in ikp_clusters.keys():
            print(key)
        sys.exit(1)

    namespace_1 = ikp_clusters[alias_1]
    namespace_2 = ikp_clusters[alias_2]

    # Create separate processes for each cluster login
    process_1 = multiprocessing.Process(target=login_to_cluster, args=(alias_1, namespace_1))
    process_2 = multiprocessing.Process(target=login_to_cluster, args=(alias_2, namespace_2))

    # Start both processes concurrently
    process_1.start()
    process_2.start()

    # Wait for both processes to finish
    process_1.join()
    process_2.join()

    print("Both clusters logged in successfully.")

    # Run kubectl commands on both clusters
    print("\nRunning kubectl commands on both clusters:")
    process_3 = multiprocessing.Process(target=run_kubectl_command_on_cluster, args=(alias_1, command))
    process_4 = multiprocessing.Process(target=run_kubectl_command_on_cluster, args=(alias_2, command))

    # Start processes for kubectl commands
    process_3.start()
    process_4.start()

    # Wait for kubectl commands to complete
    process_3.join()
    process_4.join()

    print("\nCompleted kubectl commands on both clusters.")

if __name__ == "__main__":
    handle_multiple_clusters()
