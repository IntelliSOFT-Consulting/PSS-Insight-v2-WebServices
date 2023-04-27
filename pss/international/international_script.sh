#!/bin/bash

# Load variables from the variables.env file
source ./Configurations/variables.env

# Pull the Docker image from DockerHub
docker pull ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_VERSION}

# Run the container from the image, the 'tail -f /dev/null/' command will run the container indefinitely.
# The '-d' flag run it in the background and the '-name international_local' assigns a name to the container.
docker run -d --name international_local dnjau/dhis_pss_national:v1 tail -f /dev/null

# This copies the InternationalInstance.jar from the container to the current directory on the host machine
docker cp international_local:/international_javabackend/InternationalInstance.jar ./InternationalInstance.jar

# This command stops and removes the container
docker stop international_local && docker rm international_local

# Build the docker image
docker build -t ${DOCKER_IMAGE_NAME_LOCAL}:${DOCKER_IMAGE_VERSION_LOCAL} --load .

# Run the docker compose
docker-compose -f ./Configurations/docker-compose-app.yaml up -d

# Remove the jar file and docker images
rm InternationalInstance.jar
docker rmi -f ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_VERSION}




