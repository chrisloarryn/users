# User Management Backend

## Description

Spring Boot backend for Manage Users Accounts application.


---

## NOTES:

- The database scripts will be executed automatically when you start the docker-compose command.
- For local execution (instead of dockerized environment) is necessary to have a PostgreSQL database running on "localhost:65432" or any other port you want to use.

- The application is running on port 8080.

### The application is using the following technologies:
  - Spring Boot
  - Spring Data JPA
  - Spring Web
  - Spring Boot DevTools
  - Spring Boot Actuator
  - PostgreSQL
  - Docker
  - Karate
---

## Installation

```bash
mvn clean install
```
---
## Usage

```bash
mvn spring-boot:run
```
---
## Docker execution

It provides a PostgreSQL database and the service. To run it, execute: 

```bash
docker-compose up
```

---
## Swagger (not working yet)

Swagger is available at: http://localhost:8080/swagger-ui/index.htmlhttp://localhost:8080/swagger-ui/index.html

---

## Executing karate tests

```bash
mvn clean test -Dkarate.env="local" -Dkarate.options="--tags @users" -Ddriver=karate > log.log -X
```
---