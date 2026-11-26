package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toCreated;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toUpdated;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb decisions", description = "Lifecare ProfessionalWeb decisions (beslut), with Lifecare's JSON passed through untouched")
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
	description = "Lifecare could not be reached, would not give a session or answered with an error",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ProfessionalWebDecisionResource {

	private final ProfessionalWebService service;

	ProfessionalWebDecisionResource(final ProfessionalWebService service) {
		this.service = service;
	}

	@GetMapping(path = "/services/{serviceId}/decisions/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's underlag for a new decision on a service, made from a calculation or without one",
		description = "Lifecare's api2/Decision/GetProposalForService. Without amountType and calculationId Lifecare is asked with an empty amountType and calculationId 0, for a decision made without a calculation",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getDecisionProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId,
		@Parameter(name = "amountType", description = "Which of the calculation's amounts the decision takes, empty without a calculation", example = "TotalSum") @RequestParam(defaultValue = "") final String amountType,
		@Parameter(name = "calculationId", description = "The Lifecare calculation (beräkning) id, 0 without a calculation", example = "7") @PositiveOrZero @RequestParam(defaultValue = "0") final Long calculationId) {

		return ok(service.getDecisionProposal(serviceId, amountType, calculationId));
	}

	@GetMapping(path = "/decision-codes/{decisionCode}/reasons", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's reasons for a decision type",
		description = "Lifecare's api2/Decision/GetMappedDecisionReasons",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "array"))))
	ResponseEntity<byte[]> getDecisionReasons(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "decisionCode", description = "The Lifecare decision type (beslutstyp) code", example = "5") @Positive @PathVariable final Long decisionCode) {

		return ok(service.getDecisionReasons(decisionCode));
	}

	@GetMapping(path = "/decisions/{decisionId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a decision",
		description = "Lifecare's api2/Decision/GetDecision",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getDecision(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "decisionId", description = "The Lifecare decision id", example = "9") @Positive @PathVariable final Long decisionId) {

		return ok(service.getDecision(decisionId));
	}

	@PostMapping(path = "/services/{serviceId}/decisions", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Register a decision on a service",
		description = "Lifecare's api2/Decision/Create. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "201",
				description = "Created, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Created, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> createDecision(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId,
		@RequestBody final byte[] body) {

		return toCreated(service.createDecision(serviceId, body));
	}

	@PutMapping(path = "/decisions/{decisionId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a decision",
		description = "Lifecare's api2/Decision/Update. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "200",
				description = "Updated, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Updated, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> updateDecision(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "decisionId", description = "The Lifecare decision id", example = "9") @Positive @PathVariable final Long decisionId,
		@RequestBody final byte[] body) {

		return toUpdated(service.updateDecision(decisionId, body));
	}
}
