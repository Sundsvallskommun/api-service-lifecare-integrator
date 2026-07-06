package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A person a decision concerns")
public class DecisionPerson {

	@Schema(description = "Name of the person", examples = "Kalle Karlsson")
	private String name;

	@Schema(description = "Whether the person is a co-applicant", examples = "false")
	private Boolean coApplicant;

	public static DecisionPerson create() {
		return new DecisionPerson();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public DecisionPerson withName(final String name) {
		this.name = name;
		return this;
	}

	public Boolean getCoApplicant() {
		return coApplicant;
	}

	public void setCoApplicant(final Boolean coApplicant) {
		this.coApplicant = coApplicant;
	}

	public DecisionPerson withCoApplicant(final Boolean coApplicant) {
		this.coApplicant = coApplicant;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final DecisionPerson that = (DecisionPerson) o;
		return Objects.equals(name, that.name) && Objects.equals(coApplicant, that.coApplicant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, coApplicant);
	}

	@Override
	public String toString() {
		return "DecisionPerson{" +
			"name='" + name + '\'' +
			", coApplicant=" + coApplicant +
			'}';
	}
}
