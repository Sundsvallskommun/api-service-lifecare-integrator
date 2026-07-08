package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "Address of a contact")
public class Address {

	@Schema(description = "Visiting address", examples = "Storgatan 1")
	private String visitingAddress;

	@Schema(description = "Street address", examples = "Storgatan 1")
	private String streetAddress;

	@Schema(description = "Postal code", examples = "85185")
	private String postalCode;

	@Schema(description = "Postal address", examples = "Sundsvall")
	private String postalAddress;

	public static Address create() {
		return new Address();
	}

	public String getVisitingAddress() {
		return visitingAddress;
	}

	public void setVisitingAddress(final String visitingAddress) {
		this.visitingAddress = visitingAddress;
	}

	public Address withVisitingAddress(final String visitingAddress) {
		this.visitingAddress = visitingAddress;
		return this;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(final String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public Address withStreetAddress(final String streetAddress) {
		this.streetAddress = streetAddress;
		return this;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	public Address withPostalCode(final String postalCode) {
		this.postalCode = postalCode;
		return this;
	}

	public String getPostalAddress() {
		return postalAddress;
	}

	public void setPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
	}

	public Address withPostalAddress(final String postalAddress) {
		this.postalAddress = postalAddress;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Address that = (Address) o;
		return Objects.equals(visitingAddress, that.visitingAddress) && Objects.equals(streetAddress, that.streetAddress)
			&& Objects.equals(postalCode, that.postalCode) && Objects.equals(postalAddress, that.postalAddress);
	}

	@Override
	public int hashCode() {
		return Objects.hash(visitingAddress, streetAddress, postalCode, postalAddress);
	}

	@Override
	public String toString() {
		return "Address{" +
			"visitingAddress='" + visitingAddress + '\'' +
			", streetAddress='" + streetAddress + '\'' +
			", postalCode='" + postalCode + '\'' +
			", postalAddress='" + postalAddress + '\'' +
			'}';
	}
}
