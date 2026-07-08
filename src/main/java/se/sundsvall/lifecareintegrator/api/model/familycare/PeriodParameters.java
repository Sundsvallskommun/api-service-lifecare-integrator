package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
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

	public static PeriodParameters create() {
		return new PeriodParameters();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public PeriodParameters withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public LocalDate getFrom() {
		return from;
	}

	public void setFrom(final LocalDate from) {
		this.from = from;
	}

	public PeriodParameters withFrom(final LocalDate from) {
		this.from = from;
		return this;
	}

	public LocalDate getTo() {
		return to;
	}

	public void setTo(final LocalDate to) {
		this.to = to;
	}

	public PeriodParameters withTo(final LocalDate to) {
		this.to = to;
		return this;
	}

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(final Boolean ascending) {
		this.ascending = ascending;
	}

	public PeriodParameters withAscending(final Boolean ascending) {
		this.ascending = ascending;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof final PeriodParameters that))
			return false;
		if (!super.equals(o))
			return false;
		return Objects.equals(partyId, that.partyId) && Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(ascending, that.ascending);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), partyId, from, to, ascending);

	}

	@Override
	public String toString() {
		return "PeriodParameters{" +
			"partyId='" + partyId + '\'' +
			", from=" + from +
			", to=" + to +
			", ascending=" + ascending +
			", page=" + page +
			", limit=" + limit +
			'}';
	}
}
