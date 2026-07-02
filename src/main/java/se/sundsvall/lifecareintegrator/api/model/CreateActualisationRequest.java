package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "Request to create an actualisation in Lifecare FC. All ids come from the actualisation proposal endpoint.")
public class CreateActualisationRequest {

	@Schema(description = "The party id of the person the actualisation concerns", examples = "81471222-5798-11e9-ae24-57fa13b361e1")
	@NotNull
	@ValidUuid
	private String partyId;

	@Schema(description = "The date of the actualisation", examples = "2026-05-01")
	@NotNull
	@DateTimeFormat(iso = DATE)
	private LocalDate date;

	@Schema(description = "The id of the actualisation type, from the actualisation proposal endpoint", examples = "1")
	@NotNull
	private Integer typeId;

	@Schema(description = "The id of the from-who value, from the actualisation proposal endpoint", examples = "1")
	private Integer fromWhoId;

	@Schema(description = "The id of the reason, from the actualisation proposal endpoint", examples = "1")
	private Integer reasonId;

	@Schema(description = "The id of the organization, from the actualisation proposal endpoint", examples = "1")
	private Integer organisationId;

	@Schema(description = "The id of the organization unit, from the actualisation proposal endpoint", examples = "100")
	private String organisationUnitId;

	@Schema(description = "The id of the caseworker, from the actualisation proposal endpoint", examples = "abc123")
	private String caseworkerId;

	@Schema(description = "The id of the specify type, from the actualisation proposal endpoint", examples = "1")
	private Integer specifiesId;

	@Schema(description = "The id of the service to link to, from the actualisation proposal endpoint", examples = "12345")
	private Integer serviceId;

	@Schema(description = "The id of the investigation to link to, from the actualisation proposal endpoint", examples = "12345")
	private Integer investigationId;

	@Schema(description = "The id of the working status, from the actualisation proposal endpoint", examples = "1")
	private Integer workingStatusId;

	public static CreateActualisationRequest create() {
		return new CreateActualisationRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public CreateActualisationRequest withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(final LocalDate date) {
		this.date = date;
	}

	public CreateActualisationRequest withDate(final LocalDate date) {
		this.date = date;
		return this;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(final Integer typeId) {
		this.typeId = typeId;
	}

	public CreateActualisationRequest withTypeId(final Integer typeId) {
		this.typeId = typeId;
		return this;
	}

	public Integer getFromWhoId() {
		return fromWhoId;
	}

	public void setFromWhoId(final Integer fromWhoId) {
		this.fromWhoId = fromWhoId;
	}

	public CreateActualisationRequest withFromWhoId(final Integer fromWhoId) {
		this.fromWhoId = fromWhoId;
		return this;
	}

	public Integer getReasonId() {
		return reasonId;
	}

	public void setReasonId(final Integer reasonId) {
		this.reasonId = reasonId;
	}

	public CreateActualisationRequest withReasonId(final Integer reasonId) {
		this.reasonId = reasonId;
		return this;
	}

	public Integer getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(final Integer organisationId) {
		this.organisationId = organisationId;
	}

	public CreateActualisationRequest withOrganisationId(final Integer organisationId) {
		this.organisationId = organisationId;
		return this;
	}

	public String getOrganisationUnitId() {
		return organisationUnitId;
	}

	public void setOrganisationUnitId(final String organisationUnitId) {
		this.organisationUnitId = organisationUnitId;
	}

	public CreateActualisationRequest withOrganisationUnitId(final String organisationUnitId) {
		this.organisationUnitId = organisationUnitId;
		return this;
	}

	public String getCaseworkerId() {
		return caseworkerId;
	}

	public void setCaseworkerId(final String caseworkerId) {
		this.caseworkerId = caseworkerId;
	}

	public CreateActualisationRequest withCaseworkerId(final String caseworkerId) {
		this.caseworkerId = caseworkerId;
		return this;
	}

	public Integer getSpecifiesId() {
		return specifiesId;
	}

	public void setSpecifiesId(final Integer specifiesId) {
		this.specifiesId = specifiesId;
	}

	public CreateActualisationRequest withSpecifiesId(final Integer specifiesId) {
		this.specifiesId = specifiesId;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public CreateActualisationRequest withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public Integer getInvestigationId() {
		return investigationId;
	}

	public void setInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
	}

	public CreateActualisationRequest withInvestigationId(final Integer investigationId) {
		this.investigationId = investigationId;
		return this;
	}

	public Integer getWorkingStatusId() {
		return workingStatusId;
	}

	public void setWorkingStatusId(final Integer workingStatusId) {
		this.workingStatusId = workingStatusId;
	}

	public CreateActualisationRequest withWorkingStatusId(final Integer workingStatusId) {
		this.workingStatusId = workingStatusId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CreateActualisationRequest that = (CreateActualisationRequest) o;
		return Objects.equals(partyId, that.partyId) && Objects.equals(date, that.date) && Objects.equals(typeId, that.typeId) && Objects.equals(fromWhoId, that.fromWhoId)
			&& Objects.equals(reasonId, that.reasonId) && Objects.equals(organisationId, that.organisationId) && Objects.equals(organisationUnitId, that.organisationUnitId)
			&& Objects.equals(caseworkerId, that.caseworkerId) && Objects.equals(specifiesId, that.specifiesId) && Objects.equals(serviceId, that.serviceId)
			&& Objects.equals(investigationId, that.investigationId) && Objects.equals(workingStatusId, that.workingStatusId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId, date, typeId, fromWhoId, reasonId, organisationId, organisationUnitId, caseworkerId, specifiesId, serviceId, investigationId, workingStatusId);
	}

	@Override
	public String toString() {
		return "CreateActualisationRequest{" +
			"partyId='" + partyId + '\'' +
			", date=" + date +
			", typeId=" + typeId +
			", fromWhoId=" + fromWhoId +
			", reasonId=" + reasonId +
			", organisationId=" + organisationId +
			", organisationUnitId='" + organisationUnitId + '\'' +
			", caseworkerId='" + caseworkerId + '\'' +
			", specifiesId=" + specifiesId +
			", serviceId=" + serviceId +
			", investigationId=" + investigationId +
			", workingStatusId=" + workingStatusId +
			'}';
	}
}
