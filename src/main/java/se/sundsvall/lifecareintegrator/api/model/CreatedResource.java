package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "The id of the created resource in Lifecare FC")
public class CreatedResource {

	@Schema(description = "The id of the created resource in Lifecare FC", examples = "12345")
	private Integer id;

	public static CreatedResource create() {
		return new CreatedResource();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public CreatedResource withId(final Integer id) {
		this.id = id;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CreatedResource that = (CreatedResource) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CreatedResource{" +
			"id=" + id +
			'}';
	}
}
