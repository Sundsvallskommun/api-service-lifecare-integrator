package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A person included in a calculation")
public class CalculationPerson {

	@Schema(description = "The party id of the person, resolved from the identity Lifecare stores. Null when the person cannot be resolved", examples = "6a5c3d18-1f2b-4e77-9c0a-2b3d4e5f6a7b")
	private String partyId;

	@Schema(description = "Name of the person", examples = "Kalle Karlsson")
	private String name;

	@Schema(description = "The amount for the person", examples = "3160.0")
	private BigDecimal amount;

	@Schema(description = "The date any deviation is valid from", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate deviationFromDate;

	@Schema(description = "The date any deviation is valid to", examples = "2026-05-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate deviationToDate;

	public static CalculationPerson create() {
		return new CalculationPerson();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public CalculationPerson withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public CalculationPerson withName(final String name) {
		this.name = name;
		return this;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public CalculationPerson withAmount(final BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public LocalDate getDeviationFromDate() {
		return deviationFromDate;
	}

	public void setDeviationFromDate(final LocalDate deviationFromDate) {
		this.deviationFromDate = deviationFromDate;
	}

	public CalculationPerson withDeviationFromDate(final LocalDate deviationFromDate) {
		this.deviationFromDate = deviationFromDate;
		return this;
	}

	public LocalDate getDeviationToDate() {
		return deviationToDate;
	}

	public void setDeviationToDate(final LocalDate deviationToDate) {
		this.deviationToDate = deviationToDate;
	}

	public CalculationPerson withDeviationToDate(final LocalDate deviationToDate) {
		this.deviationToDate = deviationToDate;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationPerson that = (CalculationPerson) o;
		return Objects.equals(partyId, that.partyId) && Objects.equals(name, that.name) && Objects.equals(amount, that.amount) && Objects.equals(deviationFromDate, that.deviationFromDate)
			&& Objects.equals(deviationToDate, that.deviationToDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId, name, amount, deviationFromDate, deviationToDate);
	}

	@Override
	public String toString() {
		return "CalculationPerson{" +
			"partyId='" + partyId + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			", deviationFromDate=" + deviationFromDate +
			", deviationToDate=" + deviationToDate +
			'}';
	}
}
