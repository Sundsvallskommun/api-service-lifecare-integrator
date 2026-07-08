package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "Request to create a calculation in Lifecare FC. All ids come from the calculation proposal endpoint.")
public class CreateCalculationRequest {

	@Schema(description = "The party id of the person the calculation concerns", examples = "81471222-5798-11e9-ae24-57fa13b361e1")
	@NotNull
	@ValidUuid
	private String partyId;

	@Schema(description = "The id of the service to link to, from the calculation proposal endpoint", examples = "12345")
	private Integer serviceId;

	@Schema(description = "The id of the investigation to link to, from the calculation proposal endpoint", examples = "12345")
	private Integer investigationId;

	@Schema(description = "The id of the norm to base the calculation on, from the calculation proposal endpoint", examples = "1")
	@NotNull
	private Integer normId;

	@Schema(description = "The id of the actualisation to link to, from the calculation proposal endpoint", examples = "12345")
	private Integer actualisationId;

	@Schema(description = "The date of the calculation", examples = "2026-05-01")
	@NotNull
	@DateTimeFormat(iso = DATE)
	private LocalDate calculationDate;

	@Schema(description = "The date the calculation is valid from", examples = "2026-05-01")
	@NotNull
	@DateTimeFormat(iso = DATE)
	private LocalDate calculationFromDate;

	@Schema(description = "The date the calculation is valid to", examples = "2026-05-31")
	@NotNull
	@DateTimeFormat(iso = DATE)
	private LocalDate calculationToDate;

	@Schema(description = "Whether a custom household size is used", examples = "false")
	private Boolean hasCustomHouseholdSize;

	@Schema(description = "The size of the household", examples = "4")
	private Integer householdSize;

	@ArraySchema(schema = @Schema(description = "The persons included in the calculation"))
	private List<@Valid CalculationPersonRequest> persons;

	@ArraySchema(schema = @Schema(description = "The incomes included in the calculation"))
	private List<@Valid CalculationIncomeRequest> incomes;

	@ArraySchema(schema = @Schema(description = "The expenses included in the calculation"))
	private List<@Valid CalculationExpenseRequest> expenses;

	@ArraySchema(schema = @Schema(description = "The special expenses included in the calculation"))
	private List<@Valid CalculationExpenseRequest> specialExpenses;

	public static CreateCalculationRequest create() {
		return new CreateCalculationRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public CreateCalculationRequest withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public CreateCalculationRequest withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public Integer getInvestigationId() {
		return investigationId;
	}

	public void setInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
	}

	public CreateCalculationRequest withInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
		return this;
	}

	public Integer getNormId() {
		return normId;
	}

	public void setNormId(final Integer normId) {
		this.normId = normId;
	}

	public CreateCalculationRequest withNormId(final Integer normId) {
		this.normId = normId;
		return this;
	}

	public Integer getActualisationId() {
		return actualisationId;
	}

	public void setActualisationId(final Integer actualisationId) {
		this.actualisationId = actualisationId;
	}

	public CreateCalculationRequest withActualisationId(final Integer actualisationId) {
		this.actualisationId = actualisationId;
		return this;
	}

	public LocalDate getCalculationDate() {
		return calculationDate;
	}

	public void setCalculationDate(final LocalDate calculationDate) {
		this.calculationDate = calculationDate;
	}

	public CreateCalculationRequest withCalculationDate(final LocalDate calculationDate) {
		this.calculationDate = calculationDate;
		return this;
	}

	public LocalDate getCalculationFromDate() {
		return calculationFromDate;
	}

	public void setCalculationFromDate(final LocalDate calculationFromDate) {
		this.calculationFromDate = calculationFromDate;
	}

	public CreateCalculationRequest withCalculationFromDate(final LocalDate calculationFromDate) {
		this.calculationFromDate = calculationFromDate;
		return this;
	}

	public LocalDate getCalculationToDate() {
		return calculationToDate;
	}

	public void setCalculationToDate(final LocalDate calculationToDate) {
		this.calculationToDate = calculationToDate;
	}

	public CreateCalculationRequest withCalculationToDate(final LocalDate calculationToDate) {
		this.calculationToDate = calculationToDate;
		return this;
	}

	public Boolean getHasCustomHouseholdSize() {
		return hasCustomHouseholdSize;
	}

	public void setHasCustomHouseholdSize(final Boolean hasCustomHouseholdSize) {
		this.hasCustomHouseholdSize = hasCustomHouseholdSize;
	}

	public CreateCalculationRequest withHasCustomHouseholdSize(final Boolean hasCustomHouseholdSize) {
		this.hasCustomHouseholdSize = hasCustomHouseholdSize;
		return this;
	}

	public Integer getHouseholdSize() {
		return householdSize;
	}

	public void setHouseholdSize(final Integer householdSize) {
		this.householdSize = householdSize;
	}

	public CreateCalculationRequest withHouseholdSize(final Integer householdSize) {
		this.householdSize = householdSize;
		return this;
	}

	public List<CalculationPersonRequest> getPersons() {
		return persons;
	}

	public void setPersons(final List<CalculationPersonRequest> persons) {
		this.persons = persons;
	}

	public CreateCalculationRequest withPersons(final List<CalculationPersonRequest> persons) {
		this.persons = persons;
		return this;
	}

	public List<CalculationIncomeRequest> getIncomes() {
		return incomes;
	}

	public void setIncomes(final List<CalculationIncomeRequest> incomes) {
		this.incomes = incomes;
	}

	public CreateCalculationRequest withIncomes(final List<CalculationIncomeRequest> incomes) {
		this.incomes = incomes;
		return this;
	}

	public List<CalculationExpenseRequest> getExpenses() {
		return expenses;
	}

	public void setExpenses(final List<CalculationExpenseRequest> expenses) {
		this.expenses = expenses;
	}

	public CreateCalculationRequest withExpenses(final List<CalculationExpenseRequest> expenses) {
		this.expenses = expenses;
		return this;
	}

	public List<CalculationExpenseRequest> getSpecialExpenses() {
		return specialExpenses;
	}

	public void setSpecialExpenses(final List<CalculationExpenseRequest> specialExpenses) {
		this.specialExpenses = specialExpenses;
	}

	public CreateCalculationRequest withSpecialExpenses(final List<CalculationExpenseRequest> specialExpenses) {
		this.specialExpenses = specialExpenses;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CreateCalculationRequest that = (CreateCalculationRequest) o;
		return Objects.equals(partyId, that.partyId) && Objects.equals(serviceId, that.serviceId) && Objects.equals(investigationId, that.investigationId)
			&& Objects.equals(normId, that.normId) && Objects.equals(actualisationId, that.actualisationId) && Objects.equals(calculationDate, that.calculationDate)
			&& Objects.equals(calculationFromDate, that.calculationFromDate) && Objects.equals(calculationToDate, that.calculationToDate)
			&& Objects.equals(hasCustomHouseholdSize, that.hasCustomHouseholdSize) && Objects.equals(householdSize, that.householdSize) && Objects.equals(persons, that.persons)
			&& Objects.equals(incomes, that.incomes) && Objects.equals(expenses, that.expenses) && Objects.equals(specialExpenses, that.specialExpenses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId, serviceId, investigationId, normId, actualisationId, calculationDate, calculationFromDate, calculationToDate, hasCustomHouseholdSize,
			householdSize, persons, incomes, expenses, specialExpenses);
	}

	@Override
	public String toString() {
		return "CreateCalculationRequest{" +
			"partyId='" + partyId + '\'' +
			", serviceId=" + serviceId +
			", investigationId=" + investigationId +
			", normId=" + normId +
			", actualisationId=" + actualisationId +
			", calculationDate=" + calculationDate +
			", calculationFromDate=" + calculationFromDate +
			", calculationToDate=" + calculationToDate +
			", hasCustomHouseholdSize=" + hasCustomHouseholdSize +
			", householdSize=" + householdSize +
			", persons=" + persons +
			", incomes=" + incomes +
			", expenses=" + expenses +
			", specialExpenses=" + specialExpenses +
			'}';
	}
}
