package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of payments from the Lifecare family care system")
public class PagedPaymentResponse {

	@ArraySchema(schema = @Schema(description = "The payments on this page"))
	private List<Payment> payments;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedPaymentResponse create() {
		return new PagedPaymentResponse();
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(final List<Payment> payments) {
		this.payments = payments;
	}

	public PagedPaymentResponse withPayments(final List<Payment> payments) {
		this.payments = payments;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedPaymentResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedPaymentResponse that = (PagedPaymentResponse) o;
		return Objects.equals(payments, that.payments) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(payments, metaData);
	}

	@Override
	public String toString() {
		return "PagedPaymentResponse{" +
			"payments=" + payments +
			", metaData=" + metaData +
			'}';
	}
}
