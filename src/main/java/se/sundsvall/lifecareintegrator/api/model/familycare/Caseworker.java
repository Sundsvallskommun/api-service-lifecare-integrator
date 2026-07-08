package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Schema(description = "A caseworker user account in the Lifecare family care system")
public class Caseworker {

	@Schema(description = "The user id in the source system", examples = "12345")
	private String id;

	@Schema(description = "The HSA id of the user", examples = "SE2321000032-1234")
	private String hsaId;

	@Schema(description = "The network user id of the user", examples = "annand01")
	private String networkUserId;

	@Schema(description = "The first name of the user", examples = "Anna")
	private String firstName;

	@Schema(description = "The last name of the user", examples = "Andersson")
	private String lastName;

	@Schema(description = "The full name of the user", examples = "Anna Andersson")
	private String fullName;

	@Schema(description = "A description of the user", examples = "Handläggare ekonomiskt bistånd")
	private String description;

	@Schema(description = "The date and time the user account is valid from", examples = "2026-01-01T00:00:00Z")
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime validFrom;

	@Schema(description = "The date and time the user account is valid to", examples = "2026-12-31T23:59:59Z")
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime validTo;

	@Schema(description = "Whether the user account is disabled", examples = "false")
	private Boolean disabled;

	public static Caseworker create() {
		return new Caseworker();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Caseworker withId(final String id) {
		this.id = id;
		return this;
	}

	public String getHsaId() {
		return hsaId;
	}

	public void setHsaId(final String hsaId) {
		this.hsaId = hsaId;
	}

	public Caseworker withHsaId(final String hsaId) {
		this.hsaId = hsaId;
		return this;
	}

	public String getNetworkUserId() {
		return networkUserId;
	}

	public void setNetworkUserId(final String networkUserId) {
		this.networkUserId = networkUserId;
	}

	public Caseworker withNetworkUserId(final String networkUserId) {
		this.networkUserId = networkUserId;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public Caseworker withFirstName(final String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public Caseworker withLastName(final String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	public Caseworker withFullName(final String fullName) {
		this.fullName = fullName;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Caseworker withDescription(final String description) {
		this.description = description;
		return this;
	}

	public OffsetDateTime getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(final OffsetDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public Caseworker withValidFrom(final OffsetDateTime validFrom) {
		this.validFrom = validFrom;
		return this;
	}

	public OffsetDateTime getValidTo() {
		return validTo;
	}

	public void setValidTo(final OffsetDateTime validTo) {
		this.validTo = validTo;
	}

	public Caseworker withValidTo(final OffsetDateTime validTo) {
		this.validTo = validTo;
		return this;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(final Boolean disabled) {
		this.disabled = disabled;
	}

	public Caseworker withDisabled(final Boolean disabled) {
		this.disabled = disabled;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Caseworker that = (Caseworker) o;
		return Objects.equals(id, that.id) && Objects.equals(hsaId, that.hsaId) && Objects.equals(networkUserId, that.networkUserId)
			&& Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(fullName, that.fullName)
			&& Objects.equals(description, that.description) && Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo)
			&& Objects.equals(disabled, that.disabled);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, hsaId, networkUserId, firstName, lastName, fullName, description, validFrom, validTo, disabled);
	}

	@Override
	public String toString() {
		return "Caseworker{" +
			"id='" + id + '\'' +
			", hsaId='" + hsaId + '\'' +
			", networkUserId='" + networkUserId + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", fullName='" + fullName + '\'' +
			", description='" + description + '\'' +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", disabled=" + disabled +
			'}';
	}
}
