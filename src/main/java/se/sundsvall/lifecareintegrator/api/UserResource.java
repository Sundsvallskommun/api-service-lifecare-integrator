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
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.api.model.CaseworkerUser;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/{municipalityId}/users")
@Tag(name = "Users", description = "Caseworker (user) lookup in the Lifecare family care system")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
class UserResource {

	private final FamilyCareService familyCareService;

	UserResource(final FamilyCareService familyCareService) {
		this.familyCareService = familyCareService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get the family care caseworkers (users)", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<CaseworkerUser>> getUsers(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "limit", description = "The maximum number of users to return", example = "100") @NotNull @Positive @Max(1000) @RequestParam final Integer limit,
		@Parameter(name = "offset", description = "The starting point within the result set", example = "0") @RequestParam(required = false) final Integer offset,
		@Parameter(name = "modifiedAfter", description = "Only users modified after this time") @DateTimeFormat(iso = DATE_TIME) @RequestParam(required = false) final OffsetDateTime modifiedAfter,
		@Parameter(name = "modifiedBefore", description = "Only users modified before this time") @DateTimeFormat(iso = DATE_TIME) @RequestParam(required = false) final OffsetDateTime modifiedBefore) {

		return ok(familyCareService.getUsers(municipalityId, limit, offset, modifiedAfter, modifiedBefore));
	}
}
