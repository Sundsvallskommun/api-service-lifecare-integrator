# Party

api-service-party. Translates between the public partyId and the personnummer that Lifecare needs.

## Used by

Every person-scoped operation: partyId → personnummer before calling Lifecare, and personnummer → partyId for people
named in the answer. See [architecture.md](../architecture.md).

## Calls

|                   Call                    |         Purpose         |
|-------------------------------------------|-------------------------|
| `POST /{municipalityId}/PRIVATE/legalIds` | partyIds → personnummer |
| `POST /{municipalityId}/PRIVATE/partyIds` | personnummer → partyIds |

## Authentication

OAuth2 client credentials, registration `party`.

## Code and configuration

- Client: `src/main/java/se/sundsvall/lifecareintegrator/integration/party/`, generated from
  `src/main/resources/integrations/party.yml`.
- Configuration: `integration.party` in `application.yml`.

## Behaviour worth knowing

- Both directions are cached in memory per municipality. Lookups that fail are not cached. See `PartyIntegration`.

