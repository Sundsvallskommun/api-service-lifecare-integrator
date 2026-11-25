# 0003 One ProfessionalWeb resource per area, one integration test class

**Status:** accepted, 2026-11-25

## Context

The ProfessionalWeb api2 resources have 44 endpoints across seven areas: calculations, decisions, documents, job
stimulus, payments, prints and reminders. Below the API layer there is one client, one integration and one service for
all of them. Each resource class carries the same class-level error responses, and dept44 asks for a `…ResourceTest`
and a `…ResourceFailureTest` per resource. Seven classes mean fourteen unit test classes and, as first written, seven
integration test classes, with the same setup in each.

One resource class would remove that repetition. It would also make a resource of about 700 lines and two test
classes of about 1,000 lines each. The test code would barely shrink, since there is a test per endpoint and per
validation rule either way.

## Decision

Keep one resource class per area, each with its own `@Tag`, as dept44 and the FamilyCare resources here do.

Keep all the ProfessionalWeb integration tests in one class, `ProfessionalWebIT`, with one test per endpoint, numbered
across the areas and named after the operation. Each test has its own `__files/testNN_<method>/` folder, so
nothing in a test depends on the class it sits in.

## Consequences

- The resource and unit test classes stay between about 80 and 240 lines, and Swagger groups the endpoints by area.
- The class-level error responses and the test setup are repeated per area. That is the price of the split.
- A new area adds a resource and two unit test classes, and appends its tests to `ProfessionalWebIT`.

