package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionPersonDTO;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.familycare.FamilyCareDecisionDetails;
import se.sundsvall.lifecareintegrator.api.model.familycare.RelatedPerson;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.api.model.common.DecisionConstants.SOURCE_FAMILY_CARE;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPartyId;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toStringValue;

public final class FamilyCareDecisionMapper {

	private FamilyCareDecisionMapper() {}

	public static Decision toDecision(final PersonBasedDecisionDTO decision, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(decision)
			.map(source -> Decision.create()
				.withSource(SOURCE_FAMILY_CARE)
				.withDecisionId(toStringValue(source.getId()))
				.withDecided(toLocalDate(source.getDate()))
				.withValidFrom(toLocalDate(source.getFromDate()))
				.withValidTo(toLocalDate(source.getToDate()))
				.withType(source.getType())
				.withReason(source.getReason())
				.withDecisionMaker(source.getDecisionMaker())
				.withAmount(toBigDecimal(source.getAmount()))
				.withFamilyCareDetails(FamilyCareDecisionDetails.create()
					.withInvestigationExecutionId(source.getInvestigationExecutionId())
					.withServiceId(source.getServiceId())
					.withOrganization(source.getOrganization())
					.withCoApplicant(source.getCoApplicant())
					.withReasonCoApplicant(source.getReasonCoApplicant())
					.withConnectedApplication(source.getConnectedApplication())
					.withPersons(toRelatedPersons(source.getDecisionPersonDTOs(), partyIdsByPersonNumber))))
			.orElse(null);
	}

	/** An unresolvable person keeps a null party id, so a consumer still sees that the decision concerned someone. */
	private static List<RelatedPerson> toRelatedPersons(final List<PersonBasedDecisionPersonDTO> persons, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(persons).orElse(emptyList()).stream()
			.map(person -> RelatedPerson.create()
				.withPartyId(toPartyId(person.getPersonId(), partyIdsByPersonNumber))
				.withName(person.getName())
				.withCoApplicant(person.getIsCoApplicant()))
			.toList();
	}

	public static List<String> collectPersonNumbers(final List<PersonBasedDecisionDTO> decisions) {
		return Optional.ofNullable(decisions)
			.orElseGet(List::of).stream()
			.map(PersonBasedDecisionDTO::getDecisionPersonDTOs)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.map(PersonBasedDecisionPersonDTO::getPersonId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}
}
