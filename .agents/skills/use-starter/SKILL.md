---
name: use-starter
description: Consume and extend shared starter capabilities.
---

# Use Starter

Before implementing cross-cutting concerns, check `starter/src/main/java/com/company/starter/`.

## Rules

- Import `classpath:starter.yml`.
- Inject starter beans through constructors.
- Use the starter mediator, outbox, ambassador, resilience, Swagger, exception, cryptography, OTP, validator, and filter capabilities.
- Do not reimplement capabilities already in `starter`.
- Register reusable starter capabilities as Spring auto-configurations.
- Reinstall `starter` after changes.

## Verify

```bash
mvn -f starter clean install
mvn -f <service> clean verify
```
