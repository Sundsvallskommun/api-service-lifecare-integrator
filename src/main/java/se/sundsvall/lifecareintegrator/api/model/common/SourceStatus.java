package se.sundsvall.lifecareintegrator.api.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = """
	The fetch status for one decision source. When a source is UNAVAILABLE its decisions are missing from the \
	response — consumers must not treat a degraded response as the complete decision set.""")
public class SourceStatus {

	public static final String STATUS_OK = "OK";
	public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

	@Schema(description = "The source system", examples = "ELDERLY_CARE", allowableValues = {
		"ELDERLY_CARE", "FAMILY_CARE"
	})
	private String source;

	@Schema(description = """
		The law the source covers, identifying which fetch this status belongs to. Only present for ELDERLY_CARE \
		sources. The LSS source also delivers SFB decisions.""", examples = "SOL", allowableValues = {
		"SOL", "LSS"
	}, nullable = true)
	private String law;

	@Schema(description = "The fetch status of the source", examples = "OK", allowableValues = {
		"OK", "UNAVAILABLE"
	})
	private String status;

	public static SourceStatus create() {
		return new SourceStatus();
	}

	public String getSource() {
		return source;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	public SourceStatus withSource(final String source) {
		this.source = source;
		return this;
	}

	public String getLaw() {
		return law;
	}

	public void setLaw(final String law) {
		this.law = law;
	}

	public SourceStatus withLaw(final String law) {
		this.law = law;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public SourceStatus withStatus(final String status) {
		this.status = status;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SourceStatus that = (SourceStatus) o;
		return Objects.equals(source, that.source) && Objects.equals(law, that.law) && Objects.equals(status, that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, law, status);
	}

	@Override
	public String toString() {
		return "SourceStatus{" +
			"source='" + source + '\'' +
			", law='" + law + '\'' +
			", status='" + status + '\'' +
			'}';
	}
}
