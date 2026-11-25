# Changelog

Every change to the service, newest date first. One short line per change.

## 2026-11-25

- Read everything before `to` or after `from`, or everything when both are left out, instead of a ten-year window ending today.
- Stop masking parts of UUIDs and long numeric ids as personnummer in logs and problem details.
- Refuse a reminder update whose body gives the id as a decimal such as `7.0`, as document updates already do.
- Describe the answer of `GET /professional-web/persons/{partyId}/documents` as an object in the OpenAPI, as Lifecare sends it.
- Answer 400 to a negative `offset` on `GET /users` instead of passing it to FamilyCare.
- Rename the OpenAPI operations `getAmount`, `getSharedCost` and `getJobStimuli` to `getCalculationAmount`, `getCalculationSharedCost` and `getCalculationJobStimuli`.

## 2026-11-24

- FamilyCare: pass a 404 for a person or document content through with Lifecare's message.
- Remove null guards in the mappers and the error decoder that never trigger.
- ProfessionalWeb: put the api2 calls behind one client, one integration and one service (HYDRAN-3431).
- Let dept44's PII masking keep UUIDs readable in LogSanitizer instead of doing it by hand.

## 2026-11-23

- Party: drop the `max-party-ids-per-call` and `max-legal-ids-per-call` settings.
- Party: split the lookup into one method per direction.
- ProfessionalWeb: make `amountType` and `calculationId` optional on the decision proposal (HYDRAN-3431).
- ProfessionalWeb: ask for the household by service instead of by partyId (HYDRAN-3431).
- ProfessionalWeb: answer 502 without a retry when Lifecare answers a write with an HTML page (HYDRAN-3431).
- ProfessionalWeb: move a service's job stimulus periods to the job stimulus resource (HYDRAN-3431).
- ProfessionalWeb: fix prints posting the answered parameter questions as an empty form (HYDRAN-3431).
- ProfessionalWeb: fix the Referer of a print form sent again after a session renewal (HYDRAN-3431).

## 2026-11-20

- ProfessionalWeb: add resources for reminders, payments, payees, household, decisions, journal notes, documents,
  calculations, job stimulus and prints (HYDRAN-3431).
- ProfessionalWeb: map api2 refusals to 400, 403, 404, 422 and 502 with Lifecare's text (HYDRAN-3431).
- ProfessionalWeb: send sign-in and exchange over OkHttp pinned to HTTP/1.1 (HYDRAN-3431).
- ProfessionalWeb: lay out the code like the other integrations (HYDRAN-3431).

## 2026-11-19

- Rename the EC and FC integrations to `elderlycare` and `familycare`. Breaks deployments: the keys become
  `integration.elderlycare.*` and `integration.familycare.*` (env `INTEGRATION_ELDERLYCARE_*`,
  `INTEGRATION_FAMILYCARE_*` including `USERKEY`), and the Feign client, circuit breaker and error names follow.
- Require the Lifecare keys percent-encoded and refuse to start on a decoded key. Breaks deployments holding a decoded key.
- Return nested lists in calculations, payments, investigations, services and family care decisions as `[]`, not `null`.
- Split the decision and request mappers and map lists inline.

## 2026-11-18

- Add `docs/`, `AGENTS.md` and this changelog.
- Keep Lifecare's message for statuses without an `HttpStatus` constant.
- Resolve party ids and caseworker names in the services before mapping.

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

