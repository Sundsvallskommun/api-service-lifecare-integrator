package se.sundsvall.lifecareintegrator.api.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A lookup value from a code list")
public class Lookup {

	@Schema(description = "The id of the lookup value", examples = "1")
	private Integer id;

	@Schema(description = "The display name of the lookup value", examples = "Ansökan")
	private String name;

	public static Lookup create() {
		return new Lookup();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Lookup withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Lookup withName(final String name) {
		this.name = name;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Lookup that = (Lookup) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "Lookup{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}
}
