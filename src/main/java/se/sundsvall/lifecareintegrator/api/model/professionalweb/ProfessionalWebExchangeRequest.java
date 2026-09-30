package se.sundsvall.lifecareintegrator.api.model.professionalweb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.LinkedHashMap;
import java.util.List;
import se.sundsvall.dept44.common.validators.annotation.OneOf;
import tools.jackson.databind.JsonNode;

/**
 * One ProfessionalWeb call to make through the integration account's session.
 *
 * @param method the HTTP method
 * @param path   the path below the ProfessionalWeb module; only api2 and the PDF renderer are reachable
 * @param params query parameters, in the order Lifecare expects them
 * @param body   the JSON body for a POST or DELETE, null for none
 * @param form   the fields of a browser form for a POST, null for none; never together with a body
 */
@Schema(description = "One Lifecare ProfessionalWeb call to make through the integration account's session")
public record ProfessionalWebExchangeRequest(

	@Schema(description = "HTTP method", examples = "GET", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @OneOf({
		"GET", "POST", "DELETE"
	}) String method,

	@Schema(description = "Path below WESE.FC.ProfessionalWeb",
		examples = "api2/Calculation/GetCalculation",
		requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Pattern(regexp = "^(?!.*/\\.\\.(?:/|$))(api2|RenderPdf)/[A-Za-z0-9/_.-]+$",
			message = "must be an api2 or RenderPdf path") String path,

	@Schema(description = "Query parameters, in order") LinkedHashMap<String, String> params,

	@Schema(description = "JSON body for a POST or DELETE") JsonNode body,

	@Schema(description = """
		Form fields for a POST sent as a browser form (application/x-www-form-urlencoded), in document order. A name \
		may repeat. Only for method POST, and not together with body.""") @Valid List<ProfessionalWebFormField> form) {

	@JsonIgnore
	@Schema(hidden = true)
	@AssertTrue(message = "form is only allowed for method POST")
	public boolean isFormOnlyForPost() {
		return form == null || "POST".equals(method);
	}

	@JsonIgnore
	@Schema(hidden = true)
	@AssertTrue(message = "form cannot be combined with body")
	public boolean isFormWithoutBody() {
		return form == null || body == null || body.isNull() || body.isMissingNode();
	}
}
