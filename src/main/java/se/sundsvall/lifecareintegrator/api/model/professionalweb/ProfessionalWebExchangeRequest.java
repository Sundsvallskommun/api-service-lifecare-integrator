package se.sundsvall.lifecareintegrator.api.model.professionalweb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.LinkedHashMap;
import se.sundsvall.dept44.common.validators.annotation.OneOf;
import tools.jackson.databind.JsonNode;

/**
 * One ProfessionalWeb call to make through the integration account's session.
 *
 * @param method the HTTP method
 * @param path   the path below the ProfessionalWeb module; only api2 and the PDF renderer are reachable
 * @param params query parameters, in the order Lifecare expects them
 * @param body   the JSON body for a POST or DELETE, null for none
 */
@Schema(description = "One Lifecare ProfessionalWeb call to make through the integration account's session")
public record ProfessionalWebExchangeRequest(

	@Schema(description = "HTTP method", examples = "GET", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @OneOf({
		"GET", "POST", "DELETE"
	}) String method,

	@Schema(description = "Path below WESE.FC.ProfessionalWeb",
		examples = "api2/Calculation/GetCalculation",
		requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Pattern(regexp = "^(api2|RenderPdf)/[A-Za-z0-9/_.-]+$",
			message = "must be an api2 or RenderPdf path") String path,

	@Schema(description = "Query parameters, in order") LinkedHashMap<String, String> params,

	@Schema(description = "JSON body for a POST or DELETE") JsonNode body) {
}
