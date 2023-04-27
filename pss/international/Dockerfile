FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11

# Expose the port used by the application
EXPOSE 7009

# Set the working directory in the container
WORKDIR /international_javabackend
COPY InternationalInstance.jar /Configurations/application.properties ./

# Set the command to run when the container starts
CMD ["java","-jar","./InternationalInstance.jar", "--spring.config.location=./application.properties"]