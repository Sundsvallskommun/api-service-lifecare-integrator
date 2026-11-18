# Architecture

Lifecare Integrator exposes Lifecare (TietoEvry's care management system) to the Sundsvall microservice landscape. It
reads from and writes to two Lifecare APIs, elderly care (EC) and family care (FC), and passes calls through to
Lifecare's internal ProfessionalWeb surface.

TODO: list the services that call this one. The only one known from the code is api-service-caremanagement
(ProfessionalWeb exchange).

## Layers

|    Layer    |    Package    |                                                                   Role                                                                   |
|-------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| API         | `api`         | REST resources and the public model. Everything is scoped by `{municipalityId}`                                                          |
| Service     | `service`     | `DecisionService` merges decisions across sources. `FamilyCareService` handles all other FC operations. Mappers live in `service/mapper` |
| Integration | `integration` | One package per external system, see [integrations/](integrations/)                                                                      |

## Cross-cutting rules

- **Persons are identified by partyId only.** Every person-scoped call resolves partyId → personnummer through
  [Party](integrations/party.md) before it calls Lifecare. The personnummer never appears in the public API.
- **One failing source does not fail the decision list.** The other sources' decisions are returned, and the failing
  source is reported as `UNAVAILABLE`. See `DecisionService`.
- **A decision belonging to another person is reported as not found**, never as forbidden, since a forbidden answer
  would confirm that the id exists. See `DecisionService`.
- **Lifecare's error messages are kept.** Lifecare answers in ASP.NET's `{"Message": "…"}`, not RFC 9457.
  `LifecareErrorDecoder` carries the message on as the problem's `detail`.
- **ProfessionalWeb traffic is never logged.** It carries personnummer and free text in both directions, so its paths
  are excluded from logbook in `application.yml`.

## Endpoints and their sources

All paths are under `/{municipalityId}`.

|                                   Endpoint                                   |                                                       Lifecare call                                                        |
|------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `GET /decisions`                                                             | EC `sol_decisions` and `lss_decisions`, FC `Decisions`, merged. Caseworker names from [Employee](integrations/employee.md) |
| `GET /decisions/{decisionId}`                                                | One source, named by the caller. FC has no by-id read, so the person's decisions are listed and filtered                   |
| `GET /person`, `GET /contacts`                                               | FC `Persons`, `Contacts`                                                                                                   |
| `GET /actualisations`, `GET /actualisations/proposal`                        | FC `Actualisations`, `Actualisations/Proposals`                                                                            |
| `POST /actualisations`, `POST /actualisations/{actualisationId}/attachments` | FC `Actualisations`, `Actualisations/{id}/Attachments`                                                                     |
| `GET /calculations`, `GET /calculations/proposal`, `POST /calculations`      | FC `Calculations`, `Calculations/Proposals`                                                                                |
| `GET /documents`, `GET /documents/{documentId}/content`                      | FC `Documents`, `DocumentContent`                                                                                          |
| `GET /executions`                                                            | FC `Executions`                                                                                                            |
| `GET /investigations`                                                        | FC `Investigations`                                                                                                        |
| `GET /payments`                                                              | FC `Payments`                                                                                                              |
| `GET /resource-allocations`                                                  | FC `ResourceAllocations`                                                                                                   |
| `GET /services`                                                              | FC `Services`                                                                                                              |
| `GET /users`                                                                 | FC `Users/GetUsers`                                                                                                        |
| `POST /professional-web/exchange`                                            | [ProfessionalWeb](integrations/lifecare-professionalweb.md) api2, passed through                                           |

