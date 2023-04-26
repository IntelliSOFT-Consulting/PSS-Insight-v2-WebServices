FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11

# Expose the port used by the application
EXPOSE 7001

# Set the working directory in the container
WORKDIR /national_javabackend
COPY PssNationalInstance.jar /Configurations/application.properties ./

# Set the command to run when the container starts
CMD ["java","-jar","./PssNationalInstance.jar", "--spring.config.location=./application.properties"]