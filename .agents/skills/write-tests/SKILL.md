---
name: write-tests
description: Write service tests with Testcontainers and 100% JaCoCo coverage.
---

# Write Tests

1. Reuse configurations in `src/test/java/com/company/<service>/shared/`.
2. Configure MongoDB with `.withReplicaSet()`.
3. Use `@SpringBootTest`, `MockMvcTester`, and `MongoTemplate` for integration tests.
4. Mirror the feature package structure.
5. Cover success and failure scenarios with unit and integration tests.
6. Use AssertJ and Mockito.
7. Reset MongoDB state in `@BeforeEach`.
8. Run JaCoCo verification and close all coverage gaps.

## Rules

- Docker must be running.
- Tests are independent and order-independent.
- No static state or hidden dependencies.
- No test-specific execution order.
- JaCoCo requires 100% instruction, line, and branch coverage.
- Exclude coverage only through the POM `jacoco.excludes` property.

## Verify

```bash
mvn -f <service> clean verify
```
