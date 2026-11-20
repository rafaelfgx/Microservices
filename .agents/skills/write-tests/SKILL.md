---
name: write-tests
description: Write tests with Testcontainers and reach the mandatory 100% JaCoCo coverage. Use when adding or fixing tests in any service.
---

# Write Tests

## Test Infrastructure (`src/test/java/.../shared/`)

- `MongoTestConfiguration`, `KafkaTestConfiguration`, `RedisTestConfiguration` — `@TestConfiguration` beans exposing Testcontainers with `@ServiceConnection`:

    ```java
    @TestConfiguration
    public class MongoTestConfiguration {
        @Bean
        @ServiceConnection
        public MongoDBContainer mongoContainer() {
            return new MongoDBContainer("mongo").withReplicaSet();
        }
    }
    ```

- `SpringBootTestConfiguration` — imports `GlobalExceptionHandler` and all container configurations, and mocks external dependencies (e.g. `AuthClient`, `ClientRegistrationRepository`).
- `Data` — static test fixtures and constants shared by all tests.

## Test Types (one file per concern, mirroring the feature package)

Follow the test pyramid: unit tests first, always complemented by integration tests.

- `<UseCase><Feature>HandlerTest` — unit tests for handler logic with mocked dependencies (Mockito, no Spring context).
- `<Feature>MapperTest` — MapStruct mapping assertions.
- `<Feature>RepositoryTest` — custom repository queries.
- `<Feature>IntegrationTest` — full HTTP flow with `MockMvcTester`:

    ```java
    @AutoConfigureMockMvc(addFilters = false)
    @Import(SpringBootTestConfiguration.class)
    @SpringBootTest
    class CustomerIntegrationTest {
        @Autowired MockMvcTester mvc;
        @Autowired JsonMapper json;
        @Autowired MongoTemplate mongo;

        @BeforeEach
        void beforeEach() {
            mongo.getDb().drop();
        }
    }
    ```

## Steps

1. Reuse `shared/` configurations; create them only in a new service.
2. Add fixtures to `Data`.
3. Cover every scenario: success, not found, validation failure, conflict — use `@ParameterizedTest` with `@ValueSource` for variations.
4. Assert with AssertJ (`Assertions.assertThat(result).hasStatus(...)`).
5. Run `mvn -f <service> clean verify` and close every coverage gap (JaCoCo requires 100% instruction, line, and branch coverage).
6. Only exclude classes that cannot be covered via the `jacoco.excludes` property in the service `pom.xml` (`Application` and `*Configuration` are already excluded by `parent`).

## Conventions

- Docker must be running (Testcontainers)
- Tests reset state in `@BeforeEach` (`mongo.getDb().drop()`)
- No test depends on execution order
- Reference implementation: `customerservice/src/test/java/com/company/customerservice/`

## Verify

```bash
mvn -f <service> clean verify
```
