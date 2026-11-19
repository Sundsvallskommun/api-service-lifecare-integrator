# Lifecare elderly care (EC)

Lifecare Welfare API Services, host `WE.EC.Integration.Host`. Source of SoL and LSS decisions in elderly care.

## Used by

`GET /decisions` and `GET /decisions/{decisionId}`. See [architecture.md](../architecture.md).

## Calls

|               Call               |         Purpose          |
|----------------------------------|--------------------------|
| `GET /api/v1/sol_decisions`      | A person's SoL decisions |
| `GET /api/v1/sol_decisions/{id}` | One SoL decision         |
| `GET /api/v1/lss_decisions`      | A person's LSS decisions |
| `GET /api/v1/lss_decisions/{id}` | One LSS decision         |

## Authentication

`domain` and `key` as query parameters, and nowhere else. See
`integration/elderlycare/configuration/ElderlyCareConfiguration.java`. The key is configured decoded and encoded by the service ([ADR 0002](../adr/0002-lifecare-keys-configured-decoded.md)).

## Code and configuration

- Client: `src/main/java/se/sundsvall/lifecareintegrator/integration/elderlycare/`, generated from
  `src/main/resources/integrations/elderlycare-api.json`.
- Configuration: `integration.elderlycare` in `application.yml`.

## Behaviour worth knowing

- A by-id read returns the decision for any person, so `DecisionService` checks that it belongs to the requested party
  before returning it.
- A 404 is read as "no data", not as an error (`dismiss404`).
- Feign logging defaults to `NONE` (`log-level`). Request URLs carry the personnummer and the API key, so any higher
  level writes both to the log.

TODO: field mapping notes and code values not documented in the spec.
