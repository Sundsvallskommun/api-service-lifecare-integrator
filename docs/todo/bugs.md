# Bugs

## `@CircuitBreaker` does not seem to take effect on the Feign clients

A probe on 2026-11-20 called `FamilyCareClient` and the reminder calls now on `ProfessionalWebClient` in a `junit`
context and found no call buffered in their circuit breakers (`familycare`, `lifecare-professionalweb`), although
both instances exist with their `ignoreExceptions`. The annotation sits on the Feign interfaces, as in the other
api-service-* services. Check whether the resilience4j aspect applies to the Feign proxies here at all.

Done when a test shows a failing call counted by the breaker of a Feign client, or the reason it is not is known.

## An HTML error page is read as a lost ProfessionalWeb session

`ProfessionalWebExchange.needsSession` treats every `text/html` answer as "session needed", whatever the status. A 404
or 500 page in HTML from Lifecare or IIS makes `ProfessionalWebExchange` bootstrap, sign in again for the whole
process, and send the call up to three times before answering 502. For a write such as `CreateReminder` that can
create the same thing more than once. It applies to exchange only: the api2 Feign client counts HTML as a lost
session only with a success status.

Check first that Lifecare's sign-in page always comes with status 200. Done when only a successful HTML answer counts
as a lost session, with tests for a 404 and a 500 HTML page.

## A print form sent again after a session renewal carries the old token

`ProfessionalWebIntegration` posts a template's answered parameter questions with the `X-LEGACY-TOKEN` field read
off the page. When Lifecare refuses that post for a missing session, the Feign retryer renews the session and sends
the same body again, so the form still holds the token of the session that was lost. The header carries the new
token, but whether Lifecare checks the form field is not known.

Done when a print form sent again after a renewal carries the current token, or it is shown that Lifecare reads only
the header.

## A successful HTML error page on an api2 read counts as a lost session

`ProfessionalWebSessionResponseInterceptor` treats any HTML answer with a success status to a GET, other than a print's
parameter questions, as a lost session. An HTML error page that IIS or Lifecare answers with 200 therefore makes the
client bootstrap, sign in again for the whole process, ask twice more, and answer 502 with "would not accept a
freshly established session", which points at the session instead of the page.

Done when only Lifecare's sign-in page counts as a lost session on a read, and another HTML page answers 502 with a
text that says so, with tests for both.

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
