# AGENTS.md

Documentation lives in `docs/`, in English. `docs/README.md` is the index.

Keep `docs/` true to the code at every commit. Each change updates the documentation it affects in the same commit,
not in a later one. A change is done when every statement in `docs/` and `README.md` about the code it touched still
holds. The usual cases:

- An integration added or removed, or its calls or authentication changed → `docs/integrations/<integration>.md`.
- An endpoint added or removed, or the source it reads changed → `docs/architecture.md`.
- A rule that holds across the service added or changed, such as what a response contains → `docs/architecture.md`.
- A choice that is not obvious from the code and could reasonably be undone → a new record in `docs/adr/`.
- A configuration property added, renamed or removed → `README.md`.
- Work found but left for later → an item in `docs/todo/`; work that finishes an item → that item deleted.

Every change adds one line to `CHANGELOG.md` under today's date (`## YYYY-MM-DD`), newest date first. Create the
heading if today has none. One short sentence in English, in the imperative ("Add …", "Fix …"), with the Jira key in
parentheses when there is one.

Read `docs/AGENTS.md` before editing anything under `docs/`.

## Code conventions

- Method names start with a verb that says what the method does (`getCaseworkerNames`, `collectPersonNumbers`).
  Predicates read as a question (`lacksName`); converters start with `to` (`toDecision`).
- Write conditionals as `if` statements or an Optional chain, not the ternary operator.
- Let names and structure explain the code. Add a comment only for a genuinely complex case the code cannot make
  clear on its own, such as a Lifecare quirk, and keep it short.
- References run one way: `docs/` points at the code, and nothing outside `docs/` points into it. Code, comments,
  validation and error messages, `application.yml`, `openapi.yaml` and `CHANGELOG.md` state what they need in their
  own words, without naming an ADR or a docs file.

