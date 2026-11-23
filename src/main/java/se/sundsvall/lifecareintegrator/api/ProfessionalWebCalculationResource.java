package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebCalculationService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toCreated;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toUpdated;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb calculations", description = "Lifecare ProfessionalWeb calculations (beräkningar), with Lifecare's JSON passed through untouched")
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
class ProfessionalWebCalculationResource {

	private final ProfessionalWebCalculationService service;

	ProfessionalWebCalculationResource(final ProfessionalWebCalculationService service) {
		this.service = service;
	}

	@GetMapping(path = "/services/{serviceId}/calculations/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's underlag for a new calculation on a service",
		description = "Lifecare's api2/Calculation/GetProposalService",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getCalculationProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getCalculationProposal(serviceId));
	}

	@GetMapping(path = "/services/{serviceId}/calculations/latest", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get the latest calculation on a service",
		description = "Lifecare's api2/Calculation/ListCalculations, only the latest",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "array"))))
	ResponseEntity<byte[]> getLatestCalculation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getLatestCalculation(serviceId));
	}

	@GetMapping(path = "/calculations/{calculationId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a calculation to show",
		description = "Lifecare's api2/Calculation/GetCalculation",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getCalculation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "calculationId", description = "The Lifecare calculation (beräkning) id", example = "7") @Positive @PathVariable final Long calculationId) {

		return ok(service.getCalculation(calculationId));
	}

	@GetMapping(path = "/calculations/{calculationId}/edit", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a calculation to edit",
		description = "Lifecare's api2/Calculation/GetCalculationForEdit, the object an update takes back",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getCalculationForEdit(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "calculationId", description = "The Lifecare calculation (beräkning) id", example = "7") @Positive @PathVariable final Long calculationId) {

		return ok(service.getCalculationForEdit(calculationId));
	}

	@PostMapping(path = "/calculations/place-persons", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Let Lifecare place the household's members on a norm's rows",
		description = "Lifecare's api2/Calculation/PlacePersons. Changes nothing in Lifecare. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> placePersons(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return ok(service.placePersons(body));
	}

	@PostMapping(path = "/calculations/amount", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Let Lifecare count a member's amount for a period",
		description = "Lifecare's api2/Calculation/GetAmount. Changes nothing in Lifecare. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getAmount(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return ok(service.getAmount(body));
	}

	@PostMapping(path = "/calculations/shared-cost", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Let Lifecare count a household's shared costs for a period",
		description = "Lifecare's api2/Calculation/GetSharedCost. Changes nothing in Lifecare. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "number"))))
	ResponseEntity<byte[]> getSharedCost(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return ok(service.getSharedCost(body));
	}

	@PostMapping(path = "/calculations/job-stimuli", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Let Lifecare mark which members of a calculation have job stimulus",
		description = "Lifecare's api2/Calculation/GetJobStimuliForCalculation. Changes nothing in Lifecare. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getJobStimuli(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return ok(service.getJobStimuli(body));
	}

	@PostMapping(path = "/services/{serviceId}/calculations", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a calculation on a service",
		description = "Lifecare's api2/Calculation/Create. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "201",
				description = "Created, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Created, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> createCalculation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId,
		@RequestBody final byte[] body) {

		return toCreated(service.createCalculation(serviceId, body));
	}

	@PutMapping(path = "/calculations/{calculationId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a calculation",
		description = "Lifecare's api2/Calculation/Update. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "200",
				description = "Updated, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Updated, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> updateCalculation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "calculationId", description = "The Lifecare calculation (beräkning) id", example = "7") @Positive @PathVariable final Long calculationId,
		@RequestBody final byte[] body) {

		return toUpdated(service.updateCalculation(calculationId, body));
	}

	@DeleteMapping(path = "/calculations/{calculationId}")
	@Operation(summary = "Remove a calculation",
		description = "Lifecare's api2/Calculation/Delete",
		responses = @ApiResponse(responseCode = "204", description = "Removed"))
	ResponseEntity<Void> deleteCalculation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "calculationId", description = "The Lifecare calculation (beräkning) id", example = "7") @Positive @PathVariable final Long calculationId) {

		service.deleteCalculation(calculationId);
		return noContent().build();
	}
}
