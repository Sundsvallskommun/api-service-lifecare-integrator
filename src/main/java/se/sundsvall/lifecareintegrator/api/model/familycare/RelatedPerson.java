package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A person related to a family care case")
public class RelatedPerson {

	@Schema(description = "The party id of the person, resolved from the identity Lifecare stores. Null when the person cannot be resolved", examples = "6a5c3d18-1f2b-4e77-9c0a-2b3d4e5f6a7b")
	private String partyId;

	@Schema(description = "The name of the person", examples = "Anna Andersson")
	private String name;

	@Schema(description = "Whether the person is a co-applicant. Null when the source does not state a role", examples = "false")
	private Boolean coApplicant;

	public static RelatedPerson create() {
		return new RelatedPerson();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public RelatedPerson withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
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
		return Objects.equals(partyId, that.partyId) && Objects.equals(name, that.name) && Objects.equals(coApplicant, that.coApplicant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId, name, coApplicant);
	}

	@Override
	public String toString() {
		return "RelatedPerson{" +
			"partyId='" + partyId + '\'' +
			", name='" + name + '\'' +
			", coApplicant=" + coApplicant +
			'}';
	}
}
