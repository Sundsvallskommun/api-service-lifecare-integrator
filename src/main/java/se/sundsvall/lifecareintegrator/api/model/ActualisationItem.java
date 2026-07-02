package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "An existing actualisation that a calculation can be linked to")
public class ActualisationItem {

	@Schema(description = "The id of the actualisation", examples = "12345")
	private Integer id;

	@Schema(description = "The type of the actualisation", examples = "Ansökan")
	private String type;

	@Schema(description = "The date of the actualisation", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate date;

	public static ActualisationItem create() {
		return new ActualisationItem();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public ActualisationItem withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public ActualisationItem withType(final String type) {
		this.type = type;
		return this;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(final LocalDate date) {
		this.date = date;
	}

	public ActualisationItem withDate(final LocalDate date) {
		this.date = date;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ActualisationItem that = (ActualisationItem) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(date, that.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, date);
	}

	@Override
	public String toString() {
		return "ActualisationItem{" +
			"id=" + id +
			", type='" + type + '\'' +
			", date=" + date +
			'}';
	}
}
