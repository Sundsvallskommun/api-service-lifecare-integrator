package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.math.BigDecimal;
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

class CalculationPersonTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationPerson.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var name = "Kalle Karlsson";
		final var amount = BigDecimal.valueOf(3160.0);
		final var deviationFromDate = LocalDate.now();
		final var deviationToDate = LocalDate.now().plusDays(30);

		// Act
		final var result = CalculationPerson.create()
			.withName(name)
			.withAmount(amount)
			.withDeviationFromDate(deviationFromDate)
			.withDeviationToDate(deviationToDate);

		// Assert
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getAmount()).isEqualTo(amount);
		assertThat(result.getDeviationFromDate()).isEqualTo(deviationFromDate);
		assertThat(result.getDeviationToDate()).isEqualTo(deviationToDate);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationPerson.create()).hasAllNullFieldsOrProperties();
	}
}
