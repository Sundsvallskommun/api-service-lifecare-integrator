package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of executions from the Lifecare family care system")
public class PagedExecutionResponse {

	@ArraySchema(schema = @Schema(description = "The executions on this page"))
	private List<Execution> executions;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedExecutionResponse create() {
		return new PagedExecutionResponse();
	}

	public List<Execution> getExecutions() {
		return executions;
	}

	public void setExecutions(final List<Execution> executions) {
		this.executions = executions;
	}

	public PagedExecutionResponse withExecutions(final List<Execution> executions) {
		this.executions = executions;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedExecutionResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedExecutionResponse that = (PagedExecutionResponse) o;
		return Objects.equals(executions, that.executions) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executions, metaData);
	}

	@Override
	public String toString() {
		return "PagedExecutionResponse{" +
			"executions=" + executions +
			", metaData=" + metaData +
			'}';
	}
}
