package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.api.model.CaseService;
import se.sundsvall.lifecareintegrator.api.model.Execution;
import se.sundsvall.lifecareintegrator.api.model.Investigation;
import se.sundsvall.lifecareintegrator.api.model.PagedResponse;
import se.sundsvall.lifecareintegrator.api.model.Payment;
import se.sundsvall.lifecareintegrator.api.model.ResourceAllocation;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/{municipalityId}")
@Tag(name = "Case data", description = "Person-scoped case-data reads from the Lifecare family care system")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
class CaseDataResource {

	private final FamilyCareService familyCareService;

	CaseDataResource(final FamilyCareService familyCareService) {
		this.familyCareService = familyCareService;
	}

	@GetMapping(path = "/payments", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the payments registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedResponse<Payment>> getPayments(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Start of the period", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "End of the period", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to,
		@Parameter(name = "page", description = "Page number", example = "1") @Positive @RequestParam(required = false) final Integer page,
		@Parameter(name = "pageSize", description = "Page size", example = "20") @Positive @Max(1000) @RequestParam(required = false) final Integer pageSize,
		@Parameter(name = "ascending", description = "Sort order", example = "true") @RequestParam(required = false) final Boolean ascending) {

		return ok(familyCareService.getPayments(municipalityId, partyId, from, to, page, pageSize, ascending));
	}

	@GetMapping(path = "/investigations", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the investigations registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedResponse<Investigation>> getInvestigations(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Start of the period", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "End of the period", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to,
		@Parameter(name = "page", description = "Page number", example = "1") @Positive @RequestParam(required = false) final Integer page,
		@Parameter(name = "pageSize", description = "Page size", example = "20") @Positive @Max(1000) @RequestParam(required = false) final Integer pageSize,
		@Parameter(name = "ascending", description = "Sort order", example = "true") @RequestParam(required = false) final Boolean ascending) {

		return ok(familyCareService.getInvestigations(municipalityId, partyId, from, to, page, pageSize, ascending));
	}

	@GetMapping(path = "/services", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the services registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedResponse<CaseService>> getServices(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Start of the period", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "End of the period", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to,
		@Parameter(name = "page", description = "Page number", example = "1") @Positive @RequestParam(required = false) final Integer page,
		@Parameter(name = "pageSize", description = "Page size", example = "20") @Positive @Max(1000) @RequestParam(required = false) final Integer pageSize,
		@Parameter(name = "ascending", description = "Sort order", example = "true") @RequestParam(required = false) final Boolean ascending) {

		return ok(familyCareService.getServices(municipalityId, partyId, from, to, page, pageSize, ascending));
	}

	@GetMapping(path = "/executions", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the executions registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedResponse<Execution>> getExecutions(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Start of the period", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "End of the period", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to,
		@Parameter(name = "page", description = "Page number", example = "1") @Positive @RequestParam(required = false) final Integer page,
		@Parameter(name = "pageSize", description = "Page size", example = "20") @Positive @Max(1000) @RequestParam(required = false) final Integer pageSize,
		@Parameter(name = "ascending", description = "Sort order", example = "true") @RequestParam(required = false) final Boolean ascending) {

		return ok(familyCareService.getExecutions(municipalityId, partyId, from, to, page, pageSize, ascending));
	}

	@GetMapping(path = "/resource-allocations", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the resource allocations registered on a person in the given period", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<PagedResponse<ResourceAllocation>> getResourceAllocations(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "Party id of the person", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @NotNull @RequestParam final String partyId,
		@Parameter(name = "from", description = "Start of the period", example = "2025-01-01") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate from,
		@Parameter(name = "to", description = "End of the period", example = "2026-12-31") @DateTimeFormat(iso = DATE) @RequestParam(required = false) final LocalDate to,
		@Parameter(name = "page", description = "Page number", example = "1") @Positive @RequestParam(required = false) final Integer page,
		@Parameter(name = "pageSize", description = "Page size", example = "20") @Positive @Max(1000) @RequestParam(required = false) final Integer pageSize,
		@Parameter(name = "ascending", description = "Sort order", example = "true") @RequestParam(required = false) final Boolean ascending) {

		return ok(familyCareService.getResourceAllocations(municipalityId, partyId, from, to, page, pageSize, ascending));
	}
}
