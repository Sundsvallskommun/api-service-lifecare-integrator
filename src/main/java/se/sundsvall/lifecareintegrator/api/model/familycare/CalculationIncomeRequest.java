package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An income included in a calculation")
public class CalculationIncomeRequest {

	@Schema(description = "The id of the income type, from the calculation proposal's incomeTypes", examples = "1")
	@NotNull
	private Integer typeId;

	@Schema(description = "The applicant's amount", examples = "5000.0")
	private BigDecimal applicantAmount;

	@Schema(description = "The date of the applicant's amount", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate applicantAmountDate;

	@Schema(description = "The co-applicant's amount", examples = "5000.0")
	private BigDecimal coApplicantAmount;

	@Schema(description = "The date of the co-applicant's amount", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate coApplicantAmountDate;

	@Schema(description = "A note on the income", examples = "Lön april")
	private String note;

	public static CalculationIncomeRequest create() {
		return new CalculationIncomeRequest();
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(final Integer typeId) {
		this.typeId = typeId;
	}

	public CalculationIncomeRequest withTypeId(final Integer typeId) {
		this.typeId = typeId;
		return this;
	}

	public BigDecimal getApplicantAmount() {
		return applicantAmount;
	}

	public void setApplicantAmount(final BigDecimal applicantAmount) {
		this.applicantAmount = applicantAmount;
	}

	public CalculationIncomeRequest withApplicantAmount(final BigDecimal applicantAmount) {
		this.applicantAmount = applicantAmount;
		return this;
	}

	public LocalDate getApplicantAmountDate() {
		return applicantAmountDate;
	}

	public void setApplicantAmountDate(final LocalDate applicantAmountDate) {
		this.applicantAmountDate = applicantAmountDate;
	}

	public CalculationIncomeRequest withApplicantAmountDate(final LocalDate applicantAmountDate) {
		this.applicantAmountDate = applicantAmountDate;
		return this;
	}

	public BigDecimal getCoApplicantAmount() {
		return coApplicantAmount;
	}

	public void setCoApplicantAmount(final BigDecimal coApplicantAmount) {
		this.coApplicantAmount = coApplicantAmount;
	}

	public CalculationIncomeRequest withCoApplicantAmount(final BigDecimal coApplicantAmount) {
		this.coApplicantAmount = coApplicantAmount;
		return this;
	}

	public LocalDate getCoApplicantAmountDate() {
		return coApplicantAmountDate;
	}

	public void setCoApplicantAmountDate(final LocalDate coApplicantAmountDate) {
		this.coApplicantAmountDate = coApplicantAmountDate;
	}

	public CalculationIncomeRequest withCoApplicantAmountDate(final LocalDate coApplicantAmountDate) {
		this.coApplicantAmountDate = coApplicantAmountDate;
		return this;
	}

	public String getNote() {
		return note;
	}

	public void setNote(final String note) {
		this.note = note;
	}

	public CalculationIncomeRequest withNote(final String note) {
		this.note = note;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationIncomeRequest that = (CalculationIncomeRequest) o;
		return Objects.equals(typeId, that.typeId) && Objects.equals(applicantAmount, that.applicantAmount) && Objects.equals(applicantAmountDate, that.applicantAmountDate)
			&& Objects.equals(coApplicantAmount, that.coApplicantAmount) && Objects.equals(coApplicantAmountDate, that.coApplicantAmountDate) && Objects.equals(note, that.note);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeId, applicantAmount, applicantAmountDate, coApplicantAmount, coApplicantAmountDate, note);
	}

	@Override
	public String toString() {
		return "CalculationIncomeRequest{" +
			"typeId=" + typeId +
			", applicantAmount=" + applicantAmount +
			", applicantAmountDate=" + applicantAmountDate +
			", coApplicantAmount=" + coApplicantAmount +
			", coApplicantAmountDate=" + coApplicantAmountDate +
			", note='" + note + '\'' +
			'}';
	}
}
