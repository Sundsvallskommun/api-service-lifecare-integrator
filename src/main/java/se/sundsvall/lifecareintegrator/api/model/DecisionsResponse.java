package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "The merged decisions from all Lifecare sources, with per-source fetch status")
public class DecisionsResponse {

	@Schema(description = "The merged decisions, sorted by decision date (most recent first)")
	private List<Decision> decisions;

	@Schema(description = "The fetch status per decision source")
	private List<SourceStatus> sources;

	public static DecisionsResponse create() {
		return new DecisionsResponse();
	}

	public List<Decision> getDecisions() {
		return decisions;
	}

	public void setDecisions(final List<Decision> decisions) {
		this.decisions = decisions;
	}

	public DecisionsResponse withDecisions(final List<Decision> decisions) {
		this.decisions = decisions;
		return this;
	}

	public List<SourceStatus> getSources() {
		return sources;
	}

	public void setSources(final List<SourceStatus> sources) {
		this.sources = sources;
	}

	public DecisionsResponse withSources(final List<SourceStatus> sources) {
		this.sources = sources;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final DecisionsResponse that = (DecisionsResponse) o;
		return Objects.equals(decisions, that.decisions) && Objects.equals(sources, that.sources);
	}

	@Override
	public int hashCode() {
		return Objects.hash(decisions, sources);
	}

	@Override
	public String toString() {
		return "DecisionsResponse{" +
			"decisions=" + decisions +
			", sources=" + sources +
			'}';
	}
}
