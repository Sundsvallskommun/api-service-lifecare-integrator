# ProfessionalWeb api2 as resources

## Goal

api-service-caremanagement reaches Lifecare's api2 through `POST /{municipalityId}/professional-web/exchange`, which
passes any api2 or RenderPdf call through: the caller names the method, the path and the body. That makes the
integration account reachable for any api2 operation, and it keeps all knowledge of an undocumented API in
caremanagement.

The target is one REST resource per api2 operation caremanagement uses, backed by one `@FeignClient` per api2 area,
built the way the other api-service-* integrations are. Exchange stays until every client has moved, then it goes.

## Where we are

Reminders is done as the pilot (HYDRAN-3431), and the pattern for the other areas is set by it:

- Six resources under `/{municipalityId}/professional-web/`: the reminders of a service, the proposal for a new one,
  one reminder, create, update and remove. See [architecture.md](../architecture.md).
- `ProfessionalWebReminderClient`, a `@FeignClient` with `@CircuitBreaker`, on `ProfessionalWebApi2Configuration`.
  The session, its renewal and the error mapping are Feign parts in that configuration. See
  [lifecare-professionalweb.md](../integrations/lifecare-professionalweb.md).
- Exchange is unchanged and shares the same session. Its code is in the `legacy` packages.
- Sign-in, the session and the exchange transport run on OkHttp pinned to HTTP/1.1, the same as the Feign clients.
- Unit tests per layer, and `src/integration-test/java/apptest/ProfessionalWebRemindersIT.java` runs every flow end to
  end against WireMock, exchange and a resource sharing the session included.

Done since, on the same pattern, with unit tests per layer. Where an integration test is named, it is under
`src/integration-test/java/apptest/` and runs the area end to end against WireMock:

- Job stimulus: the end date of a period and saving a person's periods, on `ProfessionalWebCalculationClient`.
  Integration test: `ProfessionalWebJobStimulusIT`.
- Payment, Payee and Household: the underlag and the latest payments of a service, a person's household on a date,
  and creating a payment and a payee, on `ProfessionalWebPaymentClient`. Integration test: `ProfessionalWebPaymentsIT`.
- Decision: the underlag for a decision made from a calculation, the reasons of a decision type, and reading,
  creating and updating a decision, on `ProfessionalWebDecisionClient`. Integration test: `ProfessionalWebDecisionsIT`.
- Document: a person's journal notes and documents, reading one to show or to edit, the proposals of a service, and
  creating and updating a journal note or a document, on `ProfessionalWebDocumentClient`. Integration test:
  `ProfessionalWebDocumentsIT`.
- Calculation: the underlag and the latest calculation of a service, reading one to show or to edit, a service's job
  stimulus, the four reckonings that change nothing in Lifecare, and creating, updating and removing a calculation, on
  `ProfessionalWebCalculationClient`. Integration test: `ProfessionalWebCalculationsIT`.
- Prints: Lifecare's PDF of a calculation, a decision and a document, on `ProfessionalWebPrintClient`, with the
  template ids under `integration.lifecare-professionalweb.print`. A template's parameter questions are answered with
  Lifecare's defaults and posted back as a form. Integration test: `ProfessionalWebPrintsIT`.

All areas caremanagement uses now have resources and an integration test.

Decided along the way:

- REST paths per domain object under `/{municipalityId}/professional-web/`, not api2's names.
- Lifecare's JSON passes through untouched in both directions, as bytes. caremanagement builds every write from an
  object Lifecare handed out, with a few fields changed, and has no typed model of api2 to reuse. A typed model here
  would have to keep the fields it does not know.
- Lifecare's refusals become 400, 403, 404, 422 (for its 461) and 502. The 4xx ones do not count against the
  circuit breaker.
- A person is given as a partyId, never a personnummer.
- The `RenderPdf` prints move here too, as resources that return Lifecare's own PDF. caremanagement shows Lifecare's
  print today and does not render these with templating, so the PDF stays Lifecare's. This service answers a
  template's parameter questions with Lifecare's defaults, as caremanagement does now, and holds the template ids as
  configuration. Templating stays outside this service.
- The session renewal exists twice while exchange lives, once in `ProfessionalWebExchange` and once in the Feign
  configuration. That is accepted for the transition.

## Adding an area

1. A `@FeignClient` beside `ProfessionalWebReminderClient` on `ProfessionalWebApi2Configuration`, with one method per
   operation and the full path including `/WESE.FC.ProfessionalWeb`. Every client after the first shares its name
   and circuit breaker and needs a `contextId` of its own.
2. A service with the checks the resources need, such as a body that must be a JSON object or an id in the body that
   must match the path.
3. A resource per operation, returning Lifecare's bytes, and the OpenAPI baseline.
4. An integration test per way the area is used.

## After the areas

- caremanagement moves to the resources, area by area, and stops calling exchange.
- Remove exchange: the `legacy` packages under `integration/professionalweb/`, `api/` and `api/model/professionalweb/`,
  with their tests. The Feign configuration then holds the only session renewal.
- Decide whether caremanagement keeps its own direct ProfessionalWeb transport once everything goes through this
  service.

## Open

- `@CircuitBreaker` does not seem to take effect on any Feign client in this service. See [bugs.md](bugs.md).
- Exchange still reads an HTML error page as a lost session. The Feign clients do not. See [bugs.md](bugs.md).

