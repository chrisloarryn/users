# users-service

Spring Boot 4 service for JWT registration, authentication, user management, and product management.

## Requirements

- JDK 25
- Docker (optional for `docker compose` and Testcontainers-backed tests)

If you use SDKMAN:

```bash
sdk env install
sdk env
```

## Run locally

```bash
./mvnw spring-boot:run
```

The default profile is `local` and points to PostgreSQL on `localhost:65432`.

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

Swagger is available locally at `http://localhost:8080/swagger-ui/index.html`.

## Tests

```bash
./mvnw test
```

## Validations

### Unit and integration

To run the same first stage used by the CI workflow:

```bash
./mvnw --batch-mode -DexcludedGroups=karate test
```

This runs Spring Boot unit and integration tests while excluding Karate so API contract coverage stays isolated from the rest of the suite.

<!-- validation-unit:start -->
Latest completed CI result from [`Validate Java Application #167`](https://github.com/chrisloarryn/users/actions/runs/23392747613) on `develop`:
- result: `CANCELLED`
- tests executed: `0`
- failures: `0`
- errors: `0`
- skipped: `0`
- duration: `0.00s`
<!-- validation-unit:end -->

When Docker is available, integration tests use PostgreSQL through Testcontainers. When Docker is not available, the `test` profile falls back to H2 for local validation.

### API contracts with Karate

To run only the contract suite:

```bash
./mvnw --batch-mode -Dtest=karate.ApiContractsKarateTest test
```

The suite is organized into:

- `src/test/java/karate/contracts/auth`
- `src/test/java/karate/contracts/users`
- `src/test/java/karate/contracts/products`
- `src/test/java/karate/helpers`
- `src/test/java/karate/karate-config.js`

Applied practices:

- short features, split by endpoint or scenario
- dynamic test data to avoid collisions across runs
- reusable helpers for user and product setup
- shared schemas to validate every field in each contract
- tags by domain and endpoint, plus `@regression`

Example tag filtering:

```bash
./mvnw --batch-mode -Dtest=karate.ApiContractsKarateTest -Dkarate.tags=@products test
```

<!-- validation-karate:start -->
Latest completed CI result from [`Validate Java Application #167`](https://github.com/chrisloarryn/users/actions/runs/23392747613) on `develop`:
- result: `CANCELLED`
- features: `n/a`
- scenarios: `n/a`
- failed scenarios: `n/a`
- duration: `n/a`
<!-- validation-karate:end -->

The HTML report is generated at `target/karate-reports/karate-summary.html`.

#### When Karate is useful

Karate is most valuable when you need end-to-end functional and HTTP contract validation without writing large amounts of plumbing code.

Recommended use cases:

- when the API changes often and you want fast contract regression feedback
- when you need field-by-field validation of requests, responses, headers, status codes, and business errors
- when multiple teams consume the service and published contract compatibility matters
- when you want readable tests for developers, QA, or analysts without dropping to `MockMvc` or manual HTTP clients
- when you want to split smoke and regression coverage by tags and run only parts of the API

Key Karate benefits:

- validates real HTTP contracts instead of only internal layers
- reduces duplication through helpers, dynamic data, and reusable schemas
- makes endpoint and domain regression coverage easier to maintain
- produces HTML reports that are easy to inspect
- complements unit and integration tests instead of replacing them

### Full local validation

To run the full local functional validation:

```bash
./mvnw --batch-mode test
```

The workflow runs the functional suites in separate jobs, so the combined reference below is synthesized automatically from the latest unit and Karate results.

<!-- validation-full:start -->
Latest completed CI result from [`Validate Java Application #167`](https://github.com/chrisloarryn/users/actions/runs/23392747613) on `develop`:
- combined result: `CHECK DETAILS`
- total checks derived from unit + Karate: `n/a`
- failures: `n/a`
- errors: `0`
<!-- validation-full:end -->

### Coverage quality gate

To run the same coverage gate used in CI:

```bash
./mvnw --batch-mode -Pcoverage verify -DexcludedGroups=karate
```

<!-- validation-coverage:start -->
Latest completed CI result from [`Validate Java Application #167`](https://github.com/chrisloarryn/users/actions/runs/23392747613) on `develop`:
- result: `CANCELLED`
- line coverage: `n/a%`
- covered lines: `n/a`
- missed lines: `n/a`
- minimum threshold: `n/a%`
<!-- validation-coverage:end -->

### Performance tests with Gatling

```bash
./mvnw --batch-mode -Pgatling verify -DskipTests=true
```

The `gatling` profile starts the application with `test,gatling`, runs the simulation against `http://127.0.0.1:8080`, and writes the report to `target/gatling`.

The simulation is organized into readable chains for authentication, user lifecycle, product lifecycle, and cleanup so new requests can be added without growing a single monolithic flow.

The current simulation covers:

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

<!-- validation-gatling:start -->
Latest completed CI result from [`Validate Java Application #167`](https://github.com/chrisloarryn/users/actions/runs/23392747613) on `develop`:
- result: `CANCELLED`
- total requests: `n/a`
- successful requests: `n/a`
- failed requests: `n/a`
- mean response time: `n/a ms`
- p95: `n/a ms`
- p99: `n/a ms`
- throughput: `n/a rps`
- failed assertions: `n/a`
<!-- validation-gatling:end -->

Each workflow run uploads the detailed HTML report in the `gatling-report` artifact. Local executions still write their report under `target/gatling`.

#### When Gatling is useful

Gatling is most valuable when you need measurable non-functional validation under load.

Recommended use cases:

- before important releases or architectural changes
- when adding critical endpoints or flows chaining multiple operations
- when you want to detect latency degradation after security, persistence, or serialization changes
- when you need repeatable throughput, percentile, and stability validation
- when you want to turn performance expectations into automated CI assertions

Key Gatling benefits:

- measures latency, throughput, and percentiles consistently
- detects performance regressions even when there are no functional failures
- models real user flows instead of only isolated requests
- produces comparable historical reports
- helps define objective thresholds to accept or reject changes

#### Karate and Gatling complement each other

Keeping both is worthwhile when the service is important for other consumers or business flows:

- Karate answers whether the API still works and whether the contract is still correct
- Gatling answers whether the API still performs well under load
- together they cover both functional and non-functional regression in the same delivery pipeline

You can tune the load with Maven properties, for example:

```bash
./mvnw -Pgatling verify -Dgatling.users=20 -Dgatling.rampSeconds=15 -Dgatling.holdSeconds=30
```

If `spring-boot:start` conflicts with another local process, you can move the JMX port used to control startup and shutdown:

```bash
./mvnw -Pgatling verify -Dgatling.spring-boot.jmx-port=19101
```

## CI workflow

The GitHub Actions workflow lives in `.github/workflows/validate.yml`.

<!-- validation-snapshot:start -->
### Latest CI Validation Snapshot
_Automatically updated by `Validate Java Application` after push runs on `main` and `develop`._

- Run: [`#167`](https://github.com/chrisloarryn/users/actions/runs/23392747613)
- Branch: `develop`
- Commit: [`84fa014`](https://github.com/chrisloarryn/users/commit/84fa014f174949ce20a5d77e105986ebef91ed61)
- Updated: March 22, 2026 01:10 UTC

| Stage | Result | Highlights |
| --- | --- | --- |
| Unit and integration | CANCELLED | tests=0, failures=0, errors=0, skipped=0, duration=0.00s, svc=0, sec=0, repo=0, api=0, err=0, other=0 |
| Karate contracts | CANCELLED | features=n/a, scenarios=n/a, failed=n/a, duration=n/a |
| Gatling performance | CANCELLED | requests=n/a, ok=n/a, ko=n/a, mean=n/ams, p95=n/ams, p99=n/ams, throughput=n/arps, failed assertions=n/a |
| Coverage quality gate | CANCELLED | line coverage=n/a%, threshold=n/a%, covered=n/a, missed=n/a |

Artifacts published by the run:
- `unit-test-report`
- `karate-report`
- `gatling-report`
- `coverage-report`
<!-- validation-snapshot:end -->

GitHub Actions is the source of truth for pipeline status. The local figures documented above are reference baselines, and runner-dependent stages such as Gatling can vary between a workstation and `ubuntu-latest`.

Validation runs are concurrency-controlled per branch or pull request. When a newer push arrives for the same ref, GitHub Actions cancels any older in-progress validation run so only the newest one continues.

`README.md`-only commits are excluded from the validation and deployment push triggers so the automatic refresh does not create an infinite CI loop.

Execution order inside a single active run:

1. `unit-tests`
2. In parallel after `unit-tests` succeeds:
   - `karate-contract-tests`
   - `gatling-performance-tests`
   - `coverage-quality-gate`

Commands used by CI:

- `./mvnw --batch-mode -DexcludedGroups=karate test`
- `./mvnw --batch-mode -Dtest=karate.ApiContractsKarateTest test`
- `./mvnw --batch-mode -Pgatling verify -DskipTests=true`
- `./mvnw --batch-mode -Pcoverage verify -DexcludedGroups=karate`

The workflow summary publishes:

- stage status for unit/integration, Karate, Gatling, and coverage
- unit test breakdown by area: `service`, `security`, `repository`, `integration`, `error`, and `other`
- coverage percentage and threshold
- Gatling latency and assertion highlights

Artifacts published by the workflow:

- `unit-test-report`
- `karate-report`
- `gatling-report`
- `coverage-report`
