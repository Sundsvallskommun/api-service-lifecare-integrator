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

class AddressTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Address.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var visitingAddress = "Storgatan 1";
		final var streetAddress = "Storgatan 2";
		final var postalCode = "85185";
		final var postalAddress = "Sundsvall";

		// Act
		final var result = Address.create()
			.withVisitingAddress(visitingAddress)
			.withStreetAddress(streetAddress)
			.withPostalCode(postalCode)
			.withPostalAddress(postalAddress);

		// Assert
		assertThat(result.getVisitingAddress()).isEqualTo(visitingAddress);
		assertThat(result.getStreetAddress()).isEqualTo(streetAddress);
		assertThat(result.getPostalCode()).isEqualTo(postalCode);
		assertThat(result.getPostalAddress()).isEqualTo(postalAddress);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Address.create()).hasAllNullFieldsOrProperties();
	}
}
