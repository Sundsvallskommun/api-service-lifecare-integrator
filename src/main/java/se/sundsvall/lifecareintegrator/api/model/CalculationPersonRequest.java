package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A person included in a calculation")
public class CalculationPersonRequest {

	@Schema(description = "The party id of the person", examples = "81471222-5798-11e9-ae24-57fa13b361e1")
	@NotNull
	@ValidUuid
	private String partyId;

	@Schema(description = "The number of days the person is included in the calculation", examples = "30")
	private Integer numberOfDays;

	@Schema(description = "The date the deviation is valid from", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate deviationFromDate;

	@Schema(description = "The date the deviation is valid to", examples = "2026-05-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate deviationToDate;

	public static CalculationPersonRequest create() {
		return new CalculationPersonRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public CalculationPersonRequest withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public Integer getNumberOfDays() {
		return numberOfDays;
	}

	public void setNumberOfDays(final Integer numberOfDays) {
		this.numberOfDays = numberOfDays;
	}

	public CalculationPersonRequest withNumberOfDays(final Integer numberOfDays) {
		this.numberOfDays = numberOfDays;
		return this;
	}

	public LocalDate getDeviationFromDate() {
		return deviationFromDate;
	}

	public void setDeviationFromDate(final LocalDate deviationFromDate) {
		this.deviationFromDate = deviationFromDate;
	}

	public CalculationPersonRequest withDeviationFromDate(final LocalDate deviationFromDate) {
		this.deviationFromDate = deviationFromDate;
		return this;
	}

	public LocalDate getDeviationToDate() {
		return deviationToDate;
	}

	public void setDeviationToDate(final LocalDate deviationToDate) {
		this.deviationToDate = deviationToDate;
	}

	public CalculationPersonRequest withDeviationToDate(final LocalDate deviationToDate) {
		this.deviationToDate = deviationToDate;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationPersonRequest that = (CalculationPersonRequest) o;
		return Objects.equals(partyId, that.partyId) && Objects.equals(numberOfDays, that.numberOfDays) && Objects.equals(deviationFromDate, that.deviationFromDate)
			&& Objects.equals(deviationToDate, that.deviationToDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId, numberOfDays, deviationFromDate, deviationToDate);
	}

	@Override
	public String toString() {
		return "CalculationPersonRequest{" +
			"partyId='" + partyId + '\'' +
			", numberOfDays=" + numberOfDays +
			", deviationFromDate=" + deviationFromDate +
			", deviationToDate=" + deviationToDate +
			'}';
	}
}
