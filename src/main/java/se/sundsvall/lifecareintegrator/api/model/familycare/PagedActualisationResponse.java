package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of actualisations from the Lifecare family care system")
public class PagedActualisationResponse {

	@Schema(description = "The actualisations on this page")
	private List<Actualisation> actualisations;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedActualisationResponse create() {
		return new PagedActualisationResponse();
	}

	public List<Actualisation> getActualisations() {
		return actualisations;
	}

	public void setActualisations(final List<Actualisation> actualisations) {
		this.actualisations = actualisations;
	}

	public PagedActualisationResponse withActualisations(final List<Actualisation> actualisations) {
		this.actualisations = actualisations;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedActualisationResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedActualisationResponse that = (PagedActualisationResponse) o;
		return Objects.equals(actualisations, that.actualisations) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(actualisations, metaData);
	}

	@Override
	public String toString() {
		return "PagedActualisationResponse{" +
			"actualisations=" + actualisations +
			", metaData=" + metaData +
			'}';
	}
}
