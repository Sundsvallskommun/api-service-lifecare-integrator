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

class ContactTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Contact.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = "12345";
		final var name = "Bo Bosson";
		final var employmentTitle = "Socialsekreterare";
		final var organizationName = "Ekonomiskt bistånd";
		final var phone = "060-123456";
		final var email = "bo.bosson@example.com";
		final var address = Address.create().withStreetAddress("Storgatan 1");
		final var typeOfContact = "Handläggare";

		// Act
		final var result = Contact.create()
			.withId(id)
			.withName(name)
			.withEmploymentTitle(employmentTitle)
			.withOrganizationName(organizationName)
			.withPhone(phone)
			.withEmail(email)
			.withAddress(address)
			.withTypeOfContact(typeOfContact);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getEmploymentTitle()).isEqualTo(employmentTitle);
		assertThat(result.getOrganizationName()).isEqualTo(organizationName);
		assertThat(result.getPhone()).isEqualTo(phone);
		assertThat(result.getEmail()).isEqualTo(email);
		assertThat(result.getAddress()).isEqualTo(address);
		assertThat(result.getTypeOfContact()).isEqualTo(typeOfContact);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Contact.create()).hasAllNullFieldsOrProperties();
	}
}
