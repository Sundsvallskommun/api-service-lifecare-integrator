# AGENTS.md

Documentation lives in `docs/`, in English. `docs/README.md` is the index.

Update the documentation in the same change as the code:

- An integration added or removed, or its calls or authentication changed → `docs/integrations/<integration>.md`.
- An endpoint added or removed, or the source it reads changed → `docs/architecture.md`.
- A configuration property added, renamed or removed → `README.md`.

Every change adds one line to `CHANGELOG.md` under today's date (`## YYYY-MM-DD`), newest date first. Create the
heading if today has none. One short sentence in English, in the imperative ("Add …", "Fix …"), with the Jira key in
parentheses when there is one.

Read `docs/AGENTS.md` before editing anything under `docs/`.
