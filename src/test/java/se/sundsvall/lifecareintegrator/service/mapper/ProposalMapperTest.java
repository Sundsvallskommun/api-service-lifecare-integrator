package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.AktualisationsAttachmentSenderTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringAttachmentTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringProposalDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsFromWhoDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsInfoDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsInvestigationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsInvestigationTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsOrganizationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsReasonDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsServiceDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsServiceTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsSpecifyTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringsWorkingStatusDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationAktualiseringDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationCalculationIncomeTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationExpenseTypeDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationHouseholdMemberDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationInvestigationDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationNormDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationProposalDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationServiceDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationSpecialExpenseTypeDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.api.model.common.Lookup;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationReference;
import se.sundsvall.lifecareintegrator.api.model.familycare.HouseholdMember;
import se.sundsvall.lifecareintegrator.api.model.familycare.Norm;
import se.sundsvall.lifecareintegrator.api.model.familycare.ProposalCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ProposalMapperTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PERSON_NUMBER = "199001011234";
	private static final String UNRESOLVED_PERSON_NUMBER = "199212312345";

	@Test
	void toActualisationProposal() {
		// Arrange
		final var source = new PersonBasedAktualiseringProposalDTO()
			.actualisationTypes(List.of(new PersonBasedAktualiseringsInfoDTO()
				.id(1)
				.name("Ansökan")
				.specifyTypeMandatory(true)
				.workingStatus(false)
				.reasons(List.of(new PersonBasedAktualiseringsReasonDTO().id(11).name("Försörjningsstöd")))
				.fromWho(List.of(new PersonBasedAktualiseringsFromWhoDTO().id(12).name("Egen ansökan")))
				.investigationTypes(List.of(new PersonBasedAktualiseringsInvestigationTypeDTO().id(13).name("Utredning EB")))
				.serviceTypes(List.of(new PersonBasedAktualiseringsServiceTypeDTO().id(14).name("Insats EB")))))
			.specifyTypes(List.of(new PersonBasedAktualiseringsSpecifyTypeDTO().id(2).name("Specificering")))
			.workingStatus(List.of(new PersonBasedAktualiseringsWorkingStatusDTO().id(3).name("Arbetslös")))
			.organizations(List.of(new PersonBasedAktualiseringsOrganizationDTO().id(4).unitId("unit-1").name("Vuxen försörjningsstöd")))
			.investigations(List.of(new PersonBasedAktualiseringsInvestigationDTO()
				.id(5).type(51).name("Utredning").startDate("2026-01-01").organisationId(6).organisationUnitId("unit-2").caseworkerId("cw-1")))
			.services(List.of(new PersonBasedAktualiseringsServiceDTO()
				.id(7).type(71).name("Insats").startDate("2026-02-01T00:00:00").organisationId(8).organisationUnitId("unit-3").caseworkerId("cw-2")))
			.attachmentTypes(List.of(new PersonBasedAktualiseringAttachmentTypeDTO()
				.id(9).name("Ansökan").senderTypes(List.of(new AktualisationsAttachmentSenderTypeDTO().id(91).name("Medborgare")))));

		// Act
		final var result = ProposalMapper.toActualisationProposal(source);

		// Assert
		final var type = result.getActualisationTypes().getFirst();
		assertThat(type.getId()).isEqualTo(1);
		assertThat(type.getName()).isEqualTo("Ansökan");
		assertThat(type.getSpecifyTypeMandatory()).isTrue();
		assertThat(type.getWorkingStatus()).isFalse();
		assertThat(type.getReasons()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(11, "Försörjningsstöd"));
		assertThat(type.getFromWho()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(12, "Egen ansökan"));
		assertThat(type.getInvestigationTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(13, "Utredning EB"));
		assertThat(type.getServiceTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(14, "Insats EB"));

		assertThat(result.getSpecifyTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(2, "Specificering"));
		assertThat(result.getWorkingStatus()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(3, "Arbetslös"));

		final var organization = result.getOrganizations().getFirst();
		assertThat(organization.getId()).isEqualTo(4);
		assertThat(organization.getUnitId()).isEqualTo("unit-1");
		assertThat(organization.getName()).isEqualTo("Vuxen försörjningsstöd");

		assertThat(result.getInvestigations())
			.extracting(ProposalCase::getId, ProposalCase::getType, ProposalCase::getName, ProposalCase::getStartDate,
				ProposalCase::getOrganisationId, ProposalCase::getOrganisationUnitId, ProposalCase::getCaseworkerId)
			.containsExactly(tuple(5, 51, "Utredning", LocalDate.parse("2026-01-01"), 6, "unit-2", "cw-1"));
		assertThat(result.getServices())
			.extracting(ProposalCase::getId, ProposalCase::getType, ProposalCase::getName, ProposalCase::getStartDate,
				ProposalCase::getOrganisationId, ProposalCase::getOrganisationUnitId, ProposalCase::getCaseworkerId)
			.containsExactly(tuple(7, 71, "Insats", LocalDate.parse("2026-02-01"), 8, "unit-3", "cw-2"));

		final var attachmentType = result.getAttachmentTypes().getFirst();
		assertThat(attachmentType.getId()).isEqualTo(9);
		assertThat(attachmentType.getName()).isEqualTo("Ansökan");
		assertThat(attachmentType.getSenderTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(91, "Medborgare"));
	}

	@Test
	void toActualisationProposalWithNull() {
		assertThat(ProposalMapper.toActualisationProposal(null)).isNull();
	}

	@Test
	void toCalculationProposal() {
		// Arrange
		final var source = new PersonBasedCalculationProposalDTO()
			.investigations(List.of(new PersonBasedCalculationInvestigationDTO().id(1).type(11).name("Utredning").startDate("2026-01-01")))
			.services(List.of(new PersonBasedCalculationServiceDTO().id(2).type(21).name("Insats").startDate("2026-02-01")))
			.norms(List.of(new PersonBasedCalculationNormDTO().id(3).name("Riksnorm 2026").fromDate("2026-01-01").toDate("2026-12-31")))
			.householdMembers(List.of(
				new PersonBasedCalculationHouseholdMemberDTO().personId(PERSON_NUMBER).name("Kalle Karlsson").childFromOtherHousehold(false),
				new PersonBasedCalculationHouseholdMemberDTO().personId(UNRESOLVED_PERSON_NUMBER).name("Lisa Larsson").childFromOtherHousehold(true)))
			.calculationIncomeTypes(List.of(new PersonBasedCalculationCalculationIncomeTypeDTO().id(4).name("Lön")))
			.calculationExpenseTypes(List.of(new PersonBasedCalculationExpenseTypeDTO().id(5).name("Hyra")))
			.calculationSpecialExpenseTypes(List.of(new PersonBasedCalculationSpecialExpenseTypeDTO().id(6).name("Tandvård")))
			.aktualiseringMandatory(true)
			.numberOfFamilyMembersNotInHousehold(1)
			.aktualiserings(List.of(new PersonBasedCalculationAktualiseringDTO().id(7).type("Ansökan").date("2026-03-01")));

		// Act: only the first household member resolves to a partyId
		final var result = ProposalMapper.toCalculationProposal(source, Map.of(PERSON_NUMBER, PARTY_ID));

		// Assert
		assertThat(result.getInvestigations())
			.extracting(ProposalCase::getId, ProposalCase::getType, ProposalCase::getName, ProposalCase::getStartDate)
			.containsExactly(tuple(1, 11, "Utredning", LocalDate.parse("2026-01-01")));
		assertThat(result.getServices())
			.extracting(ProposalCase::getId, ProposalCase::getType, ProposalCase::getName, ProposalCase::getStartDate)
			.containsExactly(tuple(2, 21, "Insats", LocalDate.parse("2026-02-01")));
		assertThat(result.getNorms())
			.extracting(Norm::getId, Norm::getName, Norm::getValidFrom, Norm::getValidTo)
			.containsExactly(tuple(3, "Riksnorm 2026", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")));

		// The person numbers are swapped for partyIds — the unresolved member keeps a null partyId
		assertThat(result.getHouseholdMembers())
			.extracting(HouseholdMember::getPartyId, HouseholdMember::getName, HouseholdMember::getChildFromOtherHousehold)
			.containsExactly(
				tuple(PARTY_ID, "Kalle Karlsson", false),
				tuple(null, "Lisa Larsson", true));

		assertThat(result.getIncomeTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(4, "Lön"));
		assertThat(result.getExpenseTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(5, "Hyra"));
		assertThat(result.getSpecialExpenseTypes()).extracting(Lookup::getId, Lookup::getName).containsExactly(tuple(6, "Tandvård"));
		assertThat(result.getActualisationMandatory()).isTrue();
		assertThat(result.getNumberOfFamilyMembersNotInHousehold()).isEqualTo(1);
		assertThat(result.getActualisations())
			.extracting(ActualisationReference::getId, ActualisationReference::getType, ActualisationReference::getDate)
			.containsExactly(tuple(7, "Ansökan", LocalDate.parse("2026-03-01")));
	}

	@Test
	void toCalculationProposalWithNull() {
		assertThat(ProposalMapper.toCalculationProposal(null, Map.of())).isNull();
	}

	@Test
	void toCalculationProposalWithEmptySource() {
		// Act
		final var result = ProposalMapper.toCalculationProposal(new PersonBasedCalculationProposalDTO(), Map.of());

		// Assert: empty vendor lists map to empty lists, not null
		assertThat(result.getInvestigations()).isEmpty();
		assertThat(result.getServices()).isEmpty();
		assertThat(result.getNorms()).isEmpty();
		assertThat(result.getHouseholdMembers()).isEmpty();
		assertThat(result.getIncomeTypes()).isEmpty();
		assertThat(result.getExpenseTypes()).isEmpty();
		assertThat(result.getSpecialExpenseTypes()).isEmpty();
		assertThat(result.getActualisations()).isEmpty();
	}
}
