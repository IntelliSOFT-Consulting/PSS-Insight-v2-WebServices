# Pss National Instance

# Prequisites
The folder should have the following:
```shell
Configurations

Dockerfile
national_script.sh
README.md
```

The Configurations Folder has the following
```shell
application.properties
docker-compose-app.yaml

docker-compose-postgres.yaml
varibales.env
```

# Database setup
Check if you have a postgres database.

1. If the database does not exist, follow the following instructions

- Make sure you update the variables.env with database values
- Update the following in the application.properties
- - spring.datasource.url=jdbc:postgresql://{{serverUrl}}:{{serverPort}}/{{database}}
- - spring.datasource.username=
- - spring.datasource.password=

```shell
cd Configurations
docker-compose -f docker-compose-postgres.yaml up -d
```

2. If the postgres database exists, create a database and update the application.properties

# Running the backend application

The backend application can be started using the following commands:

- Update the application.properties appropriately
- In the Pss directory, run the following commands
- For a first time use, start with:

```shell
chmod +x national_script.sh 
```

```
./national_script.sh
```

Check if the app is running
```
http://{{serverIp}}:7001/swagger-ui/index.html#/
```



