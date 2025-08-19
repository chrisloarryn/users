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

## Configuración del regex de contraseña (password)

La aplicación lee el patrón desde la propiedad `app.security.password.regex`. En variables de ambiente (ENV) esto se mapea como `APP_SECURITY_PASSWORD_REGEX`.

Ejemplos de uso:
- Linux/macOS (bash/zsh):
  - export APP_SECURITY_PASSWORD_REGEX='^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$'
  - mvn spring-boot:run
- Windows PowerShell:
  - $Env:APP_SECURITY_PASSWORD_REGEX = '^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$'
  - mvn spring-boot:run
- Maven (pasando la propiedad directamente):
  - mvn spring-boot:run -Dapp.security.password.regex="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$"
- Docker (run):
  - docker run -e APP_SECURITY_PASSWORD_REGEX='^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$' -p 8080:8080 imagen
- Docker Compose: ya fue agregado al archivo `docker-compose.yml` bajo `services.api.environment` como `APP_SECURITY_PASSWORD_REGEX`.
- Kubernetes: agregar una variable de entorno `APP_SECURITY_PASSWORD_REGEX` en el Deployment o definirla en un ConfigMap y referenciarlo.

Notas de escape:
- Prefiera comillas simples en shell/YAML para evitar escapar excesivo.
- En valores pasados con `-D` (Maven), use comillas dobles y escape de backslash (`\\d`).
- También puede definir el valor en `application.properties` como:
  - app.security.password.regex=^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*])(?=.{8,}).*$
