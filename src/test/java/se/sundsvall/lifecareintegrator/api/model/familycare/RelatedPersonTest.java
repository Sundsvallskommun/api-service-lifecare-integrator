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

class RelatedPersonTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(RelatedPerson.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var partyId = "6a5c3d18-1f2b-4e77-9c0a-2b3d4e5f6a7b";
		final var name = "Anna Andersson";
		final var coApplicant = false;

		// Act
		final var result = RelatedPerson.create()
			.withPartyId(partyId)
			.withName(name)
			.withCoApplicant(coApplicant);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getCoApplicant()).isEqualTo(coApplicant);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RelatedPerson.create()).hasAllNullFieldsOrProperties();
	}
}
