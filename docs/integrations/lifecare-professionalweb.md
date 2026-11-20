# Lifecare ProfessionalWeb

The internal api2 surface that Lifecare's own web client uses. This service passes calls through to it for
api-service-caremanagement and does not interpret the answers.

## Used by

`POST /professional-web/exchange`. See [architecture.md](../architecture.md).

## Calls

Two ways in, sharing one session:

1. Exchange passes any api2 or RenderPdf path through, named by the caller, over the JDK client in
   `ProfessionalWebExchange`.
2. One Feign client per api2 area, such as `ProfessionalWebReminderClient`, with one method per operation that has its
   own resource. They share `ProfessionalWebApi2Configuration`: OkHttp pinned to HTTP/1.1, redirects not followed,
   the session added by a request interceptor and checked by a response interceptor, and a retryer that bootstraps,
   then signs in again, then answers 502.

There is no spec. The client is written by hand, not generated.

## Authentication

A signed-in session of an integration account (SAML via MobilityGuard), not an API key. One session is shared by the
whole process and kept in memory. While MobilityGuard cannot be reached, a session cookie signed in elsewhere can be
configured instead. See the comment on `integration.lifecare-professionalweb` in `application.yml`.

## Code and configuration

- Code, under `src/main/java/se/sundsvall/lifecareintegrator/integration/professionalweb/`:
  - the package itself: the api2 Feign clients, one per area;
  - `configuration/`: their Feign configuration (interceptors, retryer, error decoder) and the properties;
  - `session/`: sign-in, the session and its cookies, keep-alive, the health indicator, and the HTTP client sign-in
    uses;
  - `legacy/`: `ProfessionalWebExchange`, behind exchange. Its resource and models are in `api/legacy/` and
    `api/model/professionalweb/legacy/`. All of `legacy/` goes when exchange does.
- Configuration: `integration.lifecare-professionalweb` in `application.yml`. Leaving `url` unset makes every exchange
  and api2 client call answer 502.

## Behaviour worth knowing

- Session lifecycle (sign-in, bootstrap, keep-alive, replacement): `session/ProfessionalWebSession`.
- When an answer says a session is needed, the call is retried after a bootstrap, then after a fresh sign-in, and
  otherwise answered with 502: `ProfessionalWebExchange` for exchange, `ProfessionalWebSessionRetryer` for the Feign
  clients. On the Feign clients an HTML answer counts as a lost session only with a success status.
- Exchange hands back every answer as data. The Feign clients turn a refusal into a problem instead, in
  `ProfessionalWebErrorDecoder`, and their circuit breaker `lifecare-professionalweb` ignores the 4xx ones:

  |   Lifecare    |          Answer           |
  |---------------|---------------------------|
  | 400           | 400, with Lifecare's text |
  | 403           | 403                       |
  | 404           | 404                       |
  | 461           | 422, with Lifecare's text |
  | anything else | 502                       |

  The text comes from `exceptionMessage`, `ExceptionMessage`, `message` or `Message`, in that order. Without one
  the answer says "Lifecare refused the request".

- Request and response bodies are never logged.

