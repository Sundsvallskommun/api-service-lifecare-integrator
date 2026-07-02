package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "A resource allocation from the Lifecare family care system")
public class ResourceAllocation {

	@Schema(description = "The resource allocation id in the source system", examples = "12345")
	private Integer id;

	@Schema(description = "The date the resource allocation starts", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate startDate;

	@Schema(description = "The date the resource allocation ends", examples = "2026-10-31")
	@DateTimeFormat(iso = DATE)
	private LocalDate endDate;

	@Schema(description = "The allocation percentage", examples = "100")
	private Integer percent;

	@Schema(description = "The allocated resource", examples = "Anna Andersson")
	private String resource;

	@Schema(description = "The type of the allocated resource", examples = "Handläggare")
	private String resourceType;

	@Schema(description = "The id of the service the resource allocation belongs to", examples = "23456")
	private Integer serviceId;

	public static ResourceAllocation create() {
		return new ResourceAllocation();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public ResourceAllocation withId(final Integer id) {
		this.id = id;
		return this;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
	}

	public ResourceAllocation withStartDate(final LocalDate startDate) {
		this.startDate = startDate;
		return this;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(final LocalDate endDate) {
		this.endDate = endDate;
	}

	public ResourceAllocation withEndDate(final LocalDate endDate) {
		this.endDate = endDate;
		return this;
	}

	public Integer getPercent() {
		return percent;
	}

	public void setPercent(final Integer percent) {
		this.percent = percent;
	}

	public ResourceAllocation withPercent(final Integer percent) {
		this.percent = percent;
		return this;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(final String resource) {
		this.resource = resource;
	}

	public ResourceAllocation withResource(final String resource) {
		this.resource = resource;
		return this;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(final String resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceAllocation withResourceType(final String resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
	}

	public ResourceAllocation withServiceId(final Integer serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ResourceAllocation that = (ResourceAllocation) o;
		return Objects.equals(id, that.id) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(percent, that.percent)
			&& Objects.equals(resource, that.resource) && Objects.equals(resourceType, that.resourceType) && Objects.equals(serviceId, that.serviceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, startDate, endDate, percent, resource, resourceType, serviceId);
	}

	@Override
	public String toString() {
		return "ResourceAllocation{" +
			"id=" + id +
			", startDate=" + startDate +
			", endDate=" + endDate +
			", percent=" + percent +
			", resource='" + resource + '\'' +
			", resourceType='" + resourceType + '\'' +
			", serviceId=" + serviceId +
			'}';
	}
}
