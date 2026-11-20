# AGENTS.md

Context for AI agents working in this repository.

## Overview

Event-driven microservices system built with Java and Spring Boot, using Maven multi-module conventions (each module has its own pom.xml), MongoDB for persistence, Kafka for messaging, Redis for caching, Keycloak for identity, and Kong as API gateway.

## Rules

Mandatory rules for all agent work in this repository, defined in `.agents/rules/`:

- [ROLE](.agents/rules/role.md)
- [PRINCIPLES](.agents/rules/principles.md)
- [STRUCTURE](.agents/rules/structure.md)
- [CODE-STYLE](.agents/rules/code-style.md)
- [DEPENDENCIES](.agents/rules/dependencies.md)
- [DEPENDENCY-INJECTION](.agents/rules/dependency-injection.md)
- [TESTABILITY](.agents/rules/testability.md)
- [PERFORMANCE](.agents/rules/performance.md)
- [ANTI-PATTERNS](.agents/rules/anti-patterns.md)
- [API-DESIGN](.agents/rules/api-design.md)
- [JAVA](.agents/rules/java.md)
- [OUTPUT](.agents/rules/output.md)

## Skills

Step-by-step procedures for common tasks, defined in `.agents/skills/`:

- [create-service](.agents/skills/create-service/SKILL.md) — Scaffold a new microservice (pom, ports, docker-compose, Kong, CI)
- [add-feature](.agents/skills/add-feature/SKILL.md) — Add a feature package (controller, handlers, repository, mapper, DTOs)
- [add-endpoint](.agents/skills/add-endpoint/SKILL.md) — Add a REST endpoint with mediator dispatch and Swagger annotations
- [add-kafka-flow](.agents/skills/add-kafka-flow/SKILL.md) — Publish/consume events with the outbox pattern
- [write-tests](.agents/skills/write-tests/SKILL.md) — Testcontainers setup and 100% JaCoCo coverage
- [use-starter](.agents/skills/use-starter/SKILL.md) — Consume shared starter capabilities (mediator, outbox, clients, resilience)
- [debug-local](.agents/skills/debug-local/SKILL.md) — Run locally, call authenticated APIs, inspect Kafka/Mongo/Redis/logs

## Repository Structure

| Folder               | Purpose                                                                                                                                                                         |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| parent               | Maven parent POM: shared dependencies, plugins, Java/Spring Boot versions, JaCoCo rules                                                                                         |
| starter              | Shared Spring Boot auto-configuration library used by all services (clients, cryptography, exception handling, filters, mediator, otp, outbox, resilience, swagger, validators) |
| authservice          | Authentication and user management (integrates with Keycloak)                                                                                                                   |
| configurationservice | Configuration CRUD                                                                                                                                                              |
| customerservice      | Customer CRUD (calls authservice to create users)                                                                                                                               |
| productservice       | Product CRUD                                                                                                                                                                    |
| orderservice         | Order creation, publishes/consumes Kafka events                                                                                                                                 |
| paymentservice       | Payment creation/approval/cancellation, publishes/consumes Kafka events                                                                                                         |
| .docker              | `docker-compose.yaml`, `dockerfile`, Kong/Keycloak/Filebeat configuration                                                                                                       |
| run.ps1 / run.sh     | Dev startup scripts (Windows / Linux)                                                                                                                                           |

### Service Internal Structure

Each service follows Screaming Architecture (package by feature). The canonical layout and mandatory conventions are defined in [STRUCTURE](.agents/rules/STRUCTURE.md).

## Technologies

- **Java 25** (Temurin in CI)
- **Spring Boot 4.1.1** (WebMVC, Security, OAuth2, Data MongoDB, Data Redis, Kafka, Cache, Actuator, Validation, AspectJ)
- **Maven** (build), **Lombok**, **MapStruct**, **SpringDoc OpenAPI**
- **MongoDB** (replica set), **Kafka**, **Redis**, **Keycloak** (OAuth2/JWT), **Kong** (gateway)
- **Elastic Stack** (Elasticsearch, Kibana, Filebeat, APM) for observability
- **JUnit + Testcontainers** (tests), **JaCoCo** (coverage), **Docker / Docker Compose**

## Ports

### Services

| Service              | Localhost | Docker | Kong Route                                 |
| -------------------- | --------- | ------ | ------------------------------------------ |
| authservice          | 8010      | 9010   | http://localhost:8000/authservice          |
| configurationservice | 8015      | 9015   | http://localhost:8000/configurationservice |
| customerservice      | 8020      | 9020   | http://localhost:8000/customerservice      |
| productservice       | 8025      | 9025   | http://localhost:8000/productservice       |
| orderservice         | 8030      | 9030   | http://localhost:8000/orderservice         |
| paymentservice       | 8035      | 9035   | http://localhost:8000/paymentservice       |

### Infrastructure

| Tool          | Port               | Credentials      |
| ------------- | ------------------ | ---------------- |
| Kong          | 8000 / 8001 / 8002 | —                |
| Keycloak      | 8005               | admin / password |
| Kafka         | 9092               | —                |
| Kafka UI      | 9000               | —                |
| MongoDB       | 27017              | admin / password |
| Mongo Express | 27018              | —                |
| Redis         | 6379               | password         |
| RedisInsight  | 6380               | —                |
| Elasticsearch | 9200               | —                |
| Kibana        | 5601               | —                |
| Elastic APM   | 8200               | —                |

## Install Dependencies / Build

Prerequisites: JDK 25, Maven, Docker.

`parent` and `starter` MUST be installed into the local Maven repository before building any service:

```bash
mvn -f parent clean install
mvn -f starter clean install
mvn -f <service> clean install
```

Add `-DskipTests` to skip tests during install.

## Run Tests

Docker must be running (tests use Testcontainers for MongoDB, Kafka, and Redis).

```bash
mvn -f <service> clean verify # Tests + JaCoCo coverage check
mvn -f <service> test         # Tests only, no coverage check
```

## Run Application

- **Full** (everything in Docker):
    - `docker compose -f .docker/docker-compose.yaml --profile full up --detach --build --remove-orphans`
- **Infrastructure only**:
    - `docker compose -f .docker/docker-compose.yaml up --detach --remove-orphans`
- **Dev** (infrastructure in Docker + selected services via `mvn spring-boot:run`):
    - Windows: `run.ps1`
    - Linux: `bash run.sh`
    - Scripts start Docker infrastructure, build `parent` and `starter`, then run only the services listed in the script (currently `authservice` and `configurationservice`). Run other services manually: `mvn -f <service> spring-boot:run`
