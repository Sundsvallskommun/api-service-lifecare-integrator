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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebJobStimulusService;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toUpdated;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb job stimulus", description = "Lifecare ProfessionalWeb job stimulus (jobbstimulans), with Lifecare's JSON passed through untouched")
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
class ProfessionalWebJobStimulusResource {

	private final ProfessionalWebJobStimulusService service;

	ProfessionalWebJobStimulusResource(final ProfessionalWebJobStimulusService service) {
		this.service = service;
	}

	@GetMapping(path = "/services/{serviceId}/job-stimulus", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get the job stimulus periods on a service",
		description = "Lifecare's api2/Calculation/GetJobStimulusForService",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getJobStimulus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getJobStimulus(serviceId));
	}

	@GetMapping(path = "/job-stimulus/end-date", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get the end Lifecare gives a job stimulus period starting on a date",
		description = "Lifecare's api2/Calculation/GetJobStimulusToDate. Lifecare answers with a bare JSON string",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "string"))))
	ResponseEntity<byte[]> getJobStimulusEndDate(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "fromDate", description = "The first day of the period", example = "2026-11-01") @RequestParam @DateTimeFormat(iso = DATE) final LocalDate fromDate) {

		return ok(service.getJobStimulusEndDate(fromDate));
	}

	@PostMapping(path = "/job-stimulus", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Save a person's job stimulus periods",
		description = "Lifecare's api2/Calculation/SaveJobStimulus, which replaces all of the person's periods. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "200",
				description = "Saved, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Saved, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> saveJobStimulus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return toUpdated(service.saveJobStimulus(body));
	}
}
