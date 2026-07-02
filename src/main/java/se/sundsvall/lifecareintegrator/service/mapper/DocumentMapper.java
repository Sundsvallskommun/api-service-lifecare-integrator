package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedDocumentDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDocumentDTO;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.DocumentMetadata;
import se.sundsvall.lifecareintegrator.api.model.PagedResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;

public final class DocumentMapper {

	private DocumentMapper() {}

	public static PagedResponse<DocumentMetadata> toDocuments(final ApiPaginationCompositePersonBasedDocumentDTO documents) {
		return Optional.ofNullable(documents)
			.map(source -> PagedResponse.<DocumentMetadata>create()
				.withPage(source.getPageNumber())
				.withPageSize(source.getPageSize())
				.withTotalPages(source.getTotalNumberOfPages())
				.withTotalRecords(source.getTotalNumberOfRecords())
				.withResults(toDocumentMetadataList(source.getResult())))
			.orElseGet(() -> PagedResponse.<DocumentMetadata>create().withResults(emptyList()));
	}

	private static List<DocumentMetadata> toDocumentMetadataList(final List<PersonBasedDocumentDTO> documents) {
		return Optional.ofNullable(documents)
			.map(list -> list.stream()
				.map(DocumentMapper::toDocumentMetadata)
				.toList())
			.orElse(emptyList());
	}

	private static DocumentMetadata toDocumentMetadata(final PersonBasedDocumentDTO document) {
		// Intentionally drops the ownerId — it may hold a personnummer, which never leaves this service
		return Optional.ofNullable(document)
			.map(source -> DocumentMetadata.create()
				.withId(source.getId())
				.withTitle(source.getTitle())
				.withDate(toLocalDate(source.getDate()))
				.withDocumentType(source.getDocumentType())
				.withOwnerType(source.getOwnerType()))
			.orElse(null);
	}
}
