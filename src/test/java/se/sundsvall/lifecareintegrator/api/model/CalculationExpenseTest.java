package se.sundsvall.lifecareintegrator.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CalculationExpenseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationExpense.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var type = "Hyra";
		final var appliedAmount = 8000.0;
		final var approvedAmount = 7500.0;

		// Act
		final var result = CalculationExpense.create()
			.withType(type)
			.withAppliedAmount(appliedAmount)
			.withApprovedAmount(approvedAmount);

		// Assert
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getAppliedAmount()).isEqualTo(appliedAmount);
		assertThat(result.getApprovedAmount()).isEqualTo(approvedAmount);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationExpense.create()).hasAllNullFieldsOrProperties();
	}
}
