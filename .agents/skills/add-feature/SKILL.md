---
name: add-feature
description: Add a complete CRUD or business feature to an existing service.
---

# Add Feature

1. Create `com.company.<service>.<feature>` following `structure.md`.
2. Create the MongoDB domain entity in `domains/`.
3. Create immutable request and response records.
4. Create `<Feature>Repository`, `<Feature>Mapper`, and one handler per use case.
5. Implement handlers with the appropriate starter mediator interface.
6. Create the `<Feature>Controller` and dispatch through `Mediator`.
7. Add unit and integration tests.

## Rules

- One handler per use case.
- Handlers contain use-case logic; controllers only dispatch.
- Use constructor injection.
- Use `@Transactional` for state changes or outbox writes.
- Use the starter ambassador for cross-service calls.
- Never expose domain entities or repositories.

## Verify

```bash
mvn -f <service> clean verify
```
