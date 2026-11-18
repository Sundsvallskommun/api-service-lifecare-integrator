# Documentation rules

These rules apply to every change under `docs/` and keep its structure the same from one change to the next.

## Layout

```
docs/
  README.md          index, one line per document
  architecture.md    components, cross-cutting rules, which endpoint reads which source
  integrations/      one file per external system
  adr/               decision records, created with the first record
```

- An integration file is named after the integration's key under `integration:` in
  `src/main/resources/application.yml` (`lifecare-ec.md`, `party.md`), and follows the sections of the existing files.
- A new subdirectory starts once it has at least two files; until then the file sits directly in `docs/`.
- File names are kebab-case. Decision records are named `adr/NNNN-short-title.md`, numbered from `0001`.
- Every file added, renamed or removed is reflected in `docs/README.md` in the same change.

## Where each kind of knowledge lives

Each fact has one home. The documentation links to the others instead of restating them.

|                            Knowledge                            |                   Home                   |
|-----------------------------------------------------------------|------------------------------------------|
| The map: what talks to what, knowledge spanning several classes | `docs/`                                  |
| Behaviour of a single class                                     | That class's Javadoc                     |
| What a configuration property means                             | Its comment in `application.yml`         |
| Request and response fields                                     | `src/main/resources/openapi.yaml`        |
| Ownership, environments, contacts, operations                   | Confluence, linked from `docs/README.md` |

## Writing

- Plain Markdown in English. Describe flows as numbered lists or tables.
- Reference code by repository-relative path, e.g. `src/main/java/.../DecisionService.java`.
- Mark anything you could not verify from the code as `TODO:` followed by what is missing.

## Decision records

Write one when a choice is not obvious from the code and someone could reasonably undo it. Sections: **Status**
(proposed, accepted, superseded by NNNN), **Context**, **Decision**, **Consequences**. Half a page is enough.
