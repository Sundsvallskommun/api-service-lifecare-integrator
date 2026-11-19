package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringDTO;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.Actualisation;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedActualisationResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;

public final class ActualisationMapper {

	private ActualisationMapper() {}

	public static PagedActualisationResponse toActualisations(final ApiPaginationCompositePersonBasedAktualiseringDTO composite) {
		if (composite == null) {
			return PagedActualisationResponse.create().withActualisations(emptyList());
		}

		final var actualisations = Optional.ofNullable(composite.getResult()).orElse(emptyList()).stream()
			.map(ActualisationMapper::toActualisation)
			.toList();
		return PagedActualisationResponse.create()
			.withActualisations(actualisations)
			.withMetaData(toPagingMetaData(composite.getPageNumber(), composite.getPageSize(), composite.getTotalNumberOfPages(), composite.getTotalNumberOfRecords(), actualisations.size()));
	}

	public static Actualisation toActualisation(final PersonBasedAktualiseringDTO actualisation) {
		// Intentionally drops the personId — personnummer never leaves this service
		return Optional.ofNullable(actualisation)
			.map(source -> Actualisation.create()
				.withId(source.getId())
				.withType(source.getType())
				.withName(source.getName())
				.withDate(toLocalDate(source.getDate()))
				.withReason(source.getReason())
				.withRegards(source.getRegards())
				.withFromWho(source.getFromWho())
				.withCaseworker(source.getCaseworker())
				.withOrganization(source.getOrganization())
				.withStatus(source.getStatus())
				.withInvestigationId(source.getInvestigationId())
				.withServiceId(source.getServiceId())
				.withDecisionId(source.getDecisionId()))
			.orElse(null);
	}
}
