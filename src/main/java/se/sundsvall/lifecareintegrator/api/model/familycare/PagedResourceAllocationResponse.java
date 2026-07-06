package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of resource allocations from the Lifecare family care system")
public class PagedResourceAllocationResponse {

	@Schema(description = "The resource allocations on this page")
	private List<ResourceAllocation> resourceAllocations;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedResourceAllocationResponse create() {
		return new PagedResourceAllocationResponse();
	}

	public List<ResourceAllocation> getResourceAllocations() {
		return resourceAllocations;
	}

	public void setResourceAllocations(final List<ResourceAllocation> resourceAllocations) {
		this.resourceAllocations = resourceAllocations;
	}

	public PagedResourceAllocationResponse withResourceAllocations(final List<ResourceAllocation> resourceAllocations) {
		this.resourceAllocations = resourceAllocations;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedResourceAllocationResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedResourceAllocationResponse that = (PagedResourceAllocationResponse) o;
		return Objects.equals(resourceAllocations, that.resourceAllocations) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceAllocations, metaData);
	}

	@Override
	public String toString() {
		return "PagedResourceAllocationResponse{" +
			"resourceAllocations=" + resourceAllocations +
			", metaData=" + metaData +
			'}';
	}
}
