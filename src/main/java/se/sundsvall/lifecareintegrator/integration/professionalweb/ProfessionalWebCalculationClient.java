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

@FeignClient(name = CLIENT_ID, contextId = "lifecare-professionalweb-calculation", url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebCalculationClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetJobStimulusToDate", produces = APPLICATION_JSON_VALUE)
	byte[] getJobStimulusToDate(
		@RequestParam(value = "id") final String fromDate);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/SaveJobStimulus", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] saveJobStimulus(
		@RequestBody final byte[] body);
}
