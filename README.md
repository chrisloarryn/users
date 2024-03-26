# User Management Backend

## Description

Spring Boot backend for Manage Users Accounts application.


## NOTES:

the database scripts will be executed automatically when you start the docker-compose command.


## Installation

```bash
mvn clean install
```

## Usage

```bash
mvn spring-boot:run
```

## Docker execution

It provides a PostgreSQL database and the service. To run it, execute: 

```bash
docker-compose up
```

## Swagger

Swagger is available at: http://localhost:8080/swagger-ui/index.htmlhttp://localhost:8080/swagger-ui/index.html


## Executing karate tests

```bash
mvn clean test -Dkarate.env="local" -Dkarate.options="--tags @users" -Ddriver=karate > log.log -X
```
