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
- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/users/{userId}/products`
- `GET /api/users/{userId}/products/{productId}`

Swagger queda disponible en local en `http://localhost:8080/swagger-ui/index.html`.

## Tests

```bash
./mvnw test
```

## Performance Tests

```bash
./mvnw -Pgatling verify
```

El perfil `gatling` levanta la aplicacion con `test,gatling`, ejecuta la simulacion sobre `http://127.0.0.1:8080` y deja el reporte en `target/gatling`.

Puedes ajustar la carga con propiedades Maven, por ejemplo:

```bash
./mvnw -Pgatling verify -Dgatling.users=20 -Dgatling.rampSeconds=15 -Dgatling.holdSeconds=30
```

Si `spring-boot:start` choca con otro proceso local, puedes mover el puerto JMX usado para controlar el arranque/parada:

```bash
./mvnw -Pgatling verify -Dgatling.spring-boot.jmx-port=19101
```
