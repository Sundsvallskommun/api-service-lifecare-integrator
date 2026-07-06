package se.sundsvall.lifecareintegrator.api.model.familycare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "A paged list of documents from the Lifecare family care system")
public class PagedDocumentResponse {

	@Schema(description = "The documents on this page")
	private List<DocumentMetadata> documents;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY)
	private PagingMetaData metaData;

	public static PagedDocumentResponse create() {
		return new PagedDocumentResponse();
	}

	public List<DocumentMetadata> getDocuments() {
		return documents;
	}

	public void setDocuments(final List<DocumentMetadata> documents) {
		this.documents = documents;
	}

	public PagedDocumentResponse withDocuments(final List<DocumentMetadata> documents) {
		this.documents = documents;
		return this;
	}

	public PagingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
	}

	public PagedDocumentResponse withMetaData(final PagingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PagedDocumentResponse that = (PagedDocumentResponse) o;
		return Objects.equals(documents, that.documents) && Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(documents, metaData);
	}

	@Override
	public String toString() {
		return "PagedDocumentResponse{" +
			"documents=" + documents +
			", metaData=" + metaData +
			'}';
	}
}
