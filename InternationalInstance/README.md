# Pss International Instance

# Development Environment
For the development environment use the following configurations

Database Configurations:

- Install Docker
- Run the following command to install postgres db
- Set the server url in the apps application.properties file
- The docker-compose-postgres.yaml can be found in the project directory
- Make sure to edit the .env file

```shell
docker-compose-postgres.yaml
```

You can check if it is running by running 
```shell
docker ps
```

# Application details
The application runs on port 7009 but this can be changed in the application.properties file

Swagger Documentation can be accessed using the following link.
```
http://{{serverIp}}:7009/swagger-ui/index.html#/
```


# Deployment

The backend is deployed using github actions. The pipeline will build a jar file,
run a docker build command for building a docker image then push the same to dockerhub.
The pipelines then logs into the server url provided in the secrets in github, 
pulls the docker image and run it.

The pipeline is triggered by a merge request in the main branch.










