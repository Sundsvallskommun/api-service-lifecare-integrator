package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An investigation or service that can be linked to in a proposal")
public class ProposalCase {

	@Schema(description = "The id of the investigation or service", examples = "12345")
	private Integer id;

	@Schema(description = "The type of the investigation or service", examples = "1")
	private Integer type;

	@Schema(description = "The name of the investigation or service", examples = "Utredning ekonomiskt bistånd")
	private String name;

	@Schema(description = "The start date of the investigation or service", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate startDate;

	@Schema(description = "The id of the organization the investigation or service belongs to", examples = "1")
	private Integer organisationId;

	@Schema(description = "The id of the organization unit the investigation or service belongs to", examples = "100")
	private String organisationUnitId;

	@Schema(description = "The id of the caseworker responsible for the investigation or service", examples = "abc123")
	private String caseworkerId;

	public static ProposalCase create() {
		return new ProposalCase();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public ProposalCase withId(final Integer id) {
		this.id = id;
		return this;
	}

	public Integer getType() {
		return type;
	}

	public void setType(final Integer type) {
		this.type = type;
	}

	public ProposalCase withType(final Integer type) {
		this.type = type;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ProposalCase withName(final String name) {
		this.name = name;
		return this;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
	}

	public ProposalCase withStartDate(final LocalDate startDate) {
		this.startDate = startDate;
		return this;
	}

	public Integer getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(final Integer organisationId) {
		this.organisationId = organisationId;
	}

	public ProposalCase withOrganisationId(final Integer organisationId) {
		this.organisationId = organisationId;
		return this;
	}

	public String getOrganisationUnitId() {
		return organisationUnitId;
	}

	public void setOrganisationUnitId(final String organisationUnitId) {
		this.organisationUnitId = organisationUnitId;
	}

	public ProposalCase withOrganisationUnitId(final String organisationUnitId) {
		this.organisationUnitId = organisationUnitId;
		return this;
	}

	public String getCaseworkerId() {
		return caseworkerId;
	}

	public void setCaseworkerId(final String caseworkerId) {
		this.caseworkerId = caseworkerId;
	}

	public ProposalCase withCaseworkerId(final String caseworkerId) {
		this.caseworkerId = caseworkerId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ProposalCase that = (ProposalCase) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(name, that.name) && Objects.equals(startDate, that.startDate)
			&& Objects.equals(organisationId, that.organisationId) && Objects.equals(organisationUnitId, that.organisationUnitId) && Objects.equals(caseworkerId, that.caseworkerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, name, startDate, organisationId, organisationUnitId, caseworkerId);
	}

	@Override
	public String toString() {
		return "ProposalCase{" +
			"id=" + id +
			", type=" + type +
			", name='" + name + '\'' +
			", startDate=" + startDate +
			", organisationId=" + organisationId +
			", organisationUnitId='" + organisationUnitId + '\'' +
			", caseworkerId='" + caseworkerId + '\'' +
			'}';
	}
}
