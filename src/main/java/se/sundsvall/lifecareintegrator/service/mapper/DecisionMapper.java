package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1CodeText;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionPersonDTO;
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
				.withElderlyCareDetails(toElderlyCareDecisionDetails(source)))
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
				.withElderlyCareDetails(toElderlyCareDecisionDetails(toBaseDecision(source))
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
	 * Builds the elderly-care details shared by SoL and LSS decisions. An LSS decision is a base decision plus extra
	 * fields; it is adapted to the base type via {@link #toBaseDecision} so this single mapper serves both.
	 */
	private static ElderlyCareDecisionDetails toElderlyCareDecisionDetails(final WEECIntegrationContractsDecisionV1Decision source) {
		return ElderlyCareDecisionDetails.create()
			.withInvestigationId(source.getInvestigationId())
			.withCode(toText(source.getCode()))
			.withServiceCategory(toText(source.getServiceCategory()))
			.withHours(source.getHour())
			.withHourType(toText(source.getHourType()))
			.withAmountType(toText(source.getAmountType()))
			.withQuantity(source.getQuantity())
			.withQuantityType(toText(source.getQuantityType()))
			.withVisits(source.getVisit())
			.withVisitType(toText(source.getVisitType()))
			.withDays(source.getDay())
			.withDayType(toText(source.getDayType()))
			.withDecisionLevel(toText(source.getDecisionLevel()))
			.withExecutionStartDate(toLocalDate(source.getExecutionStartDate()))
			.withExecutionEndDate(toLocalDate(source.getExecutionEndDate()))
			.withIterationNumber(source.getIterationNumber())
			.withDaysOfDecision(source.getDaysOfDecision())
			.withOrderIds(source.getOrderIds())
			.withDeleted(source.getDeleted());
	}

	/**
	 * Adapts an LSS decision to the base decision type by copying the fields the two share, so the base
	 * {@link #toElderlyCareDecisionDetails} mapper can be reused. The LSS-only fields are handled by the caller.
	 */
	private static WEECIntegrationContractsDecisionV1Decision toBaseDecision(final WEECIntegrationContractsDecisionV1LssDecision lss) {
		return new WEECIntegrationContractsDecisionV1Decision()
			.investigationId(lss.getInvestigationId())
			.code(lss.getCode())
			.serviceCategory(lss.getServiceCategory())
			.hour(lss.getHour())
			.hourType(lss.getHourType())
			.amountType(lss.getAmountType())
			.quantity(lss.getQuantity())
			.quantityType(lss.getQuantityType())
			.visit(lss.getVisit())
			.visitType(lss.getVisitType())
			.day(lss.getDay())
			.dayType(lss.getDayType())
			.decisionLevel(lss.getDecisionLevel())
			.executionStartDate(lss.getExecutionStartDate())
			.executionEndDate(lss.getExecutionEndDate())
			.iterationNumber(lss.getIterationNumber())
			.daysOfDecision(lss.getDaysOfDecision())
			.orderIds(lss.getOrderIds())
			.deleted(lss.getDeleted());
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
