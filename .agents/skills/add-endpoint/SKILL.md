---
name: add-endpoint
description: Add a REST endpoint to an existing feature controller. Use when exposing a new operation over HTTP.
---

# Add Endpoint

## Steps

1. Create the request record in `requests/` with Bean Validation annotations (`@NotBlank`, `@NotNull`, etc.); create the response record in `responses/` if the endpoint returns data.

2. Create the handler in `handlers/` implementing the appropriate mediator interface (see `add-feature` skill).

3. Add the controller method dispatching through the `Mediator`:

    ```java
    @Operation(summary = "Create")
    @PostApiResponse
    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody @Valid final Create<Feature>Request request) {
        return mediator.handle(Create<Feature>Handler.class, request);
    }
    ```

4. Annotate with Swagger metadata: `@Operation(summary = ...)` plus `@BaseApiResponse` (GET/PUT/PATCH/DELETE) or `@PostApiResponse` (POST), from `com.company.starter.swagger`.

5. Update the endpoint table for the service in `readme.md`.

## Conventions

- Status codes: 200 list/get, 201 create (body = generated UUID), 204 update/delete/actions, 404 not found, 409 conflict
- Path variables merge into the request record via `with` methods (e.g. `request.withId(id)`)
- List endpoints accept `@ParameterObject Pageable` and return `Page<Response>`
- Never expose domain entities; always map to response records
- Reference implementation: `customerservice/src/main/java/com/company/customerservice/customer/CustomerController.java`

## Verify

```bash
mvn -f <service> clean verify
```
