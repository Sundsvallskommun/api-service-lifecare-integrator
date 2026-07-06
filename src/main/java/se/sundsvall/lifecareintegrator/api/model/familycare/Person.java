package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "Person master data from Lifecare family care")
public class Person {

	@Schema(description = "The customer number in the source system", examples = "12345")
	private Integer customerNumber;

	@Schema(description = "Full name of the person", examples = "Anna Andersson")
	private String name;

	@Schema(description = "Street address", examples = "Storgatan 1")
	private String streetAddress;

	@Schema(description = "Care of (c/o) address", examples = "c/o Karlsson")
	private String careOfAddress;

	@Schema(description = "Postal code", examples = "85185")
	private String postalCode;

	@Schema(description = "Postal address", examples = "Sundsvall")
	private String postalAddress;

	@Schema(description = "Home phone number", examples = "060-123456")
	private String phoneHome;

	@Schema(description = "Work phone number", examples = "060-654321")
	private String phoneWork;

	@Schema(description = "Mobile phone number", examples = "070-1234567")
	private String phoneMobile;

	@Schema(description = "Email address", examples = "anna.andersson@example.com")
	private String email;

	@Schema(description = "Whether the person's address is protected", examples = "false")
	private Boolean addressProtection;

	@Schema(description = "Whether the person's phone number is secret", examples = "false")
	private Boolean secretPhone;

	@Schema(description = "Whether the person has protected registration", examples = "false")
	private Boolean protectedRegistration;

	public static Person create() {
		return new Person();
	}

	public Integer getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(final Integer customerNumber) {
		this.customerNumber = customerNumber;
	}

	public Person withCustomerNumber(final Integer customerNumber) {
		this.customerNumber = customerNumber;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Person withName(final String name) {
		this.name = name;
		return this;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(final String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public Person withStreetAddress(final String streetAddress) {
		this.streetAddress = streetAddress;
		return this;
	}

	public String getCareOfAddress() {
		return careOfAddress;
	}

	public void setCareOfAddress(final String careOfAddress) {
		this.careOfAddress = careOfAddress;
	}

	public Person withCareOfAddress(final String careOfAddress) {
		this.careOfAddress = careOfAddress;
		return this;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	public Person withPostalCode(final String postalCode) {
		this.postalCode = postalCode;
		return this;
	}

	public String getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
	}

	public Person withPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
		return this;
	}

	public String getPhoneHome() {
		return phoneHome;
	}

	public void setPhoneHome(final String phoneHome) {
		this.phoneHome = phoneHome;
	}

	public Person withPhoneHome(final String phoneHome) {
		this.phoneHome = phoneHome;
		return this;
	}

	public String getPhoneWork() {
		return phoneWork;
	}

	public void setPhoneWork(final String phoneWork) {
		this.phoneWork = phoneWork;
	}

	public Person withPhoneWork(final String phoneWork) {
		this.phoneWork = phoneWork;
		return this;
	}

	public String getPhoneMobile() {
		return phoneMobile;
	}

	public void setPhoneMobile(final String phoneMobile) {
		this.phoneMobile = phoneMobile;
	}

	public Person withPhoneMobile(final String phoneMobile) {
		this.phoneMobile = phoneMobile;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public Person withEmail(final String email) {
		this.email = email;
		return this;
	}

	public Boolean getAddressProtection() {
		return addressProtection;
	}

	public void setAddressProtection(final Boolean addressProtection) {
		this.addressProtection = addressProtection;
	}

	public Person withAddressProtection(final Boolean addressProtection) {
		this.addressProtection = addressProtection;
		return this;
	}

	public Boolean getSecretPhone() {
		return secretPhone;
	}

	public void setSecretPhone(final Boolean secretPhone) {
		this.secretPhone = secretPhone;
	}

	public Person withSecretPhone(final Boolean secretPhone) {
		this.secretPhone = secretPhone;
		return this;
	}

	public Boolean getProtectedRegistration() {
		return protectedRegistration;
	}

	public void setProtectedRegistration(final Boolean protectedRegistration) {
		this.protectedRegistration = protectedRegistration;
	}

	public Person withProtectedRegistration(final Boolean protectedRegistration) {
		this.protectedRegistration = protectedRegistration;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Person that = (Person) o;
		return Objects.equals(customerNumber, that.customerNumber) && Objects.equals(name, that.name) && Objects.equals(streetAddress, that.streetAddress)
			&& Objects.equals(careOfAddress, that.careOfAddress) && Objects.equals(postalCode, that.postalCode) && Objects.equals(postalAddress, that.postalAddress)
			&& Objects.equals(phoneHome, that.phoneHome) && Objects.equals(phoneWork, that.phoneWork) && Objects.equals(phoneMobile, that.phoneMobile)
			&& Objects.equals(email, that.email) && Objects.equals(addressProtection, that.addressProtection) && Objects.equals(secretPhone, that.secretPhone)
			&& Objects.equals(protectedRegistration, that.protectedRegistration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(customerNumber, name, streetAddress, careOfAddress, postalCode, postalAddress, phoneHome, phoneWork, phoneMobile, email, addressProtection, secretPhone, protectedRegistration);
	}

	@Override
	public String toString() {
		return "Person{" +
			"customerNumber=" + customerNumber +
			", name='" + name + '\'' +
			", streetAddress='" + streetAddress + '\'' +
			", careOfAddress='" + careOfAddress + '\'' +
			", postalCode='" + postalCode + '\'' +
			", postalAddress='" + postalAddress + '\'' +
			", phoneHome='" + phoneHome + '\'' +
			", phoneWork='" + phoneWork + '\'' +
			", phoneMobile='" + phoneMobile + '\'' +
			", email='" + email + '\'' +
			", addressProtection=" + addressProtection +
			", secretPhone=" + secretPhone +
			", protectedRegistration=" + protectedRegistration +
			'}';
	}
}
