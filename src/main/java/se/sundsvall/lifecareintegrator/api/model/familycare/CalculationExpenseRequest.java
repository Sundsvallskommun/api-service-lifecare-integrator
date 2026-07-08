package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "An expense or special expense included in a calculation")
public class CalculationExpenseRequest {

	@Schema(description = "The id of the expense type, from the calculation proposal's expenseTypes or specialExpenseTypes", examples = "1")
	@NotNull
	private Integer typeId;

	@Schema(description = "The amount of the expense", examples = "5000.0")
	private BigDecimal amount;

	@Schema(description = "The approved amount of the expense", examples = "4500.0")
	private BigDecimal approvedAmount;

	@Schema(description = "A note on the expense", examples = "Hyra april")
	private String note;

	public static CalculationExpenseRequest create() {
		return new CalculationExpenseRequest();
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(final Integer typeId) {
		this.typeId = typeId;
	}

	public CalculationExpenseRequest withTypeId(final Integer typeId) {
		this.typeId = typeId;
		return this;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public CalculationExpenseRequest withAmount(final BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public BigDecimal getApprovedAmount() {
		return approvedAmount;
	}

	public void setApprovedAmount(final BigDecimal approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

	public CalculationExpenseRequest withApprovedAmount(final BigDecimal approvedAmount) {
		this.approvedAmount = approvedAmount;
		return this;
	}

	public String getNote() {
		return note;
	}

	public void setNote(final String note) {
		this.note = note;
	}

	public CalculationExpenseRequest withNote(final String note) {
		this.note = note;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationExpenseRequest that = (CalculationExpenseRequest) o;
		return Objects.equals(typeId, that.typeId) && Objects.equals(amount, that.amount) && Objects.equals(approvedAmount, that.approvedAmount) && Objects.equals(note, that.note);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeId, amount, approvedAmount, note);
	}

	@Override
	public String toString() {
		return "CalculationExpenseRequest{" +
			"typeId=" + typeId +
			", amount=" + amount +
			", approvedAmount=" + approvedAmount +
			", note='" + note + '\'' +
			'}';
	}
}
