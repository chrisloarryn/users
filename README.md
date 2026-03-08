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

## Validaciones

### Unitarias e integración

Para ejecutar la misma primera etapa del workflow de CI:

```bash
./mvnw --batch-mode -DexcludedGroups=karate test
```

Esto ejecuta unit tests e integration tests de Spring Boot, excluyendo Karate para separar contratos de API del resto de la suite.

Cobertura actual validada localmente el 8 de marzo de 2026:

- `21` tests ejecutados
- `0` failures
- `0` errors

Cuando Docker esta disponible, los tests de integración usan Testcontainers con PostgreSQL. Si Docker no esta disponible, el perfil `test` cae a H2 para la validación local.

### Contratos API con Karate

Para ejecutar solo la suite de contratos:

```bash
./mvnw --batch-mode -Dtest=karate.ApiContractsKarateTest test
```

La suite esta organizada en:

- `src/test/java/karate/contracts/auth`
- `src/test/java/karate/contracts/users`
- `src/test/java/karate/contracts/products`
- `src/test/java/karate/helpers`
- `src/test/java/karate/karate-config.js`

Buenas practicas aplicadas:

- features cortos, separados por endpoint o caso
- datos dinamicos para evitar colisiones entre corridas
- helpers reusables para alta de usuarios y productos
- schemas compartidos para validar todos los campos de cada contrato
- tags por dominio y endpoint, ademas de `@regression`

Ejemplo para filtrar por tags:

```bash
./mvnw --batch-mode -Dtest=karate.ApiContractsKarateTest -Dkarate.tags=@products test
```

Cobertura actual validada localmente el 8 de marzo de 2026:

- `17` features
- `20` escenarios
- `0` fallos

El reporte HTML queda en `target/karate-reports/karate-summary.html`.

#### Cuando conviene usar Karate

Karate aporta mas valor cuando necesitas validar comportamiento funcional y contratos HTTP de punta a punta sin escribir mucho codigo de plomeria.

Casos donde conviene tenerlo:

- cuando el API cambia seguido y quieres detectar regresiones de contrato rapido
- cuando necesitas validar cada campo del request/response, headers, status codes y errores de negocio
- cuando varios equipos consumen el servicio y necesitas asegurar compatibilidad del contrato publicado
- cuando quieres pruebas legibles por desarrolladores, QA o analistas sin bajar al detalle de `MockMvc` o clientes HTTP manuales
- cuando necesitas segmentar smoke/regression por tags y ejecutar subconjuntos del API

Beneficios principales de Karate:

- valida contratos reales sobre HTTP, no solo capas internas
- reduce duplicacion gracias a helpers, data dinamica y schemas reutilizables
- facilita regresion funcional por endpoint, dominio y tag
- deja reportes HTML faciles de revisar
- complementa los tests unitarios e integración en vez de reemplazarlos

### Suite completa

Para ejecutar toda la validación funcional local:

```bash
./mvnw --batch-mode test
```

Resultado validado localmente el 8 de marzo de 2026:

- `41` tests totales
- `0` failures
- `0` errors

### Performance Tests con Gatling

```bash
./mvnw --batch-mode -Pgatling verify -DskipTests=true
```

El perfil `gatling` levanta la aplicacion con `test,gatling`, ejecuta la simulacion sobre `http://127.0.0.1:8080` y deja el reporte en `target/gatling`.

La simulacion esta implementada en:

- `src/gatling/java/com/chrisloarryn/users/performance/UsersApiSimulation.java`
- `src/gatling/java/com/chrisloarryn/users/performance/GatlingSettings.java`

La simulacion actual cubre:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`
- `POST /api/products`
- `GET /api/products`
- `GET /api/products/{id}`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/users/{userId}/products`
- `GET /api/users/{userId}/products/{productId}`

Resultado de referencia de la ultima validacion local ejecutada el 8 de marzo de 2026 con `./mvnw --batch-mode -Pgatling verify -DskipTests=true`:

- `715` requests totales
- `0` requests fallidos
- `42.06` requests por segundo promedio
- `1571 ms` en el percentil 99
- estado final: `fallido` por assertion de performance

Detalle del fallo actual:

- la assertion global de Gatling exige percentil 99 `< 1500 ms`
- la ultima corrida marco `1571 ms`
- aunque no hubo requests fallidos, Maven termina en `BUILD FAILURE` mientras no se cumpla ese umbral

Ese resultado genero el reporte en `target/gatling/usersapisimulation-20260308075542448`.

#### Cuando conviene usar Gatling

Gatling aporta mas valor cuando necesitas validar comportamiento no funcional del servicio bajo carga y con umbrales medibles.

Casos donde conviene tenerlo:

- antes de releases importantes o cambios de arquitectura
- cuando se agregan endpoints criticos o flujos que encadenan varias operaciones
- cuando quieres detectar degradacion de latencia despues de cambios en seguridad, persistencia o serializacion
- cuando necesitas validar throughput, percentiles y estabilidad con una carga repetible
- cuando quieres convertir expectativas de performance en assertions automatizadas de CI

Beneficios principales de Gatling:

- mide latencia, throughput y percentiles de forma consistente
- permite detectar regresiones de performance aunque no haya errores funcionales
- modela flujos de usuario reales y no solo requests aislados
- genera reportes historicos comparables
- ayuda a fijar thresholds objetivos para aceptar o rechazar cambios

#### Karate y Gatling no compiten

Conviene tener ambos cuando el servicio es relevante para otros consumidores o para negocio:

- Karate responde si el API sigue funcionando y si el contrato sigue correcto
- Gatling responde si el API sigue rindiendo bien bajo carga
- juntos cubren regresion funcional y regresion no funcional en el mismo pipeline

Puedes ajustar la carga con propiedades Maven, por ejemplo:

```bash
./mvnw -Pgatling verify -Dgatling.users=20 -Dgatling.rampSeconds=15 -Dgatling.holdSeconds=30
```

Si `spring-boot:start` choca con otro proceso local, puedes mover el puerto JMX usado para controlar el arranque/parada:

```bash
./mvnw -Pgatling verify -Dgatling.spring-boot.jmx-port=19101
```

## Workflow CI

El workflow de GitHub Actions esta en `.github/workflows/validate.yml` y corre en esta secuencia:

1. `unit-tests`
2. `karate-contract-tests`
3. `gatling-performance-tests`

El orden real es secuencial al inicio y paralelo despues:

- primero corre `unit-tests`
- si pasa, arrancan en paralelo `karate-contract-tests` y `gatling-performance-tests`

Comandos usados por CI:

- `./mvnw --batch-mode -DexcludedGroups=karate test`
- `./mvnw --batch-mode -Dtest=karate.ApiContractsKarateTest test`
- `./mvnw --batch-mode -Pgatling verify -DskipTests=true`

Artefactos publicados por el workflow:

- `karate-report`
- `gatling-report`
