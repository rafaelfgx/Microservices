---
name: debug-local
description: Run and debug the system locally — start infrastructure, run services, inspect Kafka/Mongo/Redis/logs, and call authenticated APIs. Use when reproducing issues or testing changes end to end.
---

# Debug Local

## Start

1. Infrastructure only (Keycloak, Kafka, Mongo, Redis + admin UIs):

    ```bash
    docker compose -f .docker/docker-compose.yaml up --detach --remove-orphans
    ```

2. Build shared modules (required after any `parent`/`starter` change):

    ```bash
    mvn -f parent clean install -DskipTests
    mvn -f starter clean install -DskipTests
    ```

3. Run the service under debug:

    ```bash
    mvn -f <service> spring-boot:run
    ```

    Or use `run.ps1` (Windows) / `bash run.sh` (Linux) to automate steps 1–3 for the services listed in the script.

4. Everything in Docker instead: add `--profile full --build` to the compose command.

## Call Authenticated APIs

1. Obtain a JWT from authservice: `POST http://localhost:8010/auth` (Keycloak realm `default`, admin console at `http://localhost:8005`, admin/password).
2. Send it as `Authorization: Bearer <token>` to any service, directly (`http://localhost:<port>`) or through Kong (`http://localhost:8000/<service>`).

## Inspect

| Concern | Tool | URL |
|---------|------|-----|
| Kafka topics/messages | Kafbat UI | `http://localhost:9000` |
| Mongo collections (incl. outbox) | Mongo Express | `http://localhost:27018` |
| Redis keys/caches | RedisInsight | `http://localhost:6380` |
| API routes | Kong Manager | `http://localhost:8002` |
| Logs (full profile) | Kibana | `http://localhost:5601/app/management/data/index_management/data_streams` |
| Traces (full profile) | Elastic APM | `http://localhost:5601/app/apm/services` |
| Service health | Actuator | `http://localhost:<port>/actuator/health` |
| API docs | Swagger UI | `http://localhost:<port>/swagger-ui.html` |

## Common Issues

- Service fails to start: check `parent`/`starter` are installed and infrastructure containers are healthy (`docker ps`)
- Event not consumed: check the topic exists (Kafbat UI) and the outbox collection in Mongo for unpublished entries
- 401 responses: token expired or Keycloak not healthy; MockMvc tests bypass security with `addFilters = false`
- Port already in use: `run.ps1`/`run.sh` kill listeners on service ports before starting
