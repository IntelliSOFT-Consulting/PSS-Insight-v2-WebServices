FROM maven:3.6.3-jdk-11-slim AS build
FROM openjdk:11
EXPOSE 7001
WORKDIR /national_javabackend
COPY . /national_javabackend
ADD target/PssNationalInstance.jar PssNationalInstance.jar
ENTRYPOINT ["java","-jar","target/PssNationalInstance.jar"]