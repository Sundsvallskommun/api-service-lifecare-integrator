# Lifecare ProfessionalWeb

The internal api2 surface that Lifecare's own web client uses, reached for api-service-caremanagement. The answers
are passed on as Lifecare's JSON, not interpreted. Where it is heading: [todo/professionalweb-api2.md](../todo/professionalweb-api2.md).

## Used by

`POST /professional-web/exchange` and the reminder resources under `/professional-web/`. See
[architecture.md](../architecture.md).

## Calls

Two ways in, sharing one session:

1. Exchange passes any api2 or RenderPdf path through, named by the caller, in `ProfessionalWebExchange`.
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
    uses, OkHttp pinned to HTTP/1.1 like the Feign clients;
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

  |   Lifecare    |                      Answer                      |
  |---------------|--------------------------------------------------|
  | 400           | 400                                              |
  | 403           | 403                                              |
  | 404           | 404                                              |
  | 461           | 422                                              |
  | anything else | 502, with Lifecare's status in front of its text |

  Every answer carries Lifecare's own text as its detail, for 500 including the id Lifecare's support asks for. The
  text comes from `exceptionMessage`, `ExceptionMessage`, `message` or `Message`, in that order. Without one the detail
  is a standard text.

- Request and response bodies are never logged.

