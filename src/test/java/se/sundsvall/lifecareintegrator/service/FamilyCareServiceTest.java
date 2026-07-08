package se.sundsvall.lifecareintegrator.service;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationHouseholdMemberDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationProposalDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedContactDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedPersonDTO;
import generated.se.sundsvall.lifecarefc.PostAktualiseringsBodyRequest;
import generated.se.sundsvall.lifecarefc.PostCalculationBodyRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.PeriodParameters;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
	private LifecareFcIntegration lifecareFcIntegrationMock;

	@InjectMocks
	private FamilyCareService familyCareService;

	@Test
	void getPerson() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getPerson(PERSON_NUMBER)).thenReturn(java.util.Optional.of(new PersonBasedPersonDTO().name("Test Person")));

		// Act
		final var result = familyCareService.getPerson(MUNICIPALITY_ID, PARTY_ID);

		// Verify: personnummer is what gets forwarded to FC, and it is not exposed
		assertThat(result.getName()).isEqualTo("Test Person");
		verify(partyIntegrationMock).getPersonNumber(MUNICIPALITY_ID, PARTY_ID);
		verify(lifecareFcIntegrationMock).getPerson(PERSON_NUMBER);
	}

	@Test
	void getPersonNotFound() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getPerson(PERSON_NUMBER)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> familyCareService.getPerson(MUNICIPALITY_ID, PARTY_ID));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No person found for partyId '%s'".formatted(PARTY_ID));
	}

	@Test
	void getContacts() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getContacts(PERSON_NUMBER)).thenReturn(List.of(new PersonBasedContactDTO().name("Contact")));

		// Act
		final var result = familyCareService.getContacts(MUNICIPALITY_ID, PARTY_ID);

		// Verify
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getName()).isEqualTo("Contact");
		verify(lifecareFcIntegrationMock).getContacts(PERSON_NUMBER);
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
		when(lifecareFcIntegrationMock.getActualisations(PERSON_NUMBER, from, to, 20, 1, true))
			.thenReturn(new ApiPaginationCompositePersonBasedAktualiseringDTO().pageNumber(1).result(List.of(new PersonBasedAktualiseringDTO().id(1))));

		// Act
		final var result = familyCareService.getActualisations(MUNICIPALITY_ID, parameters);

		// Verify: the resolved person number (not partyId) is forwarded
		assertThat(result.getActualisations()).hasSize(1);
		verify(lifecareFcIntegrationMock).getActualisations(PERSON_NUMBER, from, to, 20, 1, true);
	}

	@Test
	void getActualisationsUsesDefaultWindowWhenDatesMissing() {
		// Parameter values: no dates or ascending set — page/limit default to 1/100
		final var parameters = new PeriodParameters();
		parameters.setPartyId(PARTY_ID);

		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getActualisations(any(), any(), any(), any(), any(), any()))
			.thenReturn(new ApiPaginationCompositePersonBasedAktualiseringDTO());

		// Act
		familyCareService.getActualisations(MUNICIPALITY_ID, parameters);

		// Verify: default 10-year lookback window, default paging (page 1, limit 100)
		final var today = LocalDate.now(ZoneId.of("Europe/Stockholm"));
		verify(lifecareFcIntegrationMock).getActualisations(PERSON_NUMBER, today.minusYears(10), today, 100, 1, null);
	}

	@Test
	void getDocumentContent() {
		// Parameter values
		final var content = new byte[] {
			1, 2, 3
		};

		// Mock
		when(lifecareFcIntegrationMock.getDocumentContent("doc-1")).thenReturn(java.util.Optional.of(content));

		// Act
		final var result = familyCareService.getDocumentContent("doc-1");

		// Verify: no party resolution for document content
		assertThat(result).isEqualTo(content);
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void getDocumentContentNotFound() {
		// Mock
		when(lifecareFcIntegrationMock.getDocumentContent("doc-1")).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> familyCareService.getDocumentContent("doc-1"));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getDetail()).isEqualTo("No document content found for document 'doc-1'");
	}

	@Test
	void getUsersDoesNotResolveParty() {
		// Parameter values
		final var modifiedAfter = OffsetDateTime.parse("2026-01-01T00:00:00Z");

		// Mock
		when(lifecareFcIntegrationMock.getUsers(100, 0, modifiedAfter, null)).thenReturn(List.of());

		// Act
		final var result = familyCareService.getUsers(100, 0, modifiedAfter, null);

		// Verify
		assertThat(result).isEmpty();
		verify(lifecareFcIntegrationMock).getUsers(100, 0, modifiedAfter, null);
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
		when(lifecareFcIntegrationMock.createActualisation(any())).thenReturn(42);

		// Act
		final var result = familyCareService.createActualisation(MUNICIPALITY_ID, request);

		// Verify: the FC body carries the resolved person number, never the partyId
		final var captor = ArgumentCaptor.forClass(PostAktualiseringsBodyRequest.class);
		verify(lifecareFcIntegrationMock).createActualisation(captor.capture());
		assertThat(captor.getValue().getPersonId()).isEqualTo(PERSON_NUMBER);
		assertThat(result).isEqualTo(42);
	}

	@Test
	void getCalculationProposalSwapsHouseholdMemberPersonNumbers() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getCalculationProposal(PERSON_NUMBER)).thenReturn(new PersonBasedCalculationProposalDTO()
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
		when(lifecareFcIntegrationMock.createCalculation(any())).thenReturn(99);

		// Act
		final var result = familyCareService.createCalculation(MUNICIPALITY_ID, request);

		// Verify: applicant + member person numbers are forwarded, not partyIds
		final var captor = ArgumentCaptor.forClass(PostCalculationBodyRequest.class);
		verify(lifecareFcIntegrationMock).createCalculation(captor.capture());
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
		verify(lifecareFcIntegrationMock).addActualisationAttachment(12345, "type", "sender", "title", "senderName", file);
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void getActualisationProposalDelegates() {
		// Mock
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn(PERSON_NUMBER);
		when(lifecareFcIntegrationMock.getActualisationProposal(PERSON_NUMBER)).thenReturn(null);

		// Act
		final var result = familyCareService.getActualisationProposal(MUNICIPALITY_ID, PARTY_ID);

		// Verify
		assertThat(result).isNull();
		verify(lifecareFcIntegrationMock).getActualisationProposal(PERSON_NUMBER);
	}
}
