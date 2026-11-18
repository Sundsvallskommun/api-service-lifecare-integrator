# Employee

api-service-employee. Supplies a display name when Lifecare sends a caseworker without one.

## Used by

`GET /decisions` and `GET /decisions/{decisionId}`. See [architecture.md](../architecture.md).

## Calls

|                             Call                              |        Purpose         |
|---------------------------------------------------------------|------------------------|
| `GET /{municipalityId}/portalpersondata/{domain}/{loginName}` | Caseworker's full name |

## Authentication

OAuth2 client credentials, registration `employee`.

## Code and configuration

- Client: `src/main/java/se/sundsvall/lifecareintegrator/integration/employee/`, generated from
  `src/main/resources/integrations/employee.yml`.
- Configuration: `integration.employee` in `application.yml`.

## Behaviour worth knowing

- A failure never fails a decision read; the name is left out instead. Only `fullname` is read from the response. See
  `EmployeeIntegration`.

