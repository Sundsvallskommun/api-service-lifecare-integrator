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

class CalculationPersonRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationPersonRequest.class, allOf(
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
		final var numberOfDays = 30;
		final var deviationFromDate = LocalDate.now();
		final var deviationToDate = LocalDate.now().plusDays(1);

		// Act
		final var result = CalculationPersonRequest.create()
			.withPartyId(partyId)
			.withNumberOfDays(numberOfDays)
			.withDeviationFromDate(deviationFromDate)
			.withDeviationToDate(deviationToDate);

		// Assert
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getNumberOfDays()).isEqualTo(numberOfDays);
		assertThat(result.getDeviationFromDate()).isEqualTo(deviationFromDate);
		assertThat(result.getDeviationToDate()).isEqualTo(deviationToDate);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationPersonRequest.create()).hasAllNullFieldsOrProperties();
	}
}
