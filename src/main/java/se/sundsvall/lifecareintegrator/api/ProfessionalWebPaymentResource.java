package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebService;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toCreated;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb payments", description = "Lifecare ProfessionalWeb payments (utbetalningar), payees and households, with Lifecare's JSON passed through untouched")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "403",
	description = "Lifecare does not let the integration account do this",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404",
	description = "Lifecare found nothing there",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "422",
	description = "Lifecare refused the request, with Lifecare's text",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502",
	description = "Lifecare could not be reached, would not give a session, answered with an error or named no person for the service",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ProfessionalWebPaymentResource {

	private final ProfessionalWebService service;

	ProfessionalWebPaymentResource(final ProfessionalWebService service) {
		this.service = service;
	}

	@GetMapping(path = "/services/{serviceId}/payments/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's underlag for a new payment on a service",
		description = "Lifecare's api2/Payment/GetPaymentForCreate",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getPaymentProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getPaymentProposal(serviceId));
	}

	@GetMapping(path = "/services/{serviceId}/payments/latest", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get the latest payments on a service",
		description = "Lifecare's api2/Payment/GetLatestPayments",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "array"))))
	ResponseEntity<byte[]> getLatestPayments(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getLatestPayments(serviceId));
	}

	@GetMapping(path = "/services/{serviceId}/household", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Ask whether the person of a service has a household on a date",
		description = "Lifecare's api2/Household/HasHouseholdThisDate/, for the person Lifecare's api2/Payment/GetPaymentForCreate names for the service. Lifecare answers true or false",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "boolean"))))
	ResponseEntity<byte[]> hasHousehold(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId,
		@Parameter(name = "date", description = "The date", example = "2026-11-01") @RequestParam @DateTimeFormat(iso = DATE) final LocalDate date) {

		return ok(service.hasHousehold(serviceId, date));
	}

	@PostMapping(path = "/services/{serviceId}/payments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Register a payment on a service",
		description = "Lifecare's api2/Payment/Create. The body is sent to Lifecare as it is, and not sent again unless Lifecare refuses it for a missing session",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "201",
				description = "Created, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Created, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> createPayment(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId,
		@RequestBody final byte[] body) {

		return toCreated(service.createPayment(serviceId, body));
	}

	@PostMapping(path = "/payees", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a payee",
		description = "Lifecare's api2/Payee/Create. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "201",
				description = "Created, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Created, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> createPayee(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return toCreated(service.createPayee(body));
	}
}
