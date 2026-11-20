package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebReminderService;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb reminders", description = "Lifecare ProfessionalWeb reminders, with Lifecare's JSON passed through untouched")
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
class ProfessionalWebReminderResource {

	private final ProfessionalWebReminderService service;

	ProfessionalWebReminderResource(final ProfessionalWebReminderService service) {
		this.service = service;
	}

	@GetMapping(path = "/services/{serviceId}/reminders", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "List the reminders of a service",
		description = "Lifecare's api2/Reminders/ListRemindersByServiceId",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> listReminders(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@Positive @PathVariable final Long serviceId) {

		return ok(service.listReminders(serviceId));
	}

	@GetMapping(path = "/services/{serviceId}/reminders/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's proposal for a new reminder on a service",
		description = "Lifecare's api2/Reminders/GetProposalForService",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getReminderProposal(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@Positive @PathVariable final Long serviceId) {

		return ok(service.getReminderProposal(serviceId));
	}

	@GetMapping(path = "/reminders/{reminderId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a reminder",
		description = "Lifecare's api2/Reminders/GetReminderEditComposite",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getReminder(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@Positive @PathVariable final Long reminderId) {

		return ok(service.getReminder(reminderId));
	}

	@PostMapping(path = "/reminders", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a reminder",
		description = "Lifecare's api2/Reminders/CreateReminder. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = @ApiResponse(responseCode = "201",
			description = "Created, with Lifecare's answer when it gave one",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> createReminder(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final String body) {

		return service.createReminder(body)
			.map(answer -> status(CREATED).body(answer))
			.orElseGet(() -> status(CREATED).build());
	}

	@PutMapping(path = "/reminders/{reminderId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Update a reminder",
		description = "Lifecare's api2/Reminders/UpdateReminder. The body is sent to Lifecare as it is, and its reminderId must be the one in the path",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = @ApiResponse(responseCode = "204", description = "Updated"))
	ResponseEntity<Void> updateReminder(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@Positive @PathVariable final Long reminderId,
		@RequestBody final String body) {

		service.updateReminder(reminderId, body);
		return noContent().build();
	}

	@DeleteMapping(path = "/reminders/{reminderId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Remove a reminder",
		description = "Lifecare's api2/Reminders/RemoveReminder",
		responses = @ApiResponse(responseCode = "204", description = "Removed"))
	ResponseEntity<Void> removeReminder(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@Positive @PathVariable final Long reminderId) {

		service.removeReminder(reminderId);
		return noContent().build();
	}
}
