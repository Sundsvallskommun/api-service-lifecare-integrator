package se.sundsvall.lifecareintegrator.integration.elderlycare;

import generated.se.sundsvall.elderlycare.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.elderlycare.WEECIntegrationContractsDecisionV1LssDecision;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.elderlycare.configuration.ElderlyCareConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.elderlycare.configuration.ElderlyCareConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.elderlycare.url}", configuration = ElderlyCareConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface ElderlyCareClient {

	@GetMapping(path = "/api/v1/sol_decisions", produces = APPLICATION_JSON_VALUE)
	List<WEECIntegrationContractsDecisionV1Decision> getSolDecisions(
		@RequestParam(value = "q", required = false) final String q,
		@RequestParam(value = "limit", required = false) final Integer limit);

	@GetMapping(path = "/api/v1/sol_decisions/{id}", produces = APPLICATION_JSON_VALUE)
	WEECIntegrationContractsDecisionV1Decision getSolDecision(@PathVariable final String id);

	@GetMapping(path = "/api/v1/lss_decisions", produces = APPLICATION_JSON_VALUE)
	List<WEECIntegrationContractsDecisionV1LssDecision> getLssDecisions(
		@RequestParam(value = "q", required = false) final String q,
		@RequestParam(value = "limit", required = false) final Integer limit);

	@GetMapping(path = "/api/v1/lss_decisions/{id}", produces = APPLICATION_JSON_VALUE)
	WEECIntegrationContractsDecisionV1LssDecision getLssDecision(@PathVariable final String id);
}
