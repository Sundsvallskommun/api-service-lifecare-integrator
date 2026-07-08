package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CalculationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Calculation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = 12345;
		final var norm = "Riksnorm";
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusDays(1);
		final var incomeSum = BigDecimal.valueOf(15000.0);
		final var expenseSum = BigDecimal.valueOf(8000.0);
		final var specialExpenseSum = BigDecimal.valueOf(1500.0);
		final var normSum = BigDecimal.valueOf(6320.0);
		final var commonHouseholdCost = BigDecimal.valueOf(2000.0);
		final var familyCost = BigDecimal.valueOf(4000.0);
		final var balance = BigDecimal.valueOf(-2820.0);
		final var totalSum = BigDecimal.valueOf(2820.0);
		final var investigationId = 456;
		final var serviceId = 789;
		final var finalCalculation = true;
		final var connectedApplication = 1011;
		final var persons = List.of(CalculationPerson.create());
		final var incomes = List.of(CalculationIncome.create());
		final var expenses = List.of(CalculationExpense.create());
		final var specialExpenses = List.of(CalculationExpense.create());

		// Act
		final var result = Calculation.create()
			.withId(id)
			.withNorm(norm)
			.withFromDate(fromDate)
			.withToDate(toDate)
			.withIncomeSum(incomeSum)
			.withExpenseSum(expenseSum)
			.withSpecialExpenseSum(specialExpenseSum)
			.withNormSum(normSum)
			.withCommonHouseholdCost(commonHouseholdCost)
			.withFamilyCost(familyCost)
			.withBalance(balance)
			.withTotalSum(totalSum)
			.withInvestigationId(investigationId)
			.withServiceId(serviceId)
			.withFinalCalculation(finalCalculation)
			.withConnectedApplication(connectedApplication)
			.withPersons(persons)
			.withIncomes(incomes)
			.withExpenses(expenses)
			.withSpecialExpenses(specialExpenses);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getNorm()).isEqualTo(norm);
		assertThat(result.getFromDate()).isEqualTo(fromDate);
		assertThat(result.getToDate()).isEqualTo(toDate);
		assertThat(result.getIncomeSum()).isEqualTo(incomeSum);
		assertThat(result.getExpenseSum()).isEqualTo(expenseSum);
		assertThat(result.getSpecialExpenseSum()).isEqualTo(specialExpenseSum);
		assertThat(result.getNormSum()).isEqualTo(normSum);
		assertThat(result.getCommonHouseholdCost()).isEqualTo(commonHouseholdCost);
		assertThat(result.getFamilyCost()).isEqualTo(familyCost);
		assertThat(result.getBalance()).isEqualTo(balance);
		assertThat(result.getTotalSum()).isEqualTo(totalSum);
		assertThat(result.getInvestigationId()).isEqualTo(investigationId);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
		assertThat(result.getFinalCalculation()).isEqualTo(finalCalculation);
		assertThat(result.getConnectedApplication()).isEqualTo(connectedApplication);
		assertThat(result.getPersons()).isEqualTo(persons);
		assertThat(result.getIncomes()).isEqualTo(incomes);
		assertThat(result.getExpenses()).isEqualTo(expenses);
		assertThat(result.getSpecialExpenses()).isEqualTo(specialExpenses);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Calculation.create()).hasAllNullFieldsOrProperties();
	}
}
