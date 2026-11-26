# Before the next deploy

Configuration every environment needs before the changes of 2026-11-19 can start there.

- Rename the keys: `integration.lifecare-ec.*` becomes `integration.elderlycare.*` and `integration.lifecare-fc.*`
  becomes `integration.familycare.*`. As environment variables: `INTEGRATION_LIFECAREEC_*` becomes
  `INTEGRATION_ELDERLYCARE_*` and `INTEGRATION_LIFECAREFC_*` becomes `INTEGRATION_FAMILYCARE_*`, `USERKEY` included.
- Give `key` and `user-key` percent-encoded (`%2B`, `%2F`, `%3D`). The service refuses to start on a decoded key.
- Rename any overrides under `resilience4j.circuitbreaker.instances.lifecare-*` or
  `spring.cloud.openfeign.client.config.lifecare-*`, and any dashboards or alerts on the circuit breaker names.
- Clients that match on the problem `detail` see `elderlycare error: …` and `familycare error: …` instead of
  `lifecare-ec error: …` and `lifecare-fc error: …`.

Done when every environment runs the new configuration. Then delete this file.
