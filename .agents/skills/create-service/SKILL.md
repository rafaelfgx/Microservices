---
name: create-service
description: Scaffold a Spring Boot microservice following repository conventions.
---

# Create Service

1. Create `<service>/pom.xml` inheriting from `parent` and depending on `starter`.
2. Create `src/main/java/com/company/<service>/Application.java`.
3. Create `src/main/resources/application.yml` with:

    ```yaml
    spring:
        config:
            import: classpath:starter.yml
        application:
            name: <service>
    server:
        port: <port>
    ```

4. Add feature packages according to `structure.md`.
5. Add shared Testcontainers infrastructure and feature tests.
6. Register the service in `.docker/docker-compose.yaml` and `.docker/kong.yml`.
7. Add the service to `.github/workflows/build.yaml`.
8. Update `AGENTS.md` and `readme.md`.

## Rules

- Package: `com.company.<service>`.
- Local port: next available port, incrementing by 5.
- Docker port: local port + 1000.
- Service names end with `service`.
- Service-specific HTTP dependencies use `SPRING_HTTP_SERVICECLIENT_*`.

## Verify

```bash
mvn -f parent clean install
mvn -f starter clean install
mvn -f <service> clean verify
```
