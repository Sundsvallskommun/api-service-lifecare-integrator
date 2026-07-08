package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of services from the Lifecare family care system")
public class PagedServiceResponse {

	@ArraySchema(schema = @Schema(description = "The services on this page"))
	private List<CaseService> services;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedServiceResponse create() {
		return new PagedServiceResponse();
	}

	public List<CaseService> getServices() {
		return services;
	}

	public void setServices(final List<CaseService> services) {
		this.services = services;
	}

	public PagedServiceResponse withServices(final List<CaseService> services) {
		this.services = services;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedServiceResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedServiceResponse that = (PagedServiceResponse) o;
		return Objects.equals(services, that.services) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(services, metaData);
	}

	@Override
	public String toString() {
		return "PagedServiceResponse{" +
			"services=" + services +
			", metaData=" + metaData +
			'}';
	}
}
