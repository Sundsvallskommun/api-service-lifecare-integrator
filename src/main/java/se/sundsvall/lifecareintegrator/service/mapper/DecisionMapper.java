package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1CodeText;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionPersonDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.elderlycare.ElderlyCareDecisionDetails;
import se.sundsvall.lifecareintegrator.api.model.familycare.FamilyCareDecisionDetails;
import se.sundsvall.lifecareintegrator.api.model.familycare.RelatedPerson;

import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toStringValue;

public final class DecisionMapper {

	public static final String SOURCE_ELDERLY_CARE = "ELDERLY_CARE";
	public static final String SOURCE_FAMILY_CARE = "FAMILY_CARE";
	public static final String LAW_SOL = "SOL";
	public static final String LAW_LSS = "LSS";

	private DecisionMapper() {}

	public static Decision toDecision(final WEECIntegrationContractsDecisionV1Decision decision) {
		return Optional.ofNullable(decision)
			.map(source -> Decision.create()
				.withSource(SOURCE_ELDERLY_CARE)
				.withLaw(LAW_SOL)
				.withDecisionId(toStringValue(source.getId()))
				.withDecided(toLocalDate(source.getDate()))
				.withValidFrom(toLocalDate(source.getFromDate()))
				.withValidTo(toLocalDate(source.getToDate()))
				.withType(toText(source.getType()))
				.withReason(toText(source.getReason()))
				.withDecisionMaker(toFullName(source.getCaseworker()))
				.withAmount(toBigDecimal(source.getAmount()))
				.withElderlyCareDetails(toElderlyCareDecisionDetails(
					source.getInvestigationId(), source.getCode(), source.getServiceCategory(), source.getHour(), source.getHourType(), source.getAmountType(),
					source.getQuantity(), source.getQuantityType(), source.getVisit(), source.getVisitType(), source.getDay(), source.getDayType(),
					source.getDecisionLevel(), source.getExecutionStartDate(), source.getExecutionEndDate(), source.getIterationNumber(), source.getDaysOfDecision(),
					source.getOrderIds(), source.getDeleted())))
			.orElse(null);
	}

	public static Decision toDecision(final WEECIntegrationContractsDecisionV1LssDecision decision) {
		return Optional.ofNullable(decision)
			.map(source -> Decision.create()
				.withSource(SOURCE_ELDERLY_CARE)
				.withLaw(LAW_LSS)
				.withDecisionId(toStringValue(source.getId()))
				.withDecided(toLocalDate(source.getDate()))
				.withValidFrom(toLocalDate(source.getFromDate()))
				.withValidTo(toLocalDate(source.getToDate()))
				.withType(toText(source.getType()))
				.withReason(toText(source.getReason()))
				.withDecisionMaker(toFullName(source.getCaseworker()))
				.withAmount(toBigDecimal(source.getAmount()))
				.withElderlyCareDetails(toElderlyCareDecisionDetails(
					source.getInvestigationId(), source.getCode(), source.getServiceCategory(), source.getHour(), source.getHourType(), source.getAmountType(),
					source.getQuantity(), source.getQuantityType(), source.getVisit(), source.getVisitType(), source.getDay(), source.getDayType(),
					source.getDecisionLevel(), source.getExecutionStartDate(), source.getExecutionEndDate(), source.getIterationNumber(), source.getDaysOfDecision(),
					source.getOrderIds(), source.getDeleted())
					// LSS-only fields on top of the shared elderly-care details
					.withPersonCategory1(source.getPersonCategory1())
					.withPersonCategory2(source.getPersonCategory2())
					.withPersonCategory3(source.getPersonCategory3())
					.withPersonCategory3P(source.getPersonCategory3P())
					.withIncreasedHourlyAmount(toBigDecimal(source.getIncreasedHourlyAmount()))
					.withStandardAmount(toBigDecimal(source.getStandardAmount()))
					.withSfbCaseworker(toFullName(source.getSfbCaseworker()))))
			.orElse(null);
	}

	public static Decision toDecision(final PersonBasedDecisionDTO decision) {
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
					.withPersons(toRelatedPersons(source.getDecisionPersonDTOs()))))
			.orElse(null);
	}

	/**
	 * Builds the elderly-care details shared by SoL and LSS decisions (the two vendor types have no common supertype, so
	 * the shared field wiring lives here). LSS decisions add their extra fields onto the returned object.
	 */
	private static ElderlyCareDecisionDetails toElderlyCareDecisionDetails(final Integer investigationId, final WEECIntegrationContractsCommonV1CodeText code,
		final WEECIntegrationContractsCommonV1CodeText serviceCategory, final Double hours, final WEECIntegrationContractsCommonV1CodeText hourType,
		final WEECIntegrationContractsCommonV1CodeText amountType, final Double quantity, final WEECIntegrationContractsCommonV1CodeText quantityType,
		final Double visits, final WEECIntegrationContractsCommonV1CodeText visitType, final Double days, final WEECIntegrationContractsCommonV1CodeText dayType,
		final WEECIntegrationContractsCommonV1CodeText decisionLevel, final OffsetDateTime executionStartDate, final OffsetDateTime executionEndDate,
		final Integer iterationNumber, final Double daysOfDecision, final List<Integer> orderIds, final Boolean deleted) {
		return ElderlyCareDecisionDetails.create()
			.withInvestigationId(investigationId)
			.withCode(toText(code))
			.withServiceCategory(toText(serviceCategory))
			.withHours(hours)
			.withHourType(toText(hourType))
			.withAmountType(toText(amountType))
			.withQuantity(quantity)
			.withQuantityType(toText(quantityType))
			.withVisits(visits)
			.withVisitType(toText(visitType))
			.withDays(days)
			.withDayType(toText(dayType))
			.withDecisionLevel(toText(decisionLevel))
			.withExecutionStartDate(toLocalDate(executionStartDate))
			.withExecutionEndDate(toLocalDate(executionEndDate))
			.withIterationNumber(iterationNumber)
			.withDaysOfDecision(daysOfDecision)
			.withOrderIds(orderIds)
			.withDeleted(deleted);
	}

	private static List<RelatedPerson> toRelatedPersons(final List<PersonBasedDecisionPersonDTO> persons) {
		// Intentionally drops the personId — personnummer never leaves this service
		return Optional.ofNullable(persons)
			.map(list -> list.stream()
				.map(person -> RelatedPerson.create()
					.withName(person.getName())
					.withCoApplicant(person.getIsCoApplicant()))
				.toList())
			.orElse(null);
	}

	private static String toText(final WEECIntegrationContractsCommonV1CodeText codeText) {
		return Optional.ofNullable(codeText)
			.map(WEECIntegrationContractsCommonV1CodeText::getText)
			.orElse(null);
	}

	private static String toFullName(final WEECIntegrationContractsCommonV1Caseworker caseworker) {
		return Optional.ofNullable(caseworker)
			.map(WEECIntegrationContractsCommonV1Caseworker::getFullName)
			.orElse(null);
	}
}
