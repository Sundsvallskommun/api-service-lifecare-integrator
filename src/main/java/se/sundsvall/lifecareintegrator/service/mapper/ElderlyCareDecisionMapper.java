package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.elderlycare.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.elderlycare.WEECIntegrationContractsCommonV1CodeText;
import generated.se.sundsvall.elderlycare.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.elderlycare.WEECIntegrationContractsDecisionV1LssDecision;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.util.StringUtils;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.elderlycare.ElderlyCareDecisionDetails;

import static org.springframework.util.StringUtils.hasText;
import static se.sundsvall.lifecareintegrator.api.model.common.DecisionConstants.LAW_LSS;
import static se.sundsvall.lifecareintegrator.api.model.common.DecisionConstants.LAW_SFB;
import static se.sundsvall.lifecareintegrator.api.model.common.DecisionConstants.LAW_SOL;
import static se.sundsvall.lifecareintegrator.api.model.common.DecisionConstants.SOURCE_ELDERLY_CARE;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toBigDecimal;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toStringValue;

public final class ElderlyCareDecisionMapper {

	/** The {@code Law} code EC uses for Socialförsäkringsbalken; everything else on this endpoint is LSS. */
	private static final int LAW_CODE_SFB = 7;

	private ElderlyCareDecisionMapper() {}

	/**
	 * @param caseworkerNames display names by caseworker id, for the caseworkers EC sends without one — see
	 *                        {@link #collectUnnamedSolCaseworkerIds(List)}
	 */
	public static Decision toDecision(final WEECIntegrationContractsDecisionV1Decision decision, final Map<String, String> caseworkerNames) {
		return Decision.create()
			.withSource(SOURCE_ELDERLY_CARE)
			.withLaw(LAW_SOL)
			.withDecisionId(toStringValue(decision.getId()))
			.withDecided(toLocalDate(decision.getDate()))
			.withValidFrom(toLocalDate(decision.getFromDate()))
			.withValidTo(toLocalDate(decision.getToDate()))
			.withType(toText(decision.getType()))
			.withReason(toText(decision.getReason()))
			.withDecisionMaker(toFullName(decision.getCaseworker(), caseworkerNames))
			.withAmount(toBigDecimal(decision.getAmount()))
			.withElderlyCareDetails(toElderlyCareDecisionDetails(decision));
	}

	/**
	 * @param caseworkerNames display names by caseworker id — see {@link #collectUnnamedLssCaseworkerIds(List)}. The SFB
	 *                        caseworker is the usual reason it is needed.
	 */
	public static Decision toDecision(final WEECIntegrationContractsDecisionV1LssDecision decision, final Map<String, String> caseworkerNames) {
		return Decision.create()
			.withSource(SOURCE_ELDERLY_CARE)
			.withLaw(toLaw(decision.getLaw()))
			.withDecisionId(toStringValue(decision.getId()))
			.withDecided(toLocalDate(decision.getDate()))
			.withValidFrom(toLocalDate(decision.getFromDate()))
			.withValidTo(toLocalDate(decision.getToDate()))
			.withType(toText(decision.getType()))
			.withReason(toText(decision.getReason()))
			.withDecisionMaker(toFullName(decision.getCaseworker(), caseworkerNames))
			.withAmount(toBigDecimal(decision.getAmount()))
			.withElderlyCareDetails(toElderlyCareDecisionDetails(toBaseDecision(decision))
				// LSS-only fields on top of the shared elderly-care details
				.withPersonCategory1(decision.getPersonCategory1())
				.withPersonCategory2(decision.getPersonCategory2())
				.withPersonCategory3(decision.getPersonCategory3())
				.withPersonCategory3P(decision.getPersonCategory3P())
				.withIncreasedHourlyAmount(toBigDecimal(decision.getIncreasedHourlyAmount()))
				.withStandardAmount(toBigDecimal(decision.getStandardAmount()))
				.withSfbCaseworker(toFullName(decision.getSfbCaseworker(), caseworkerNames)));
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

	/**
	 * The ids of the caseworkers EC sent without a name, so the caller can look the names up before mapping.
	 */
	public static List<String> collectUnnamedSolCaseworkerIds(final List<WEECIntegrationContractsDecisionV1Decision> decisions) {
		return decisions.stream()
			.map(WEECIntegrationContractsDecisionV1Decision::getCaseworker)
			.filter(Objects::nonNull)
			.filter(ElderlyCareDecisionMapper::lacksName)
			.map(WEECIntegrationContractsCommonV1Caseworker::getId)
			.filter(StringUtils::hasText)
			.distinct()
			.toList();
	}

	/**
	 * As {@link #collectUnnamedSolCaseworkerIds(List)}, for both the caseworker and the SFB caseworker of LSS decisions.
	 */
	public static List<String> collectUnnamedLssCaseworkerIds(final List<WEECIntegrationContractsDecisionV1LssDecision> decisions) {
		return decisions.stream()
			.flatMap(decision -> Stream.of(decision.getCaseworker(), decision.getSfbCaseworker()))
			.filter(Objects::nonNull)
			.filter(ElderlyCareDecisionMapper::lacksName)
			.map(WEECIntegrationContractsCommonV1Caseworker::getId)
			.filter(StringUtils::hasText)
			.distinct()
			.toList();
	}

	private static boolean lacksName(final WEECIntegrationContractsCommonV1Caseworker caseworker) {
		return !hasText(caseworker.getFullName());
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
	private static String toFullName(final WEECIntegrationContractsCommonV1Caseworker caseworker, final Map<String, String> caseworkerNames) {
		if (caseworker == null) {
			return null;
		}
		if (hasText(caseworker.getFullName())) {
			return caseworker.getFullName();
		}
		if (!hasText(caseworker.getId())) {
			return null;
		}
		return caseworkerNames.get(caseworker.getId());
	}
}
