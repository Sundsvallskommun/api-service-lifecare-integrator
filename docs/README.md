# Documentation

Getting started and configuration are in the [root README](../README.md). The API contract is
[openapi.yaml](../src/main/resources/openapi.yaml). Changes are listed in the [changelog](../CHANGELOG.md).

|                                           Document                                           |                                     Covers                                      |
|----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| [architecture.md](architecture.md)                                                           | Components, cross-cutting rules, and which endpoint reads which source          |
| [integrations/elderlycare.md](integrations/elderlycare.md)                                   | Lifecare elderly care: SoL and LSS decisions                                    |
| [integrations/familycare.md](integrations/familycare.md)                                     | Lifecare family care: persons, case data, decisions, payments, documents, users |
| [integrations/lifecare-professionalweb.md](integrations/lifecare-professionalweb.md)         | Pass-through to Lifecare's internal api2 surface                                |
| [integrations/party.md](integrations/party.md)                                               | partyId ↔ personnummer                                                          |
| [integrations/employee.md](integrations/employee.md)                                         | Display names for Lifecare caseworkers                                          |
| [adr/0001-empty-lists-instead-of-null.md](adr/0001-empty-lists-instead-of-null.md)           | A list in a response is `[]`, never `null`                                      |
| [adr/0002-lifecare-keys-configured-encoded.md](adr/0002-lifecare-keys-configured-encoded.md) | Lifecare keys are configured percent-encoded and passed on as they are          |

Ownership, environments and contacts: Confluence. TODO: link to the service page.
