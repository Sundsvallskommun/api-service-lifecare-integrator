# ProfessionalWeb api2 as resources

api-service-caremanagement reaches api2 through `POST /{municipalityId}/professional-web/exchange`, which passes any
api2 or RenderPdf call through. The plan is to replace it with one REST resource per api2 operation, one area at a
time, with exchange kept until every client has moved.

## Areas

Reminders is the pilot. The rest follow the same pattern once it has been reviewed.

|           Area            |                                                                                          api2 operations careM uses                                                                                          |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Reminders                 | ListRemindersByServiceId, GetProposalForService, GetReminderEditComposite, CreateReminder, UpdateReminder, RemoveReminder                                                                                    |
| Calculation               | GetProposalService, ListCalculations, GetCalculation, GetCalculationForEdit, GetJobStimulusForService, PlacePersons, GetAmount, GetSharedCost, GetJobStimuliForCalculation, Create, Update, Delete           |
| Decision                  | GetProposalForService, GetMappedDecisionReasons, GetDecision, Create, Update                                                                                                                                 |
| Document                  | GetDocumentsListForClient, GetJournalNoteWithContent, GetDocumentWithContent, GetNoteProposalForService, GetDocumentProposalForService, CreateJournalNote, CreateDocument, UpdateJournalNote, UpdateDocument |
| Payment, Payee, Household | GetPaymentForCreate, GetLatestPayments, Payment/Create, Payee/Create, HasHouseholdThisDate                                                                                                                   |
| Job stimulus              | GetJobStimulusToDate, SaveJobStimulus                                                                                                                                                                        |

Decided so far: REST paths per domain object under `/{municipalityId}/professional-web/`; Lifecare's JSON passed
through untouched in both directions; Lifecare's refusals mapped to 400, 403, 404, 422 (for its 461) and 502; a person
is given as a partyId, not a personnummer.

## After the areas

- Remove exchange once api-service-caremanagement no longer calls it.
- The `RenderPdf` prints are not moving here. api-service-caremanagement will fetch the data from this service and
  render the PDF through templating. Until then the prints go through exchange.
- Decide whether api-service-caremanagement keeps its own direct ProfessionalWeb transport once everything goes
  through this service.

