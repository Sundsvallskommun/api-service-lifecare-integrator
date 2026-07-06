package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "Decision details specific to the family care (FC) system")
public class FamilyCareDecisionDetails {

	@Schema(description = "Id of the investigation execution the decision belongs to", examples = "5678")
	private Integer investigationExecutionId;

	@Schema(description = "Id of the service the decision belongs to", examples = "910")
	private Integer serviceId;

	@Schema(description = "The organization the decision was made in", examples = "Vuxen försörjningsstöd")
	private String organization;

	@Schema(description = "Name of the co-applicant", examples = "Berit Bengtsson")
	private String coApplicant;

	@Schema(description = "The decision reason for the co-applicant", examples = "Bifall")
	private String reasonCoApplicant;

	@Schema(description = "Id of the connected application", examples = "1112")
	private Integer connectedApplication;

	@Schema(description = "The persons the decision concerns")
	private List<RelatedPerson> persons;

	public static FamilyCareDecisionDetails create() {
		return new FamilyCareDecisionDetails();
	}

	public Integer getInvestigationExecutionId() {
		return investigationExecutionId;
	}

	public void setInvestigationExecutionId(final Integer investigationExecutionId) {
		this.investigationExecutionId = investigationExecutionId;
	}

	public FamilyCareDecisionDetails withInvestigationExecutionId(final Integer investigationExecutionId) {
		this.investigationExecutionId = investigationExecutionId;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public FamilyCareDecisionDetails withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public FamilyCareDecisionDetails withOrganization(final String organization) {
		this.organization = organization;
		return this;
	}

	public String getCoApplicant() {
		return coApplicant;
	}

	public void setCoApplicant(final String coApplicant) {
		this.coApplicant = coApplicant;
	}

	public FamilyCareDecisionDetails withCoApplicant(final String coApplicant) {
		this.coApplicant = coApplicant;
		return this;
	}

	public String getReasonCoApplicant() {
		return reasonCoApplicant;
	}

	public void setReasonCoApplicant(final String reasonCoApplicant) {
		this.reasonCoApplicant = reasonCoApplicant;
	}

	public FamilyCareDecisionDetails withReasonCoApplicant(final String reasonCoApplicant) {
		this.reasonCoApplicant = reasonCoApplicant;
		return this;
	}

	public Integer getConnectedApplication() {
		return connectedApplication;
	}

	public void setConnectedApplication(final Integer connectedApplication) {
		this.connectedApplication = connectedApplication;
	}

	public FamilyCareDecisionDetails withConnectedApplication(final Integer connectedApplication) {
		this.connectedApplication = connectedApplication;
		return this;
	}

	public List<RelatedPerson> getPersons() {
		return persons;
	}

	public void setPersons(final List<RelatedPerson> persons) {
		this.persons = persons;
	}

	public FamilyCareDecisionDetails withPersons(final List<RelatedPerson> persons) {
		this.persons = persons;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FamilyCareDecisionDetails that = (FamilyCareDecisionDetails) o;
		return Objects.equals(investigationExecutionId, that.investigationExecutionId) && Objects.equals(serviceId, that.serviceId)
			&& Objects.equals(organization, that.organization) && Objects.equals(coApplicant, that.coApplicant)
			&& Objects.equals(reasonCoApplicant, that.reasonCoApplicant) && Objects.equals(connectedApplication, that.connectedApplication)
			&& Objects.equals(persons, that.persons);
	}

	@Override
	public int hashCode() {
		return Objects.hash(investigationExecutionId, serviceId, organization, coApplicant, reasonCoApplicant, connectedApplication, persons);
	}

	@Override
	public String toString() {
		return "FamilyCareDecisionDetails{" +
			"investigationExecutionId=" + investigationExecutionId +
			", serviceId=" + serviceId +
			", organization='" + organization + '\'' +
			", coApplicant='" + coApplicant + '\'' +
			", reasonCoApplicant='" + reasonCoApplicant + '\'' +
			", connectedApplication=" + connectedApplication +
			", persons=" + persons +
			'}';
	}
}
