package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
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

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetProposalService", produces = APPLICATION_JSON_VALUE)
	byte[] getProposalService(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/ListCalculations", produces = APPLICATION_JSON_VALUE)
	byte[] listCalculations(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestParam(value = "investigationId") final long investigationId,
		@RequestParam(value = "serviceId") final long serviceId,
		@RequestParam(value = "onlylatest") final boolean onlyLatest);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetCalculation", produces = APPLICATION_JSON_VALUE)
	byte[] getCalculation(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetCalculationForEdit", produces = APPLICATION_JSON_VALUE)
	byte[] getCalculationForEdit(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetJobStimulusForService", produces = APPLICATION_JSON_VALUE)
	byte[] getJobStimulusForService(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/PlacePersons", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] placePersons(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetAmount", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] getAmount(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetSharedCost", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] getSharedCost(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetJobStimuliForCalculation", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] getJobStimuliForCalculation(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] create(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/Update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] update(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@DeleteMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/Delete", consumes = APPLICATION_JSON_VALUE)
	void delete(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);
}
