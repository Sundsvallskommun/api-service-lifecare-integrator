package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, contextId = "lifecare-professionalweb-decision", url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebDecisionClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/GetProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getProposalForService(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestParam(value = "amountType") final String amountType,
		@RequestParam(value = "calculationId") final long calculationId,
		@RequestParam(value = "proposalId") final int proposalId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/GetMappedDecisionReasons", produces = APPLICATION_JSON_VALUE)
	byte[] getMappedDecisionReasons(
		@RequestParam(value = "id") final long decisionCode);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/GetDecision", produces = APPLICATION_JSON_VALUE)
	byte[] getDecision(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] create(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/Update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] update(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);
}
