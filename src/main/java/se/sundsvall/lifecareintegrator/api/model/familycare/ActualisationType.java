package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.lifecareintegrator.api.model.common.Lookup;

@Schema(description = "An actualisation type with its valid sub-code-lists")
public class ActualisationType {

	@Schema(description = "The id of the actualisation type", examples = "1")
	private Integer id;

	@Schema(description = "The name of the actualisation type", examples = "Ansökan")
	private String name;

	@Schema(description = "Whether a specify type must be given for this actualisation type", examples = "true")
	private Boolean specifyTypeMandatory;

	@Schema(description = "Whether a working status must be given for this actualisation type", examples = "false")
	private Boolean workingStatus;

	@Schema(description = "The valid reasons for this actualisation type")
	private List<Lookup> reasons;

	@Schema(description = "The valid from-who values for this actualisation type")
	private List<Lookup> fromWho;

	@Schema(description = "The valid investigation types for this actualisation type")
	private List<Lookup> investigationTypes;

	@Schema(description = "The valid service types for this actualisation type")
	private List<Lookup> serviceTypes;

	public static ActualisationType create() {
		return new ActualisationType();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public ActualisationType withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ActualisationType withName(final String name) {
		this.name = name;
		return this;
	}

	public Boolean getSpecifyTypeMandatory() {
		return specifyTypeMandatory;
	}

	public void setSpecifyTypeMandatory(final Boolean specifyTypeMandatory) {
		this.specifyTypeMandatory = specifyTypeMandatory;
	}

	public ActualisationType withSpecifyTypeMandatory(final Boolean specifyTypeMandatory) {
		this.specifyTypeMandatory = specifyTypeMandatory;
		return this;
	}

	public Boolean getWorkingStatus() {
		return workingStatus;
	}

	public void setWorkingStatus(final Boolean workingStatus) {
		this.workingStatus = workingStatus;
	}

	public ActualisationType withWorkingStatus(final Boolean workingStatus) {
		this.workingStatus = workingStatus;
		return this;
	}

	public List<Lookup> getReasons() {
		return reasons;
	}

	public void setReasons(final List<Lookup> reasons) {
		this.reasons = reasons;
	}

	public ActualisationType withReasons(final List<Lookup> reasons) {
		this.reasons = reasons;
		return this;
	}

	public List<Lookup> getFromWho() {
		return fromWho;
	}

	public void setFromWho(final List<Lookup> fromWho) {
		this.fromWho = fromWho;
	}

	public ActualisationType withFromWho(final List<Lookup> fromWho) {
		this.fromWho = fromWho;
		return this;
	}

	public List<Lookup> getInvestigationTypes() {
		return investigationTypes;
	}

	public void setInvestigationTypes(final List<Lookup> investigationTypes) {
		this.investigationTypes = investigationTypes;
	}

	public ActualisationType withInvestigationTypes(final List<Lookup> investigationTypes) {
		this.investigationTypes = investigationTypes;
		return this;
	}

	public List<Lookup> getServiceTypes() {
		return serviceTypes;
	}

	public void setServiceTypes(final List<Lookup> serviceTypes) {
		this.serviceTypes = serviceTypes;
	}

	public ActualisationType withServiceTypes(final List<Lookup> serviceTypes) {
		this.serviceTypes = serviceTypes;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ActualisationType that = (ActualisationType) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(specifyTypeMandatory, that.specifyTypeMandatory)
			&& Objects.equals(workingStatus, that.workingStatus) && Objects.equals(reasons, that.reasons) && Objects.equals(fromWho, that.fromWho)
			&& Objects.equals(investigationTypes, that.investigationTypes) && Objects.equals(serviceTypes, that.serviceTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, specifyTypeMandatory, workingStatus, reasons, fromWho, investigationTypes, serviceTypes);
	}

	@Override
	public String toString() {
		return "ActualisationType{" +
			"id=" + id +
			", name='" + name + '\'' +
			", specifyTypeMandatory=" + specifyTypeMandatory +
			", workingStatus=" + workingStatus +
			", reasons=" + reasons +
			", fromWho=" + fromWho +
			", investigationTypes=" + investigationTypes +
			", serviceTypes=" + serviceTypes +
			'}';
	}
}
