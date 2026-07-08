package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.time.LocalDate;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class PeriodParametersTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(PeriodParameters.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange

		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusDays(1);
		final var ascending = true;

		// Act
		final var result = PeriodParameters.create()
			.withPartyId(partyId)
			.withFrom(fromDate)
			.withTo(toDate)
			.withAscending(ascending);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getFrom()).isEqualTo(fromDate);
		assertThat(result.getTo()).isEqualTo(toDate);
		assertThat(result.getAscending()).isEqualTo(ascending);

	}
}
