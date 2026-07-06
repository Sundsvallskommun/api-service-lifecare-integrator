package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringDTO;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.familycare.Actualisation;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedActualisationResponse;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPagingMetaData;

public final class ActualisationMapper {

	private ActualisationMapper() {}

	public static PagedActualisationResponse toActualisations(final ApiPaginationCompositePersonBasedAktualiseringDTO composite) {
		return Optional.ofNullable(composite)
			.map(source -> {
				final var actualisations = toActualisationList(source.getResult());
				return PagedActualisationResponse.create()
					.withActualisations(actualisations)
					.withMetaData(toPagingMetaData(source.getPageNumber(), source.getPageSize(), source.getTotalNumberOfPages(), source.getTotalNumberOfRecords(), actualisations.size()));
			})
			.orElseGet(() -> PagedActualisationResponse.create().withActualisations(emptyList()));
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

	private static List<Actualisation> toActualisationList(final List<PersonBasedAktualiseringDTO> actualisations) {
		return Optional.ofNullable(actualisations)
			.map(list -> list.stream()
				.map(ActualisationMapper::toActualisation)
				.toList())
			.orElse(emptyList());
	}
}
