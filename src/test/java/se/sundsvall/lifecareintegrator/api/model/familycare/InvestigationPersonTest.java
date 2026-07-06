package se.sundsvall.lifecareintegrator.api.model.familycare;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class InvestigationPersonTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(InvestigationPerson.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var name = "Anna Andersson";
		final var coApplicant = false;

		// Act
		final var result = InvestigationPerson.create()
			.withName(name)
			.withCoApplicant(coApplicant);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getCoApplicant()).isEqualTo(coApplicant);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(InvestigationPerson.create()).hasAllNullFieldsOrProperties();
	}
}
