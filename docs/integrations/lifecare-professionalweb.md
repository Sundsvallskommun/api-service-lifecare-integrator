# Lifecare ProfessionalWeb

The internal api2 surface that Lifecare's own web client uses, reached for api-service-caremanagement. The answers
are passed on as Lifecare's JSON, not interpreted. Where it is heading: [todo/professionalweb-api2.md](../todo/professionalweb-api2.md).

## Used by

`POST /professional-web/exchange` and the api2 resources under `/professional-web/`. See
[architecture.md](../architecture.md).

## Calls

Two ways in, sharing one session:

1. Exchange passes any api2 or RenderPdf path through, named by the caller, in `ProfessionalWebExchange`.
2. One Feign client, `ProfessionalWebClient`, with one method per api2 operation that has its own resource, and the
   two `RenderPdf` calls a print needs. Its configuration is `ProfessionalWebApi2Configuration`: OkHttp pinned to
   HTTP/1.1, redirects not followed, the session added by a request interceptor and checked by a response
   interceptor, and a retryer that bootstraps, then signs in again, then answers 502. `ProfessionalWebService` calls
   it through `ProfessionalWebIntegration`, which holds what api2 expects: the businessType codes, the fixed
   parameters Lifecare's web app sends, the print queries, and the person a household is asked for.

There is no spec. The client is written by hand, not generated.

## Authentication

A signed-in session of an integration account (SAML via MobilityGuard), not an API key. One session is shared by the
whole process and kept in memory. While MobilityGuard cannot be reached, a session cookie signed in elsewhere can be
configured instead. See the comment on `integration.lifecare-professionalweb` in `application.yml`.

## Code and configuration

- Code, under `src/main/java/se/sundsvall/lifecareintegrator/integration/professionalweb/`:
  - the package itself: `ProfessionalWebClient`, `ProfessionalWebIntegration` and the print's parameter form;
  - `configuration/`: the client's Feign configuration (interceptors, retryer, error decoder) and the properties;
  - `session/`: sign-in, the session and its cookies, keep-alive, the health indicator, and the HTTP client sign-in
    uses, OkHttp pinned to HTTP/1.1 like the Feign client;
  - `legacy/`: `ProfessionalWebExchange`, behind exchange. Its resource and models are in `api/legacy/` and
    `api/model/professionalweb/legacy/`. All of `legacy/` goes when exchange does.
- Configuration: `integration.lifecare-professionalweb` in `application.yml`. Leaving `url` unset makes every exchange
  and api2 client call answer 502.

## Behaviour worth knowing

- Session lifecycle (sign-in, bootstrap, keep-alive, replacement): `session/ProfessionalWebSession`.
- When an answer says a session is needed, the call is retried after a bootstrap, then after a fresh sign-in, and
  otherwise answered with 502: `ProfessionalWebExchange` for exchange, `ProfessionalWebSessionRetryer` for the Feign
  client. On the Feign client an HTML answer counts as a lost session only on a read (GET) with a success status, and
  never when it is a print's parameter questions. HTML answers are read into memory for that check, in
  `ProfessionalWebHtmlBufferingClient`.
- A write (POST, PUT, DELETE) is sent again only when Lifecare refuses it for a missing session: 360, 401 or a redirect
  to the identity portal. A successful HTML answer to a write, other than a print's parameter questions, answers 502
  without a retry, in `ProfessionalWebSessionResponseInterceptor`. This matters most for `Payment/Create`, which moves
  money.
- Feign sends a query parameter with an empty value as its bare name. `ProfessionalWebEmptyQueryValueClient` puts the
  `=` back, so Lifecare gets `amountType=` as its web app sends it.
- Some print templates ask parameter questions before they render. `ProfessionalWebIntegration` answers them with
  Lifecare's defaults (`ProfessionalWebParameterForm`) and posts the form back to the same path and query. The request
  interceptor sends a form the way a browser does: navigation headers and the print's own address as Referer, also when
  the form is sent again after a session renewal. Asked
  again, or answered with something that is not a PDF, the print answers 502.
- The Feign client sends every body as the bytes it is given, with Feign's plain encoder set in
  `ProfessionalWebApi2Configuration`. dept44's encoder would post a print's form as an empty body.
- Exchange hands back every answer as data. The Feign client turns a refusal into a problem instead, in
  `ProfessionalWebErrorDecoder`, and its circuit breaker `lifecare-professionalweb` ignores the 4xx ones:

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

