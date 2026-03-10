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
Latest completed CI result from [`Validate Java Application #158`](https://github.com/chrisloarryn/users/actions/runs/22905621273) on `develop`:
- result: `PASS`
- tests executed: `59`
- failures: `0`
- errors: `0`
- skipped: `0`
- duration: `26.32s`
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
Latest completed CI result from [`Validate Java Application #158`](https://github.com/chrisloarryn/users/actions/runs/22905621273) on `develop`:
- result: `PASS`
- features: `18`
- scenarios: `101`
- failed scenarios: `0`
- duration: `15.35s`
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
Latest completed CI result from [`Validate Java Application #158`](https://github.com/chrisloarryn/users/actions/runs/22905621273) on `develop`:
- combined result: `PASS`
- total checks derived from unit + Karate: `160`
- failures: `0`
- errors: `0`
<!-- validation-full:end -->

### Coverage quality gate

To run the same coverage gate used in CI:

```bash
./mvnw --batch-mode -Pcoverage verify -DexcludedGroups=karate
```

<!-- validation-coverage:start -->
Latest completed CI result from [`Validate Java Application #158`](https://github.com/chrisloarryn/users/actions/runs/22905621273) on `develop`:
- result: `PASS`
- line coverage: `98.19%`
- covered lines: `217`
- missed lines: `4`
- minimum threshold: `90.00%`
<!-- validation-coverage:end -->

### Performance tests with Gatling

```bash
./mvnw --batch-mode -Pgatling verify -DskipTests=true
```

The `gatling` profile starts the application with `test,gatling`, runs the simulation against `http://127.0.0.1:8080`, and writes the report to `target/gatling`.

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
Latest completed CI result from [`Validate Java Application #158`](https://github.com/chrisloarryn/users/actions/runs/22905621273) on `develop`:
- result: `PASS`
- total requests: `715`
- successful requests: `715`
- failed requests: `0`
- mean response time: `24 ms`
- p95: `88 ms`
- p99: `97 ms`
- throughput: `42.06 rps`
- failed assertions: `0`
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

- Run: [`#158`](https://github.com/chrisloarryn/users/actions/runs/22905621273)
- Branch: `develop`
- Commit: [`8f52d68`](https://github.com/chrisloarryn/users/commit/8f52d68d9b7b68d52486b197d05b8af96d9f1161)
- Updated: March 10, 2026 13:49 UTC

| Stage | Result | Highlights |
| --- | --- | --- |
| Unit and integration | PASS | tests=59, failures=0, errors=0, skipped=0, duration=26.32s, svc=23, sec=8, repo=5, api=20, err=3, other=0 |
| Karate contracts | PASS | features=18, scenarios=101, failed=0, duration=15.35s |
| Gatling performance | PASS | requests=715, ok=715, ko=0, mean=24ms, p95=88ms, p99=97ms, throughput=42.06rps, failed assertions=0 |
| Coverage quality gate | PASS | line coverage=98.19%, threshold=90.00%, covered=217, missed=4 |

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
