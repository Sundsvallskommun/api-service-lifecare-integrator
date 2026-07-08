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

class PersonTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Person.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var customerNumber = 12345;
		final var name = "Anna Andersson";
		final var streetAddress = "Storgatan 1";
		final var careOfAddress = "c/o Karlsson";
		final var postalCode = "85185";
		final var postalAddress = "Sundsvall";
		final var phoneHome = "060-123456";
		final var phoneWork = "060-654321";
		final var phoneMobile = "070-1234567";
		final var email = "anna.andersson@example.com";
		final var addressProtection = true;
		final var secretPhone = false;
		final var protectedRegistration = true;

		// Act
		final var result = Person.create()
			.withCustomerNumber(customerNumber)
			.withName(name)
			.withStreetAddress(streetAddress)
			.withCareOfAddress(careOfAddress)
			.withPostalCode(postalCode)
			.withPostalAddress(postalAddress)
			.withPhoneHome(phoneHome)
			.withPhoneWork(phoneWork)
			.withPhoneMobile(phoneMobile)
			.withEmail(email)
			.withAddressProtection(addressProtection)
			.withSecretPhone(secretPhone)
			.withProtectedRegistration(protectedRegistration);

		// Assert
		assertThat(result.getCustomerNumber()).isEqualTo(customerNumber);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getStreetAddress()).isEqualTo(streetAddress);
		assertThat(result.getCareOfAddress()).isEqualTo(careOfAddress);
		assertThat(result.getPostalCode()).isEqualTo(postalCode);
		assertThat(result.getPostalAddress()).isEqualTo(postalAddress);
		assertThat(result.getPhoneHome()).isEqualTo(phoneHome);
		assertThat(result.getPhoneWork()).isEqualTo(phoneWork);
		assertThat(result.getPhoneMobile()).isEqualTo(phoneMobile);
		assertThat(result.getEmail()).isEqualTo(email);
		assertThat(result.getAddressProtection()).isEqualTo(addressProtection);
		assertThat(result.getSecretPhone()).isEqualTo(secretPhone);
		assertThat(result.getProtectedRegistration()).isEqualTo(protectedRegistration);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Person.create()).hasAllNullFieldsOrProperties();
	}
}
