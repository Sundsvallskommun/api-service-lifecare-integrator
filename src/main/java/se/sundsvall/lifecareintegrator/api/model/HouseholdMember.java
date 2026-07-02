package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A member of the household")
public class HouseholdMember {

	@Schema(description = "The party id of the household member; may be null when the member could not be resolved", examples = "81471222-5798-11e9-ae24-57fa13b361e1", nullable = true)
	private String partyId;

	@Schema(description = "The name of the household member", examples = "Anna Andersson")
	private String name;

	@Schema(description = "Whether the member is a child from another household", examples = "false")
	private Boolean childFromOtherHousehold;

	public static HouseholdMember create() {
		return new HouseholdMember();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public HouseholdMember withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public HouseholdMember withName(final String name) {
		this.name = name;
		return this;
	}

	public Boolean getChildFromOtherHousehold() {
		return childFromOtherHousehold;
	}

	public void setChildFromOtherHousehold(final Boolean childFromOtherHousehold) {
		this.childFromOtherHousehold = childFromOtherHousehold;
	}

	public HouseholdMember withChildFromOtherHousehold(final Boolean childFromOtherHousehold) {
		this.childFromOtherHousehold = childFromOtherHousehold;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final HouseholdMember that = (HouseholdMember) o;
		return Objects.equals(partyId, that.partyId) && Objects.equals(name, that.name) && Objects.equals(childFromOtherHousehold, that.childFromOtherHousehold);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId, name, childFromOtherHousehold);
	}

	@Override
	public String toString() {
		return "HouseholdMember{" +
			"partyId='" + partyId + '\'' +
			", name='" + name + '\'' +
			", childFromOtherHousehold=" + childFromOtherHousehold +
			'}';
	}
}
