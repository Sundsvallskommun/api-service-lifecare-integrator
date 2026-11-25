# 0004 A ProfessionalWeb update checks that the body's id matches the path

**Status:** accepted, 2026-11-25

## Context

`PUT /reminders/{reminderId}`, `PUT /journal-notes/{id}` and `PUT /documents/{id}` name the object twice: once in our
path and once in the body (`reminderId`, `documentId`). The Lifecare calls behind them, `Reminders/UpdateReminder/`,
`Document/UpdateJournalNote/` and `Document/UpdateDocument/`, take no id in their own URL. Lifecare updates the object
the body names.

The body goes to Lifecare as the bytes the client sent, as every ProfessionalWeb resource does
([architecture.md](../architecture.md)). The id in our path therefore never reaches Lifecare. Without a check, a client
could call `PUT /reminders/7` with a body naming reminder 8, and Lifecare would change reminder 8.

The two checks had been written separately and differed. The document check refused `1.0`, while the reminder check
accepted `7.0` as 7.

Three ways to keep path and body from disagreeing:

1. **Check that the body's id matches the path, and answer 400 when it does not.**
   - For: the body stays untouched. The API keeps the usual `PUT /…/{id}` shape that caremanagement already builds
     against. A client that sends the wrong id hears about it.
   - Against: the id is still given twice, and the check needs parsing the body.
2. **Write the path's id into the body before sending it.**
   - For: the path is the only source of the id, and no client can get it wrong.
   - Against: the body has to be read and written again. That breaks the untouched-body rule and can change other
     values on the way, such as `1500.50` becoming `1500.5`. A client that sent a wrong id is never told.
3. **Drop the id from our path** (`PUT /reminders` with the body alone), as Lifecare's own calls do.
   - For: the id exists in one place only.
   - Against: the paths stop looking like the rest of the API, and caremanagement's calls change.

## Decision

Option 1. `ProfessionalWebService.requireSameId` is the one check for all three updates. The body must be a JSON
object whose id field holds a whole number equal to the path's id. Anything else (another id, a string, `null`, a
decimal such as `7.0` or `7.5`, a number out of range, a missing or nested field) is answered with 400, and nothing is
sent to Lifecare.

A body that also has the id field in another case, such as `ReminderId` beside `reminderId`, is refused too. Lifecare
runs on ASP.NET and may read field names without regard to case, taking the last one. The check would then pass on
`reminderId` while Lifecare acted on `ReminderId`. How Lifecare reads them is not verified, so the check does not rely
on it.

## Consequences

- The body still passes through as the client sent it.
- An update of a reminder whose body gives the id as `7.0` is now refused, as one of a document already was.
- A new update that sends its body to a Lifecare call without an id in the URL uses `requireSameId`.
- The decision and calculation updates pass the path's id to Lifecare as `businessId`. Whether Lifecare also acts on
  an id in those bodies is not known, so they have no body check yet.

