package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedDocumentDTO;
import generated.se.sundsvall.familycare.PersonBasedDocumentDTO;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.DocumentMetadata;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedDocumentResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;

public final class DocumentMapper {

	private DocumentMapper() {}

	public static PagedDocumentResponse toDocuments(final ApiPaginationCompositePersonBasedDocumentDTO documents) {
		if (documents == null) {
			return PagedDocumentResponse.create().withDocuments(emptyList());
		}

		final var documentList = Optional.ofNullable(documents.getResult()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.map(DocumentMapper::toDocumentMetadata)
			.toList();
		return PagedDocumentResponse.create()
			.withDocuments(documentList)
			.withMetaData(toPagingMetaData(documents.getPageNumber(), documents.getPageSize(), documents.getTotalNumberOfPages(), documents.getTotalNumberOfRecords(), documentList.size()));
	}

	private static DocumentMetadata toDocumentMetadata(final PersonBasedDocumentDTO source) {
		// Intentionally drops the ownerId — it may hold a personnummer, which never leaves this service
		return DocumentMetadata.create()
			.withId(source.getId())
			.withTitle(source.getTitle())
			.withDate(toLocalDate(source.getDate()))
			.withDocumentType(source.getDocumentType())
			.withOwnerType(source.getOwnerType());
	}
}
