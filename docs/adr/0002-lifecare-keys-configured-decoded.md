# 0002 Lifecare keys are configured decoded

**Status:** accepted, 2026-11-19

## Context

The Lifecare API keys are base64, so they can contain `+`, `/` and `=`. Lifecare hands each key out in two forms,
decoded (`ab+cd=`) and percent-encoded (`ab%2Bcd%3D`). The key travels in the query string, where a raw `+` is read as
a space, so it has to be encoded exactly once on the way out.

The service used to accept both forms. It looked for a `%XX` sequence, decoded the key if it found one, and
encoded the result. That took a pattern, a decode step that had to guard `+` from `URLDecoder`, and tests for both
forms, all to cover a choice we make ourselves when we set the configuration.

## Decision

`integration.elderlycare.key`, `integration.familycare.key` and `integration.familycare.user-key` hold the key in
its decoded form. `ElderlyCareConfiguration` and `FamilyCareConfiguration` encode it once with `URLEncoder` and
write a space as `%20`, not `+`.

## Consequences

- A key configured in its encoded form is encoded again (`%2B` becomes `%252B`) and no longer matches. Lifecare does
  not always say so: a wrong key on the FC person-based reads can answer an empty `200`. Every environment has to hold
  the decoded form before this change is deployed.
- Encoding the key is one call, small enough to live in each configuration class instead of a shared helper, and
  there is one form to test.

