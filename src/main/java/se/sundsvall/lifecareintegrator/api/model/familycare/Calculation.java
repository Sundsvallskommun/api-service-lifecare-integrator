package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A calculation from Lifecare Family Care, re-modelled to a common shape")
public class Calculation {

	@Schema(description = "The calculation id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The norm the calculation is based on", examples = "Riksnorm")
	private String norm;

	@Schema(description = "The date the calculation is valid from", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate fromDate;

	@Schema(description = "The date the calculation is valid to", examples = "2026-05-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate toDate;

	@Schema(description = "The sum of all incomes", examples = "15000.0")
	private BigDecimal incomeSum;

	@Schema(description = "The sum of all expenses", examples = "8000.0")
	private BigDecimal expenseSum;

	@Schema(description = "The sum of all special expenses", examples = "1500.0")
	private BigDecimal specialExpenseSum;

	@Schema(description = "The norm sum", examples = "6320.0")
	private BigDecimal normSum;

	@Schema(description = "The common household cost", examples = "2000.0")
	private BigDecimal commonHouseholdCost;

	@Schema(description = "The family cost", examples = "4000.0")
	private BigDecimal familyCost;

	@Schema(description = "The calculation balance", examples = "-2820.0")
	private BigDecimal balance;

	@Schema(description = "The calculation total sum", examples = "2820.0")
	private BigDecimal totalSum;

	@Schema(description = "The id of the investigation connected to the calculation", examples = "456")
	private Integer investigationId;

	@Schema(description = "The id of the service connected to the calculation", examples = "789")
	private Integer serviceId;

	@Schema(description = "Whether the calculation is final", examples = "true")
	private Boolean finalCalculation;

	@Schema(description = "The id of the application connected to the calculation", examples = "1011")
	private Integer connectedApplication;

	@Schema(description = "The persons included in the calculation")
	private List<CalculationPerson> persons;

	@Schema(description = "The income rows of the calculation")
	private List<CalculationIncome> incomes;

	@Schema(description = "The expense rows of the calculation")
	private List<CalculationExpense> expenses;

	@Schema(description = "The special expense rows of the calculation")
	private List<CalculationExpense> specialExpenses;

	public static Calculation create() {
		return new Calculation();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Calculation withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getNorm() {
		return norm;
	}

	public void setNorm(final String norm) {
		this.norm = norm;
	}

	public Calculation withNorm(final String norm) {
		this.norm = norm;
		return this;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(final LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public Calculation withFromDate(final LocalDate fromDate) {
		this.fromDate = fromDate;
		return this;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(final LocalDate toDate) {
		this.toDate = toDate;
	}

	public Calculation withToDate(final LocalDate toDate) {
		this.toDate = toDate;
		return this;
	}

	public BigDecimal getIncomeSum() {
		return incomeSum;
	}

	public void setIncomeSum(final BigDecimal incomeSum) {
		this.incomeSum = incomeSum;
	}

	public Calculation withIncomeSum(final BigDecimal incomeSum) {
		this.incomeSum = incomeSum;
		return this;
	}

	public BigDecimal getExpenseSum() {
		return expenseSum;
	}

	public void setExpenseSum(final BigDecimal expenseSum) {
		this.expenseSum = expenseSum;
	}

	public Calculation withExpenseSum(final BigDecimal expenseSum) {
		this.expenseSum = expenseSum;
		return this;
	}

	public BigDecimal getSpecialExpenseSum() {
		return specialExpenseSum;
	}

	public void setSpecialExpenseSum(final BigDecimal specialExpenseSum) {
		this.specialExpenseSum = specialExpenseSum;
	}

	public Calculation withSpecialExpenseSum(final BigDecimal specialExpenseSum) {
		this.specialExpenseSum = specialExpenseSum;
		return this;
	}

	public BigDecimal getNormSum() {
		return normSum;
	}

	public void setNormSum(final BigDecimal normSum) {
		this.normSum = normSum;
	}

	public Calculation withNormSum(final BigDecimal normSum) {
		this.normSum = normSum;
		return this;
	}

	public BigDecimal getCommonHouseholdCost() {
		return commonHouseholdCost;
	}

	public void setCommonHouseholdCost(final BigDecimal commonHouseholdCost) {
		this.commonHouseholdCost = commonHouseholdCost;
	}

	public Calculation withCommonHouseholdCost(final BigDecimal commonHouseholdCost) {
		this.commonHouseholdCost = commonHouseholdCost;
		return this;
	}

	public BigDecimal getFamilyCost() {
		return familyCost;
	}

	public void setFamilyCost(final BigDecimal familyCost) {
		this.familyCost = familyCost;
	}

	public Calculation withFamilyCost(final BigDecimal familyCost) {
		this.familyCost = familyCost;
		return this;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(final BigDecimal balance) {
		this.balance = balance;
	}

	public Calculation withBalance(final BigDecimal balance) {
		this.balance = balance;
		return this;
	}

	public BigDecimal getTotalSum() {
		return totalSum;
	}

	public void setTotalSum(final BigDecimal totalSum) {
		this.totalSum = totalSum;
	}

	public Calculation withTotalSum(final BigDecimal totalSum) {
		this.totalSum = totalSum;
		return this;
	}

	public Integer getInvestigationId() {
		return investigationId;
	}

	public void setInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
	}

	public Calculation withInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public Calculation withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public Boolean getFinalCalculation() {
		return finalCalculation;
	}

	public void setFinalCalculation(final Boolean finalCalculation) {
		this.finalCalculation = finalCalculation;
	}

	public Calculation withFinalCalculation(final Boolean finalCalculation) {
		this.finalCalculation = finalCalculation;
		return this;
	}

	public Integer getConnectedApplication() {
		return connectedApplication;
	}

	public void setConnectedApplication(final Integer connectedApplication) {
		this.connectedApplication = connectedApplication;
	}

	public Calculation withConnectedApplication(final Integer connectedApplication) {
		this.connectedApplication = connectedApplication;
		return this;
	}

	public List<CalculationPerson> getPersons() {
		return persons;
	}

	public void setPersons(final List<CalculationPerson> persons) {
		this.persons = persons;
	}

	public Calculation withPersons(final List<CalculationPerson> persons) {
		this.persons = persons;
		return this;
	}

	public List<CalculationIncome> getIncomes() {
		return incomes;
	}

	public void setIncomes(final List<CalculationIncome> incomes) {
		this.incomes = incomes;
	}

	public Calculation withIncomes(final List<CalculationIncome> incomes) {
		this.incomes = incomes;
		return this;
	}

	public List<CalculationExpense> getExpenses() {
		return expenses;
	}

	public void setExpenses(final List<CalculationExpense> expenses) {
		this.expenses = expenses;
	}

	public Calculation withExpenses(final List<CalculationExpense> expenses) {
		this.expenses = expenses;
		return this;
	}

	public List<CalculationExpense> getSpecialExpenses() {
		return specialExpenses;
	}

	public void setSpecialExpenses(final List<CalculationExpense> specialExpenses) {
		this.specialExpenses = specialExpenses;
	}

	public Calculation withSpecialExpenses(final List<CalculationExpense> specialExpenses) {
		this.specialExpenses = specialExpenses;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Calculation that = (Calculation) o;
		return Objects.equals(id, that.id) && Objects.equals(norm, that.norm) && Objects.equals(fromDate, that.fromDate) && Objects.equals(toDate, that.toDate)
			&& Objects.equals(incomeSum, that.incomeSum) && Objects.equals(expenseSum, that.expenseSum) && Objects.equals(specialExpenseSum, that.specialExpenseSum)
			&& Objects.equals(normSum, that.normSum) && Objects.equals(commonHouseholdCost, that.commonHouseholdCost) && Objects.equals(familyCost, that.familyCost)
			&& Objects.equals(balance, that.balance) && Objects.equals(totalSum, that.totalSum) && Objects.equals(investigationId, that.investigationId)
			&& Objects.equals(serviceId, that.serviceId) && Objects.equals(finalCalculation, that.finalCalculation) && Objects.equals(connectedApplication, that.connectedApplication)
			&& Objects.equals(persons, that.persons) && Objects.equals(incomes, that.incomes) && Objects.equals(expenses, that.expenses)
			&& Objects.equals(specialExpenses, that.specialExpenses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, norm, fromDate, toDate, incomeSum, expenseSum, specialExpenseSum, normSum, commonHouseholdCost, familyCost, balance, totalSum,
			investigationId, serviceId, finalCalculation, connectedApplication, persons, incomes, expenses, specialExpenses);
	}

	@Override
	public String toString() {
		return "Calculation{" +
			"id=" + id +
			", norm='" + norm + '\'' +
			", fromDate=" + fromDate +
			", toDate=" + toDate +
			", incomeSum=" + incomeSum +
			", expenseSum=" + expenseSum +
			", specialExpenseSum=" + specialExpenseSum +
			", normSum=" + normSum +
			", commonHouseholdCost=" + commonHouseholdCost +
			", familyCost=" + familyCost +
			", balance=" + balance +
			", totalSum=" + totalSum +
			", investigationId=" + investigationId +
			", serviceId=" + serviceId +
			", finalCalculation=" + finalCalculation +
			", connectedApplication=" + connectedApplication +
			", persons=" + persons +
			", incomes=" + incomes +
			", expenses=" + expenses +
			", specialExpenses=" + specialExpenses +
			'}';
	}
}
