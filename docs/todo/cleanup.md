# Cleanup

Code and documentation that work but should be simpler or more accurate.

## Code

- The eight `Paged*Response` classes become records, from about 580 lines to 70. Code: `api/model/*/Paged*Response.java`.
- `ElderlyCareDecisionMapper.toBaseDecision` copies an LSS decision into a SoL object that is thrown away straight
  after. Map the shared fields directly.
- `ElderlyCareClient` reads a single decision with `dismiss404` and a `null` answer. An `Optional` would say "maybe
  missing" in the type.
- `DecisionService`: `SourceResult` could carry its status directly, and `overlapsWindow` has a branch that cannot be
  reached.
- Block lambdas: the three `.map(decision -> { … })` in `DecisionService.findDecision` and
  `findFamilyCareDecision`. Use named methods.
- `FamilyCareService.createCalculation` still maps persons with `.map(persons -> persons.stream()…).orElse(List.of())`
  instead of `Optional.ofNullable(list).orElse(emptyList()).stream()`.
- `POST /professional-web/reminders` without an answer from Lifecare answers 201 with no body, where the other
  ProfessionalWeb writes answer 204. Use `ProfessionalWebResponses` there too.

## Tests

- A failure test for `persons[0].partyId` missing or invalid in `POST /calculations`.
- Nothing checks that the EC and FC Feign clients log at `NONE` by default. Removing the customizer would start
  logging keys and personnummer without a failing test.
- The tests added for the encoded keys are named without the `test…` prefix the rest of their files use.

## Configuration and documentation

- `integrations/familycare.md` says an FC 404 reaches the caller as 404. In the decision list it marks the source
  as unavailable instead.

