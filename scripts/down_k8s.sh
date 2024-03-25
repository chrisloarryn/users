#!/bin/bash

if [ ! -f "./keys/service-account.json" ]; then
	echo "File ./keys/service-account.json does not exist. Please create it and try again"
	exit 1
fi

# reutilizable function to delete images in the artifact registry in a: region, project, repository, image older than N days
# receives 2 arguments: 1. the image to delete, 2. the number of days
delete_images() {
	# ERROR: (gcloud.artifacts.docker.images.delete) unrecognized arguments:
  #--before=15d (did you mean '--format'?)
  #--force (did you mean '--format'?)
  #To search the help text of gcloud commands, run:
 # gcloud help -- SEARCH_TERMS
#-e \e[1;32mDeleted all images in the artifact registry older than 15 days\e[0m
#-e \e[1;32mDeleted  all images in the artifact registry older than 15 days\e[0m
	# delete all the images in the artifact registry old than 15 days
	gcloud artifacts docker images delete "${1}" --delete-tags --before="${2}" --quiet

	# echo with colors: "Deleted all images in the artifact registry older than 15 days"
	echo -e "\e[1;32mDeleted all images in the artifact registry older than 15 days\e[0m"
}

# reutilizable function to create a namespace
create_namespace() {
	# create namespace
	kubectl create namespace "${1}"

	# echo with colors: "Created namespace ${1}"
	echo -e "\e[1;32mCreated namespace ${1}\e[0m"
}

# reutilizable function to delete a namespace
delete_namespace() {
	# delete namespace
	kubectl delete namespace "${1}" --ignore-not-found

	# echo with colors: "Deleted namespace ${1}"
	echo -e "\e[1;32mDeleted namespace ${1}\e[0m"
}


LOCATION="southamerica-west1"
PROJECT_ID="chrisloarryn-316421"
REPOSITORY_NAME="cloud-run-source-deploy"
APP="cuenta-movimiento"
VERSION="1.1"
IMAGE="${APP}:${VERSION}"
GCP_IMAGE="${LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY_NAME}/${IMAGE}"
K8S_NAMESPACE="chrisloarryn-k8s-"
K8S_ENVIRONMENTS=("dev" "staging" "prod")
K8S_CLUSTER="chrisloarryn-k8s-cluster"
MY_GOOGLE_EMAIL="chrisloarryn@gmail.com"

# login to gcloud with MY_GOOGLE_EMAIL account which has the necessary permissions and is not a service account
gcloud auth login "${MY_GOOGLE_EMAIL}"

# echo with colors: "Logging in with service account"
echo -e "\e[1;32mLogging in with service account\e[0m"

# if first argument is 1, then enter the if statement
if [ "$1" == "1" ]; then
	# delete all the images in the artifact registry old than 15 days
	delete_images "${GCP_IMAGE}-dev" "15d"

	echo -e "\e[1;32mDeleted  all images in the artifact registry older than 15 days\e[0m"

	exit 0
fi

# echo with colors: "gcloud container clusters get-credentials "${K8S_CLUSTER}" --region "${LOCATION}" --project "${PROJECT_ID}"""
echo -e "\e[1;32mgcloud container clusters get-credentials "${K8S_CLUSTER}" --region "${LOCATION}" --project "${PROJECT_ID}"\e[0m"

# get the kubernetes credentials using the service account
gcloud container clusters get-credentials "${K8S_CLUSTER}" --region "${LOCATION}" --project "${PROJECT_ID}"

# update the kubernetes context
gcloud components install gke-gcloud-auth-plugin



# show all namespaces for the current project
kubectl get namespaces

# if there is no namespaces, then exit
if [ "$(kubectl get namespaces | grep "${K8S_NAMESPACE}" | wc -l)" -eq 0 ]; then
	echo "No namespaces found"
	

	# so delete the kubernetes cluster
	gcloud container clusters delete "${K8S_CLUSTER}" --region "${LOCATION}" --quiet

	# delete all the images in the artifact registry old than 15 days
	delete_images "${GCP_IMAGE}-dev" "15d"

	# echo with colors: "Deleted the kubernetes cluster"
	echo -e "\e[1;32mDeleted the kubernetes cluster\e[0m"

	exit 0
fi

# clean all deployments in each environment
for env in "${K8S_ENVIRONMENTS[@]}"; do
	# delete service
	kubectl delete deployment -n "${K8S_NAMESPACE}${env}" "${APP}" --ignore-not-found

	# delete service
	kubectl delete service -n "${K8S_NAMESPACE}${env}" "${APP}" --ignore-not-found

	# delete ingress
	kubectl delete ingress -n "${K8S_NAMESPACE}${env}" "${APP}" --ignore-not-found

	# delete secret
	kubectl delete secret -n "${K8S_NAMESPACE}${env}" "${APP}" --ignore-not-found

	# delete configmap
	kubectl delete configmap -n "${K8S_NAMESPACE}${env}" "${APP}" --ignore-not-found

	# delete namespace
	kubectl delete namespace "${K8S_NAMESPACE}${env}" --ignore-not-found

	# delete all the images in the artifact registry old than 15 days
	delete_images "${GCP_IMAGE}" "15d"

	# delete the kubernetes cluster
	gcloud container clusters delete "${K8S_CLUSTER}" --region "${LOCATION}" --quiet


	# echo with colors: "Deleted all resources in namespace ${K8S_NAMESPACE}${env}" and exit
	echo -e "\e[1;32mDeleted all resources in namespace ${K8S_NAMESPACE}${env}\e[0m"

	# echo with colors: "Deleted all images in the artifact registry older than 15 days"
	echo -e "\e[1;32mDeleted all images in the artifact registry older than 15 days\e[0m"

	# echo with colors: "Deleted the kubernetes cluster"
	echo -e "\e[1;32mDeleted the kubernetes cluster\e[0m"

	# echo with colors: "Deleted all resources in namespace ${K8S_NAMESPACE}${env}" and exit
	echo -e "\e[1;32mDeleted all resources in namespace ${K8S_NAMESPACE}${env}\e[0m"
	exit 0
done



