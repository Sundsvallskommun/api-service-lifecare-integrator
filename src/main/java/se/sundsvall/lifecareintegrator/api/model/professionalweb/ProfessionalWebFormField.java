package se.sundsvall.lifecareintegrator.api.model.professionalweb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One field of a browser form, as it is sent in an application/x-www-form-urlencoded body.
 *
 * @param name  the field name; the same name may occur in several fields
 * @param value the field value, empty when the field has none
 */
@Schema(description = "One field of a browser form")
public record ProfessionalWebFormField(

	@Schema(description = "Field name, which may repeat across the fields of one form", examples = "51_0_2_1", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String name,

	@Schema(description = "Field value, empty when the field has none", examples = "true", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull String value) {
}
