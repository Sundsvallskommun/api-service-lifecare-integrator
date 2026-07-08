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

class OrganizationTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Organization.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = 1;
		final var unitId = "100";
		final var name = "Vuxenenheten";

		// Act
		final var result = Organization.create()
			.withId(id)
			.withUnitId(unitId)
			.withName(name);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getUnitId()).isEqualTo(unitId);
		assertThat(result.getName()).isEqualTo(name);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Organization.create()).hasAllNullFieldsOrProperties();
	}
}
