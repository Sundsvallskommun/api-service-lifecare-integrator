package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.api.model.common.CreatedResource;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedActualisationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PeriodParameters;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@RestController
@Validated
@RequestMapping("/{municipalityId}/actualisations")
@Tag(name = "Actualisations", description = "Actualisation (case intake) resource for the Lifecare family care system")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
class ActualisationResource {

	private final FamilyCareService familyCareService;

	ActualisationResource(final FamilyCareService familyCareService) {
		this.familyCareService = familyCareService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the actualisations (case intakes) registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedActualisationResponse> getActualisations(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid final PeriodParameters parameters) {

		return ok(familyCareService.getActualisations(municipalityId, parameters));
	}

	@GetMapping(path = "/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the proposal (valid code lists) needed to create an actualisation for a person", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<ActualisationProposal> getActualisationProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId) {

		return ok(familyCareService.getActualisationProposal(municipalityId, partyId));
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Create an actualisation (case intake) in Lifecare family care. Ids in the request come from the proposal endpoint", responses = {
		@ApiResponse(responseCode = "201", description = "Created", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<CreatedResource> createActualisation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final CreateActualisationRequest request) {

		final var id = familyCareService.createActualisation(municipalityId, request);
		return created(fromPath("/{municipalityId}/actualisations/{id}").buildAndExpand(municipalityId, id).toUri())
			.body(CreatedResource.create().withId(id));
	}

	@PostMapping(path = "/{actualisationId}/attachments", consumes = MULTIPART_FORM_DATA_VALUE)
	@Operation(description = "Upload a document and bind it to an actualisation. Type codes come from the proposal endpoint's attachmentTypes", responses = {
		@ApiResponse(responseCode = "204", description = "No content"),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> addActualisationAttachment(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "actualisationId", description = "Id of the actualisation", example = "12345") @NotNull @PathVariable final Integer actualisationId,
		@Parameter(name = "documentType", description = "The document type code") @NotBlank @RequestPart("documentType") final String documentType,
		@Parameter(name = "senderType", description = "The document sender type code") @NotBlank @RequestPart("senderType") final String senderType,
		@Parameter(name = "title", description = "The document title") @RequestPart(value = "title", required = false) final String title,
		@Parameter(name = "senderName", description = "The sender name") @RequestPart(value = "senderName", required = false) final String senderName,
		@RequestPart("file") final MultipartFile file) {

		familyCareService.addActualisationAttachment(actualisationId, documentType, senderType, title, senderName, file);
		return noContent().build();
	}
}
