package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.PersonBasedDecisionDTO;
import generated.se.sundsvall.familycare.PersonBasedDecisionPersonDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.lifecareintegrator.api.model.familycare.RelatedPerson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class FamilyCareDecisionMapperTest {

	/** The batch party lookup's answer: only 199001011234 resolves. */
	private static final Map<String, String> PARTY_IDS = Map.of("199001011234", "party-1");

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
			.decisionPersonDTOs(List.of(
				new PersonBasedDecisionPersonDTO().personId("199001011234").name("Kalle Karlsson").isCoApplicant(false),
				new PersonBasedDecisionPersonDTO().personId("201001011234").name("Okänd Person").isCoApplicant(true),
				new PersonBasedDecisionPersonDTO().personId(null).name("Utan Personnummer").isCoApplicant(false)));

		// Act
		final var result = FamilyCareDecisionMapper.toDecision(source, PARTY_IDS);

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

		// The personnummer is swapped for the party id; unresolvable persons keep a null party id
		assertThat(details.getPersons())
			.extracting(RelatedPerson::getPartyId, RelatedPerson::getName, RelatedPerson::getCoApplicant)
			.containsExactly(
				tuple("party-1", "Kalle Karlsson", false),
				tuple(null, "Okänd Person", true),
				tuple(null, "Utan Personnummer", false));
		assertThat(result.toString()).doesNotContain("199001011234", "201001011234");
	}

	@Test
	void toDecisionFromFcDecisionWithoutPersons() {
		final var result = FamilyCareDecisionMapper.toDecision(new PersonBasedDecisionDTO().decisionPersonDTOs(null), PARTY_IDS);

		assertThat(result.getFamilyCareDetails().getPersons()).isEmpty();
	}

	@Test
	void toDecisionFromFcDecisionWithNull() {
		assertThat(FamilyCareDecisionMapper.toDecision((PersonBasedDecisionDTO) null, PARTY_IDS)).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"garbage-date", "2026-13-99T00:00:00", "2026"
	})
	void toDecisionFromFcDecisionWithUnparseableDates(final String date) {
		// Act
		final var result = FamilyCareDecisionMapper.toDecision(new PersonBasedDecisionDTO()
			.date(date)
			.fromDate(date)
			.toDate(date), PARTY_IDS);

		// Assert
		assertThat(result.getDecided()).isNull();
		assertThat(result.getValidFrom()).isNull();
		assertThat(result.getValidTo()).isNull();
	}

	@Test
	void collectPersonNumbers() {
		final var decisions = List.of(
			new PersonBasedDecisionDTO().decisionPersonDTOs(List.of(
				new PersonBasedDecisionPersonDTO().personId("199001011234"),
				new PersonBasedDecisionPersonDTO().personId(null))),
			new PersonBasedDecisionDTO().decisionPersonDTOs(List.of(
				new PersonBasedDecisionPersonDTO().personId("199001011234"),
				new PersonBasedDecisionPersonDTO().personId("201001011234"))),
			new PersonBasedDecisionDTO().decisionPersonDTOs(null));

		assertThat(FamilyCareDecisionMapper.collectPersonNumbers(decisions)).containsExactly("199001011234", "201001011234");
		assertThat(FamilyCareDecisionMapper.collectPersonNumbers(null)).isEmpty();
	}
}
