package se.sundsvall.lifecareintegrator.api.model.professionalweb;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lifecare's answer to one ProfessionalWeb call, whatever its status. Carried as data rather than as this endpoint's
 * own status, so the gateway cannot rewrite Lifecare's own codes (360, 461) or a PDF on the way.
 *
 * @param status      Lifecare's status
 * @param contentType Lifecare's content type
 * @param body        the raw body, base64
 */
@Schema(description = "Lifecare's answer to one ProfessionalWeb call, whatever its status")
public record ProfessionalWebExchangeResponse(

	@Schema(description = "Lifecare's status, including its own codes such as 461", examples = "200") int status,

	@Schema(description = "Lifecare's content type", examples = "application/json; charset=utf-8") String contentType,

	@Schema(description = "The raw body, base64 encoded") String body) {
}
