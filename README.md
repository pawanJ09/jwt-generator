# SOAP Header Generator

This utility project can be used to generate the Json Web Token (JWT) using private key, 
keystore alias and keystore password.

## Requirements

For building and running the application you need:

- [JDK 11](https://www.oracle.com/java/technologies/downloads/#java11-mac)
- [Maven 3](https://maven.apache.org)
- Environment variables:

```shell
export LOG_LEVEL=debug
export CLIENT_ID=
export ISU_NAME=
export AUD=wd
export JWT_EXPIRE=100
export KEYSTORE_LOCATION=
export KEYSTORE_ALIAS=
export KEYSTORE_PASSWORD=
```

## To Build and Package the application

```shell
mvn clean install
```

## Running the application locally

There are several ways to run this Java application on your local machine. One way is to execute
the `main` method in the `com.generator.jwt.JwtGenerator` class from your IDE.

Alternatively you can use the java -jar command.

```shell
java -jar target/jwt-generator-1.0-SNAPSHOT-shaded.jar
```
