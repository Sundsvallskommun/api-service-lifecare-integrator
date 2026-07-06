package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A person related to a family care case")
public class RelatedPerson {

	@Schema(description = "The name of the person", examples = "Anna Andersson")
	private String name;

	@Schema(description = "Whether the person is a co-applicant. Null when the source does not state a role", examples = "false")
	private Boolean coApplicant;

	public static RelatedPerson create() {
		return new RelatedPerson();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public RelatedPerson withName(final String name) {
		this.name = name;
		return this;
	}

	public Boolean getCoApplicant() {
		return coApplicant;
	}

	public void setCoApplicant(final Boolean coApplicant) {
		this.coApplicant = coApplicant;
	}

	public RelatedPerson withCoApplicant(final Boolean coApplicant) {
		this.coApplicant = coApplicant;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final RelatedPerson that = (RelatedPerson) o;
		return Objects.equals(name, that.name) && Objects.equals(coApplicant, that.coApplicant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, coApplicant);
	}

	@Override
	public String toString() {
		return "RelatedPerson{" +
			"name='" + name + '\'' +
			", coApplicant=" + coApplicant +
			'}';
	}
}
