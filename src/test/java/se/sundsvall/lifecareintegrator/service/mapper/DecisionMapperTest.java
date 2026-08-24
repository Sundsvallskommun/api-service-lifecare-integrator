package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1CodeText;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionPersonDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.lifecareintegrator.api.model.common.Decision;
import se.sundsvall.lifecareintegrator.api.model.elderlycare.ElderlyCareDecisionDetails;

import static java.time.Month.FEBRUARY;
import static org.assertj.core.api.Assertions.assertThat;

class DecisionMapperTest {

	/** No caseworker resolution — Lifecare supplies the name in every case these tests cover. */
	private static final Function<String, Optional<String>> NO_NAMES = _ -> Optional.empty();

	private static final LocalDateTime DECISION_DATE = LocalDateTime.parse("2026-05-01T00:00:00");
	private static final LocalDateTime FROM_DATE = LocalDateTime.parse("2026-05-15T00:00:00");
	private static final LocalDateTime TO_DATE = LocalDateTime.parse("2026-10-31T00:00:00");

	@Test
	void toDecisionFromSolDecision() {
		// Arrange
		final var source = new WEECIntegrationContractsDecisionV1Decision()
			.id(123)
			.investigationId(456)
			.personId("199001011234")
			.code(codeText("Beviljad"))
			.type(codeText("Hemtjänst"))
			.date(DECISION_DATE)
			.fromDate(FROM_DATE)
			.toDate(TO_DATE)
			.reason(codeText("Bifall"))
			.caseworker(new WEECIntegrationContractsCommonV1Caseworker().fullName("Anna Andersson"))
			.serviceCategory(codeText("Städning"))
			.hour(10.5)
			.hourType(codeText("Per vecka"))
			.amount(5000.0)
			.amountType(codeText("Per månad"))
			.quantity(2.0)
			.quantityType(codeText("Per dag"))
			.visit(3.0)
			.visitType(codeText("Per vecka"))
			.decisionLevel(codeText("Delegation"))
			.day(5.0)
			.dayType(codeText("Per månad"))
			.executionStartDate(FROM_DATE)
			.executionEndDate(TO_DATE)
			.iterationNumber(1)
			.daysOfDecision(184.0)
			.orderIds(List.of(1, 2))
			.deleted(false);

		// Act
		final var result = DecisionMapper.toDecision(source, NO_NAMES);
		final var expected = Decision.create()
			.withSource("ELDERLY_CARE")
			.withLaw("SOL")
			.withDecisionId("123")
			.withDecided(LocalDate.parse("2026-05-01"))
			.withValidFrom(LocalDate.parse("2026-05-15"))
			.withValidTo(LocalDate.parse("2026-10-31"))
			.withType("Hemtjänst")
			.withReason("Bifall")
			.withDecisionMaker("Anna Andersson")
			.withAmount(BigDecimal.valueOf(5000.0))
			.withElderlyCareDetails(ElderlyCareDecisionDetails.create()
				.withInvestigationId(456)
				.withCode("Beviljad")
				.withServiceCategory("Städning")
				.withHours(10.5)
				.withHourType("Per vecka")
				.withAmountType("Per månad")
				.withQuantity(2.0)
				.withQuantityType("Per dag")
				.withVisits(3.0)
				.withVisitType("Per vecka")
				.withDays(5.0)
				.withDayType("Per månad")
				.withDecisionLevel("Delegation")
				.withExecutionStartDate(LocalDate.parse("2026-05-15"))
				.withExecutionEndDate(LocalDate.parse("2026-10-31"))
				.withIterationNumber(1)
				.withDaysOfDecision(184.0)
				.withOrderIds(List.of(1, 2))
				.withDeleted(false));

		// Assert
		assertThat(result).usingRecursiveComparison().isEqualTo(expected);
	}

	@Test
	void toDecisionFromSolDecisionWithNull() {
		assertThat(DecisionMapper.toDecision((WEECIntegrationContractsDecisionV1Decision) null, NO_NAMES)).isNull();
	}

	@Test
	void toDecisionFromSolDecisionWithMinimalInput() {
		// Act
		final var result = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1Decision(), NO_NAMES);

		// Assert
		assertThat(result.getSource()).isEqualTo("ELDERLY_CARE");
		assertThat(result.getLaw()).isEqualTo("SOL");
		assertThat(result).hasAllNullFieldsOrPropertiesExcept("source", "law", "elderlyCareDetails");
		assertThat(result.getElderlyCareDetails()).hasAllNullFieldsOrPropertiesExcept("orderIds");
	}

	@Test
	void toDecisionFromLssDecision() {
		// Arrange
		final var source = new WEECIntegrationContractsDecisionV1LssDecision()
			.id(789)
			.investigationId(456)
			.type(codeText("Personlig assistans"))
			.date(DECISION_DATE)
			.fromDate(FROM_DATE)
			.toDate(TO_DATE)
			.reason(codeText("Bifall"))
			.caseworker(new WEECIntegrationContractsCommonV1Caseworker().fullName("Anna Andersson"))
			.amount(300.0)
			.personCategory1(true)
			.personCategory2(false)
			.personCategory3(false)
			.personCategory3P(false)
			.increasedHourlyAmount(150.0)
			.standardAmount(300.0)
			.sfbCaseworker(new WEECIntegrationContractsCommonV1Caseworker().fullName("Bo Bengtsson"));

		// Act
		final var result = DecisionMapper.toDecision(source, NO_NAMES);

		// Assert
		assertThat(result.getSource()).isEqualTo("ELDERLY_CARE");
		assertThat(result.getLaw()).isEqualTo("LSS");
		assertThat(result.getDecisionId()).isEqualTo("789");
		assertThat(result.getDecided()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(result.getType()).isEqualTo("Personlig assistans");
		assertThat(result.getFamilyCareDetails()).isNull();

		final var details = result.getElderlyCareDetails();
		assertThat(details.getInvestigationId()).isEqualTo(456);
		assertThat(details.getPersonCategory1()).isTrue();
		assertThat(details.getPersonCategory2()).isFalse();
		assertThat(details.getPersonCategory3()).isFalse();
		assertThat(details.getPersonCategory3P()).isFalse();
		assertThat(details.getIncreasedHourlyAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
		assertThat(details.getStandardAmount()).isEqualByComparingTo(BigDecimal.valueOf(300.0));
		assertThat(details.getSfbCaseworker()).isEqualTo("Bo Bengtsson");
	}

	@Test
	void toDecisionFromLssDecisionWithNull() {
		assertThat(DecisionMapper.toDecision((WEECIntegrationContractsDecisionV1LssDecision) null, NO_NAMES)).isNull();
	}

	@Test
	void toDecisionFromFcDecision() {
		// Arrange
		final var source = new PersonBasedDecisionDTO()
			.id(555)
			.date("2026-05-01T00:00:00")
			.type("Ekonomiskt bistånd")
			.fromDate("2026-05-01")
			.toDate("2026-05-31")
			.reason("Bifall")
			.decisionMaker("Cecilia Carlsson")
			.organization("Vuxen försörjningsstöd")
			.investigationExecutionId(777)
			.serviceId(888)
			.amount(9000.0)
			.coApplicant("Berit Bengtsson")
			.reasonCoApplicant("Bifall")
			.connectedApplication(999)
			.decisionPersonDTOs(List.of(new PersonBasedDecisionPersonDTO()
				.personId("199001011234")
				.name("Kalle Karlsson")
				.isCoApplicant(false)));

		// Act
		final var result = DecisionMapper.toDecision(source);

		// Assert
		assertThat(result.getSource()).isEqualTo("FAMILY_CARE");
		assertThat(result.getLaw()).isNull();
		assertThat(result.getDecisionId()).isEqualTo("555");
		assertThat(result.getDecided()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(result.getValidFrom()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(result.getValidTo()).isEqualTo(LocalDate.parse("2026-05-31"));
		assertThat(result.getType()).isEqualTo("Ekonomiskt bistånd");
		assertThat(result.getReason()).isEqualTo("Bifall");
		assertThat(result.getDecisionMaker()).isEqualTo("Cecilia Carlsson");
		assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(9000.0));
		assertThat(result.getElderlyCareDetails()).isNull();

		final var details = result.getFamilyCareDetails();
		assertThat(details.getInvestigationExecutionId()).isEqualTo(777);
		assertThat(details.getServiceId()).isEqualTo(888);
		assertThat(details.getOrganization()).isEqualTo("Vuxen försörjningsstöd");
		assertThat(details.getCoApplicant()).isEqualTo("Berit Bengtsson");
		assertThat(details.getReasonCoApplicant()).isEqualTo("Bifall");
		assertThat(details.getConnectedApplication()).isEqualTo(999);

		// The person id (personnummer) must never survive the mapping
		assertThat(details.getPersons()).hasSize(1);
		assertThat(details.getPersons().getFirst().getName()).isEqualTo("Kalle Karlsson");
		assertThat(details.getPersons().getFirst().getCoApplicant()).isFalse();
	}

	@Test
	void toDecisionFromFcDecisionWithNull() {
		assertThat(DecisionMapper.toDecision((PersonBasedDecisionDTO) null)).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"garbage-date", "2026-13-99T00:00:00", "2026"
	})
	void toDecisionFromFcDecisionWithUnparseableDates(final String date) {
		// Act
		final var result = DecisionMapper.toDecision(new PersonBasedDecisionDTO()
			.date(date)
			.fromDate(date)
			.toDate(date));

		// Assert
		assertThat(result.getDecided()).isNull();
		assertThat(result.getValidFrom()).isNull();
		assertThat(result.getValidTo()).isNull();
	}

	private static WEECIntegrationContractsCommonV1CodeText codeText(final String text) {
		return new WEECIntegrationContractsCommonV1CodeText().code(1).text(text);
	}

	@ParameterizedTest
	@CsvSource(value = {
		"3, LSS", "7, SFB", "0, LSS", "null, LSS"
	}, nullValues = "null")
	void theLawIsReadFromTheDecisionNotAssumedFromTheEndpoint(final Integer lawCode, final String expected) {
		// lss_decisions serves both: 3 is LSS, 7 is SFB — assistansersättning decided by Försäkringskassan. Anything
		// unrecognised falls back to what the endpoint is.
		final var decision = DecisionMapper.toDecision(
			new WEECIntegrationContractsDecisionV1LssDecision().id(1).law(lawCode), NO_NAMES);

		assertThat(decision.getLaw()).isEqualTo(expected);
	}

	@Test
	void ecSentinelDatesDoNotReachTheCitizen() {
		final var notSet = LocalDateTime.parse("0001-01-01T00:00:00");
		final var noEndDate = LocalDateTime.parse("9999-12-31T23:59:59.9999999");

		final var decision = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1Decision()
			.id(1)
			.fromDate(notSet)
			.toDate(noEndDate)
			.executionStartDate(notSet)
			.executionEndDate(noEndDate), NO_NAMES);

		assertThat(decision.getValidFrom()).isNull();
		assertThat(decision.getValidTo()).isNull();
		assertThat(decision.getElderlyCareDetails().getExecutionStartDate()).isNull();
		assertThat(decision.getElderlyCareDetails().getExecutionEndDate()).isNull();
	}

	@Test
	void anOrdinaryDateIsLeftAlone() {
		final var decision = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1Decision()
			.id(1)
			.fromDate(LocalDateTime.parse("2024-02-07T00:00:00")), NO_NAMES);

		assertThat(decision.getValidFrom()).isEqualTo(LocalDate.of(2024, FEBRUARY, 7));
	}

	@Test
	void aCaseworkerNameIsResolvedOnlyWhenLifecareDoesNotSupplyOne() {
		final Function<String, Optional<String>> names = loginName -> Optional.of(loginName)
			.filter("LOHE"::equals)
			.map(_ -> "Lotta Helsinger");

		final var supplied = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1LssDecision()
			.id(1)
			.caseworker(new WEECIntegrationContractsCommonV1Caseworker().id("LOHE").fullName("Helsinger Lotta")), names);
		assertThat(supplied.getDecisionMaker()).isEqualTo("Helsinger Lotta");

		final var blank = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1LssDecision()
			.id(2)
			.sfbCaseworker(new WEECIntegrationContractsCommonV1Caseworker().id("LOHE").fullName("")), names);
		assertThat(blank.getElderlyCareDetails().getSfbCaseworker()).isEqualTo("Lotta Helsinger");

		final var unresolvable = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1LssDecision()
			.id(3)
			.sfbCaseworker(new WEECIntegrationContractsCommonV1Caseworker().id("FÖRSKASSAN").fullName("")), names);
		assertThat(unresolvable.getElderlyCareDetails().getSfbCaseworker()).isNull();

		final var withoutId = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1LssDecision()
			.id(4)
			.sfbCaseworker(new WEECIntegrationContractsCommonV1Caseworker().fullName("")), names);
		assertThat(withoutId.getElderlyCareDetails().getSfbCaseworker()).isNull();
	}
}
