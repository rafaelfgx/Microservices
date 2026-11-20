---
name: use-starter
description: Consume shared capabilities from the starter library (mediator, outbox, clients, resilience, swagger, cryptography, otp, validators). Use before implementing any cross-cutting concern in a service.
---

# Use Starter

The `starter` module is a Spring Boot auto-configuration library shared by all services. Never reimplement a capability that exists there. Services activate it by importing `classpath:starter.yml` in `application.yml`.

## Capabilities (`com.company.starter.*`)

| Package | Capability | Entry Points |
|---------|-----------|--------------|
| `mediator` | Controller-to-handler dispatch with validation | `Mediator`, `Handler`, `RequestHandler`, `ResponseHandler`, `RequestResponseHandler` |
| `outbox` | Transactional Kafka publishing (outbox pattern) | `OutboxService.save(topic, key, event)`, `OutboxScheduler` |
| `configuration` | Dynamic configuration consumed from configurationservice via Kafka, cached in memory | `ConfigurationService.getValue(id, type)`, typed getters |
| `clients` | Typed HTTP clients for other services (ambassador pattern) | `AuthAmbassador` (activated by `spring.http.serviceclient.auth.base-url`) |
| `resilience` | Retry and concurrency limiting | `@DefaultRetryable`, `@DefaultConcurrencyLimit` |
| `exception` | Global error handling (Problem Details) | `GlobalExceptionHandler` |
| `swagger` | Standard OpenAPI response annotations | `@BaseApiResponse`, `@PostApiResponse` |
| `cryptography` | Hashing/encryption | `CryptographyService` |
| `otp` | One-time password generation/validation | `OtpService` |
| `validators` | Reusable Bean Validation constraints | `password/`, `username/` |
| `filters` | Servlet filters (logging, correlation) | Auto-configured |
| `configurations` | Redis cache manager, shared beans | Auto-configured |

## Steps

1. Check this table (or `starter/src/main/java/com/company/starter/`) before writing cross-cutting code.
2. Inject the starter bean via constructor injection.
3. For new HTTP clients to other services, follow the ambassador pattern: interface annotated client + `@ImportHttpServices` + resilience annotations, conditional on a `spring.http.serviceclient.<name>.base-url` property.
4. If a capability is generic and reusable, add it to `starter` (with auto-configuration registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`), not to a service.

## Conventions

- Changes to `starter` require reinstalling it before building services: `mvn -f starter clean install`
- Reference implementation: `starter/src/main/java/com/company/starter/clients/auth/AuthAmbassador.java`

## Verify

```bash
mvn -f starter clean install
mvn -f <service> clean verify
```
