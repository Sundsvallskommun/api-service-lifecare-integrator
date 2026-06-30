package se.sundsvall.lifecareintegrator.integration.lifecareec;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.lifecareec.configuration.LifecareEcConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.lifecareec.configuration.LifecareEcConfiguration.CLIENT_ID;

/**
 * Feign contract for the Lifecare Welfare API Services / Elderly care (EC) API ({@code WE.EC.Integration.Host}). Scoped
 * to the Mina Sidor decision facade: SoL and LSS decisions only (read-only). The mandatory {@code domain} + {@code key}
 * auth (and the
 * {@code X-API-Key} header) are added globally by {@link LifecareEcConfiguration}, so they are not part of these method
 * signatures. Citizen lookup is done by the caller as {@code q=PersonId:YYYYMMDDXXXX}.
 */
@FeignClient(name = CLIENT_ID, url = "${integration.lifecare-ec.url}", configuration = LifecareEcConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface LifecareEcClient {

	/**
	 * List SoL decisions, filtered by the {@code q} query (e.g. {@code PersonId:YYYYMMDDXXXX}).
	 *
	 * @param  gt     greater-than delta filter (UTC datetime, optional)
	 * @param  lt     less-than delta filter (UTC datetime, optional)
	 * @param  q      query filter, e.g. {@code PersonId:YYYYMMDDXXXX}
	 * @param  limit  page size (optional)
	 * @param  offset offset into the result (optional)
	 * @return        the SoL decisions
	 */
	@GetMapping(path = "/api/v1/sol_decisions", produces = APPLICATION_JSON_VALUE)
	List<WEECIntegrationContractsDecisionV1Decision> getSolDecisions(
		@RequestParam(value = "gt", required = false) final String gt,
		@RequestParam(value = "lt", required = false) final String lt,
		@RequestParam(value = "q", required = false) final String q,
		@RequestParam(value = "limit", required = false) final Integer limit,
		@RequestParam(value = "offset", required = false) final Integer offset);

	/**
	 * Read a single SoL decision by its Lifecare id.
	 *
	 * @param  id the decision id
	 * @return    the SoL decision
	 */
	@GetMapping(path = "/api/v1/sol_decisions/{id}", produces = APPLICATION_JSON_VALUE)
	WEECIntegrationContractsDecisionV1Decision getSolDecision(@PathVariable final String id);

	/**
	 * List LSS decisions, filtered by the {@code q} query (e.g. {@code PersonId:YYYYMMDDXXXX}).
	 *
	 * @param  gt     greater-than delta filter (UTC datetime, optional)
	 * @param  lt     less-than delta filter (UTC datetime, optional)
	 * @param  q      query filter, e.g. {@code PersonId:YYYYMMDDXXXX}
	 * @param  limit  page size (optional)
	 * @param  offset offset into the result (optional)
	 * @return        the LSS decisions
	 */
	@GetMapping(path = "/api/v1/lss_decisions", produces = APPLICATION_JSON_VALUE)
	List<WEECIntegrationContractsDecisionV1LssDecision> getLssDecisions(
		@RequestParam(value = "gt", required = false) final String gt,
		@RequestParam(value = "lt", required = false) final String lt,
		@RequestParam(value = "q", required = false) final String q,
		@RequestParam(value = "limit", required = false) final Integer limit,
		@RequestParam(value = "offset", required = false) final Integer offset);

	/**
	 * Read a single LSS decision by its Lifecare id.
	 *
	 * @param  id the decision id
	 * @return    the LSS decision
	 */
	@GetMapping(path = "/api/v1/lss_decisions/{id}", produces = APPLICATION_JSON_VALUE)
	WEECIntegrationContractsDecisionV1LssDecision getLssDecision(@PathVariable final String id);
}
