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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebDocumentService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toCreated;
import static se.sundsvall.lifecareintegrator.api.ProfessionalWebResponses.toUpdated;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb documents", description = "Lifecare ProfessionalWeb journal notes and documents, with Lifecare's JSON passed through untouched")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "403",
	description = "Lifecare does not let the integration account do this",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404",
	description = "Lifecare found nothing there, or Party knows no person by the partyId",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "422",
	description = "Lifecare refused the request, with Lifecare's text",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502",
	description = "Lifecare could not be reached, would not give a session or answered with an error",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ProfessionalWebDocumentResource {

	private final ProfessionalWebDocumentService service;

	ProfessionalWebDocumentResource(final ProfessionalWebDocumentService service) {
		this.service = service;
	}

	@GetMapping(path = "/persons/{partyId}/documents", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "List a person's journal notes and documents",
		description = "Lifecare's api2/Document/GetDocumentsListForClient/",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "array"))))
	ResponseEntity<byte[]> getDocuments(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @PathVariable final String partyId) {

		return ok(service.getDocuments(municipalityId, partyId));
	}

	@GetMapping(path = "/journal-notes/{id}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a journal note to show",
		description = "Lifecare's api2/Document/GetJournalNoteWithContent/, without revisions",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getJournalNote(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id of the journal note", example = "1") @Positive @PathVariable final Long id) {

		return ok(service.getJournalNote(id));
	}

	@GetMapping(path = "/journal-notes/{id}/edit", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a journal note to edit",
		description = "Lifecare's api2/Document/GetJournalNoteWithContent/, with revisions, as the object an update takes back",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getJournalNoteForEdit(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id of the journal note", example = "1") @Positive @PathVariable final Long id) {

		return ok(service.getJournalNoteForEdit(id));
	}

	@GetMapping(path = "/documents/{id}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a document to show",
		description = "Lifecare's api2/Document/GetDocumentWithContent/, without revisions",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getDocument(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id", example = "1") @Positive @PathVariable final Long id) {

		return ok(service.getDocument(id));
	}

	@GetMapping(path = "/documents/{id}/edit", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get a document to edit",
		description = "Lifecare's api2/Document/GetDocumentWithContent/, with revisions, as the object an update takes back",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getDocumentForEdit(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id", example = "1") @Positive @PathVariable final Long id) {

		return ok(service.getDocumentForEdit(id));
	}

	@GetMapping(path = "/services/{serviceId}/journal-notes/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's proposal for a new journal note on a service",
		description = "Lifecare's api2/Document/GetNoteProposalForService",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getJournalNoteProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getJournalNoteProposal(serviceId));
	}

	@GetMapping(path = "/services/{serviceId}/documents/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get Lifecare's proposal for a new document on a service",
		description = "Lifecare's api2/Document/GetDocumentProposalForService",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's answer",
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))))
	ResponseEntity<byte[]> getDocumentProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "serviceId", description = "The Lifecare service (insats) id", example = "42") @Positive @PathVariable final Long serviceId) {

		return ok(service.getDocumentProposal(serviceId));
	}

	@PostMapping(path = "/journal-notes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a journal note",
		description = "Lifecare's api2/Document/CreateJournalNote/. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "201",
				description = "Created, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Created, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> createJournalNote(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return toCreated(service.createJournalNote(body));
	}

	@PutMapping(path = "/journal-notes/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a journal note",
		description = "Lifecare's api2/Document/UpdateJournalNote/. The body is sent to Lifecare as it is, and its documentId must be the one in the path",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "200",
				description = "Updated, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Updated, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> updateJournalNote(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id of the journal note", example = "1") @Positive @PathVariable final Long id,
		@RequestBody final byte[] body) {

		return toUpdated(service.updateJournalNote(id, body));
	}

	@PostMapping(path = "/documents", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a document",
		description = "Lifecare's api2/Document/CreateDocument/. The body is sent to Lifecare as it is",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "201",
				description = "Created, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Created, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> createDocument(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final byte[] body) {

		return toCreated(service.createDocument(body));
	}

	@PutMapping(path = "/documents/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a document",
		description = "Lifecare's api2/Document/UpdateDocument/. The body is sent to Lifecare as it is, and its documentId must be the one in the path",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
			content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
		responses = {
			@ApiResponse(responseCode = "200",
				description = "Updated, with Lifecare's answer",
				content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(type = "object"))),
			@ApiResponse(responseCode = "204", description = "Updated, without an answer from Lifecare")
		})
	ResponseEntity<byte[]> updateDocument(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id", example = "1") @Positive @PathVariable final Long id,
		@RequestBody final byte[] body) {

		return toUpdated(service.updateDocument(id, body));
	}
}
