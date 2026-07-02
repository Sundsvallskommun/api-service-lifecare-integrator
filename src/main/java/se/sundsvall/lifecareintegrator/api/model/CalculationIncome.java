package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An income row in a calculation")
public class CalculationIncome {

	@Schema(description = "The income type", examples = "Lön")
	private String type;

	@Schema(description = "The income amount for the applicant", examples = "15000.0")
	private Double amountApplicant;

	@Schema(description = "The date the applicant income was searched", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate applicantSearchDate;

	@Schema(description = "The income amount for the co-applicant", examples = "12000.0")
	private Double amountCoApplicant;

	@Schema(description = "The date the co-applicant income was searched", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate coApplicantSearchDate;

	public static CalculationIncome create() {
		return new CalculationIncome();
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public CalculationIncome withType(final String type) {
		this.type = type;
		return this;
	}

	public Double getAmountApplicant() {
		return amountApplicant;
	}

	public void setAmountApplicant(final Double amountApplicant) {
		this.amountApplicant = amountApplicant;
	}

	public CalculationIncome withAmountApplicant(final Double amountApplicant) {
		this.amountApplicant = amountApplicant;
		return this;
	}

	public LocalDate getApplicantSearchDate() {
		return applicantSearchDate;
	}

	public void setApplicantSearchDate(final LocalDate applicantSearchDate) {
		this.applicantSearchDate = applicantSearchDate;
	}

	public CalculationIncome withApplicantSearchDate(final LocalDate applicantSearchDate) {
		this.applicantSearchDate = applicantSearchDate;
		return this;
	}

	public Double getAmountCoApplicant() {
		return amountCoApplicant;
	}

	public void setAmountCoApplicant(final Double amountCoApplicant) {
		this.amountCoApplicant = amountCoApplicant;
	}

	public CalculationIncome withAmountCoApplicant(final Double amountCoApplicant) {
		this.amountCoApplicant = amountCoApplicant;
		return this;
	}

	public LocalDate getCoApplicantSearchDate() {
		return coApplicantSearchDate;
	}

	public void setCoApplicantSearchDate(final LocalDate coApplicantSearchDate) {
		this.coApplicantSearchDate = coApplicantSearchDate;
	}

	public CalculationIncome withCoApplicantSearchDate(final LocalDate coApplicantSearchDate) {
		this.coApplicantSearchDate = coApplicantSearchDate;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationIncome that = (CalculationIncome) o;
		return Objects.equals(type, that.type) && Objects.equals(amountApplicant, that.amountApplicant) && Objects.equals(applicantSearchDate, that.applicantSearchDate)
			&& Objects.equals(amountCoApplicant, that.amountCoApplicant) && Objects.equals(coApplicantSearchDate, that.coApplicantSearchDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, amountApplicant, applicantSearchDate, amountCoApplicant, coApplicantSearchDate);
	}

	@Override
	public String toString() {
		return "CalculationIncome{" +
			"type='" + type + '\'' +
			", amountApplicant=" + amountApplicant +
			", applicantSearchDate=" + applicantSearchDate +
			", amountCoApplicant=" + amountCoApplicant +
			", coApplicantSearchDate=" + coApplicantSearchDate +
			'}';
	}
}
