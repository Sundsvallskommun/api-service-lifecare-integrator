# Documentation

Getting started and configuration are in the [root README](../README.md). The API contract is
[openapi.yaml](../src/main/resources/openapi.yaml). Changes are listed in the [changelog](../CHANGELOG.md).

|                                       Document                                       |                                     Covers                                      |
|--------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| [architecture.md](architecture.md)                                                   | Components, cross-cutting rules, and which endpoint reads which source          |
| [integrations/lifecare-ec.md](integrations/lifecare-ec.md)                           | Lifecare elderly care: SoL and LSS decisions                                    |
| [integrations/lifecare-fc.md](integrations/lifecare-fc.md)                           | Lifecare family care: persons, case data, decisions, payments, documents, users |
| [integrations/lifecare-professionalweb.md](integrations/lifecare-professionalweb.md) | Pass-through to Lifecare's internal api2 surface                                |
| [integrations/party.md](integrations/party.md)                                       | partyId ↔ personnummer                                                          |
| [integrations/employee.md](integrations/employee.md)                                 | Display names for Lifecare caseworkers                                          |

Ownership, environments and contacts: Confluence. TODO: link to the service page.
