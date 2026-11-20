# Lifecare ProfessionalWeb

The internal api2 surface that Lifecare's own web client uses. This service passes calls through to it for
api-service-caremanagement and does not interpret the answers.

## Used by

`POST /professional-web/exchange`. See [architecture.md](../architecture.md).

## Calls

Two ways in, sharing one session:

1. Exchange passes any api2 or RenderPdf path through, named by the caller.
2. `ProfessionalWebApi2Client`, a Feign client with one method per api2 operation that has its own resource. Its
   Feign `Client` hands each request to `ProfessionalWebExchange`, so it gets the same session, escalation and
   logging as exchange. It does not open connections of its own.

There is no spec. The client is written by hand, not generated.

## Authentication

A signed-in session of an integration account (SAML via MobilityGuard), not an API key. One session is shared by the
whole process and kept in memory. While MobilityGuard cannot be reached, a session cookie signed in elsewhere can be
configured instead. See the comment on `integration.lifecare-professionalweb` in `application.yml`.

## Code and configuration

- Code: `src/main/java/se/sundsvall/lifecareintegrator/integration/professionalweb/`.
- Configuration: `integration.lifecare-professionalweb` in `application.yml`. Leaving `url` unset makes every exchange
  answer 502.

## Behaviour worth knowing

- Session lifecycle (sign-in, bootstrap, keep-alive, replacement): `ProfessionalWebSession`.
- When an answer says a session is needed, the call is retried after a bootstrap, then after a fresh sign-in, and
  otherwise answered with 502: `ProfessionalWebExchange`.
- Request and response bodies are never logged.

