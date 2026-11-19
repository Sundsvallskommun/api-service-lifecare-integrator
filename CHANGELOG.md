# Changelog

Every change to the service, newest date first. One short line per change.

## 2026-11-19

- Give a person without a personnummer a null party id explicitly, and test the party id mapping in every flow.
- Replace the text-only PDF fixture in `DocumentsIT` with a minimal binary PDF, so git never converts its line endings.
- Map lists inline in the mappers and drop `mapList` and its local copies; paged mappings return early on a null composite.
- Split the proposal and calculation request mappings into one small converter per list.

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

