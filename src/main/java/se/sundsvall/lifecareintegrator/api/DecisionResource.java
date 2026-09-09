package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.OneOf;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.common.DecisionsResponse;
import se.sundsvall.lifecareintegrator.service.DecisionService;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.LAW_LSS;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.LAW_SFB;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.LAW_SOL;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.SOURCE_ELDERLY_CARE;
import static se.sundsvall.lifecareintegrator.service.mapper.DecisionMapper.SOURCE_FAMILY_CARE;

@RestController
@Validated
@RequestMapping("/{municipalityId}/decisions")
@Tag(name = "Decisions", description = "Unified decision resource, merging the Lifecare elderly care (SoL + LSS) and family care decision sources")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
class DecisionResource {

	static final String DECISION_ID_PATTERN = "^\\d+$";
	static final String DECISION_ID_MESSAGE = "must be a numeric decision id";

	private final DecisionService decisionService;

	DecisionResource(final DecisionService decisionService) {
		this.decisionService = decisionService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = """
		Get all decisions for a person, merged from all Lifecare decision sources. \
		A source that cannot be reached is reported as UNAVAILABLE in the response instead of failing the request.""", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<DecisionsResponse> getDecisions(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Only include decisions valid on or after this date", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "Only include decisions valid on or before this date", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to) {

		return ok(decisionService.getDecisions(municipalityId, partyId, from, to));
	}

	@GetMapping(path = "/{decisionId}", produces = APPLICATION_JSON_VALUE)
	@Operation(description = """
		Get a single decision for a person. A decisionId is only unique within its source system, so the source \
		(and, for ELDERLY_CARE, the law) must be given along with it — pass back the source, law and decisionId of an \
		item from the decision list. SFB decisions are served by the LSS source, so law LSS and SFB address the same \
		ids. A decision that does not belong to the person is reported as not found. \
		Unlike the list, a source that cannot be reached fails the request.""", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Decision> getDecision(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "decisionId", description = "The decision id in the source system", example = "12345") @Pattern(regexp = DECISION_ID_PATTERN, message = DECISION_ID_MESSAGE) @PathVariable final String decisionId,
		@Parameter(name = "partyId", description = "Party id of the person the decision must belong to", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "source", description = "The source system the decision originates from", example = SOURCE_ELDERLY_CARE) @OneOf({
			SOURCE_ELDERLY_CARE, SOURCE_FAMILY_CARE
		}) @NotNull @RequestParam final String source,
		@Parameter(name = "law", description = "The law of the decision. Required for ELDERLY_CARE, not applicable for FAMILY_CARE", example = LAW_SOL) @OneOf(value = {
			LAW_SOL, LAW_LSS, LAW_SFB
		}, nullable = true) @RequestParam(required = false) final String law) {

		return ok(decisionService.getDecision(municipalityId, partyId, decisionId, source, law));
	}
}
