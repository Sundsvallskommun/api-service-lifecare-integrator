# 0002 Lifecare keys are configured percent-encoded

**Status:** accepted, 2026-11-19

## Context

The Lifecare API keys are base64, so they can contain `+`, `/` and `=`. Lifecare hands each key out in two forms,
decoded (`ab+cd=`) and percent-encoded (`ab%2Bcd%3D`). The key travels in the query string, where a raw `+` is read as
a space, so it has to reach Lifecare encoded exactly once.

The service used to accept both forms. It looked for a `%XX` sequence, decoded the key if it found one, and
encoded the result. That took a pattern, a decode step that had to guard `+` from `URLDecoder`, and tests for both
forms, all to cover a choice we make ourselves when we set the configuration.

## Decision

`integration.elderlycare.key`, `integration.familycare.key` and `integration.familycare.user-key` hold the key
percent-encoded. `ElderlyCareConfiguration` and `FamilyCareConfiguration` pass it on as it is. Feign leaves an
existing `%XX` alone, so the key reaches Lifecare encoded once.

The properties records reject a key that contains `+`, `/` or `=`, so a decoded key stops the service at startup.

## Consequences

- No encoding code. The only thing to test is that the key reaches the wire unchanged, which
  `FamilyCareKeySelectionTest` does through a real Feign client.
- The service depends on Feign leaving `%XX` alone. A Feign upgrade that changed that would show up in that test.
- A decoded key fails at startup instead of failing silently. Lifecare does not always report a wrong key: on the FC
  person-based reads it can answer an empty `200`.

