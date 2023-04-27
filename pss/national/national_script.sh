#!/bin/bash

# Load variables from the variables.env file
source ./Configurations/variables.env

# Pull the Docker image from DockerHub
docker pull ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_VERSION}

# Run the container from the image, the 'tail -f /dev/null/' command will run the container indefinitely.
# The '-d' flag run it in the background and the '-name national_local' assigns a name to the container.
docker run -d --name national_local dnjau/dhis_pss_national:v1 tail -f /dev/null

# This copies the PssNationalInstance.jar from the container to the current directory on the host machine
docker cp national_local:/national_javabackend/PssNationalInstance.jar ./PssNationalInstance.jar

# This command stops and removes the container
docker stop national_local && docker rm national_local

# Build the docker image
docker build -t ${DOCKER_IMAGE_NAME_LOCAL}:${DOCKER_IMAGE_VERSION_LOCAL} --load .

# Run the docker compose
docker-compose -f ./Configurations/docker-compose-app.yaml up -d

# Remove the jar file and docker images
rm PssNationalInstance.jar
docker rmi -f ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_VERSION}




