# Changelog

Every change to the service, newest date first. One short line per change.

## 2026-11-20

- Tidy the ProfessionalWeb print code and give each new ProfessionalWeb resource a failure test that checks the exact violation (HYDRAN-3431).
- Add ProfessionalWeb resources that return Lifecare's PDF of a calculation, a decision and a document, answering a template's parameter questions with Lifecare's defaults, with the template ids as configuration (HYDRAN-3431).
- Add ProfessionalWeb resources for calculations: the underlag, the latest, reading one, a service's job stimulus, the reckonings, and creating, updating and removing one (HYDRAN-3431).
- Add ProfessionalWeb resources for a person's journal notes and documents, reading one to show or to edit, the proposals of a service, and creating and updating them (HYDRAN-3431).
- Add ProfessionalWeb resources for a decision's underlag and the reasons of a decision type, and for reading, creating and updating a decision (HYDRAN-3431).
- Add ProfessionalWeb resources for a service's payment underlag and latest payments, a person's household on a date, and creating a payment or a payee (HYDRAN-3431).
- Add ProfessionalWeb resources that read the end date of a job stimulus period and save a person's periods (HYDRAN-3431).
- Plan resources for the Lifecare prints as well, returning Lifecare's own PDF, instead of leaving them to templating (HYDRAN-3431).
- Carry Lifecare's text as the detail of every api2 refusal, and Lifecare's status in front of it on a 502 (HYDRAN-3431).
- Document where the ProfessionalWeb api2 work stands and where it is heading (HYDRAN-3431).
- Send ProfessionalWeb sign-in and exchange over OkHttp pinned to HTTP/1.1, like the Feign clients, instead of the JDK HTTP client (HYDRAN-3431).
- Lay out the ProfessionalWeb code like the other integrations, with the api2 Feign clients beside `configuration`, `session` for sign-in and the session, and `legacy` for the exchange pass-through (HYDRAN-3431).
- Make the reminder client a `@FeignClient` with a circuit breaker, carrying the ProfessionalWeb session in its own Feign configuration instead of through exchange, and answer 400 for a body reminderId too large for a number (HYDRAN-3431).
- Add an integration test that a reminder read after an exchange call sends the session cookie Lifecare rotated on the exchange call (HYDRAN-3431).
- Add an integration test that a reminder read Lifecare answers with 360 bootstraps the module and answers with the retried read (HYDRAN-3431).
- Add an integration test that a reminder update Lifecare refuses with 461 answers 422 with Lifecare's text, and one it fails with 500 answers 502 (HYDRAN-3431).
- Add an integration test that creates, updates and removes a reminder through ProfessionalWeb and checks the bodies Lifecare receives (HYDRAN-3431).
- Add an integration test that reads a service's reminders, the proposal and a single reminder through ProfessionalWeb (HYDRAN-3431).
- Let the reminder update and remove answer clients that accept JSON, keep request bodies byte for byte, and describe the path parameters in the API (HYDRAN-3431).
- Test the reminder resources and the reminder service separately (HYDRAN-3431).
- Move the reminder body checks from the resource into a ProfessionalWeb reminder service (HYDRAN-3431).
- Add resources that create, update and remove a reminder in ProfessionalWeb, passing the body on unchanged (HYDRAN-3431).
- Add resources that read a service's reminders, the proposal for a new one and a single reminder from ProfessionalWeb (HYDRAN-3431).
- Map Lifecare api2 refusals to 400, 403, 404, 422 and 502 problems in the api2 client (HYDRAN-3431).
- Add an api2 Feign client that shares the ProfessionalWeb session with exchange (HYDRAN-3431).

## 2026-11-19

- Give a person without a personnummer a null party id explicitly, and test the party id mapping in every flow.
- Replace the text-only PDF fixture in `DocumentsIT` with a minimal binary PDF, so git never converts its line endings.
- Map lists inline in the mappers and drop `mapList` and its local copies; paged mappings return early on a null composite.
- Split the proposal and calculation request mappings into one small converter per list.
- Return nested lists in calculations, payments, investigations, services and family care decisions as `[]` instead of `null`.
- State in `AGENTS.md` that every commit keeps `docs/` in step with the code.
- Split `DecisionMapper` into `ElderlyCareDecisionMapper` and `FamilyCareDecisionMapper`, and move the source and law values to `DecisionConstants`.
- Rename the Lifecare EC and FC integrations to `elderlycare` and `familycare` in packages, classes and configuration, which breaks deployments: the keys become `integration.elderlycare.*` and `integration.familycare.*` (env `INTEGRATION_ELDERLYCARE_*`, `INTEGRATION_FAMILYCARE_*` including `USERKEY`), and the Feign client, circuit breaker and error message names change to match.
- Remove the comments from `ElderlyCareClient` and `FamilyCareClient`, and name every `@RequestParam` in `FamilyCareClient` with `value =`.
- Remove the Javadoc from the elderly care and family care configuration classes, and document the logging level and the FC 404 instead.
- Configure the Lifecare keys percent-encoded only, pass them on unchanged and refuse to start on a decoded key, replacing `LifecareApiKey`, which breaks deployments that hold a decoded key.
- Add a code convention to `AGENTS.md`: nothing outside `docs/` refers to an ADR or a docs file.
- Add `docs/todo/` for known work not done yet: deploy configuration, bugs, decisions needed, cleanup and the api2 resources.

## 2026-11-18

- Add `docs/` with architecture and integration notes, `AGENTS.md` and this changelog.
- Resolve party ids the same way in every flow: services look them up and pass a map to the mappers.
- Look up caseworker names in `DecisionService` before mapping, so the decision mappers make no calls.
- Add code conventions for method names, conditionals and comments to `AGENTS.md`.
- Drop the error body buffering in `LifecareErrorDecoder` that dept44 has done itself since 8.0.12, and keep Lifecare's
  message for statuses without an `HttpStatus` constant.

## 2026-10-27

- Bump dept44-service-parent to 8.1.0.

## 2026-10-09

- Bump dept44-service-parent to 8.0.12.

## 2026-09-30

- ProfessionalWeb: pass a browser form through.
- ProfessionalWeb: stop reading Lifecare's parameter page as a lost session.
- Keep a null error body snippet null when capping it.
- Bump dept44-service-parent to 8.0.11.

## 2026-09-28

- ProfessionalWeb: block path traversal in the pass-through.

## 2026-09-25

- Add a pass-through to Lifecare ProfessionalWeb for api-service-caremanagement:
  `POST /{municipalityId}/professional-web/exchange`.
- ProfessionalWeb: run on a handed-in session while MobilityGuard is unreachable.
- ProfessionalWeb: keep the traffic out of payload logging.
- ProfessionalWeb: go through the JVM's proxy, like the FC client.

## 2026-09-24

- Party: cache partyId ↔ personnummer lookups in memory.

## 2026-09-23

- Carry Lifecare's own error message on as the problem `detail`.

## 2026-09-22

- FC: expose a partyId for decision and calculation persons.
- FC: request the first page instead of the second.
- FC: generate the models for the dates FamilyCare actually sends.
- FC: accept every LogonType that GetUsers returns.
- Document how the two FC surfaces refuse the wrong key.

## 2026-09-09

- Add `GET /{municipalityId}/decisions/{decisionId}`.
- FC: send dates as RFC 3339 date-times (HYDRAN-2963).

## 2026-08-31

- Keep partyIds readable under dept44 8.0.10's wider personnummer mask.
- Bump dept44-service-parent to 8.0.10.

## 2026-08-24

- Decisions: correct the payload, name months instead of numbering them, and narrow what reaches the log
  (HYDRAN-2805).
- Employee: the client returns an Optional (HYDRAN-2805).
- EC: read date-times as LocalDateTime (HYDRAN-2612).
- Send the Lifecare API key in the query string only (HYDRAN-2612).

## 2026-08-21

- Recover Lifecare error messages and log the error body verbatim (HYDRAN-2612).
- Pin the Lifecare clients to HTTP/1.1 and keep secrets out of the logs (HYDRAN-2612).

## 2026-08-20

- FC: separate key for the user directory (HYDRAN-2612).
- EC: fix the person filter (HYDRAN-2612).

## 2026-08-17

- Fix the OpenAPI specification.
- Bump dept44-service-parent to 8.0.9.

## 2026-07-13

- Bump dept44-service-parent to 8.0.8.

## 2026-07-06

- Use BigDecimal for monetary fields (HYDRAN-2629).
- Normalise the API models and group them into domain sub-packages (HYDRAN-2629).

## 2026-07-03

- Resolve the default decision window in Europe/Stockholm time (HYDRAN-2629).

## 2026-07-02

- Add the public API and service layer: unified decisions and the full FC surface (HYDRAN-2629).

## 2026-06-30

- Add the Lifecare EC integration: SoL and LSS decisions (HYDRAN-2613).
- Add the Lifecare FC integration (HYDRAN-2613).
- Add the Party integration (HYDRAN-2608).

## 2026-06-29

- Create the service from the dept44 template.

