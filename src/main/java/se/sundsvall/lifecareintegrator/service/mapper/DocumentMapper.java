package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedDocumentDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDocumentDTO;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.DocumentMetadata;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedDocumentResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;

public final class DocumentMapper {

	private DocumentMapper() {}

	public static PagedDocumentResponse toDocuments(final ApiPaginationCompositePersonBasedDocumentDTO documents) {
		return Optional.ofNullable(documents)
			.map(source -> {
				final var documentList = toDocumentMetadataList(source.getResult());
				return PagedDocumentResponse.create()
					.withDocuments(documentList)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), documentList.size()));
			})
			.orElseGet(() -> PagedDocumentResponse.create().withDocuments(emptyList()));
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
