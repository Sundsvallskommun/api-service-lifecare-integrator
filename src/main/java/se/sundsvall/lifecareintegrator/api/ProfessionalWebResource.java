package se.sundsvall.lifecareintegrator.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Base64;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.lifecareintegrator.api.model.professionalweb.ProfessionalWebExchangeRequest;
import se.sundsvall.lifecareintegrator.api.model.professionalweb.ProfessionalWebExchangeResponse;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebExchange;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/{municipalityId}/professional-web")
@Tag(name = "ProfessionalWeb", description = "Pass-through to Lifecare ProfessionalWeb for api-service-caremanagement")
class ProfessionalWebResource {

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final ProfessionalWebExchange exchange;

	ProfessionalWebResource(final ProfessionalWebExchange exchange) {
		this.exchange = exchange;
	}

	@PostMapping(path = "/exchange", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Make one Lifecare ProfessionalWeb call through the integration account's session",
		description = """
			For api-service-caremanagement, which cannot reach Lifecare from outside the municipal network. The \
			integrator signs in as its integration account, keeps the session and handles Lifecare's session \
			escalation; the caller decides what to call and interprets the answer. Lifecare's answer is returned as \
			data with this endpoint's own status 200, whatever Lifecare answered, so the gateway cannot rewrite \
			Lifecare's own codes or a PDF. Only api2 and RenderPdf paths are reachable.""",
		responses = {
			@ApiResponse(responseCode = "200", description = "Lifecare answered", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
				Problem.class, ConstraintViolationProblem.class
			}))),
			@ApiResponse(responseCode = "502",
				description = "Lifecare could not be reached or would not give a session",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<ProfessionalWebExchangeResponse> exchange(
		@ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final ProfessionalWebExchangeRequest request) {

		final var body = Optional.ofNullable(request.body())
			.filter(node -> !node.isNull() && !node.isMissingNode())
			.map(node -> JSON.writeValueAsString(node).getBytes(UTF_8))
			.orElse(null);
		final var response = exchange.exchange(request.method(), request.path(), request.params(), body);
		return ok(new ProfessionalWebExchangeResponse(response.status(), response.contentType(), Base64.getEncoder().encodeToString(response.body())));
	}
}
