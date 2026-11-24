# Architecture

Lifecare Integrator exposes Lifecare (TietoEvry's care management system) to the Sundsvall microservice landscape. It
reads from and writes to two Lifecare APIs, elderly care (EC) and family care (FC), and passes calls through to
Lifecare's internal ProfessionalWeb surface.

TODO: list the services that call this one. The only one known from the code is api-service-caremanagement
(ProfessionalWeb exchange).

## Layers

|    Layer    |    Package    |                                                                                                                                                Role                                                                                                                                                |
|-------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| API         | `api`         | REST resources and the public model. Everything is scoped by `{municipalityId}`                                                                                                                                                                                                                    |
| Service     | `service`     | `DecisionService` merges decisions across sources. `FamilyCareService` handles all other FC operations. `ProfessionalWebService` backs all the ProfessionalWeb resources and checks bodies and ids before it calls Lifecare through `ProfessionalWebIntegration`. Mappers live in `service/mapper` |
| Integration | `integration` | One package per external system, see [integrations/](integrations/)                                                                                                                                                                                                                                |

## Cross-cutting rules

- **Persons are identified by partyId only.** Every person-scoped call resolves partyId → personnummer through
  [Party](integrations/party.md) before it calls Lifecare. The personnummer never appears in the public API.
- **One failing source does not fail the decision list.** The other sources' decisions are returned, and the failing
  source is reported as `UNAVAILABLE`. See `DecisionService`.
- **A decision belonging to another person is reported as not found**, never as forbidden, since a forbidden answer
  would confirm that the id exists. See `DecisionService`.
- **Lifecare's error messages are kept.** Lifecare answers in ASP.NET's `{"Message": "…"}`, not RFC 9457.
  `LifecareErrorDecoder` carries the message on as the problem's `detail`.
- **A list in a response is never null.** A missing list from Lifecare becomes `[]`, see
  [adr/0001](adr/0001-empty-lists-instead-of-null.md).
- **The ProfessionalWeb resources pass JSON through untouched in both directions.** Lifecare's answer is returned as
  it came, and a client's body is sent to Lifecare as it came, personnummer included. They map Lifecare's refusals to
  problems, see [ProfessionalWeb](integrations/lifecare-professionalweb.md). Exchange returns every answer as data
  instead.
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
| `GET /professional-web/services/{serviceId}/reminders`                       | ProfessionalWeb `api2/Reminders/ListRemindersByServiceId`                                                                  |
| `GET /professional-web/services/{serviceId}/reminders/proposal`              | ProfessionalWeb `api2/Reminders/GetProposalForService`                                                                     |
| `GET /professional-web/reminders/{reminderId}`                               | ProfessionalWeb `api2/Reminders/GetReminderEditComposite`                                                                  |
| `POST /professional-web/reminders`                                           | ProfessionalWeb `api2/Reminders/CreateReminder/`                                                                           |
| `PUT /professional-web/reminders/{reminderId}`                               | ProfessionalWeb `api2/Reminders/UpdateReminder/`. The body's `reminderId` must match the path                              |
| `DELETE /professional-web/reminders/{reminderId}`                            | ProfessionalWeb `api2/Reminders/RemoveReminder/`                                                                           |
| `GET /professional-web/services/{serviceId}/job-stimulus`                    | ProfessionalWeb `api2/Calculation/GetJobStimulusForService`                                                                |
| `GET /professional-web/job-stimulus/end-date`                                | ProfessionalWeb `api2/Calculation/GetJobStimulusToDate`                                                                    |
| `POST /professional-web/job-stimulus`                                        | ProfessionalWeb `api2/Calculation/SaveJobStimulus`, which replaces all of the person's periods                             |
| `GET /professional-web/services/{serviceId}/payments/proposal`               | ProfessionalWeb `api2/Payment/GetPaymentForCreate`                                                                         |
| `GET /professional-web/services/{serviceId}/payments/latest`                 | ProfessionalWeb `api2/Payment/GetLatestPayments`                                                                           |
| `GET /professional-web/services/{serviceId}/household`                       | ProfessionalWeb `api2/Household/HasHouseholdThisDate/`, for the `payment.susPersonId` of `GetPaymentForCreate`             |
| `POST /professional-web/services/{serviceId}/payments`                       | ProfessionalWeb `api2/Payment/Create`, not sent again unless Lifecare refuses it for a missing session                     |
| `POST /professional-web/payees`                                              | ProfessionalWeb `api2/Payee/Create`                                                                                        |
| `GET /professional-web/services/{serviceId}/decisions/proposal`              | ProfessionalWeb `api2/Decision/GetProposalForService`, `amountType=&calculationId=0` without a calculation                 |
| `GET /professional-web/decision-codes/{decisionCode}/reasons`                | ProfessionalWeb `api2/Decision/GetMappedDecisionReasons`                                                                   |
| `GET /professional-web/decisions/{decisionId}`                               | ProfessionalWeb `api2/Decision/GetDecision`                                                                                |
| `POST /professional-web/services/{serviceId}/decisions`                      | ProfessionalWeb `api2/Decision/Create`                                                                                     |
| `PUT /professional-web/decisions/{decisionId}`                               | ProfessionalWeb `api2/Decision/Update`                                                                                     |
| `GET /professional-web/persons/{partyId}/documents`                          | ProfessionalWeb `api2/Document/GetDocumentsListForClient/`, with the personnummer from Party                               |
| `GET /professional-web/journal-notes/{id}`, `GET …/journal-notes/{id}/edit`  | ProfessionalWeb `api2/Document/GetJournalNoteWithContent/`, to show or to edit                                             |
| `GET /professional-web/documents/{id}`, `GET …/documents/{id}/edit`          | ProfessionalWeb `api2/Document/GetDocumentWithContent/`, to show or to edit                                                |
| `GET /professional-web/services/{serviceId}/journal-notes/proposal`          | ProfessionalWeb `api2/Document/GetNoteProposalForService`                                                                  |
| `GET /professional-web/services/{serviceId}/documents/proposal`              | ProfessionalWeb `api2/Document/GetDocumentProposalForService`                                                              |
| `POST /professional-web/journal-notes`, `POST /professional-web/documents`   | ProfessionalWeb `api2/Document/CreateJournalNote/`, `api2/Document/CreateDocument/`                                        |
| `PUT /professional-web/journal-notes/{id}`, `PUT …/documents/{id}`           | ProfessionalWeb `api2/Document/UpdateJournalNote/`, `UpdateDocument/`. The body's `documentId` must match the path         |
| `GET /professional-web/services/{serviceId}/calculations/proposal`           | ProfessionalWeb `api2/Calculation/GetProposalService`                                                                      |
| `GET /professional-web/services/{serviceId}/calculations/latest`             | ProfessionalWeb `api2/Calculation/ListCalculations`, only the latest                                                       |
| `GET /professional-web/calculations/{calculationId}`                         | ProfessionalWeb `api2/Calculation/GetCalculation`                                                                          |
| `GET /professional-web/calculations/{calculationId}/edit`                    | ProfessionalWeb `api2/Calculation/GetCalculationForEdit`                                                                   |
| `POST /professional-web/calculations/place-persons`                          | ProfessionalWeb `api2/Calculation/PlacePersons`, changes nothing in Lifecare                                               |
| `POST /professional-web/calculations/amount`                                 | ProfessionalWeb `api2/Calculation/GetAmount`, changes nothing in Lifecare                                                  |
| `POST /professional-web/calculations/shared-cost`                            | ProfessionalWeb `api2/Calculation/GetSharedCost`, changes nothing in Lifecare                                              |
| `POST /professional-web/calculations/job-stimuli`                            | ProfessionalWeb `api2/Calculation/GetJobStimuliForCalculation`, changes nothing in Lifecare                                |
| `POST /professional-web/services/{serviceId}/calculations`                   | ProfessionalWeb `api2/Calculation/Create`                                                                                  |
| `PUT /professional-web/calculations/{calculationId}`                         | ProfessionalWeb `api2/Calculation/Update`                                                                                  |
| `DELETE /professional-web/calculations/{calculationId}`                      | ProfessionalWeb `api2/Calculation/Delete`                                                                                  |
| `GET /professional-web/calculations/{calculationId}/pdf`                     | ProfessionalWeb `RenderPdf/PrintContainer`, Lifecare's PDF                                                                 |
| `GET /professional-web/decisions/{decisionId}/pdf`                           | ProfessionalWeb `RenderPdf/PrintDecision`, Lifecare's PDF                                                                  |
| `GET /professional-web/documents/{id}/pdf`                                   | ProfessionalWeb `RenderPdf/PrintDocument`, Lifecare's PDF                                                                  |

The three `pdf` endpoints need `Accept: application/pdf`. They answer 406 to any other `Accept`, `*/*` included.
