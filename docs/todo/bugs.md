# Bugs

## `@CircuitBreaker` does not seem to take effect on the Feign clients

A probe on 2026-11-20 called `FamilyCareClient` and `ProfessionalWebReminderClient` in a `junit` context and found
no call buffered in their circuit breakers (`familycare`, `lifecare-professionalweb`), although both instances exist
with their `ignoreExceptions`. The annotation sits on the Feign interfaces, as in the other api-service-* services.
Check whether the resilience4j aspect applies to the Feign proxies here at all.

Done when a test shows a failing call counted by the breaker of a Feign client, or the reason it is not is known.

## An HTML error page is read as a lost ProfessionalWeb session

`ProfessionalWebHttp.needsSession` treats every `text/html` answer as "session needed", whatever the status. A 404
or 500 page in HTML from Lifecare or IIS makes `ProfessionalWebExchange` bootstrap, sign in again for the whole
process, and send the call up to three times before answering 502. For a write such as `CreateReminder` that can
create the same thing more than once. It applies to exchange only: the api2 Feign clients count HTML as a lost
session only with a success status.

Check first that Lifecare's sign-in page always comes with status 200. Done when only a successful HTML answer counts
as a lost session, with tests for a 404 and a 500 HTML page.

## FC decision list throws on `"Result": null`

`FamilyCareIntegration.getAllDecisions` adds `result.getResult()` to the list without a null check. When FamilyCare
answers a page with `"Result": null`, the call throws a `NullPointerException`, and `GET /decisions` reports FC as
unavailable.

Done when: a page with `"Result": null` adds nothing and the loop goes on, with a test for it.

## The Party circuit breaker counts client errors

`resilience4j.circuitbreaker.instances` in `application.yml` sets `ignoreExceptions: ClientProblem` for
`elderlycare`, `familycare` and `employee`, but not for `party`. A run of 4xx answers from Party, such as unknown
partyIds, can open the breaker and take down every flow that resolves a party.

Done when: `party` ignores `ClientProblem` like the other instances.
