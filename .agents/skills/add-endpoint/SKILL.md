---
name: add-endpoint
description: Add a REST endpoint to an existing feature controller.
---

# Add Endpoint

1. Create the request record in `requests/`; add the response record when required.
2. Create or update the handler implementing the appropriate mediator interface.
3. Add the controller method and dispatch through `Mediator`.
4. Add `@Operation` and `@BaseApiResponse` or `@PostApiResponse`.
5. Update the endpoint table in `readme.md`.

## Rules

- `200`: list/get.
- `201`: create, with generated UUID in the body.
- `204`: update/delete/action.
- `404`: not found.
- `409`: conflict.
- Merge path variables into request records.
- List endpoints use `Pageable` and return `Page<Response>`.
- Never expose domain entities.

## Verify

```bash
mvn -f <service> clean verify
```
