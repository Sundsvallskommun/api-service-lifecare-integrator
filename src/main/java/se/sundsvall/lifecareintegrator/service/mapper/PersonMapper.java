package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.PersonBasedAddressDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedContactDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedPersonDTO;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.Address;
import se.sundsvall.lifecareintegrator.api.model.familycare.Contact;
import se.sundsvall.lifecareintegrator.api.model.familycare.Person;

import static java.util.Collections.emptyList;

public final class PersonMapper {

	private PersonMapper() {}

	public static Person toPerson(final PersonBasedPersonDTO person) {
		// Intentionally drops the personId — personnummer never leaves this service
		return Optional.ofNullable(person)
			.map(source -> Person.create()
				.withCustomerNumber(source.getCustomerNumber())
				.withName(source.getName())
				.withStreetAddress(source.getStreetAddress())
				.withCareOfAddress(source.getCareOfAddress())
				.withPostalCode(source.getPostalCode())
				.withPostalAddress(source.getPostalAddress())
				.withPhoneHome(source.getPhoneHome())
				.withPhoneWork(source.getPhoneWork())
				.withPhoneMobile(source.getPhoneMobile())
				.withEmail(source.getEmail())
				.withAddressProtection(source.getAddressProtection())
				.withSecretPhone(source.getSecretPhone())
				.withProtectedRegistration(source.getProtectedRegistration()))
			.orElse(null);
	}

	public static List<Contact> toContacts(final List<PersonBasedContactDTO> contacts) {
		return Optional.ofNullable(contacts)
			.map(list -> list.stream()
				.map(PersonMapper::toContact)
				.toList())
			.orElse(emptyList());
	}

	private static Contact toContact(final PersonBasedContactDTO contact) {
		return Optional.ofNullable(contact)
			.map(source -> Contact.create()
				.withId(source.getId())
				.withName(source.getName())
				.withEmploymentTitle(source.getEmploymentTitle())
				.withOrganizationName(source.getOrganizationName())
				.withPhone(source.getPhone())
				.withEmail(source.getEmail())
				.withAddress(toAddress(source.getAddress()))
				.withTypeOfContact(source.getTypeOfContact()))
			.orElse(null);
	}

	private static Address toAddress(final PersonBasedAddressDTO address) {
		// Intentionally drops the vendor-internal address id
		return Optional.ofNullable(address)
			.map(source -> Address.create()
				.withVisitingAddress(source.getVisitingAddress())
				.withStreetAddress(source.getStreetAddress())
				.withPostalCode(source.getPostalCode())
				.withPostalAddress(source.getPostalAddress()))
			.orElse(null);
	}
}
