# Decisions needed

Behaviour that looks wrong but needs a decision before anyone changes it.

## No ownership check on documents and attachments

`GET /documents/{id}/content` returns any document whose id the caller knows, and
`POST /actualisations/{id}/attachments` attaches a file to any actualisation. Decisions answer "not found" for an id
that belongs to someone else; these two do not check at all. Possibly a security problem.

Code: `DocumentResource`, `FamilyCareService.getDocumentContent`, `FamilyCareService.addActualisationAttachment`.

## `from` and `to` mean different things per source

EC decisions are filtered locally on overlap with the window. FC decisions are filtered by FamilyCare itself, with a
ten-year default window. A by-id lookup in FC always uses the default window, so a decision shown in the list can
answer 404 on `GET /decisions/{decisionId}`.

Code: `DecisionService`, `DateWindow`.

## `fetchSource` catches every exception

The decision list catches `Exception` around both the Lifecare call and the mapping. A bug in a mapper, or a Party
error, is reported as the source being `UNAVAILABLE`. The same Party error in the calculation flow answers 502.

Code: `DecisionService.fetchSource`.

## Page numbers are translated one way only

The page number goes down to FC 0-based (`toFcPageNr`), but the page in FC's answer is passed back as it is. If FC
answers 0-based, the client gets `page=0`. Needs checking against a real FC first.

Code: `FamilyCareIntegration.toFcPageNr`, `MapperUtil.toPagingMetaData`.

## `Location` points at resources that do not exist

`POST /calculations` and `POST /actualisations` answer 201 with a `Location` of `/{municipalityId}/calculations/{id}`
and `/{municipalityId}/actualisations/{id}`. Neither path has a GET.

Code: `CalculationResource`, `ActualisationResource`.

## A handed-in ProfessionalWeb session stops being kept alive (on hold)

A session cookie configured by hand is no longer kept alive after its time to live (20 minutes), which contradicts
the comment in `application.yml`. A reset also throws away the cookies Lifecare has rotated. On hold until the api2
work in [professionalweb-api2.md](professionalweb-api2.md) reaches the session code.

Code: `ProfessionalWebSession`.
