package se.sundsvall.lifecareintegrator.api.model.familycare;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An existing actualisation that a calculation can be linked to")
public class ActualisationReference {

	@Schema(description = "The id of the actualisation", examples = "12345")
	private Integer id;

	@Schema(description = "The type of the actualisation", examples = "Ansökan")
	private String type;

	@Schema(description = "The date of the actualisation", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate date;

	public static ActualisationReference create() {
		return new ActualisationReference();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public ActualisationReference withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public ActualisationReference withType(final String type) {
		this.type = type;
		return this;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(final LocalDate date) {
		this.date = date;
	}

	public ActualisationReference withDate(final LocalDate date) {
		this.date = date;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ActualisationReference that = (ActualisationReference) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(date, that.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, date);
	}

	@Override
	public String toString() {
		return "ActualisationReference{" +
			"id=" + id +
			", type='" + type + '\'' +
			", date=" + date +
			'}';
	}
}
