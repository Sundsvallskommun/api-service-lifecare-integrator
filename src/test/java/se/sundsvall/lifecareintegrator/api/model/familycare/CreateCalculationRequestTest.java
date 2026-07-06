package se.sundsvall.lifecareintegrator.api.model.familycare;

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

class CreateCalculationRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CreateCalculationRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";
		final var serviceId = 12345;
		final var investigationId = 23456;
		final var normId = 1;
		final var actualisationId = 34567;
		final var calculationDate = LocalDate.now();
		final var calculationFromDate = LocalDate.now().plusDays(1);
		final var calculationToDate = LocalDate.now().plusDays(2);
		final var hasCustomHouseholdSize = false;
		final var householdSize = 4;
		final var persons = List.of(CalculationPersonRequest.create());
		final var incomes = List.of(CalculationIncomeRequest.create());
		final var expenses = List.of(CalculationExpenseRequest.create());
		final var specialExpenses = List.of(CalculationExpenseRequest.create());

		// Act
		final var result = CreateCalculationRequest.create()
			.withPartyId(partyId)
			.withServiceId(serviceId)
			.withInvestigationId(investigationId)
			.withNormId(normId)
			.withActualisationId(actualisationId)
			.withCalculationDate(calculationDate)
			.withCalculationFromDate(calculationFromDate)
			.withCalculationToDate(calculationToDate)
			.withHasCustomHouseholdSize(hasCustomHouseholdSize)
			.withHouseholdSize(householdSize)
			.withPersons(persons)
			.withIncomes(incomes)
			.withExpenses(expenses)
			.withSpecialExpenses(specialExpenses);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
		assertThat(result.getInvestigationId()).isEqualTo(investigationId);
		assertThat(result.getNormId()).isEqualTo(normId);
		assertThat(result.getActualisationId()).isEqualTo(actualisationId);
		assertThat(result.getCalculationDate()).isEqualTo(calculationDate);
		assertThat(result.getCalculationFromDate()).isEqualTo(calculationFromDate);
		assertThat(result.getCalculationToDate()).isEqualTo(calculationToDate);
		assertThat(result.getHasCustomHouseholdSize()).isEqualTo(hasCustomHouseholdSize);
		assertThat(result.getHouseholdSize()).isEqualTo(householdSize);
		assertThat(result.getPersons()).isEqualTo(persons);
		assertThat(result.getIncomes()).isEqualTo(incomes);
		assertThat(result.getExpenses()).isEqualTo(expenses);
		assertThat(result.getSpecialExpenses()).isEqualTo(specialExpenses);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CreateCalculationRequest.create()).hasAllNullFieldsOrProperties();
	}
}
