package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A person included in an investigation")
public class InvestigationPerson {

	@Schema(description = "The name of the person", examples = "Anna Andersson")
	private String name;

	@Schema(description = "Whether the person is a co-applicant", examples = "false")
	private Boolean coApplicant;

	public static InvestigationPerson create() {
		return new InvestigationPerson();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public InvestigationPerson withName(final String name) {
		this.name = name;
		return this;
	}

	public Boolean getCoApplicant() {
		return coApplicant;
	}

	public void setCoApplicant(final Boolean coApplicant) {
		this.coApplicant = coApplicant;
	}

	public InvestigationPerson withCoApplicant(final Boolean coApplicant) {
		this.coApplicant = coApplicant;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final InvestigationPerson that = (InvestigationPerson) o;
		return Objects.equals(name, that.name) && Objects.equals(coApplicant, that.coApplicant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, coApplicant);
	}

	@Override
	public String toString() {
		return "InvestigationPerson{" +
			"name='" + name + '\'' +
			", coApplicant=" + coApplicant +
			'}';
	}
}
