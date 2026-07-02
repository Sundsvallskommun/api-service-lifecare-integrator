package se.sundsvall.lifecareintegrator.integration.lifecarefc;

import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedPaymentDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedDecisionDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedPersonDTO;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.exception.ClientProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class LifecareFcIntegrationTest {

	private static final String PERSON_NUMBER = "199001011234";
	private static final String DOCUMENT_ID = "12345";
	private static final LocalDate START_DATE = LocalDate.of(2016, 7, 1);
	private static final LocalDate END_DATE = LocalDate.of(2026, 7, 1);

	@Mock
	private LifecareFcClient lifecareFcClientMock;

	@InjectMocks
	private LifecareFcIntegration lifecareFcIntegration;

	@Test
	void getPerson() {
		// Parameter values
		final var person = new PersonBasedPersonDTO().name("Test Person");

		// Mock
		when(lifecareFcClientMock.getPerson(PERSON_NUMBER)).thenReturn(person);

		// Act
		final var result = lifecareFcIntegration.getPerson(PERSON_NUMBER);

		// Verify
		assertThat(result).hasValue(person);
		verify(lifecareFcClientMock).getPerson(PERSON_NUMBER);
		verifyNoMoreInteractions(lifecareFcClientMock);
	}

	@Test
	void getPersonNotFound() {
		// Mock
		when(lifecareFcClientMock.getPerson(PERSON_NUMBER)).thenThrow(new ClientProblem(NOT_FOUND, "Not Found"));

		// Act
		final var result = lifecareFcIntegration.getPerson(PERSON_NUMBER);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getPersonOtherClientProblemIsRethrown() {
		// Mock
		when(lifecareFcClientMock.getPerson(PERSON_NUMBER)).thenThrow(new ClientProblem(BAD_GATEWAY, "Bad Gateway"));

		// Act
		final var exception = assertThrows(ClientProblem.class, () -> lifecareFcIntegration.getPerson(PERSON_NUMBER));

		// Verify
		assertThat(exception.getStatus()).isEqualTo(BAD_GATEWAY);
	}

	@Test
	void getDocumentContent() {
		// Parameter values
		final var content = new byte[] {
			1, 2, 3
		};

		// Mock
		when(lifecareFcClientMock.getDocumentContent(DOCUMENT_ID)).thenReturn(content);

		// Act
		final var result = lifecareFcIntegration.getDocumentContent(DOCUMENT_ID);

		// Verify
		assertThat(result).hasValue(content);
	}

	@Test
	void getDocumentContentNotFound() {
		// Mock
		when(lifecareFcClientMock.getDocumentContent(DOCUMENT_ID)).thenThrow(new ClientProblem(NOT_FOUND, "Not Found"));

		// Act
		final var result = lifecareFcIntegration.getDocumentContent(DOCUMENT_ID);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getAllDecisionsSinglePage() {
		// Parameter values
		final var decision = new PersonBasedDecisionDTO().id(1);

		// Mock
		when(lifecareFcClientMock.getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 1, null))
			.thenReturn(new ApiPaginationCompositePersonBasedDecisionDTO().totalNumberOfPages(1).result(List.of(decision)));

		// Act
		final var result = lifecareFcIntegration.getAllDecisions(PERSON_NUMBER, START_DATE, END_DATE);

		// Verify
		assertThat(result).containsExactly(decision);
		verify(lifecareFcClientMock).getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 1, null);
		verifyNoMoreInteractions(lifecareFcClientMock);
	}

	@Test
	void getAllDecisionsPagesThroughAllPages() {
		// Parameter values
		final var firstPageDecision = new PersonBasedDecisionDTO().id(1);
		final var secondPageDecision = new PersonBasedDecisionDTO().id(2);

		// Mock
		when(lifecareFcClientMock.getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 1, null))
			.thenReturn(new ApiPaginationCompositePersonBasedDecisionDTO().totalNumberOfPages(2).result(List.of(firstPageDecision)));
		when(lifecareFcClientMock.getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 2, null))
			.thenReturn(new ApiPaginationCompositePersonBasedDecisionDTO().totalNumberOfPages(2).result(List.of(secondPageDecision)));

		// Act
		final var result = lifecareFcIntegration.getAllDecisions(PERSON_NUMBER, START_DATE, END_DATE);

		// Verify
		assertThat(result).containsExactly(firstPageDecision, secondPageDecision);
		verify(lifecareFcClientMock).getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 1, null);
		verify(lifecareFcClientMock).getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 2, null);
		verifyNoMoreInteractions(lifecareFcClientMock);
	}

	@Test
	void getAllDecisionsWithNullResponse() {
		// Mock
		when(lifecareFcClientMock.getDecisions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 100, 1, null)).thenReturn(null);

		// Act
		final var result = lifecareFcIntegration.getAllDecisions(PERSON_NUMBER, START_DATE, END_DATE);

		// Verify
		assertThat(result).isEmpty();
	}

	@Test
	void getPaymentsDelegatesWithFormattedDates() {
		// Parameter values
		final var composite = new ApiPaginationCompositePersonBasedPaymentDTO().pageNumber(2);

		// Mock
		when(lifecareFcClientMock.getPayments(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 15, 2, true)).thenReturn(composite);

		// Act
		final var result = lifecareFcIntegration.getPayments(PERSON_NUMBER, START_DATE, END_DATE, 15, 2, true);

		// Verify
		assertThat(result).isSameAs(composite);
		verify(lifecareFcClientMock).getPayments(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 15, 2, true);
		verifyNoMoreInteractions(lifecareFcClientMock);
	}

	@Test
	void getContactsDelegates() {
		// Parameter values
		final var contact = new generated.se.sundsvall.lifecarefc.PersonBasedContactDTO().name("Contact");

		// Mock
		when(lifecareFcClientMock.getContacts(PERSON_NUMBER)).thenReturn(List.of(contact));

		// Act
		final var result = lifecareFcIntegration.getContacts(PERSON_NUMBER);

		// Verify
		assertThat(result).containsExactly(contact);
	}

	@Test
	void getContactsWithNullResponse() {
		// Mock
		when(lifecareFcClientMock.getContacts(PERSON_NUMBER)).thenReturn(null);

		// Act + Verify
		assertThat(lifecareFcIntegration.getContacts(PERSON_NUMBER)).isEmpty();
	}

	@Test
	void periodReadsDelegateWithFormattedDates() {
		// Mock — each period read forwards the formatted window to the client
		when(lifecareFcClientMock.getActualisation(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedAktualiseringDTO());
		when(lifecareFcClientMock.getCalculations(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedCalculationDTO());
		when(lifecareFcClientMock.getInvestigations(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedInvestigationDTO());
		when(lifecareFcClientMock.getServices(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedServiceDTO());
		when(lifecareFcClientMock.getExecutions(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedExecutionDTO());
		when(lifecareFcClientMock.getResourceAllocations(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedResourceAllocationDTO());
		when(lifecareFcClientMock.getDocuments(PERSON_NUMBER, START_DATE.toString(), END_DATE.toString(), 10, 1, true))
			.thenReturn(new generated.se.sundsvall.lifecarefc.ApiPaginationCompositePersonBasedDocumentDTO());

		// Act + Verify
		assertThat(lifecareFcIntegration.getActualisations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(lifecareFcIntegration.getCalculations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(lifecareFcIntegration.getInvestigations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(lifecareFcIntegration.getServices(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(lifecareFcIntegration.getExecutions(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(lifecareFcIntegration.getResourceAllocations(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
		assertThat(lifecareFcIntegration.getDocuments(PERSON_NUMBER, START_DATE, END_DATE, 10, 1, true)).isNotNull();
	}

	@Test
	void proposalAndWriteBackDelegate() {
		// Parameter values
		final var actualisationBody = new generated.se.sundsvall.lifecarefc.PostAktualiseringsBodyRequest();
		final var calculationBody = new generated.se.sundsvall.lifecarefc.PostCalculationBodyRequest();
		final var file = new org.springframework.mock.web.MockMultipartFile("file", new byte[] {
			1
		});

		// Mock
		when(lifecareFcClientMock.getActualisationProposal(PERSON_NUMBER)).thenReturn(new generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringProposalDTO());
		when(lifecareFcClientMock.createActualisation(actualisationBody)).thenReturn(1);
		when(lifecareFcClientMock.getCalculationProposal(PERSON_NUMBER)).thenReturn(new generated.se.sundsvall.lifecarefc.PersonBasedCalculationProposalDTO());
		when(lifecareFcClientMock.createCalculation(calculationBody)).thenReturn(2);

		// Act + Verify
		assertThat(lifecareFcIntegration.getActualisationProposal(PERSON_NUMBER)).isNotNull();
		assertThat(lifecareFcIntegration.createActualisation(actualisationBody)).isEqualTo(1);
		assertThat(lifecareFcIntegration.getCalculationProposal(PERSON_NUMBER)).isNotNull();
		assertThat(lifecareFcIntegration.createCalculation(calculationBody)).isEqualTo(2);

		lifecareFcIntegration.addActualisationAttachment(5, "type", "sender", "title", "senderName", file);
		verify(lifecareFcClientMock).postActualisationAttachment(5, "type", "sender", "title", "senderName", file);
	}

	@Test
	void getUsersDelegatesWithFormattedTimestamps() {
		// Parameter values
		final var modifiedAfter = OffsetDateTime.parse("2026-01-01T00:00:00Z");

		// Mock
		when(lifecareFcClientMock.getUsers(50, 10, modifiedAfter.toString(), null)).thenReturn(List.of());

		// Act
		final var result = lifecareFcIntegration.getUsers(50, 10, modifiedAfter, null);

		// Verify
		assertThat(result).isEmpty();
		verify(lifecareFcClientMock).getUsers(50, 10, modifiedAfter.toString(), null);
		verifyNoMoreInteractions(lifecareFcClientMock);
	}
}
