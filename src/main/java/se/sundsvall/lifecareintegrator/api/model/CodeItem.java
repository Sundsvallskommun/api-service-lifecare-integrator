package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "A generic code-list entry")
public class CodeItem {

	@Schema(description = "The id of the code-list entry", examples = "1")
	private Integer id;

	@Schema(description = "The name of the code-list entry", examples = "Ansökan")
	private String name;

	public static CodeItem create() {
		return new CodeItem();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public CodeItem withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public CodeItem withName(final String name) {
		this.name = name;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CodeItem that = (CodeItem) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "CodeItem{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}
}
