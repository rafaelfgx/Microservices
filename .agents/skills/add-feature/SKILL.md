---
name: add-feature
description: Add a complete feature package (controller, handlers, repository, mapper, DTOs, domain) to an existing service. Use when implementing a new business capability or CRUD.
---

# Add Feature

## Steps

1. Create the feature package `com.company.<service>.<feature>` following `.agents/rules/structure.md`.

2. Create the domain entity in `domains/` (MongoDB document with UUID id).

3. Create input DTOs in `requests/` and output DTOs in `responses/` as records, with Bean Validation annotations on requests.

4. Create `<Feature>Repository` extending Spring Data MongoDB repository.

5. Create `<Feature>Mapper` as a MapStruct interface mapping requests to domain and domain to responses.

6. Create one handler per use case in `handlers/`, implementing a starter mediator interface:

    - `RequestResponseHandler<Request, Response>` — input and output
    - `RequestHandler<Request>` — input only
    - `ResponseHandler<Response>` — output only
    - `Handler` — neither

    ```java
    @RequiredArgsConstructor
    @Component
    public class Create<Feature>Handler implements RequestResponseHandler<Create<Feature>Request, ResponseEntity<UUID>> {
        private final <Feature>Mapper mapper;
        private final <Feature>Repository repository;

        @Override
        public ResponseEntity<UUID> handle(final Create<Feature>Request request) {
            final var entity = repository.save(mapper.to<Feature>(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getId());
        }
    }
    ```

7. Create validator handlers (e.g. `Create<Feature>ValidatorHandler`) for business validation, invoked by the main handler.

8. Create `<Feature>Controller` dispatching to handlers through the `Mediator` (see `add-endpoint` skill).

9. Add tests mirroring the package (see `write-tests` skill).

## Conventions

- Handlers are named `<UseCase><Feature>Handler` (e.g. `CreateCustomerHandler`, `ListOrderHandler`)
- Handlers contain the use case logic; controllers only dispatch
- Cross-service calls go through starter ambassadors (e.g. `AuthAmbassador`), never direct HTTP clients
- Reference implementation: `customerservice/src/main/java/com/company/customerservice/customer/`

## Verify

```bash
mvn -f <service> clean verify
```
