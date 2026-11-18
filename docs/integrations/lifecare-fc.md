# Lifecare family care (FC)

Lifecare FamilyCare, API `WESE.FC.Api.FC`. Source of everything in family care: persons, case data, decisions,
payments, documents and users.

## Used by

Every endpoint except `/professional-web/exchange`. See [architecture.md](../architecture.md).

## Calls

All under `/apifc/v1`.

|                                               Call                                                |                Purpose                |
|---------------------------------------------------------------------------------------------------|---------------------------------------|
| `GET Persons`, `GET Contacts`                                                                     | Person and contacts                   |
| `GET Actualisations`, `GET Actualisations/Proposals`, `POST Actualisations`                       | Actualisations: read, propose, create |
| `POST Actualisations/{id}/Attachments`                                                            | Attach a file to an actualisation     |
| `GET Calculations`, `GET Calculations/Proposals`, `POST Calculations`                             | Calculations: read, propose, create   |
| `GET Decisions`                                                                                   | Decisions. There is no by-id read     |
| `GET Payments`, `GET Investigations`, `GET Services`, `GET Executions`, `GET ResourceAllocations` | Case data in a date window            |
| `GET Documents`, `GET DocumentContent`                                                            | Document metadata and the PDF         |
| `GET Users/GetUsers`                                                                              | User directory                        |

## Authentication

`domain` and `key` as query parameters (`integration/LifecareApiKey.java`). `/Users/*` is licensed to a separate
consumer and may need its own `user-key`. The two surfaces refuse the wrong key in different ways; the comment on
`integration.lifecare-fc` in `application.yml` describes how.

## Code and configuration

- Client: `src/main/java/se/sundsvall/lifecareintegrator/integration/lifecarefc/`, generated from
  `src/main/resources/integrations/lifecare-fc-api.json`.
- Configuration: `integration.lifecare-fc` in `application.yml`.

## Behaviour worth knowing

- Dates must be sent as RFC 3339 with a time component. See `integration/lifecarefc/FamilyCareDates.java`.
- The period reads require both dates. An open window in our API falls back to a ten-year default. See
  `service/DateWindow.java`.
- Errors arrive as `{"Message": "…"}` and are kept as the problem's `detail`. See `integration/LifecareErrorDecoder.java`.

TODO: field mapping notes and code values not documented in the spec.
