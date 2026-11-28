---
name: debug-local
description: Run and debug the system locally.
---

# Debug Local

## Start

```bash
docker compose -f .docker/docker-compose.yaml up --detach --remove-orphans
mvn -f parent clean install -DskipTests
mvn -f starter clean install -DskipTests
mvn -f <service> spring-boot:run
```

Use `run.ps1` on Windows or `run.sh` on Linux for the configured services.

## Inspect

- Kong: `http://localhost:8001`
- Keycloak: `http://localhost:8005`
- Kafka: `http://localhost:9000`
- MongoDB: `http://localhost:27018`
- Redis: `http://localhost:6380`
- Kibana: `http://localhost:5601`
- Actuator: `http://localhost:<port>/actuator/health`
- Swagger: `http://localhost:<port>/swagger-ui.html`

## Authenticate

```text
POST http://localhost:8010/auth
Authorization: Bearer <token>
```
