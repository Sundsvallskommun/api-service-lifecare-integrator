package se.sundsvall.lifecareintegrator.service;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedCalculationDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationHouseholdMemberDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationPersonDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedContactDTO;
import generated.se.sundsvall.familycare.PersonBasedPersonDTO;
import generated.se.sundsvall.familycare.PostAktualiseringsBodyRequest;
import generated.se.sundsvall.familycare.PostCalculationBodyRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.PeriodParameters;
import se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyCareServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String PERSON_NUMBER = "199001011234";
	private static final String MEMBER_PARTY_ID = "b3a1b9c2-5798-11e9-ae24-57fa13b361e2";
	private static final String MEMBER_PERSON_NUMBER = "199212312345";

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private FamilyCareIntegration familyCareIntegrationMock;

	@InjectMocks
	private FamilyCareService familyCareService;

	@Test
	void getPerson() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getPerson(PERSON_NUMBER)).thenReturn(new PersonBasedPersonDTO().name("Test Person"));

		// Act
		final var result = familyCareService.getPerson(MUNICIPALITY_ID, PARTY_ID);

		// Verify: personnummer is what gets forwarded to FC, and it is not exposed
		assertThat(result.getName()).isEqualTo("Test Person");
		verify(partyIntegrationMock).getPersonNumber(MUNICIPALITY_ID, PARTY_ID);
		verify(familyCareIntegrationMock).getPerson(PERSON_NUMBER);
	}

	@Test
	void getContacts() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getContacts(PERSON_NUMBER)).thenReturn(List.of(new PersonBasedContactDTO().name("Contact")));

		// Act
		final var result = familyCareService.getContacts(MUNICIPALITY_ID, PARTY_ID);

		// Verify
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getName()).isEqualTo("Contact");
		verify(familyCareIntegrationMock).getContacts(PERSON_NUMBER);
	}

	@Test
	void getActualisationsResolvesPartyAndForwardsWindow() {
		// Parameter values
		final var from = LocalDate.parse("2026-01-01");
		final var to = LocalDate.parse("2026-06-30");
		final var parameters = new PeriodParameters();
		parameters.setPartyId(PARTY_ID);
		parameters.setFrom(from);
		parameters.setTo(to);
		parameters.setLimit(20);
		parameters.setPage(1);
		parameters.setAscending(true);

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getActualisations(PERSON_NUMBER, from, to, 20, 1, true))
			.thenReturn(new ApiPaginationCompositePersonBasedAktualiseringDTO().pageNumber(1).result(List.of(new PersonBasedAktualiseringDTO().id(1))));

		// Act
		final var result = familyCareService.getActualisations(MUNICIPALITY_ID, parameters);

		// Verify: the resolved person number (not partyId) is forwarded
		assertThat(result.getActualisations()).hasSize(1);
		verify(familyCareIntegrationMock).getActualisations(PERSON_NUMBER, from, to, 20, 1, true);
	}

	@Test
	void getActualisationsUsesOpenWindowWhenDatesMissing() {
		// Parameter values: no dates or ascending set — page/limit default to 1/100
		final var parameters = new PeriodParameters();
		parameters.setPartyId(PARTY_ID);

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getActualisations(any(), any(), any(), any(), any(), any()))
			.thenReturn(new ApiPaginationCompositePersonBasedAktualiseringDTO());

		// Act
		familyCareService.getActualisations(MUNICIPALITY_ID, parameters);

		// Verify: everything is asked for, with default paging (page 1, limit 100)
		verify(familyCareIntegrationMock).getActualisations(PERSON_NUMBER, LocalDate.parse("1900-01-01"), LocalDate.parse("2100-12-31"), 100, 1, null);
	}

	@Test
	void getDocumentContent() {
		// Parameter values
		final var content = new byte[] {
			1, 2, 3
		};

		// Mock
		when(familyCareIntegrationMock.getDocumentContent("doc-1")).thenReturn(content);

		// Act
		final var result = familyCareService.getDocumentContent("doc-1");

		// Verify: no party resolution for document content
		assertThat(result).isEqualTo(content);
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void getUsersDoesNotResolveParty() {
		// Parameter values
		final var modifiedAfter = OffsetDateTime.parse("2026-01-01T00:00:00Z");

		// Mock
		when(familyCareIntegrationMock.getUsers(100, 0, modifiedAfter, null)).thenReturn(List.of());

		// Act
		final var result = familyCareService.getUsers(100, 0, modifiedAfter, null);

		// Verify
		assertThat(result).isEmpty();
		verify(familyCareIntegrationMock).getUsers(100, 0, modifiedAfter, null);
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void createActualisationResolvesPartyFromRequest() {
		// Arrange
		final var request = CreateActualisationRequest.create()
			.withPartyId(PARTY_ID)
			.withDate(LocalDate.parse("2026-05-01"))
			.withTypeId(1);

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.createActualisation(any())).thenReturn(42);

		// Act
		final var result = familyCareService.createActualisation(MUNICIPALITY_ID, request);

		// Verify: the FC body carries the resolved person number, never the partyId
		final var captor = ArgumentCaptor.forClass(PostAktualiseringsBodyRequest.class);
		verify(familyCareIntegrationMock).createActualisation(captor.capture());
		assertThat(captor.getValue().getPersonId()).isEqualTo(PERSON_NUMBER);
		assertThat(result).isEqualTo(42);
	}

	@Test
	void getCalculationProposalSwapsHouseholdMemberPersonNumbers() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getCalculationProposal(PERSON_NUMBER)).thenReturn(new PersonBasedCalculationProposalDTO()
			.householdMembers(List.of(new PersonBasedCalculationHouseholdMemberDTO().personId(MEMBER_PERSON_NUMBER).name("Member"))));
		when(partyIntegrationMock.getPartyIds(MUNICIPALITY_ID, List.of(MEMBER_PERSON_NUMBER))).thenReturn(Map.of(MEMBER_PERSON_NUMBER, MEMBER_PARTY_ID));

		// Act
		final var result = familyCareService.getCalculationProposal(MUNICIPALITY_ID, PARTY_ID);

		// Verify: household members expose partyId, not person number
		assertThat(result.getHouseholdMembers()).hasSize(1);
		assertThat(result.getHouseholdMembers().getFirst().getPartyId()).isEqualTo(MEMBER_PARTY_ID);
		assertThat(result.getHouseholdMembers().getFirst().getName()).isEqualTo("Member");
	}

	@Test
	void getCalculationProposalWithoutHouseholdMembers() {
		// Mock: FC may send "HouseholdMembers": null
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getCalculationProposal(PERSON_NUMBER)).thenReturn(new PersonBasedCalculationProposalDTO().householdMembers(null));

		// Act
		final var result = familyCareService.getCalculationProposal(MUNICIPALITY_ID, PARTY_ID);

		// Verify
		assertThat(result.getHouseholdMembers()).isEmpty();
		verify(partyIntegrationMock).getPartyIds(MUNICIPALITY_ID, List.of());
	}

	@Test
	void getCalculationProposalWithoutProposal() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getCalculationProposal(PERSON_NUMBER)).thenReturn(null);

		// Act & verify
		assertThat(familyCareService.getCalculationProposal(MUNICIPALITY_ID, PARTY_ID)).isNull();
	}

	@Test
	void getCalculationsSwapsPersonNumbersForPartyIds() {
		// Parameter values
		final var parameters = new PeriodParameters();
		parameters.setPartyId(PARTY_ID);

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getCalculations(any(), any(), any(), any(), any(), any())).thenReturn(new ApiPaginationCompositePersonBasedCalculationDTO()
			.result(List.of(new PersonBasedCalculationDTO().id(1).calculationPersonDTOs(List.of(
				new PersonBasedCalculationPersonDTO().personId(MEMBER_PERSON_NUMBER).name("Member"))))));
		when(partyIntegrationMock.getPartyIds(MUNICIPALITY_ID, List.of(MEMBER_PERSON_NUMBER))).thenReturn(Map.of(MEMBER_PERSON_NUMBER, MEMBER_PARTY_ID));

		// Act
		final var result = familyCareService.getCalculations(MUNICIPALITY_ID, parameters);

		// Verify: persons expose partyId, never the person number
		assertThat(result.getCalculations()).singleElement()
			.satisfies(calculation -> assertThat(calculation.getPersons()).singleElement()
				.satisfies(person -> assertThat(person.getPartyId()).isEqualTo(MEMBER_PARTY_ID)));
		assertThat(result.toString()).doesNotContain(MEMBER_PERSON_NUMBER);
	}

	@Test
	void createCalculationBatchResolvesMemberPartyIds() {
		// Arrange
		final var request = CreateCalculationRequest.create()
			.withPartyId(PARTY_ID)
			.withNormId(3)
			.withCalculationDate(LocalDate.parse("2026-05-01"))
			.withCalculationFromDate(LocalDate.parse("2026-05-01"))
			.withCalculationToDate(LocalDate.parse("2026-05-31"))
			.withPersons(List.of(CalculationPersonRequest.create().withPartyId(MEMBER_PARTY_ID)));

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(partyIntegrationMock.getPersonNumbers(MUNICIPALITY_ID, List.of(MEMBER_PARTY_ID))).thenReturn(Map.of(MEMBER_PARTY_ID, MEMBER_PERSON_NUMBER));
		when(familyCareIntegrationMock.createCalculation(any())).thenReturn(99);

		// Act
		final var result = familyCareService.createCalculation(MUNICIPALITY_ID, request);

		// Verify: applicant + member person numbers are forwarded, not partyIds
		final var captor = ArgumentCaptor.forClass(PostCalculationBodyRequest.class);
		verify(familyCareIntegrationMock).createCalculation(captor.capture());
		assertThat(captor.getValue().getPersonId()).isEqualTo(PERSON_NUMBER);
		assertThat(captor.getValue().getCalculationPersons().getFirst().getPersonId()).isEqualTo(MEMBER_PERSON_NUMBER);
		assertThat(result).isEqualTo(99);
	}

	@Test
	void addActualisationAttachmentDelegates() {
		// Arrange
		final var file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[] {
			1, 2, 3
		});

		// Act
		familyCareService.addActualisationAttachment(12345, "type", "sender", "title", "senderName", file);

		// Verify
		verify(familyCareIntegrationMock).addActualisationAttachment(12345, "type", "sender", "title", "senderName", file);
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void getActualisationProposalDelegates() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(familyCareIntegrationMock.getActualisationProposal(PERSON_NUMBER)).thenReturn(null);

		// Act
		final var result = familyCareService.getActualisationProposal(MUNICIPALITY_ID, PARTY_ID);

		// Verify
		assertThat(result).isNull();
		verify(familyCareIntegrationMock).getActualisationProposal(PERSON_NUMBER);
	}
}
