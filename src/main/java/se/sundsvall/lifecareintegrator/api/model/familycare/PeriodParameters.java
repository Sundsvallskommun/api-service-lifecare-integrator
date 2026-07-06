package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingBase;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

/**
 * Shared query parameters for the person-scoped, date-windowed family care reads. Extends the dept44 paging base
 * ({@code page} + {@code limit}); {@code from}/{@code to} are optional and default to a wide window in the service.
 */
@ParameterObject
@Schema(description = "Query parameters for the person-scoped family care period reads")
public class PeriodParameters extends AbstractParameterPagingBase {

	@Schema(description = "Party id of the person", examples = "81471222-5798-11e9-ae24-57fa13b361e1", requiredMode = Schema.RequiredMode.REQUIRED)
	@ValidUuid
	@NotNull
	private String partyId;

	@Schema(description = "Start of the period (inclusive)", examples = "2025-01-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate from;

	@Schema(description = "End of the period (inclusive)", examples = "2026-12-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate to;

	@Schema(description = "Sort order by date", examples = "true")
	private Boolean ascending;

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public LocalDate getFrom() {
		return from;
	}

	public void setFrom(final LocalDate from) {
		this.from = from;
	}

	public LocalDate getTo() {
		return to;
	}

	public void setTo(final LocalDate to) {
		this.to = to;
	}

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(final Boolean ascending) {
		this.ascending = ascending;
	}
}
