package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.PersonBasedAddressDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedContactDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedPersonDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.api.model.familycare.Contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class PersonMapperTest {

	@Test
	void toPersonWithNull() {
		assertThat(PersonMapper.toPerson(null)).isNull();
	}

	@Test
	void toPerson() {
		// Arrange — personId and source are vendor-internal and must never be mapped
		final var dto = new PersonBasedPersonDTO()
			.personId("190001011234")
			.customerNumber(12345)
			.name("Anna Andersson")
			.streetAddress("Storgatan 1")
			.careOfAddress("c/o Karlsson")
			.postalCode("85185")
			.postalAddress("Sundsvall")
			.phoneHome("060-123456")
			.phoneWork("060-654321")
			.phoneMobile("070-1234567")
			.email("anna.andersson@example.com")
			.source("FC")
			.addressProtection(true)
			.secretPhone(false)
			.protectedRegistration(true);

		// Act
		final var result = PersonMapper.toPerson(dto);

		// Assert
		assertThat(result.getCustomerNumber()).isEqualTo(12345);
		assertThat(result.getName()).isEqualTo("Anna Andersson");
		assertThat(result.getStreetAddress()).isEqualTo("Storgatan 1");
		assertThat(result.getCareOfAddress()).isEqualTo("c/o Karlsson");
		assertThat(result.getPostalCode()).isEqualTo("85185");
		assertThat(result.getPostalAddress()).isEqualTo("Sundsvall");
		assertThat(result.getPhoneHome()).isEqualTo("060-123456");
		assertThat(result.getPhoneWork()).isEqualTo("060-654321");
		assertThat(result.getPhoneMobile()).isEqualTo("070-1234567");
		assertThat(result.getEmail()).isEqualTo("anna.andersson@example.com");
		assertThat(result.getAddressProtection()).isTrue();
		assertThat(result.getSecretPhone()).isFalse();
		assertThat(result.getProtectedRegistration()).isTrue();
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void toPersonWithMinimalInput() {
		// Act
		final var result = PersonMapper.toPerson(new PersonBasedPersonDTO().name("Anna Andersson"));

		// Assert
		assertThat(result).hasAllNullFieldsOrPropertiesExcept("name");
		assertThat(result.getName()).isEqualTo("Anna Andersson");
	}

	@Test
	void toContactsWithNull() {
		assertThat(PersonMapper.toContacts(null)).isEmpty();
	}

	@Test
	void toContacts() {
		// Arrange — the vendor-internal address id must never be mapped
		final var contact = new PersonBasedContactDTO()
			.id("12345")
			.name("Bo Bosson")
			.employmentTitle("Socialsekreterare")
			.organizationName("Ekonomiskt bistånd")
			.phone("060-123456")
			.email("bo.bosson@example.com")
			.address(new PersonBasedAddressDTO()
				.id("address-id")
				.visitingAddress("Storgatan 1")
				.streetAddress("Storgatan 2")
				.postalCode("85185")
				.postalAddress("Sundsvall"))
			.typeOfContact("Handläggare");

		// Act
		final var result = PersonMapper.toContacts(List.of(contact));

		// Assert
		assertThat(result).hasSize(1);
		final var first = result.getFirst();
		assertThat(first.getId()).isEqualTo("12345");
		assertThat(first.getName()).isEqualTo("Bo Bosson");
		assertThat(first.getEmploymentTitle()).isEqualTo("Socialsekreterare");
		assertThat(first.getOrganizationName()).isEqualTo("Ekonomiskt bistånd");
		assertThat(first.getPhone()).isEqualTo("060-123456");
		assertThat(first.getEmail()).isEqualTo("bo.bosson@example.com");
		assertThat(first.getTypeOfContact()).isEqualTo("Handläggare");
		assertThat(first).hasNoNullFieldsOrProperties();
		assertThat(first.getAddress().getVisitingAddress()).isEqualTo("Storgatan 1");
		assertThat(first.getAddress().getStreetAddress()).isEqualTo("Storgatan 2");
		assertThat(first.getAddress().getPostalCode()).isEqualTo("85185");
		assertThat(first.getAddress().getPostalAddress()).isEqualTo("Sundsvall");
		assertThat(first.getAddress()).hasNoNullFieldsOrProperties();
	}

	@Test
	void toContactsWithMultipleEntries() {
		// Arrange
		final var contacts = List.of(
			new PersonBasedContactDTO().id("1").name("Bo Bosson"),
			new PersonBasedContactDTO().id("2").name("Cia Ciasson"));

		// Act
		final var result = PersonMapper.toContacts(contacts);

		// Assert
		assertThat(result)
			.extracting(Contact::getId, Contact::getName)
			.containsExactly(tuple("1", "Bo Bosson"), tuple("2", "Cia Ciasson"));
	}

	@Test
	void toContactsWithMinimalInput() {
		// Act
		final var result = PersonMapper.toContacts(List.of(new PersonBasedContactDTO().name("Bo Bosson")));

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst()).hasAllNullFieldsOrPropertiesExcept("name");
		assertThat(result.getFirst().getName()).isEqualTo("Bo Bosson");
	}
}
