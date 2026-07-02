package se.sundsvall.lifecareintegrator.api.model;

import java.time.LocalDate;
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

class CalculationIncomeTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationIncome.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var type = "Lön";
		final var amountApplicant = 15000.0;
		final var applicantSearchDate = LocalDate.now();
		final var amountCoApplicant = 12000.0;
		final var coApplicantSearchDate = LocalDate.now().plusDays(1);

		// Act
		final var result = CalculationIncome.create()
			.withType(type)
			.withAmountApplicant(amountApplicant)
			.withApplicantSearchDate(applicantSearchDate)
			.withAmountCoApplicant(amountCoApplicant)
			.withCoApplicantSearchDate(coApplicantSearchDate);

		// Assert
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getAmountApplicant()).isEqualTo(amountApplicant);
		assertThat(result.getApplicantSearchDate()).isEqualTo(applicantSearchDate);
		assertThat(result.getAmountCoApplicant()).isEqualTo(amountCoApplicant);
		assertThat(result.getCoApplicantSearchDate()).isEqualTo(coApplicantSearchDate);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationIncome.create()).hasAllNullFieldsOrProperties();
	}
}
