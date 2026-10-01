# Structure

- You must follow Screaming Architecture: package by feature, never by technical layer
- You must place each feature in its own package containing its controller, repository, mapper, domains, handlers, requests, and responses
- You must name classes after the feature: `<Feature>Controller`, `<Feature>Repository`, `<Feature>Mapper`
- You must use the mediator pattern: controllers dispatch to command/query handlers in `handlers/`
- You must separate input DTOs (`requests/`) from output DTOs (`responses/`) and domain entities (`domains/`)
- You must place Kafka events in `events/` and Kafka listeners in `listeners/`
- You must mirror the feature package structure in tests and keep shared test infrastructure in `shared/`

Canonical service layout:

```
<service>/
├── pom.xml                                  # Inherits from parent, depends on starter
└── src/
    ├── main/
    │   ├── java/com/company/<service>/
    │   │   ├── Application.java             # Spring Boot entry point
    │   │   └── <feature>/                   # e.g. customer/, order/, payment/
    │   │       ├── <Feature>Controller.java
    │   │       ├── <Feature>Repository.java
    │   │       ├── <Feature>Mapper.java     # MapStruct
    │   │       ├── domains/                 # Domain entities
    │   │       ├── handlers/                # Mediator command/query handlers
    │   │       ├── requests/                # Input DTOs
    │   │       ├── responses/               # Output DTOs
    │   │       ├── events/                  # Kafka events (when applicable)
    │   │       └── listeners/               # Kafka listeners (when applicable)
    │   └── resources/application.yml        # Imports classpath:starter.yml
    └── test/java/com/company/<service>/
        ├── <feature>/                       # Feature tests
        └── shared/                          # Testcontainers configs (Mongo, Kafka, Redis) and test data
```
