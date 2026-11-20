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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebPrintService;

import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb prints", description = "Lifecare ProfessionalWeb prints, Lifecare's own PDF of a calculation, a decision or a document")
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
	description = "Lifecare could not be reached, would not give a session, answered with an error or with something that is not a PDF",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ProfessionalWebPrintResource {

	private final ProfessionalWebPrintService service;

	ProfessionalWebPrintResource(final ProfessionalWebPrintService service) {
		this.service = service;
	}

	@GetMapping(path = "/calculations/{calculationId}/pdf", produces = APPLICATION_PDF_VALUE)
	@Operation(summary = "Get Lifecare's print of a calculation",
		description = "Lifecare's RenderPdf/PrintContainer, with the configured calculation template",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's PDF",
			content = @Content(mediaType = APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))))
	ResponseEntity<byte[]> getCalculationPdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "calculationId", description = "The Lifecare calculation (beräkning) id", example = "7") @Positive @PathVariable final Long calculationId) {

		return ok().contentType(APPLICATION_PDF).body(service.getCalculationPdf(calculationId));
	}

	@GetMapping(path = "/decisions/{decisionId}/pdf", produces = APPLICATION_PDF_VALUE)
	@Operation(summary = "Get Lifecare's print of a decision",
		description = "Lifecare's RenderPdf/PrintDecision, with the configured decision template. Parameter questions are answered with Lifecare's defaults",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's PDF",
			content = @Content(mediaType = APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))))
	ResponseEntity<byte[]> getDecisionPdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "decisionId", description = "The Lifecare decision id", example = "9") @Positive @PathVariable final Long decisionId) {

		return ok().contentType(APPLICATION_PDF).body(service.getDecisionPdf(decisionId));
	}

	@GetMapping(path = "/documents/{id}/pdf", produces = APPLICATION_PDF_VALUE)
	@Operation(summary = "Get Lifecare's print of a written document or form",
		description = "Lifecare's RenderPdf/PrintDocument, with the configured document template",
		responses = @ApiResponse(responseCode = "200",
			description = "Lifecare's PDF",
			content = @Content(mediaType = APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))))
	ResponseEntity<byte[]> getDocumentPdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "id", description = "The Lifecare document id", example = "1") @Positive @PathVariable final Long id) {

		return ok().contentType(APPLICATION_PDF).body(service.getDocumentPdf(id));
	}
}
