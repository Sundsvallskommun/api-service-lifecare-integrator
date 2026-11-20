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

// Paths as Lifecare's web app sends them: the trailing slash on HasHouseholdThisDate and its absence on Payee/Create
// are both needed
@FeignClient(name = CLIENT_ID, contextId = "lifecare-professionalweb-payment", url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebPaymentClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payment/GetPaymentForCreate", produces = APPLICATION_JSON_VALUE)
	byte[] getPaymentForCreate(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payment/GetLatestPayments", produces = APPLICATION_JSON_VALUE)
	byte[] getLatestPayments(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Household/HasHouseholdThisDate/", produces = APPLICATION_JSON_VALUE)
	byte[] hasHouseholdThisDate(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "date") final String date);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payment/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createPayment(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payee/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createPayee(
		@RequestBody final byte[] body);
}
