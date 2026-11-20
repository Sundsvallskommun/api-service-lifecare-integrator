package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebApi2Client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

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

	private final ProfessionalWebApi2Client client;

	ProfessionalWebReminderResource(final ProfessionalWebApi2Client client) {
		this.client = client;
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

		return ResponseEntity.ok(client.listRemindersByServiceId(serviceId));
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

		return ResponseEntity.ok(client.getProposalForService(serviceId));
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

		return ResponseEntity.ok(client.getReminderEditComposite(reminderId));
	}
}
