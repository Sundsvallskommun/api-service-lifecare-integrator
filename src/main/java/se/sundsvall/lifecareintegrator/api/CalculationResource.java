package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
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
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.api.model.Calculation;
import se.sundsvall.lifecareintegrator.api.model.CalculationProposal;
import se.sundsvall.lifecareintegrator.api.model.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.api.model.CreatedResource;
import se.sundsvall.lifecareintegrator.api.model.PagedResponse;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@RestController
@Validated
@RequestMapping("/{municipalityId}/calculations")
@Tag(name = "Calculations", description = "Calculation (normberäkning) resource for the Lifecare family care system")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
class CalculationResource {

	private final FamilyCareService familyCareService;

	CalculationResource(final FamilyCareService familyCareService) {
		this.familyCareService = familyCareService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the calculations registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedResponse<Calculation>> getCalculations(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Start of the period", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "End of the period", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to,
		@Parameter(name = "page", description = "Page number", example = "1") @Positive @RequestParam(required = false) final Integer page,
		@Parameter(name = "pageSize", description = "Page size", example = "20") @Positive @Max(1000) @RequestParam(required = false) final Integer pageSize,
		@Parameter(name = "ascending", description = "Sort order", example = "true") @RequestParam(required = false) final Boolean ascending) {

		return ok(familyCareService.getCalculations(municipalityId, partyId, from, to, page, pageSize, ascending));
	}

	@GetMapping(path = "/proposal", produces = APPLICATION_JSON_VALUE)
	@Operation(description = """
		Get the proposal (norms, household members, income/expense types and linkable cases) needed to create a \
		calculation for a person. Household members are identified by partyId — a member without a partyId could not \
		be resolved and cannot be referenced in a create request""", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<CalculationProposal> getCalculationProposal(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId) {

		return ok(familyCareService.getCalculationProposal(municipalityId, partyId));
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Create a calculation in Lifecare family care. Ids in the request come from the proposal endpoint", responses = {
		@ApiResponse(responseCode = "201", description = "Created", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<CreatedResource> createCalculation(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final CreateCalculationRequest request) {

		final var id = familyCareService.createCalculation(municipalityId, request);
		return created(fromPath("/{municipalityId}/calculations/{id}").buildAndExpand(municipalityId, id).toUri())
			.body(CreatedResource.create().withId(id));
	}
}
