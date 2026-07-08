package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedDocumentDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDocumentDTO;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

	@Test
	void toDocumentsWithNull() {
		// Act
		final var result = DocumentMapper.toDocuments(null);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getDocuments()).isEmpty();
		assertThat(result.getMetaData()).isNull();
	}

	@Test
	void toDocuments() {
		// Arrange: ownerId is dropped — the model has no such field
		final var composite = new ApiPaginationCompositePersonBasedDocumentDTO()
			.pageNumber(1)
			.pageSize(10)
			.totalNumberOfPages(2)
			.totalNumberOfRecords(15)
			.result(List.of(new PersonBasedDocumentDTO()
				.id("doc-1")
				.title("Ansökan om försörjningsstöd")
				.date("2026-05-01T00:00:00")
				.documentType("Ansökan")
				.ownerId("199001011234")
				.ownerType("Person")));

		// Act
		final var result = DocumentMapper.toDocuments(composite);

		// Assert
		assertThat(result.getMetaData().getPage()).isEqualTo(1);
		assertThat(result.getMetaData().getLimit()).isEqualTo(10);
		assertThat(result.getMetaData().getCount()).isEqualTo(1);
		assertThat(result.getMetaData().getTotalPages()).isEqualTo(2);
		assertThat(result.getMetaData().getTotalRecords()).isEqualTo(15L);

		final var document = result.getDocuments().getFirst();
		assertThat(document.getId()).isEqualTo("doc-1");
		assertThat(document.getTitle()).isEqualTo("Ansökan om försörjningsstöd");
		assertThat(document.getDate()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(document.getDocumentType()).isEqualTo("Ansökan");
		assertThat(document.getOwnerType()).isEqualTo("Person");
	}

	@Test
	void toDocumentsWithUnparseableDate() {
		// Arrange
		final var composite = new ApiPaginationCompositePersonBasedDocumentDTO()
			.result(List.of(new PersonBasedDocumentDTO().id("doc-1").date("garbage-date")));

		// Act
		final var result = DocumentMapper.toDocuments(composite);

		// Assert
		assertThat(result.getDocuments().getFirst().getDate()).isNull();
	}
}
