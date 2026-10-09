---
name: create-service
description: Scaffold a new microservice following repository conventions. Use when creating a new Spring Boot service in this repository.
---

# Create Service

## Steps

1. Create `<service>/pom.xml` inheriting from `parent` and depending on `starter`:

    ```xml
    <artifactId>{service}</artifactId>
    <name>{service}</name>
    <version>1.0.0</version>
    <parent>
        <groupId>com.company</groupId>
        <artifactId>parent</artifactId>
        <version>1.0.0</version>
        <relativePath/>
    </parent>
    <dependencies>
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>starter</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    ```

2. Create `src/main/java/com/company/<service>/Application.java` (standard `@SpringBootApplication` entry point).

3. Create `src/main/resources/application.yml` importing the starter configuration and assigning the next free port (ports increment by 5: 8010, 8015, ..., 8040):

    ```yaml
    spring:
        config:
            import: classpath:starter.yml
    ---
    server:
        port: <port>
    spring:
        application:
            name: <service>
    ```

4. Add feature packages following the structure rule (`.agents/rules/structure.md`).

5. Add tests with the `shared/` Testcontainers configurations (see `write-tests` skill).

6. Register the service in `.docker/docker-compose.yaml` (Docker external port = localhost port + 1000):

    ```yaml
    <service>:
        <<: *service
        image: <service>
        container_name: <service>
        depends_on:
            <<: *infrastructure
        build:
            <<: *build
            args:
                NAME: <service>
        ports:
            - "<port + 1000>:<port>"
        environment:
            <<: *environment
            PORT: <port>
    ```

7. Register the route in `.docker/kong.yml`:

    ```yaml
    - name: <service>
      url: http://<service>:<port>
      routes:
          - name: <service>
            paths:
                - /<service>
    ```

8. Add the service to the CI matrix in `.github/workflows/build.yaml`.

9. Optionally add the service to the `services` lists in `run.ps1` and `run.sh`.

10. Update `AGENTS.md` (Repository Structure and Ports tables) and `readme.md` (Services section).

## Conventions

- Package root: `com.company.<service>`
- Localhost port: previous service port + 5; Docker port: localhost + 1000
- Service name is lowercase, suffixed with `service` (e.g. `inventoryservice`)

## Verify

```bash
mvn -f parent clean install
mvn -f starter clean install
mvn -f <service> clean verify
```
