package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1Caseworker;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsCommonV1CodeText;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1Decision;
import generated.se.sundsvall.lifecareec.WEECIntegrationContractsDecisionV1LssDecision;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionPersonDTO;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionMapperTest {

	private static final OffsetDateTime DECISION_DATE = OffsetDateTime.parse("2026-05-01T00:00:00Z");
	private static final OffsetDateTime FROM_DATE = OffsetDateTime.parse("2026-05-15T00:00:00Z");
	private static final OffsetDateTime TO_DATE = OffsetDateTime.parse("2026-10-31T00:00:00Z");

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
		final var result = DecisionMapper.toDecision(source);

		// Assert
		assertThat(result.getSource()).isEqualTo("ELDERLY_CARE");
		assertThat(result.getLaw()).isEqualTo("SOL");
		assertThat(result.getDecisionId()).isEqualTo("123");
		assertThat(result.getDecided()).isEqualTo(LocalDate.parse("2026-05-01"));
		assertThat(result.getValidFrom()).isEqualTo(LocalDate.parse("2026-05-15"));
		assertThat(result.getValidTo()).isEqualTo(LocalDate.parse("2026-10-31"));
		assertThat(result.getType()).isEqualTo("Hemtjänst");
		assertThat(result.getReason()).isEqualTo("Bifall");
		assertThat(result.getDecisionMaker()).isEqualTo("Anna Andersson");
		assertThat(result.getAmount()).isEqualTo(5000.0);
		assertThat(result.getFamilyCareDetails()).isNull();

		final var details = result.getElderlyCareDetails();
		assertThat(details.getInvestigationId()).isEqualTo(456);
		assertThat(details.getCode()).isEqualTo("Beviljad");
		assertThat(details.getServiceCategory()).isEqualTo("Städning");
		assertThat(details.getHours()).isEqualTo(10.5);
		assertThat(details.getHourType()).isEqualTo("Per vecka");
		assertThat(details.getAmountType()).isEqualTo("Per månad");
		assertThat(details.getQuantity()).isEqualTo(2.0);
		assertThat(details.getQuantityType()).isEqualTo("Per dag");
		assertThat(details.getVisits()).isEqualTo(3.0);
		assertThat(details.getVisitType()).isEqualTo("Per vecka");
		assertThat(details.getDays()).isEqualTo(5.0);
		assertThat(details.getDayType()).isEqualTo("Per månad");
		assertThat(details.getDecisionLevel()).isEqualTo("Delegation");
		assertThat(details.getExecutionStartDate()).isEqualTo(LocalDate.parse("2026-05-15"));
		assertThat(details.getExecutionEndDate()).isEqualTo(LocalDate.parse("2026-10-31"));
		assertThat(details.getIterationNumber()).isEqualTo(1);
		assertThat(details.getDaysOfDecision()).isEqualTo(184.0);
		assertThat(details.getOrderIds()).containsExactly(1, 2);
		assertThat(details.getDeleted()).isFalse();
		assertThat(details.getPersonCategory1()).isNull();
		assertThat(details.getSfbCaseworker()).isNull();
	}

	@Test
	void toDecisionFromSolDecisionWithNull() {
		assertThat(DecisionMapper.toDecision((WEECIntegrationContractsDecisionV1Decision) null)).isNull();
	}

	@Test
	void toDecisionFromSolDecisionWithMinimalInput() {
		// Act
		final var result = DecisionMapper.toDecision(new WEECIntegrationContractsDecisionV1Decision());

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
		final var result = DecisionMapper.toDecision(source);

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
		assertThat(details.getIncreasedHourlyAmount()).isEqualTo(150.0);
		assertThat(details.getStandardAmount()).isEqualTo(300.0);
		assertThat(details.getSfbCaseworker()).isEqualTo("Bo Bengtsson");
	}

	@Test
	void toDecisionFromLssDecisionWithNull() {
		assertThat(DecisionMapper.toDecision((WEECIntegrationContractsDecisionV1LssDecision) null)).isNull();
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
		assertThat(result.getAmount()).isEqualTo(9000.0);
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
}
