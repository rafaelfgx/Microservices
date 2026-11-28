# AGENTS.md

Context for AI agents working in this repository.

## Overview

Event-driven microservices system built with Java and Spring Boot, using Maven multi-module conventions (each module has its own pom.xml), MongoDB for persistence, Kafka for messaging, Redis for caching, Keycloak for identity, and Kong as API gateway.

## Rules

Mandatory rules for all agent work in this repository, defined in `.agents/rules/`:

- [Role](.agents/rules/role.md)
- [Principles](.agents/rules/principles.md)
- [Structure](.agents/rules/structure.md)
- [Code Style](.agents/rules/code-style.md)
- [Dependencies](.agents/rules/dependencies.md)
- [Dependency Injection](.agents/rules/dependency-injection.md)
- [Testability](.agents/rules/testability.md)
- [Performance](.agents/rules/performance.md)
- [Anti Patterns](.agents/rules/anti-patterns.md)
- [API Design](.agents/rules/api-design.md)
- [Java](.agents/rules/java.md)
- [Output](.agents/rules/output.md)

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

### Service Structure

Each service follows Screaming Architecture (package by feature). The canonical layout and mandatory conventions are defined in [Structure](.agents/rules/structure.md).

## Technologies

- **Java** (Temurin in CI)
- **Spring Boot** (WebMVC, Security, OAuth2, Data MongoDB, Data Redis, Kafka, Cache, Actuator, Validation, AspectJ)
- **Maven**, **Lombok**, **MapStruct**, **SpringDoc OpenAPI**
- **MongoDB** (Replica Set), **Kafka**, **Redis**, **Keycloak** (OAuth2/JWT), **Kong** (Gateway)
- **Elastic Stack** (Elasticsearch, Kibana, Filebeat, APM)
- **JUnit + Testcontainers**, **JaCoCo**, **Docker / Docker Compose**

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

| Tool          | Port  | Credentials      |
| ------------- | ----- | ---------------- |
| Kong          | 8000  | —                |
| Keycloak      | 8005  | admin / password |
| Kafka         | 9092  | —                |
| Kafka UI      | 9000  | —                |
| MongoDB       | 27017 | admin / password |
| Mongo Express | 27018 | —                |
| Redis         | 6379  | password         |
| RedisInsight  | 6380  | —                |
| Elasticsearch | 9200  | —                |
| Kibana        | 5601  | —                |
| Elastic APM   | 8200  | —                |

## Install Dependencies / Build

Prerequisites: JDK, Maven, Docker.

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
mvn -f <service> clean verify
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
