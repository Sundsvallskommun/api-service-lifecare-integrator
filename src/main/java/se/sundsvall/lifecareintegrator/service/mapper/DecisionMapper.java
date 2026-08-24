package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1CodeText;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionPersonDTO;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.elderlycare.ElderlyCareDecisionDetails;
import se.sundsvall.lifecareintegrator.api.model.familycare.FamilyCareDecisionDetails;
import se.sundsvall.lifecareintegrator.api.model.familycare.RelatedPerson;

import static java.util.function.Predicate.not;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toStringValue;

public final class DecisionMapper {

	public static final String SOURCE_ELDERLY_CARE = "ELDERLY_CARE";
	public static final String SOURCE_FAMILY_CARE = "FAMILY_CARE";
	public static final String LAW_SOL = "SOL";
	public static final String LAW_LSS = "LSS";
	public static final String LAW_SFB = "SFB";

	/** The {@code Law} code EC uses for Socialförsäkringsbalken; everything else on this endpoint is LSS. */
	private static final int LAW_CODE_SFB = 7;

	private DecisionMapper() {}

	/**
	 * @param caseworkerNames resolves a Lifecare caseworker id to a display name, for the cases where EC sends a
	 *                        caseworker without one. Returns null when the id cannot be resolved.
	 */
	public static Decision toDecision(final WEECIntegrationContractsDecisionV1Decision decision, final Function<String, Optional<String>> caseworkerNames) {
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
				.withDecisionMaker(toFullName(source.getCaseworker(), caseworkerNames))
				.withAmount(toBigDecimal(source.getAmount()))
				.withElderlyCareDetails(toElderlyCareDecisionDetails(source)))
			.orElse(null);
	}

	/**
	 * @param caseworkerNames resolves a Lifecare caseworker id to a display name — see the SoL overload. The SFB
	 *                        caseworker is the usual reason it is needed.
	 */
	public static Decision toDecision(final WEECIntegrationContractsDecisionV1LssDecision decision, final Function<String, Optional<String>> caseworkerNames) {
		return Optional.ofNullable(decision)
			.map(source -> Decision.create()
				.withSource(SOURCE_ELDERLY_CARE)
				.withLaw(toLaw(source.getLaw()))
				.withDecisionId(toStringValue(source.getId()))
				.withDecided(toLocalDate(source.getDate()))
				.withValidFrom(toLocalDate(source.getFromDate()))
				.withValidTo(toLocalDate(source.getToDate()))
				.withType(toText(source.getType()))
				.withReason(toText(source.getReason()))
				.withDecisionMaker(toFullName(source.getCaseworker(), caseworkerNames))
				.withAmount(toBigDecimal(source.getAmount()))
				.withElderlyCareDetails(toElderlyCareDecisionDetails(toBaseDecision(source))
					// LSS-only fields on top of the shared elderly-care details
					.withPersonCategory1(source.getPersonCategory1())
					.withPersonCategory2(source.getPersonCategory2())
					.withPersonCategory3(source.getPersonCategory3())
					.withPersonCategory3P(source.getPersonCategory3P())
					.withIncreasedHourlyAmount(toBigDecimal(source.getIncreasedHourlyAmount()))
					.withStandardAmount(toBigDecimal(source.getStandardAmount()))
					.withSfbCaseworker(toFullName(source.getSfbCaseworker(), caseworkerNames))))
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

	/**
	 * The legal basis of a decision read from {@code lss_decisions}. The endpoint serves two: {@code 3} is LSS, and
	 * {@code 7} is SFB — assistansersättning, decided by Försäkringskassan rather than the municipality. Anything else,
	 * including a missing code, falls back to LSS, which is what the endpoint is.
	 */
	private static String toLaw(final Integer lawCode) {
		return Optional.ofNullable(lawCode)
			.filter(code -> code == LAW_CODE_SFB)
			.map(_ -> LAW_SFB)
			.orElse(LAW_LSS);
	}

	/**
	 * The caseworker's full name, or null when there is none to show. EC sends the SFB caseworker as an object carrying
	 * only an id, with the name fields empty — a blank name must be left out rather than published as an empty string.
	 */
	private static String toFullName(final WEECIntegrationContractsCommonV1Caseworker caseworker, final Function<String, Optional<String>> caseworkerNames) {
		return Optional.ofNullable(caseworker)
			.map(source -> Optional.ofNullable(source.getFullName())
				.filter(not(String::isBlank))
				.orElseGet(() -> resolveName(source.getId(), caseworkerNames)))
			.filter(not(String::isBlank))
			.orElse(null);
	}

	/** The name behind a caseworker id, or null when there is no id or it resolves to nothing. */
	private static String resolveName(final String loginName, final Function<String, Optional<String>> caseworkerNames) {
		return Optional.ofNullable(loginName)
			.filter(not(String::isBlank))
			.flatMap(caseworkerNames)
			.orElse(null);
	}
}
