package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "An organization entry")
public class OrganizationItem {

	@Schema(description = "The id of the organization", examples = "1")
	private Integer id;

	@Schema(description = "The id of the organization unit", examples = "100")
	private String unitId;

	@Schema(description = "The name of the organization", examples = "Vuxenenheten")
	private String name;

	public static OrganizationItem create() {
		return new OrganizationItem();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public OrganizationItem withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(final String unitId) {
		this.unitId = unitId;
	}

	public OrganizationItem withUnitId(final String unitId) {
		this.unitId = unitId;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public OrganizationItem withName(final String name) {
		this.name = name;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final OrganizationItem that = (OrganizationItem) o;
		return Objects.equals(id, that.id) && Objects.equals(unitId, that.unitId) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, unitId, name);
	}

	@Override
	public String toString() {
		return "OrganizationItem{" +
			"id=" + id +
			", unitId='" + unitId + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
