# Bugs

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
