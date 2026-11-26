package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.PersonBasedDecisionDTO;
import generated.se.sundsvall.familycare.PersonBasedDecisionPersonDTO;
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
		return Decision.create()
			.withSource(SOURCE_FAMILY_CARE)
			.withDecisionId(toStringValue(decision.getId()))
			.withDecided(toLocalDate(decision.getDate()))
			.withValidFrom(toLocalDate(decision.getFromDate()))
			.withValidTo(toLocalDate(decision.getToDate()))
			.withType(decision.getType())
			.withReason(decision.getReason())
			.withDecisionMaker(decision.getDecisionMaker())
			.withAmount(toBigDecimal(decision.getAmount()))
			.withFamilyCareDetails(FamilyCareDecisionDetails.create()
				.withInvestigationExecutionId(decision.getInvestigationExecutionId())
				.withServiceId(decision.getServiceId())
				.withOrganization(decision.getOrganization())
				.withCoApplicant(decision.getCoApplicant())
				.withReasonCoApplicant(decision.getReasonCoApplicant())
				.withConnectedApplication(decision.getConnectedApplication())
				.withPersons(toRelatedPersons(decision.getDecisionPersonDTOs(), partyIdsByPersonNumber)));
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
		return decisions.stream()
			.map(PersonBasedDecisionDTO::getDecisionPersonDTOs)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.map(PersonBasedDecisionPersonDTO::getPersonId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}
}
