package se.sundsvall.lifecareintegrator.service.mapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.api.model.CalculationExpenseRequest;
import se.sundsvall.lifecareintegrator.api.model.CalculationIncomeRequest;
import se.sundsvall.lifecareintegrator.api.model.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.CreateCalculationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

	private static final String PERSON_NUMBER = "199001011234";
	private static final String MEMBER_PARTY_ID = "b3a1b9c2-5798-11e9-ae24-57fa13b361e2";
	private static final String MEMBER_PERSON_NUMBER = "199212312345";

	@Test
	void toPostAktualiseringsBodyRequest() {
		// Arrange
		final var request = CreateActualisationRequest.create()
			.withPartyId("81471222-5798-11e9-ae24-57fa13b361e1")
			.withDate(LocalDate.parse("2026-05-01"))
			.withTypeId(1)
			.withFromWhoId(2)
			.withReasonId(3)
			.withOrganisationId(4)
			.withOrganisationUnitId("unit-1")
			.withCaseworkerId("cw-1")
			.withSpecifiesId(5)
			.withServiceId(6)
			.withInvestigationId(7)
			.withWorkingStatusId(8);

		// Act
		final var result = RequestMapper.toPostAktualiseringsBodyRequest(request, PERSON_NUMBER);

		// Assert: the person number is injected, never sourced from the public request
		assertThat(result.getPersonId()).isEqualTo(PERSON_NUMBER);
		assertThat(result.getDate()).isEqualTo("2026-05-01");
		assertThat(result.getType()).isEqualTo(1);
		assertThat(result.getFromWho()).isEqualTo(2);
		assertThat(result.getReason()).isEqualTo(3);
		assertThat(result.getOrganisationId()).isEqualTo(4);
		assertThat(result.getOrganisationUnitId()).isEqualTo("unit-1");
		assertThat(result.getCaseworkerId()).isEqualTo("cw-1");
		assertThat(result.getSpecifies()).isEqualTo(5);
		assertThat(result.getServiceId()).isEqualTo(6);
		assertThat(result.getInvestigationId()).isEqualTo(7);
		assertThat(result.getWorkingStatus()).isEqualTo(8);
	}

	@Test
	void toPostAktualiseringsBodyRequestWithNull() {
		assertThat(RequestMapper.toPostAktualiseringsBodyRequest(null, PERSON_NUMBER)).isNull();
	}

	@Test
	void toPostCalculationBodyRequest() {
		// Arrange
		final var request = CreateCalculationRequest.create()
			.withPartyId("81471222-5798-11e9-ae24-57fa13b361e1")
			.withServiceId(1)
			.withInvestigationId(2)
			.withNormId(3)
			.withActualisationId(4)
			.withCalculationDate(LocalDate.parse("2026-05-01"))
			.withCalculationFromDate(LocalDate.parse("2026-05-01"))
			.withCalculationToDate(LocalDate.parse("2026-05-31"))
			.withHasCustomHouseholdSize(true)
			.withHouseholdSize(3)
			.withPersons(List.of(CalculationPersonRequest.create()
				.withPartyId(MEMBER_PARTY_ID)
				.withNumberOfDays(30)
				.withDeviationFromDate(LocalDate.parse("2026-05-10"))
				.withDeviationToDate(LocalDate.parse("2026-05-20"))))
			.withIncomes(List.of(CalculationIncomeRequest.create()
				.withTypeId(11)
				.withApplicantAmount(1000.0)
				.withApplicantAmountDate(LocalDate.parse("2026-05-01"))
				.withCoApplicantAmount(500.0)
				.withCoApplicantAmountDate(LocalDate.parse("2026-05-02"))
				.withNote("income note")))
			.withExpenses(List.of(CalculationExpenseRequest.create()
				.withTypeId(21)
				.withAmount(2000.0)
				.withApprovedAmount(1800.0)
				.withNote("expense note")))
			.withSpecialExpenses(List.of(CalculationExpenseRequest.create()
				.withTypeId(31)
				.withAmount(300.0)
				.withApprovedAmount(300.0)
				.withNote("special note")));

		// Act
		final var result = RequestMapper.toPostCalculationBodyRequest(request, PERSON_NUMBER, Map.of(MEMBER_PARTY_ID, MEMBER_PERSON_NUMBER));

		// Assert: applicant person number injected, top-level fields mapped
		assertThat(result.getPersonId()).isEqualTo(PERSON_NUMBER);
		assertThat(result.getServiceId()).isEqualTo(1);
		assertThat(result.getInvestigationId()).isEqualTo(2);
		assertThat(result.getNormId()).isEqualTo(3);
		assertThat(result.getAktualiseringId()).isEqualTo(4);
		assertThat(result.getCalculationDate()).isEqualTo("2026-05-01");
		assertThat(result.getCalculationFromDate()).isEqualTo("2026-05-01");
		assertThat(result.getCalculationToDate()).isEqualTo("2026-05-31");
		assertThat(result.getHasCustomHouseholdSize()).isTrue();
		assertThat(result.getHouseholdSize()).isEqualTo(3);

		// The household member's partyId is swapped for the resolved person number
		final var person = result.getCalculationPersons().getFirst();
		assertThat(person.getPersonId()).isEqualTo(MEMBER_PERSON_NUMBER);
		assertThat(person.getNumberOfDays()).isEqualTo(30);
		assertThat(person.getDeviationFromDate()).isEqualTo(OffsetDateTime.of(2026, 5, 10, 0, 0, 0, 0, ZoneOffset.UTC));
		assertThat(person.getDeviationToDate()).isEqualTo(OffsetDateTime.of(2026, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC));

		final var income = result.getCalculationIncomes().getFirst();
		assertThat(income.getId()).isEqualTo(11);
		assertThat(income.getApplicantAmount()).isEqualTo(1000.0);
		assertThat(income.getApplicantAmountDate()).isEqualTo(OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC));
		assertThat(income.getCoApplicantAmount()).isEqualTo(500.0);
		assertThat(income.getCoApplicantAmountDate()).isEqualTo(OffsetDateTime.of(2026, 5, 2, 0, 0, 0, 0, ZoneOffset.UTC));
		assertThat(income.getNote()).isEqualTo("income note");

		final var expense = result.getCalculationExpenses().getFirst();
		assertThat(expense.getId()).isEqualTo(21);
		assertThat(expense.getAmount()).isEqualTo(2000.0);
		assertThat(expense.getApprovedAmount()).isEqualTo(1800.0);
		assertThat(expense.getNote()).isEqualTo("expense note");

		final var specialExpense = result.getCalculationSpecialExpenses().getFirst();
		assertThat(specialExpense.getId()).isEqualTo(31);
		assertThat(specialExpense.getAmount()).isEqualTo(300.0);
		assertThat(specialExpense.getApprovedAmount()).isEqualTo(300.0);
		assertThat(specialExpense.getNote()).isEqualTo("special note");
	}

	@Test
	void toPostCalculationBodyRequestWithNull() {
		assertThat(RequestMapper.toPostCalculationBodyRequest(null, PERSON_NUMBER, Map.of())).isNull();
	}

	@Test
	void toPostCalculationBodyRequestWithEmptyLists() {
		// Arrange: no nested lists set
		final var request = CreateCalculationRequest.create()
			.withPartyId("81471222-5798-11e9-ae24-57fa13b361e1")
			.withNormId(3)
			.withCalculationDate(LocalDate.parse("2026-05-01"))
			.withCalculationFromDate(LocalDate.parse("2026-05-01"))
			.withCalculationToDate(LocalDate.parse("2026-05-31"));

		// Act
		final var result = RequestMapper.toPostCalculationBodyRequest(request, PERSON_NUMBER, Map.of());

		// Assert
		assertThat(result.getCalculationPersons()).isEmpty();
		assertThat(result.getCalculationIncomes()).isEmpty();
		assertThat(result.getCalculationExpenses()).isEmpty();
		assertThat(result.getCalculationSpecialExpenses()).isEmpty();
	}
}
