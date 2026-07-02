package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A norm that a calculation can be based on")
public class Norm {

	@Schema(description = "The id of the norm", examples = "1")
	private Integer id;

	@Schema(description = "The name of the norm", examples = "Riksnorm 2026")
	private String name;

	@Schema(description = "The date the norm is valid from", examples = "2026-01-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate validFrom;

	@Schema(description = "The date the norm is valid to", examples = "2026-12-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate validTo;

	public static Norm create() {
		return new Norm();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Norm withId(final Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Norm withName(final String name) {
		this.name = name;
		return this;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(final LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public Norm withValidFrom(final LocalDate validFrom) {
		this.validFrom = validFrom;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(final LocalDate validTo) {
		this.validTo = validTo;
	}

	public Norm withValidTo(final LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Norm that = (Norm) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(validFrom, that.validFrom) && Objects.equals(validTo, that.validTo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, validFrom, validTo);
	}

	@Override
	public String toString() {
		return "Norm{" +
			"id=" + id +
			", name='" + name + '\'' +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			'}';
	}
}
