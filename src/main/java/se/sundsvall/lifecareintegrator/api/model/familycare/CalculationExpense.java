package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "An expense row in a calculation, used for both regular and special expenses")
public class CalculationExpense {

	@Schema(description = "The expense type", examples = "Hyra")
	private String type;

	@Schema(description = "The applied amount", examples = "8000.0")
	private Double appliedAmount;

	@Schema(description = "The approved amount", examples = "7500.0")
	private Double approvedAmount;

	public static CalculationExpense create() {
		return new CalculationExpense();
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public CalculationExpense withType(final String type) {
		this.type = type;
		return this;
	}

	public Double getAppliedAmount() {
		return appliedAmount;
	}

	public void setAppliedAmount(final Double appliedAmount) {
		this.appliedAmount = appliedAmount;
	}

	public CalculationExpense withAppliedAmount(final Double appliedAmount) {
		this.appliedAmount = appliedAmount;
		return this;
	}

	public Double getApprovedAmount() {
		return approvedAmount;
	}

	public void setApprovedAmount(final Double approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

	public CalculationExpense withApprovedAmount(final Double approvedAmount) {
		this.approvedAmount = approvedAmount;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationExpense that = (CalculationExpense) o;
		return Objects.equals(type, that.type) && Objects.equals(appliedAmount, that.appliedAmount) && Objects.equals(approvedAmount, that.approvedAmount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, appliedAmount, approvedAmount);
	}

	@Override
	public String toString() {
		return "CalculationExpense{" +
			"type='" + type + '\'' +
			", appliedAmount=" + appliedAmount +
			", approvedAmount=" + approvedAmount +
			'}';
	}
}
