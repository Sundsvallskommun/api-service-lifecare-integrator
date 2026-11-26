# 0005 The ProfessionalWeb keep-alive uses plain Spring scheduling

**Status:** accepted, 2026-11-25

## Context

dept44 asks for scheduled jobs to run through `@Dept44Scheduled`. That annotation locks each run with ShedLock, through
a `JdbcTemplateLockProvider` on the service's `DataSource`, so that only one instance runs a job at a time.

`ProfessionalWebKeepAlive` pings Lifecare's Heartbeat every `keep-alive-interval` so the ProfessionalWeb session does
not time out while idle. Each instance holds its own session in memory (`ProfessionalWebSession`). The service has no
database.

## Decision

`ProfessionalWebKeepAlive` uses Spring's `@Scheduled` and `@EnableScheduling`, not `@Dept44Scheduled`.

## Consequences

- No database is added for a lock nobody needs. A lock would also be wrong here: it would let one instance keep its
  session alive and let every other instance's session time out.
- Every instance runs the keep-alive on its own schedule, against its own session. That is the intended behaviour.
- If the service ever gets a database for other reasons, this still stays on `@Scheduled`, since the job is per
  instance by design.

