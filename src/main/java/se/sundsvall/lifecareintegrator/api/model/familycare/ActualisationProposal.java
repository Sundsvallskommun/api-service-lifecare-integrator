package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.lifecareintegrator.api.model.common.Lookup;

@Schema(description = "A proposal with the valid values needed to create an actualisation in Lifecare FC")
public class ActualisationProposal {

	@Schema(description = "The valid actualisation types")
	private List<ActualisationType> actualisationTypes;

	@Schema(description = "The valid specify types")
	private List<Lookup> specifyTypes;

	@Schema(description = "The valid working statuses")
	private List<Lookup> workingStatus;

	@Schema(description = "The valid organizations")
	private List<Organization> organizations;

	@Schema(description = "The investigations that can be linked to")
	private List<ProposalCase> investigations;

	@Schema(description = "The services that can be linked to")
	private List<ProposalCase> services;

	@Schema(description = "The valid attachment types")
	private List<AttachmentType> attachmentTypes;

	public static ActualisationProposal create() {
		return new ActualisationProposal();
	}

	public List<ActualisationType> getActualisationTypes() {
		return actualisationTypes;
	}

	public void setActualisationTypes(final List<ActualisationType> actualisationTypes) {
		this.actualisationTypes = actualisationTypes;
	}

	public ActualisationProposal withActualisationTypes(final List<ActualisationType> actualisationTypes) {
		this.actualisationTypes = actualisationTypes;
		return this;
	}

	public List<Lookup> getSpecifyTypes() {
		return specifyTypes;
	}

	public void setSpecifyTypes(final List<Lookup> specifyTypes) {
		this.specifyTypes = specifyTypes;
	}

	public ActualisationProposal withSpecifyTypes(final List<Lookup> specifyTypes) {
		this.specifyTypes = specifyTypes;
		return this;
	}

	public List<Lookup> getWorkingStatus() {
		return workingStatus;
	}

	public void setWorkingStatus(final List<Lookup> workingStatus) {
		this.workingStatus = workingStatus;
	}

	public ActualisationProposal withWorkingStatus(final List<Lookup> workingStatus) {
		this.workingStatus = workingStatus;
		return this;
	}

	public List<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(final List<Organization> organizations) {
		this.organizations = organizations;
	}

	public ActualisationProposal withOrganizations(final List<Organization> organizations) {
		this.organizations = organizations;
		return this;
	}

	public List<ProposalCase> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(final List<ProposalCase> investigations) {
		this.investigations = investigations;
	}

	public ActualisationProposal withInvestigations(final List<ProposalCase> investigations) {
		this.investigations = investigations;
		return this;
	}

	public List<ProposalCase> getServices() {
		return services;
	}

	public void setServices(final List<ProposalCase> services) {
		this.services = services;
	}

	public ActualisationProposal withServices(final List<ProposalCase> services) {
		this.services = services;
		return this;
	}

	public List<AttachmentType> getAttachmentTypes() {
		return attachmentTypes;
	}

	public void setAttachmentTypes(final List<AttachmentType> attachmentTypes) {
		this.attachmentTypes = attachmentTypes;
	}

	public ActualisationProposal withAttachmentTypes(final List<AttachmentType> attachmentTypes) {
		this.attachmentTypes = attachmentTypes;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ActualisationProposal that = (ActualisationProposal) o;
		return Objects.equals(actualisationTypes, that.actualisationTypes) && Objects.equals(specifyTypes, that.specifyTypes) && Objects.equals(workingStatus, that.workingStatus)
			&& Objects.equals(organizations, that.organizations) && Objects.equals(investigations, that.investigations) && Objects.equals(services, that.services)
			&& Objects.equals(attachmentTypes, that.attachmentTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(actualisationTypes, specifyTypes, workingStatus, organizations, investigations, services, attachmentTypes);
	}

	@Override
	public String toString() {
		return "ActualisationProposal{" +
			"actualisationTypes=" + actualisationTypes +
			", specifyTypes=" + specifyTypes +
			", workingStatus=" + workingStatus +
			", organizations=" + organizations +
			", investigations=" + investigations +
			", services=" + services +
			", attachmentTypes=" + attachmentTypes +
			'}';
	}
}
