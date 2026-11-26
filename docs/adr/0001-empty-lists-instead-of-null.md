# 0001 Empty lists instead of null

**Status:** accepted, 2026-11-19

## Context

Lifecare sometimes sends a list as `null`. The mappers treated that two ways. A top-level list, such as the payments
on a page, became `[]`. A nested list, such as the persons on a payment, became `null`. A client had to know which
kind of list it was reading to handle it safely.

One rule for every list is easier for a client than two. It can iterate any list without a null check.

## Decision

Every list in a response is a list, never `null`. When Lifecare sends `null` or leaves the field out, the response
holds `[]`.

Each mapper method that maps a list starts with `Optional.ofNullable(list).orElse(emptyList()).stream()`. A single
object that is missing still maps to `null`.

## Consequences

- The nested lists changed from `null` to `[]`: `persons`, `incomes`, `expenses` and `specialExpenses` on a
  calculation, `persons` on a payment, an investigation, a service and a family care decision. A client that told
  `null` apart from `[]` sees a difference. The schema in `openapi.yaml` is unchanged.
- A new list mapping follows the same form, so there is one way to read it. Undoing this decision for one field
  brings back the split this record removed.

