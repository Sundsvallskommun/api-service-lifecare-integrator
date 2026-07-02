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

class CalculationExpenseRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationExpenseRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var typeId = 1;
		final var amount = 5000.0;
		final var approvedAmount = 4500.0;
		final var note = "Hyra april";

		// Act
		final var result = CalculationExpenseRequest.create()
			.withTypeId(typeId)
			.withAmount(amount)
			.withApprovedAmount(approvedAmount)
			.withNote(note);

		// Assert
		assertThat(result.getTypeId()).isEqualTo(typeId);
		assertThat(result.getAmount()).isEqualTo(amount);
		assertThat(result.getApprovedAmount()).isEqualTo(approvedAmount);
		assertThat(result.getNote()).isEqualTo(note);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationExpenseRequest.create()).hasAllNullFieldsOrProperties();
	}
}
