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

class HouseholdMemberTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(HouseholdMember.class, allOf(
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
		final var name = "Anna Andersson";
		final var childFromOtherHousehold = false;

		// Act
		final var result = HouseholdMember.create()
			.withPartyId(partyId)
			.withName(name)
			.withChildFromOtherHousehold(childFromOtherHousehold);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getChildFromOtherHousehold()).isEqualTo(childFromOtherHousehold);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(HouseholdMember.create()).hasAllNullFieldsOrProperties();
	}
}
