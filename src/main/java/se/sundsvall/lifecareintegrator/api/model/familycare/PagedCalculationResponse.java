package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of calculations from the Lifecare family care system")
public class PagedCalculationResponse {

	@Schema(description = "The calculations on this page")
	private List<Calculation> calculations;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedCalculationResponse create() {
		return new PagedCalculationResponse();
	}

	public List<Calculation> getCalculations() {
		return calculations;
	}

	public void setCalculations(final List<Calculation> calculations) {
		this.calculations = calculations;
	}

	public PagedCalculationResponse withCalculations(final List<Calculation> calculations) {
		this.calculations = calculations;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedCalculationResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedCalculationResponse that = (PagedCalculationResponse) o;
		return Objects.equals(calculations, that.calculations) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(calculations, metaData);
	}

	@Override
	public String toString() {
		return "PagedCalculationResponse{" +
			"calculations=" + calculations +
			", metaData=" + metaData +
			'}';
	}
}
