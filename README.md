# users-service

Servicio Spring Boot 4 para registro, autenticación JWT y administración de usuarios.

## Requisitos

- JDK 25
- Docker (opcional para `docker compose` y tests con Testcontainers)

Si usas SDKMAN:

```bash
sdk env install
sdk env
```

## Ejecutar en local

```bash
./mvnw spring-boot:run
```

El perfil por defecto es `local` y apunta a PostgreSQL en `localhost:65432`.

## Docker Compose

```bash
docker compose up --build
```

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

Swagger queda disponible en local en `http://localhost:8080/swagger-ui/index.html`.

## Tests

```bash
./mvnw test
```
