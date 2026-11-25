# Lifecare Integrator

_Integration service towards Lifecare (Tieto/TietoEvry care management system) for the Sundsvall microservice landscape._

## Getting Started

### Prerequisites

- **Java 25 or higher**
- **Maven**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Sundsvallskommun/api-service-lifecare-integrator.git
   cd api-service-lifecare-integrator
   ```
2. **Configure the application.** See [Configuration](#configuration). The service does not start without the EC,
   FamilyCare, Party and Employee settings.
3. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

- **[api-service-party](https://github.com/Sundsvallskommun/api-service-party)** translates between partyId and
  personnummer. OAuth2 client credentials, registration `party`.
- **[api-service-employee](https://github.com/Sundsvallskommun/api-service-employee)** puts a name to a Lifecare
  caseworker id when Lifecare sends none. OAuth2 client credentials, registration `employee`.
- **Lifecare elderly care (EC)**, **Lifecare FamilyCare (FC)** and **Lifecare ProfessionalWeb**, run by the vendor.

How each one is called is in [docs/integrations/](docs/integrations/).

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `src/main/resources/openapi.yaml` file for the OpenAPI specification.

Architecture and integration notes are in [docs/](docs/README.md).

## Usage

### Example Request

```bash
curl -X GET "http://localhost:8080/2281/person?partyId=81471222-5798-11e9-ae24-57fa13b361e1"
```

## Configuration

`src/main/resources/application.yml` holds only the keys every environment overrides, without comments. Every other
key has its default in code. The tables below list every key, its default and what it means.
Override every secret per environment and never commit one.

The OAuth2 clients for Party and Employee are the usual `spring.security.oauth2.client.registration.party` and
`.employee` settings (`client-id`, `client-secret`) with a matching `provider` token URL.

### `integration.elderlycare`

|        Key        | Default |                                                       Meaning                                                       |
|-------------------|---------|---------------------------------------------------------------------------------------------------------------------|
| `url`             |         | Host plus module path, ending in `/WE.EC.Integration.Host`. Required.                                               |
| `domain`          |         | Lifecare domain, sent as a query parameter. Required.                                                               |
| `key`             |         | API key, sent as a query parameter. Required. Secret.                                                               |
| `connect-timeout` | `5`     | Seconds.                                                                                                            |
| `read-timeout`    | `30`    | Seconds.                                                                                                            |
| `log-level`       | `NONE`  | Feign log level. Request URLs carry the personnummer and the key, so any level above `NONE` writes both to the log. |

EC rejects a request that has the key in a header as well, so the key goes in the query and nowhere else. Give it
percent-encoded (`%2B`, `%2F`, `%3D`, never a raw `+`, `/` or `=`). It is sent as it is, and a decoded key stops the
service at startup.

### `integration.familycare`

|        Key        | Default |                                Meaning                                 |
|-------------------|---------|------------------------------------------------------------------------|
| `url`             |         | Host plus module path, ending in `/WESE.FC.Api.FC`. Required.          |
| `domain`          |         | Lifecare domain. Required.                                             |
| `key`             |         | API key for the person-based reads. Required. Secret, percent-encoded. |
| `user-key`        | `key`   | API key for `/Users/*`. Secret, percent-encoded.                       |
| `connect-timeout` | `5`     | Seconds.                                                               |
| `read-timeout`    | `30`    | Seconds.                                                               |
| `log-level`       | `NONE`  | Feign log level, as for EC.                                            |

`/Users/*` is licensed to a separate consumer ("Användarinformation IFO") from the person-based case APIs ("API IFO
Personbaserade"). Set `user-key` where that split applies. The two refuse the wrong key in different ways. `/Users`
answers 403 to a case key. The person-based reads answer 200 with an empty body to a user key, and nothing in the log
says so.

### `integration.party`

|         Key          | Default |                     Meaning                     |
|----------------------|---------|-------------------------------------------------|
| `url`                |         | api-service-party. Required.                    |
| `connect-timeout`    | `5`     | Seconds.                                        |
| `read-timeout`       | `30`    | Seconds.                                        |
| `cache-time-to-live` | `PT15M` | How long a lookup stays in the in-memory cache. |
| `cache-maximum-size` | `10000` | Most lookups held per direction.                |

A partyId and its personnummer belong together for life, so the time to live only bounds how long the heap holds
them. A lookup Party cannot answer is never cached.

### `integration.employee`

|        Key        |  Default   |              Meaning              |
|-------------------|------------|-----------------------------------|
| `url`             |            | api-service-employee. Required.   |
| `domain`          | `personal` | Employee's domain for the lookup. |
| `connect-timeout` | `5`        | Seconds.                          |
| `read-timeout`    | `10`       | Seconds.                          |

Employee is asked only when Lifecare sends a caseworker without a name. A failure leaves the name out and never fails
the decision read.

### `integration.lifecare-professionalweb`

|                                             Key                                             |                    Default                    |                                                                Meaning                                                                |
|---------------------------------------------------------------------------------------------|-----------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `url`                                                                                       |                                               | Lifecare host without a module path. Unset turns the integration off, and every ProfessionalWeb call, exchange included, answers 502. |
| `domain`                                                                                    |                                               | Lifecare domain, such as `SundsvallVoO_PLUS`.                                                                                         |
| `actor`                                                                                     | `Actor_Professional`                          | Lifecare actor.                                                                                                                       |
| `idp-method`                                                                                | `saml`                                        | Identity provider method.                                                                                                             |
| `federation-profile`                                                                        |                                               | Federation profile, such as `Sundsvall_Intra`.                                                                                        |
| `username`, `password`                                                                      |                                               | The integration account. Secrets.                                                                                                     |
| `session-ttl`                                                                               | `20m`                                         | How long a session is used before the service signs in again.                                                                         |
| `connect-timeout`                                                                           | `10`                                          | Seconds.                                                                                                                              |
| `read-timeout`                                                                              | `30`                                          | Seconds.                                                                                                                              |
| `session-cookie`                                                                            |                                               | Cookie header of a session the integration account signed in to elsewhere. Secret.                                                    |
| `keep-alive-interval`                                                                       | `5m`                                          | How often a held session is kept alive.                                                                                               |
| `print.decision-template-id`, `print.calculation-template-id`, `print.document-template-id` | the templates api-service-caremanagement uses | The Lifecare print templates the `pdf` resources render with. Lifecare refuses a document template the document's owner lacks.        |

`domain`, `actor`, `idp-method` and `federation-profile` are what Lifecare's identity portal reads off the entry URL
on a browser's first visit. The service signs in with `username` and `password` through MobilityGuard. Where
MobilityGuard cannot be reached, set `session-cookie` instead. The service then uses that session rather than signing
in. It is a live credential, and it has to be replaced when Lifecare stops accepting it.

### Logging

`logbook.excluded.paths` keeps `/**/professional-web/**` out of dept44's request and response payload log. Those paths
carry what caseworkers wrote and Lifecare's answers, personnummer included, in both directions.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-lifecare-integrator&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-lifecare-integrator)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-lifecare-integrator&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-lifecare-integrator)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-lifecare-integrator&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-lifecare-integrator)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-lifecare-integrator&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-lifecare-integrator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-lifecare-integrator&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-lifecare-integrator)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-lifecare-integrator&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-lifecare-integrator)

---

© 2024 Sundsvalls kommun
