package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of investigations from the Lifecare family care system")
public class PagedInvestigationResponse {

	@Schema(description = "The investigations on this page")
	private List<Investigation> investigations;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedInvestigationResponse create() {
		return new PagedInvestigationResponse();
	}

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(final List<Investigation> investigations) {
		this.investigations = investigations;
	}

	public PagedInvestigationResponse withInvestigations(final List<Investigation> investigations) {
		this.investigations = investigations;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedInvestigationResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedInvestigationResponse that = (PagedInvestigationResponse) o;
		return Objects.equals(investigations, that.investigations) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(investigations, metaData);
	}

	@Override
	public String toString() {
		return "PagedInvestigationResponse{" +
			"investigations=" + investigations +
			", metaData=" + metaData +
			'}';
	}
}
