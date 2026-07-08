package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A contact (e.g. caseworker) attached to a person in Lifecare family care")
public class Contact {

	@Schema(description = "Id of the contact", examples = "12345")
	private String id;

	@Schema(description = "Name of the contact", examples = "Bo Bosson")
	private String name;

	@Schema(description = "Employment title of the contact", examples = "Socialsekreterare")
	private String employmentTitle;

	@Schema(description = "Organization name of the contact", examples = "Ekonomiskt bistånd")
	private String organizationName;

	@Schema(description = "Phone number", examples = "060-123456")
	private String phone;

	@Schema(description = "Email address", examples = "bo.bosson@example.com")
	private String email;

	@Schema(description = "Address of the contact")
	private Address address;

	@Schema(description = "Type of contact", examples = "Handläggare")
	private String typeOfContact;

	public static Contact create() {
		return new Contact();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Contact withId(final String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Contact withName(final String name) {
		this.name = name;
		return this;
	}

	public String getEmploymentTitle() {
		return employmentTitle;
	}

	public void setEmploymentTitle(final String employmentTitle) {
		this.employmentTitle = employmentTitle;
	}

	public Contact withEmploymentTitle(final String employmentTitle) {
		this.employmentTitle = employmentTitle;
		return this;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(final String organizationName) {
		this.organizationName = organizationName;
	}

	public Contact withOrganizationName(final String organizationName) {
		this.organizationName = organizationName;
		return this;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}

	public Contact withPhone(final String phone) {
		this.phone = phone;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public Contact withEmail(final String email) {
		this.email = email;
		return this;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(final Address address) {
		this.address = address;
	}

	public Contact withAddress(final Address address) {
		this.address = address;
		return this;
	}

	public String getTypeOfContact() {
		return typeOfContact;
	}

	public void setTypeOfContact(final String typeOfContact) {
		this.typeOfContact = typeOfContact;
	}

	public Contact withTypeOfContact(final String typeOfContact) {
		this.typeOfContact = typeOfContact;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Contact that = (Contact) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(employmentTitle, that.employmentTitle)
			&& Objects.equals(organizationName, that.organizationName) && Objects.equals(phone, that.phone) && Objects.equals(email, that.email)
			&& Objects.equals(address, that.address) && Objects.equals(typeOfContact, that.typeOfContact);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, employmentTitle, organizationName, phone, email, address, typeOfContact);
	}

	@Override
	public String toString() {
		return "Contact{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", employmentTitle='" + employmentTitle + '\'' +
			", organizationName='" + organizationName + '\'' +
			", phone='" + phone + '\'' +
			", email='" + email + '\'' +
			", address=" + address +
			", typeOfContact='" + typeOfContact + '\'' +
			'}';
	}
}
