# Pss Instance

## Requirements

For building and running the application you need:

- [JDK 11.0.18](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3.9.0](https://maven.apache.org)

## Pipeline Documentation

The pipeline workflow is in the .github/workflows/maven.publish.yml

The high-level architecture is [pipeline](https://drive.google.com/file/d/16nqTQUpqRb1L-nCLvKKxxdASW6YE5WSV/view?usp=sharing) 

# Microservice Architecture
The system is developed into 2 microservices:
- InternationalInstance
- NationalInstance

# Containerisation
The system uses docker in its deployment.
The images are build using docker buildx command. This enables the build to build images for the different OS

```
docker buildx create --use --name workflowBuilder
docker buildx build --platform=linux/amd64,linux/arm64 -t ${{ env.IMAGE_NAME_INTERNATIONAL }} --push .
```
The first command creates a new builder instance for Docker buildx
The second command allows the user to specify the different platforms. 

It also tags the image with the provided name
and pushes it to dockerhub.












