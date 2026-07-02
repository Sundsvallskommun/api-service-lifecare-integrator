package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "Metadata for a document stored in the Lifecare family care system. The document content is fetched separately")
public class DocumentMetadata {

	@Schema(description = "The document id in the source system", examples = "12345")
	private String id;

	@Schema(description = "The document title", examples = "Utredning ekonomiskt bistånd")
	private String title;

	@Schema(description = "The document date", examples = "2026-05-01")
	@DateTimeFormat(iso = DATE)
	private LocalDate date;

	@Schema(description = "The document type", examples = "Utredning")
	private String documentType;

	@Schema(description = "The type of the document owner", examples = "Person")
	private String ownerType;

	public static DocumentMetadata create() {
		return new DocumentMetadata();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public DocumentMetadata withId(final String id) {
		this.id = id;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public DocumentMetadata withTitle(final String title) {
		this.title = title;
		return this;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(final LocalDate date) {
		this.date = date;
	}

	public DocumentMetadata withDate(final LocalDate date) {
		this.date = date;
		return this;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(final String documentType) {
		this.documentType = documentType;
	}

	public DocumentMetadata withDocumentType(final String documentType) {
		this.documentType = documentType;
		return this;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(final String ownerType) {
		this.ownerType = ownerType;
	}

	public DocumentMetadata withOwnerType(final String ownerType) {
		this.ownerType = ownerType;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final DocumentMetadata that = (DocumentMetadata) o;
		return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(date, that.date)
			&& Objects.equals(documentType, that.documentType) && Objects.equals(ownerType, that.ownerType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, date, documentType, ownerType);
	}

	@Override
	public String toString() {
		return "DocumentMetadata{" +
			"id='" + id + '\'' +
			", title='" + title + '\'' +
			", date=" + date +
			", documentType='" + documentType + '\'' +
			", ownerType='" + ownerType + '\'' +
			'}';
	}
}
